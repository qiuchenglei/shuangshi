package io.agora.rtc.lib.rtm;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.agora.rtc.Constants;
import io.agora.rtc.shuangshi.AGApplication;
import io.agora.rtc.shuangshi.BuildConfig;
import io.agora.rtc.lib.util.LogUtil;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmChannel;
import io.agora.rtm.RtmChannelListener;
import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmClientListener;
import io.agora.rtm.RtmMessage;
import io.agora.rtm.RtmStatusCode;

public class RtmManager {
    private final LogUtil log = new LogUtil("RtmManager");

    private RtmClient mRtmClient;
    private List<MyRtmClientListener> mListenerList = new ArrayList<>();

    private static RtmManager rtmManager;

    private RtmManager(Context context, String appId) {
        init(context, appId);
    }

    public static synchronized RtmManager createInstance(Context context, String appId) {
        if (context != null && !TextUtils.isEmpty(appId) && rtmManager == null) {
            rtmManager = new RtmManager(context, appId);
        }
        return rtmManager;
    }

    private RtmClientListener mClientListener = new RtmClientListener() {
        @Override
        public void onConnectionStateChanged(int state, int reason) {
            log.i("state:" + state + ",reason:" + reason);
            for (MyRtmClientListener listener : mListenerList) {
                if (listener != null) {
                    listener.onConnectionStateChanged(state, reason);
                }
            }
        }

        @Override
        public void onMessageReceived(RtmMessage rtmMessage, String peerId) {
            log.i("msgArgs:" + rtmMessage.getText() + ", peerId：" + peerId);

            for (MyRtmClientListener listener : mListenerList) {
                if (listener != null) {
                    listener.onMessageReceived(rtmMessage, peerId);
                }
            }
        }

        @Override
        public void onTokenExpired() {
            log.i("onTokenExpired");
        }

        @Override
        public void onPeersOnlineStatusChanged(Map<String, Integer> map) {
        }
    };

    private void init(Context context, String appId) {
        try {
            mRtmClient = RtmClient.createInstance(context, appId, mClientListener);

//            if (BuildConfig.DEBUG) {
//                mRtmClient.setParameters("{\"rtm.log_filter\": 65535}");
//            }
            mRtmClient.setLogFilter(0);
        } catch (Exception e) {
            log.e(Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtm sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    public static int LOGIN_STATUS_IDLE = 0;
    public static int LOGIN_STATUS_LOGGING = 1;
    public static int LOGIN_STATUS_SUCCESS = 2;
    public static int LOGIN_STATUS_FAILURE = 3;
    private volatile int loginStatus = LOGIN_STATUS_IDLE;

    public int getLoginStatus() {
        return loginStatus;
    }

    private void changeLoginStatus(int status) {
        loginStatus = status;
        for (MyRtmClientListener listener : mListenerList) {
            if (listener != null) {
                listener.onLoginStatusChanged(status);
            }
        }
    }

    private int retryCount = 0;

    /**
     * API CALL: login RTM server
     */
    public void login(final String uid) {
        changeLoginStatus(LOGIN_STATUS_LOGGING);
        mRtmClient.login(null, uid, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                retryCount = 0;
                log.i("login success");
                changeLoginStatus(LOGIN_STATUS_SUCCESS);
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                log.i("login failed");
                // 如果error是rejected，可能已经在房间导致，尝试退出重登
                if (retryCount > 1 || errorInfo.getErrorCode() != RtmStatusCode.LoginError.LOGIN_ERR_REJECTED) {
                    changeLoginStatus(LOGIN_STATUS_FAILURE);
                    String failStr = "login failed, info:" + errorInfo.toString();
                    log.e(failStr);
                    retryCount = 0;
                }
                retryCount++;
                logout();
                login(uid);
            }
        });
    }

    public void logout() {
        mRtmClient.logout(new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {

            }
        });
        changeLoginStatus(LOGIN_STATUS_IDLE);
    }


    public RtmChannel createChannel(String channel, RtmChannelListener rtmChannelListener) {
        if (TextUtils.isEmpty(channel)) {
            return null;
        }

        try {
            log.i("create channel." + channel);
            return mRtmClient.createChannel(channel, rtmChannelListener);
        } catch (RuntimeException e) {
            log.e("Fails to create channel. Maybe the channel ID is invalid," +
                    " or already in use. See the API reference for more information.");
            return null;
        }
    }

    public void joinChannel(RtmChannel rtmChannel, ResultCallback<Void> callback) {
        if (rtmChannel != null) {
            rtmChannel.join(callback);
        }
    }

    public void leaveChannel(RtmChannel rtmChannel) {
        if (rtmChannel != null) {
            rtmChannel.leave(new ResultCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    log.d("leave success");
                }

                @Override
                public void onFailure(ErrorInfo errorInfo) {
                    log.d("leave failure, " + errorInfo.toString());
                }
            });
        }
    }

    public void releaseChannel(RtmChannel rtmChannel) {
        if (rtmChannel != null) {
            rtmChannel.release();
        }
    }

    public RtmClient getRtmClient() {
        return mRtmClient;
    }

    public void registerListener(MyRtmClientListener listener) {
        mListenerList.add(listener);
        listener.onLoginStatusChanged(loginStatus);
    }

    public static class MyRtmClientListener implements RtmClientListener {
        public void onLoginStatusChanged(int loginStatus) {
        }

        @Override
        public void onConnectionStateChanged(int i, int i1) {
        }

        @Override
        public void onMessageReceived(RtmMessage rtmMessage, String s) {
        }

        @Override
        public void onTokenExpired() {
        }

        @Override
        public void onPeersOnlineStatusChanged(Map<String, Integer> map) {
        }
    }

    public void unregisterListener(MyRtmClientListener listener) {
        mListenerList.remove(listener);
    }

    public void sendP2PMsg(String peerId, String msg, final ResultCallback<Void> callback) {
        if (msg == null) {
            return;
        }

        RtmMessage rtmMessage = mRtmClient.createMessage();
        rtmMessage.setText(msg);
        mRtmClient.sendMessageToPeer(peerId, rtmMessage, callback);
        log.d("send:" + msg);
    }

}
