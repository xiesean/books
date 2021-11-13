//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.sean.mlbook;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.sean.google.BaseApplication;

import org.sean.mlbook.service.DownloadService;

public class MApplication extends BaseApplication {

    private static MApplication instance;

    public static MApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.IS_RELEASE) {
            try {
                ApplicationInfo appInfo = getPackageManager()
                        .getApplicationInfo(getPackageName(),
                                PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        instance = this;
        ProxyManager.initProxy();
        startService(new Intent(this, DownloadService.class));
        // 启动服务的地方
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(new Intent(this, DownloadService.class));
        } else {
            this.startService(new Intent(this, DownloadService.class));
        }
    }
}
