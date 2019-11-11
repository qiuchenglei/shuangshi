package io.agora.rtc.lib.net;

import android.net.Uri;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.agora.rtc.lib.util.LogUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NetManager {

    private static NetManager netManager = new NetManager();
    private static final LogUtil log = new LogUtil("NetManager");

    private OkHttpClient client;

    private NetManager() {
        client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS).build();
    }

    public static NetManager getInstance() {
        return netManager;
    }

    public static OkHttpClient getOkHttpClient() {
        return netManager.client;
    }

    public static String appendGetParams(String url, Map<String, String> params) {
        if (url == null || params == null || params.isEmpty()) {
            return url;
        }
        Uri.Builder builder = Uri.parse(url).buildUpon();
        Set<String> keys = params.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            builder.appendQueryParameter(key, params.get(key));
        }
        return builder.build().toString();
    }

    public interface CallBack {
        void onSuccess(String json);

        void onFailure(IOException e);
    }

    public Call getRequest(String url, final CallBack callBack) {
        Request request = new Request.Builder().url(url).get().build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (callBack != null)
                    callBack.onFailure(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    ResponseBody body = response.body();
                    if (body == null)
                        throw new Throwable("response body is null.");

                    if (response.code() == 200) {
                        if (callBack != null)
                            callBack.onSuccess(body.string());
                    } else {
                        onFailure(call, new IOException(body.string()));
                    }
                } catch (Throwable e) {
                    onFailure(call, new IOException(e.toString()));
                }
            }
        });
        return call;
    }

}
