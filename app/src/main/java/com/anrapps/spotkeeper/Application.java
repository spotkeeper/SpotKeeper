package com.anrapps.spotkeeper;

import com.anrapps.spotkeeper.util.PrefUtils;
import com.squareup.leakcanary.LeakCanary;

public class Application extends android.app.Application {

    private static String mToken;

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
        mToken = PrefUtils.getUserAccessToken(this);
    }

    public static void setToken(String token) {
        mToken = token;
    }

    public static String getToken() {
        return mToken;
    }
}
