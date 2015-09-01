package com.anrapps.spotkeeper.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefUtils {

    private static final String PREF_KEY_USER_IS_LOGGED_IN = "user_logged_in";
    private static final String PREF_KEY_USER_ID= "user_id";
    private static final String PREF_KEY_USER_ACCESS_TOKEN = "user_access_token";
    private static final String PREF_KEY_LAST_TOKEN_TIME = "last_token_time";
    private static final String PREF_KEY_FIRST_TIME_INIT = "first_time_init";


    private static final String PREFERENCE_SKIP_FILES = "skip_files";
    private static final String PREFERENCE_VOLUME_MAX = "volume_max";
    private static final String PREFERENCE_OUTPUT_FORMAT = "output_format";
    private static final String PREFERENCE_AUDIO_ENCODER = "audio_encoder";
    private static final String PREFERENCE_ENCODING_BITRATE = "encoding_bitrate";
    private static final String PREFERENCE_SAMPLE_RATE = "sampling_rate";

    public static void setNotFirstTimeInit(Context context) {
        sp(context).edit().putBoolean(PREF_KEY_FIRST_TIME_INIT, false).commit();
    }

    public static boolean isFirstTimeInit(Context context) {
        return sp(context).getBoolean(PREF_KEY_FIRST_TIME_INIT, true);
    }

    public static void setLoggedUserId(Context context, String userId) {
        sp(context).edit().putString(PREF_KEY_USER_ID, userId).commit();
    }

    public static String getLoggedUserId(Context context) {
        return sp(context).getString(PREF_KEY_USER_ID, null);
    }

    public static void setLastTokenTime(Context context) {
        sp(context).edit().putLong(PREF_KEY_LAST_TOKEN_TIME, System.currentTimeMillis()).commit();
    }

    public static boolean isLastTokenExpired(Context context) {
        long lastTokenTime = sp(context).getLong(PREF_KEY_LAST_TOKEN_TIME, -1);
        return lastTokenTime == -1 || ((System.currentTimeMillis() - lastTokenTime) > 2700000); //45 minutes
    }

    public static void setUserAccessToken(Context context, String token) {
        sp(context).edit().putString(PREF_KEY_USER_ACCESS_TOKEN, token).commit();
    }

    public static String getUserAccessToken(Context context) {
        return sp(context).getString(PREF_KEY_USER_ACCESS_TOKEN, null);
    }


    public static boolean getSkipFilesEnabled(Context context) {
        return sp(context).getBoolean(PREFERENCE_SKIP_FILES, true);
    }

    public static boolean getVolumeMaxEnabled(Context context) {
        return sp(context).getBoolean(PREFERENCE_VOLUME_MAX, true);
    }

    public static int getOutputFormat(Context context) {
        return Integer.parseInt(sp(context).getString(PREFERENCE_OUTPUT_FORMAT, "0"));
    }

    public static int getAudioEncoder(Context context) {
        return Integer.parseInt(sp(context).getString(PREFERENCE_AUDIO_ENCODER, "0"));
    }

    public static int getEncodingBitrate(Context context) {
        return Integer.parseInt(sp(context).getString(PREFERENCE_ENCODING_BITRATE, "0"));
    }

    public static int getSamplingRate(Context context) {
        return Integer.parseInt(sp(context).getString(PREFERENCE_SAMPLE_RATE, "0"));
    }

    private static SharedPreferences sp(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }


}
