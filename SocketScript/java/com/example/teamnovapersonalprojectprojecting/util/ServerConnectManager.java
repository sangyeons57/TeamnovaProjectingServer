package com.example.teamnovapersonalprojectprojecting.util;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ServerConnectManager {
    public static final String BASE_URL = "http://" + SocketConnection.SERVER_ADDRESS + "/";

    private String path;
    private FormBody formBody;
    private FormBody.Builder builder;

    private Map<String, String> params;

    private static OkHttpClient client;
    private Request request;

    private static String sessionCookie = null;
    private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

    public ServerConnectManager(String path){
        this.path = path;
        this.params = new HashMap<>();

        if(client == null){
            client = new OkHttpClient().newBuilder()
                    .cookieJar(new CookieJar() {

                        @Override
                        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                            cookieStore.put(url, cookies);
                        }

                        @Override
                        public List<Cookie> loadForRequest(HttpUrl url) {
                            List<Cookie> cookies = cookieStore.get(url);
                            return cookies != null ? cookies : new ArrayList<>();
                        }
                    })
                    .build();
        }

        builder = new FormBody.Builder();
        Log("Create Server: " + path);
    }

    public void getPHPSession(Response response){
        for (String header : response.headers("Set-Cookie")) {
            if (header.startsWith("PHPSESSID")) {
                sessionCookie = header.split(";")[0];
            }
        }
    }
    public void clearPHPSession(){
        sessionCookie = null;
    }

    public static void Log(String message){
        Log.d("Server", message);
    }
    public static void Loge(String message){
        Log.e("Server", message);
    }

    public ServerConnectManager add(JsonUtil.Key key, String value) {
        return add(key.toString(), value);
    }
    public ServerConnectManager add(String key, String value){
        params.put(key, value);
        builder.add(key, value);
        Log("add {" + key + " : " + value + "}");
        return this;
    }

    public Response getExecute() throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + path).newBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String url = urlBuilder.build().toString();

        formBody = builder.build();
        request = buildRequest(url, sessionCookie);

        Log("Get Execute: " + url);
        return client.newCall(request).execute();
    }
    public Response postExecute() throws IOException {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }

        formBody = builder.build();
        request = buildRequest(BASE_URL + path,formBody, sessionCookie);

        Log("Post Execute: " + path);
        return client.newCall(request).execute();
    }

    public void getEnqueue(Callback callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + path).newBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String url = urlBuilder.build().toString();
        request = buildRequest(url, sessionCookie);

        client.newCall(request).enqueue(callback);
        Log("Get Enqueue: " + url);
    }

    public void postEnqueue(Callback callback) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }

        Log("cookiestore:" + cookieStore);
        formBody = builder.build();
        request = buildRequest(BASE_URL + path,formBody, sessionCookie);
        client.newCall(request).enqueue(callback);
        Log("Post Enqueue: " + path);
    }

    private Request buildRequest(String url, String sessionCookie) {
        if(sessionCookie == null){
            return new Request.Builder()
                    .url(url)
                    .get()
                    .build();
        } else {
            return new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Cookie", sessionCookie)
                    .build();
        }
    }
    private Request buildRequest(String url, FormBody formBody , String sessionCookie) {
        if(sessionCookie == null){
            return new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
        } else {
            return new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .addHeader("Cookie", sessionCookie)
                    .build();
        }
    }


    public static class EasyCallbackGeneric<T extends AppCompatActivity> extends EasyCallback {
        protected T activity;

        public EasyCallbackGeneric(AppCompatActivity activity) {
            activity = (T) activity;
        }
    }
    public static class EasyCallback implements Callback {
        protected Handler mainHandler;
        String responseBody;

        public EasyCallback() {
            mainHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            if (!response.isSuccessful() && response.body() != null) {
                onResponseFailure(response);
                throw new IOException("Unexpected code " + response);
            }
            responseBody = response.body().string();
            onResponseSuccess(response);
            try {
                JSONObject jsonObject = new JSONObject(responseBody);
                onGetJson(jsonObject);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        protected void onGetJson(JSONObject jsonObject) throws IOException, JSONException {
            Log(jsonObject.toString());
        };

        protected void onResponseSuccess(Response response) throws  IOException{
            Log("onResponseSuccess: " + responseBody);
        }
        protected void onResponseFailure(Response response) throws  IOException{
            Loge("onResponseFailure: " + response);
            Loge("onResponseFailure: " + response.body().string());
        }
    }

    public enum Path {
        TEST("Script/test/"),

        CERTIFICATION("Script/Certification/"),
        CHANGE_PROFILE("Script/ChangeProfile/"),
        USERS("Script/Users/"),
        FRIENDS("Script/Friends/");

        private final String directory;
        private Path(String directory) {
            this.directory = directory;
        }

        public String getPath(String filename){
            return this.directory + filename;
        }
    }
}
