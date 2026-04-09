package com.example.civsetu.network;

import android.content.Context;
import com.example.civsetu.utils.PreferenceManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = PreferenceManager.getInstance(context).getToken();
        Request.Builder builder = chain.request().newBuilder();

        if (token != null) {
            builder.addHeader("Authorization", "Bearer " + token);
        }

        return chain.proceed(builder.build());
    }
}
