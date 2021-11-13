package com.sean.google.firebase;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.sean.google.BaseApplication;

public class EventUtil {
    private static FirebaseAnalytics instance = getInstance(BaseApplication.getContext());

    public static FirebaseAnalytics getInstance(Context context) {
        return FirebaseAnalytics.getInstance(context);
    }

    public static void onEvent(String eventId) {
        if (instance != null) {
            Bundle params = new Bundle();
            instance.logEvent(eventId, params);
        }
    }
}
