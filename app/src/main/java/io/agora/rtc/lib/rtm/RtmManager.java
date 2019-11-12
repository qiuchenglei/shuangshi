package io.agora.rtc.lib.rtm;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

public class RtmManager {
    private final LogUtil log = new LogUtil("RtmManager");

    private RtmClient mRtmClient;
    private Set<MyRtmClientListener> mListenerList = new LinkedHashSet<>();

    private RtmManager(Context context, String appID) {
        init(context, appID);
    }

    public static RtmManager createInstance(Context context, String appId) {
        if (context == null || TextUtils.isEmpty(appId))
            return null;

        return new RtmManager(context, appId);
    }

    private RtmClientListener mClientListener = new RtmClientListener() {
        @Override
        public void onConnectionStateChanged(int state, int reason) {
            log.i("state:" + state + ",reason:" + reason);
            for (MyRtmClientListener listener : mListenerList) {
                if (listener != null)
                    listener.onConnectionStateChanged(state, reason);
            }
        }

        @Override
        public void onMessageReceived(RtmMessage rtmMessage, String peerId) {
            log.i("msgArgs:" + rtmMessage.getText() + ", peerId：" + peerId);

            for (MyRtmClientListener listener : mListenerList) {
                if (listener != null)
                    listener.onMessageReceived(rtmMessage, peerId);
            }
        }

        @Override
        public void onTokenExpired() {
            log.i("onTokenExpired");
        }
    };

    private void init(Context context, String appID) {
        try {
            mRtmClient = RtmClient.createInstance(context, appID, mClientListener);

            if (BuildConfig.DEBUG) {
//                mRtmClient.setParameters("{\"rtm.log_filter\": 65535}");
            }
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

    private void changeLoginStatus(int status) {
        loginStatus = status;
        for (MyRtmClientListener listener : mListenerList) {
            if (listener != null)
                listener.onLoginStatusChanged(status);
        }
    }

    /**
     * API CALL: login RTM server
     */
    public void login(final String uid) {
        changeLoginStatus(LOGIN_STATUS_LOGGING);
        mRtmClient.login(null, uid, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                log.i("login success");
                changeLoginStatus(LOGIN_STATUS_SUCCESS);
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                changeLoginStatus(LOGIN_STATUS_FAILURE);
                String failStr = "login failed, info:" + errorInfo.toString();
                log.e(failStr);
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


    public RtmChannel createAndJoinChannel(String channel, RtmChannelListener rtmChannelListener, ResultCallback<Void> callback) {
        if (TextUtils.isEmpty(channel))
            return null;

        RtmChannel rtmChannel = null;
        try {
            log.i("create channel." + channel);
            rtmChannel = mRtmClient.createChannel(channel, rtmChannelListener);

            rtmChannel.join(callback);
        } catch (RuntimeException e) {
            log.e("Fails to create channel. Maybe the channel ID is invalid," +
                    " or already in use. See the API reference for more information.");
        }

        return rtmChannel;
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
            rtmChannel.release();
            rtmChannel = null;
        }
    }

    public interface LoginStatusListener {
        void onLoginStatusChanged(int loginStatus);
    }

    public RtmClient getRtmClient() {
        return mRtmClient;
    }

    public void registerListener(MyRtmClientListener listener) {
        mListenerList.add(listener);
        listener.onLoginStatusChanged(loginStatus);
    }

    public interface MyRtmClientListener extends RtmClientListener {
        void onLoginStatusChanged(int loginStatus);
    }

    public void unregisterListener(MyRtmClientListener listener) {
        mListenerList.remove(listener);
    }

    public void sendP2PMsg(String peerId, String msg, final ResultCallback<Void> callback) {
        if (msg == null)
            return;

        RtmMessage rtmMessage = mRtmClient.createMessage();
        rtmMessage.setText(msg);
        mRtmClient.sendMessageToPeer(peerId, rtmMessage, callback);
        log.d("send:" + msg);
    }

}
