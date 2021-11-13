package com.sean.google.util;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.sean.google.BaseApplication;

import java.io.File;


public class AppUtil {
    private static final int WHAT_SCHEDULE_FULL_SYNC = 0;
    private static final int WHAT_TOAST = 1;

    private static Application application = BaseApplication.getApplication();

    static Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case WHAT_SCHEDULE_FULL_SYNC:
                    break;
                case WHAT_TOAST:
                    Toast.makeText(application, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    public static void showToast(String msg) {
        if (application != null) {
            Message message = new Message();
            message.what = WHAT_TOAST;
            message.obj = msg;
            handler.sendMessage(message);
        }
    }

    public static void runScheduleFullSync() {
        runUiThread(WHAT_SCHEDULE_FULL_SYNC);
    }

    private static void runUiThread(int what) {
        Message msg = new Message();
        msg.what = what;
        handler.sendMessage(msg);
    }

    /**
     * 跳转到应用商店评分
     *
     * @param context
     */
    public static void goAppShop(Context context) {
        try {
            Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            // Do nothing
        }
    }

    public static void shareApp(Context context, String title, String text) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, text);
            intent.setType("text/plain");
            context.startActivity(Intent.createChooser(intent, title));
        } catch (Exception e) {
            // Do nothing
        }
    }

    public static void shareFile(Context context, String title, File file) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("application/octet-stream");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            context.startActivity(Intent.createChooser(intent, title));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void judgePermission(Activity activity, String FileUrl, String filename) {
        /**
         * 先检查权限是否打开，打开了就可以直接访问，未打开则先申请一波权限
         */
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED || (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                makeAnExtraRequest(activity);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void makeAnExtraRequest(Activity activity) {
        int code = 100;
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        for (String string : permissions) {
            if (activity.checkSelfPermission(string) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(permissions, code);
                code++;
                return;
            }
        }
    }


}
