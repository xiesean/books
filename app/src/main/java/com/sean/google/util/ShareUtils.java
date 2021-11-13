package com.sean.google.util;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author：ivan huang
 * @date：2019-08-09
 * @describle 分享工具类
 */
public class ShareUtils {
    public static final String PACKAGE_WECHAT = "com.tencent.mm";
    public static final String PACKAGE_MOBILE_QQ = "com.tencent.mobileqq";
    public static final String PACKAGE_QZONE = "com.qzone";
    public static final String PACKAGE_SINA = "com.sina.weibo";
    /**
     * 微信7.0版本号，兼容处理微信7.0版本分享到朋友圈不支持多图片的问题
     */
    private static final int VERSION_CODE_FOR_WEI_XIN_VER7 = 1380;

    // 判断是否安装指定app
    public static boolean isInstallApp(Context context, String app_package) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pInfo = packageManager.getInstalledPackages(0);
        if (pInfo != null) {
            for (int i = 0; i < pInfo.size(); i++) {
                String pn = pInfo.get(i).packageName;
                if (app_package.equals(pn)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 分享图片给QQ好友
     *
     * @param bitmap 文件路径
     */
    public static void shareImageToQQ(Context mContext, String bitmap) {
        if (isInstallApp(mContext, ShareUtils.PACKAGE_MOBILE_QQ)) {
            try {
                //                Uri uriToImage = Uri.parse(MediaStore.Images.Media.insertImage(
                //                        mContext.getContentResolver(), bitmap, null, null));
                Uri uriToImage = Uri.parse(bitmap);
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shareIntent.setType("image/*");
                // 遍历所有支持发送图片的应用。找到需要的应用
                ComponentName componentName = new ComponentName(PACKAGE_MOBILE_QQ, "com.tencent.mobileqq.activity.JumpActivity");

                shareIntent.setComponent(componentName);
                // mContext.startActivity(shareIntent);
                mContext.startActivity(Intent.createChooser(shareIntent, "Share"));
            } catch (Exception e) {
                //            ContextUtil.getInstance().showToastMsg("分享图片到**失败");
            }
        } else {
            Toast.makeText(mContext, "您需要安装QQ客户端", Toast.LENGTH_LONG).show();
        }
        /*
        之前有同学说在分享QQ和微信的时候，发现只要QQ或微信在打开的情况下，再调用分享只是打开了QQ和微信，却没有调用选择分享联系人的情况
        解决办法如下：
        mActivity.startActivity(intent);//如果微信或者QQ已经唤醒或者打开，这样只能唤醒微信，不能分享
        请使用 mActivity.startActivity(Intent.createChooser(intent, "Share"));
        */
    }

    /**
     * 直接分享图片到微信好友
     *
     * @param picFile 文件路径
     */
    public static void shareWechatFriend(Context mContext, String picFile) {
        if (isInstallApp(mContext, ShareUtils.PACKAGE_WECHAT)) {
            Intent intent = new Intent();
            ComponentName cop = new ComponentName(PACKAGE_WECHAT, "com.tencent.mm.ui.tools.ShareImgUI");
            intent.setComponent(cop);
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("image/*");
            Uri uri = null;
            if (picFile != null) {
                //这部分代码主要功能是判断了下文件是否存在，在android版本高过7.0（包括7.0版本）
                // 当前APP是不能直接向外部应用提供file开头的的文件路径，需要通过FileProvider转换一下。否则在7.0及以上版本手机将直接crash。
                try {
                    ApplicationInfo applicationInfo = mContext.getApplicationInfo();
                    int targetSDK = applicationInfo.targetSdkVersion;
                    if (targetSDK >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        uri = Uri.parse(MediaStore.Images.Media.insertImage(mContext.getContentResolver(), new File(picFile).getAbsolutePath(), "pangu", null));
                    } else {
                        uri = Uri.fromFile(new File(picFile));
                    }
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (getVersionCode(mContext, PACKAGE_WECHAT) > VERSION_CODE_FOR_WEI_XIN_VER7) {
                // 微信7.0及以上版本
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
            }
            // context.startActivity(intent);
            mContext.startActivity(Intent.createChooser(intent, "Share"));
        } else {
            Toast.makeText(mContext, "您需要安装微信客户端", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 直接文件到微信好友
     *
     * @param picFile 文件路径
     */
    public static void shareWechatFriend(Context mContext, File picFile) {
        if (isInstallApp(mContext, ShareUtils.PACKAGE_WECHAT)) {
            Intent intent = new Intent();
            //
//            ComponentName cop = new ComponentName(PACKAGE_WECHAT, "com.tencent.mm.ui.tools.ShareImgUI");
//            intent.setComponent(cop);
            intent.setPackage(PACKAGE_WECHAT);
            intent.setAction(Intent.ACTION_SEND);
            String type = "*/*";
            for (int i = 0; i < MATCH_ARRAY.length; i++) {
                //判断文件的格式
                if (picFile.getAbsolutePath().toString().contains(MATCH_ARRAY[i][0].toString())) {
                    type = MATCH_ARRAY[i][1];
                    break;
                }
            }
            intent.setType(type);
            Uri uri = null;
            if (picFile != null) {
                //这部分代码主要功能是判断了下文件是否存在，在android版本高过7.0（包括7.0版本）
                // 当前APP是不能直接向外部应用提供file开头的的文件路径，需要通过FileProvider转换一下。否则在7.0及以上版本手机将直接crash。
                try {
                    ApplicationInfo applicationInfo = mContext.getApplicationInfo();
                    int targetSDK = applicationInfo.targetSdkVersion;
                    if (targetSDK >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        uri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".fileprovider", picFile);
                    } else {
                        uri = Uri.fromFile(picFile);
                    }
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (getVersionCode(mContext, PACKAGE_WECHAT) > VERSION_CODE_FOR_WEI_XIN_VER7) {
                // 微信7.0及以上版本
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
            }
            // context.startActivity(intent);
            mContext.startActivity(Intent.createChooser(intent, "Share"));
        } else {
            Toast.makeText(mContext, "您需要安装微信客户端", Toast.LENGTH_LONG).show();
        }
    }

    //建立一个文件类型与文件后缀名的匹配表
    private static final String[][] MATCH_ARRAY = {
            //{后缀名，    文件类型}
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".prop", "text/plain"},
            {".rar", "application/x-rar-compressed"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/zip"},
            {"", "*/*"}
    };

    /**
     * 直接分享文本和图片到微信朋友圈
     * 在分享微信朋友圈的时候需要注意一点，分享的图片要保存在微信可获取到的目录下
     * 一定不能保存在/data/data/****这个内置目录中，否则将获取不到图片报“获取不到图片资源，.....”导致分享失败。
     */
    public static void shareWechatMoment(Context context, String picFile) {
        if (isInstallApp(context, ShareUtils.PACKAGE_WECHAT)) {
            Intent intent = new Intent();
            //分享精确到微信的页面，朋友圈页面，或者选择好友分享页面
            ComponentName comp = new ComponentName(PACKAGE_WECHAT, "com.tencent.mm.ui.tools.ShareToTimeLineUI");
            intent.setComponent(comp);
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("image/*");
            //添加Uri图片地址--用于添加多张图片
            Uri uri = null;
            if (picFile != null) {
                try {
                    ApplicationInfo applicationInfo = context.getApplicationInfo();
                    int targetSDK = applicationInfo.targetSdkVersion;
                    if (targetSDK >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        uri = Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), new File(picFile).getAbsolutePath(), "pangu", null));
                    } else {
                        uri = Uri.fromFile(new File(picFile));
                    }
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (getVersionCode(context, PACKAGE_WECHAT) > VERSION_CODE_FOR_WEI_XIN_VER7) {
                // 微信7.0及以上版本
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
            }
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "您需要安装微信客户端", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 分享到新浪微博
     *
     * @param photoPath 文件路径
     */
    public static void shareToSinaFriends(Context context, String photoPath) {
        if (!isInstallApp(context, ShareUtils.PACKAGE_SINA)) {
            Toast.makeText(context, "新浪微博没有安装！", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(photoPath);
        if (!file.exists()) {
            String tip = "文件不存在";
            Toast.makeText(context, tip + " path = " + photoPath, Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        // 使用以下两种type有一定的区别，"text/plain"分享给指定的粉丝或好友 ；"image/*"分享到微博内容,下面这两个设置type的代码必须写在查询语句前面，否则找不到带有分享功能的应用。
        //        intent.setType("text/plain");
        intent.setType("image/*");// 分享文本|文本+图片|图片 到微博内容时使用
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> matchs = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        ResolveInfo resolveInfo = null;
        for (ResolveInfo each : matchs) {
            String pkgName = each.activityInfo.applicationInfo.packageName;
            if ("com.sina.weibo".equals(pkgName)) {
                resolveInfo = each;
                break;
            }
        }
        intent.setClassName(PACKAGE_SINA, resolveInfo.activityInfo.name);// 这里在使用resolveInfo的时候需要做判空处理防止crash
        intent.putExtra(Intent.EXTRA_TEXT, "Test Text String !!");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        context.startActivity(intent);
    }


    /**
     * 分享多图片到微信朋友圈
     *
     * @param bmp 分享的图片的Bitmap对象
     */
    public void shareImageToWechat(Bitmap bmp, Context mContext) {
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsoluteFile();
        String fileName = "share";
        File appDir = new File(file, fileName);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        fileName = System.currentTimeMillis() + ".jpg";
        File currentFile = new File(appDir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(currentFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ArrayList<Uri> uris = new ArrayList<>();
        Uri uri = null;
        try {
            ApplicationInfo applicationInfo = mContext.getApplicationInfo();
            int targetSDK = applicationInfo.targetSdkVersion;
            if (targetSDK >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = Uri.parse(MediaStore.Images.Media.insertImage(mContext.getContentResolver(), currentFile.getAbsolutePath(), fileName, null));
            } else {
                uri = Uri.fromFile(new File(currentFile.getPath()));
            }
            uris.add(uri);
        } catch (Exception ex) {

        }
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        ComponentName comp = new ComponentName(PACKAGE_WECHAT, "com.tencent.mm.ui.tools.ShareToTimeLineUI");
        intent.setComponent(comp);
        intent.setType("image/*");
        //        intent.putExtra("Kdescription", content);
        if (getVersionCode(mContext, PACKAGE_WECHAT) < VERSION_CODE_FOR_WEI_XIN_VER7) {
            // 微信7.0以下版本
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        } else {
            // 微信7.0及以上版本
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
        }
        mContext.startActivity(intent);
    }

    /**
     * 获取制定包名应用的版本的versionCode
     *
     * @param context
     * @param
     * @return
     */
    private static int getVersionCode(Context context, String packageName) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(packageName, 0);
            int version = info.versionCode;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}

