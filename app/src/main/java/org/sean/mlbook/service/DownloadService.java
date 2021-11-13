//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.sean.mlbook.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import org.sean.mlbook.R;
import org.sean.mlbook.base.observer.SimpleObserver;
import org.sean.mlbook.bean.BookContentBean;
import org.sean.mlbook.bean.BookShelfBean;
import org.sean.mlbook.bean.ChapterListBean;
import org.sean.mlbook.bean.DownloadChapterBean;
import org.sean.mlbook.bean.DownloadChapterListBean;
import org.sean.mlbook.common.RxBusTag;
import org.sean.mlbook.dao.BookContentBeanDao;
import org.sean.mlbook.dao.BookShelfBeanDao;
import org.sean.mlbook.dao.DbHelper;
import org.sean.mlbook.dao.DownloadChapterBeanDao;
import org.sean.mlbook.model.impl.WebBookModelImpl;
import org.sean.mlbook.view.impl.MainActivity;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class DownloadService extends Service {
    public static final int reTryTimes = 1;
    private NotificationManager notifyManager;
    private int notifiId = 19931118;
    private Boolean isStartDownload = false;
    private Boolean isInit = false;
    private Boolean isDownloading = false;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
        isInit = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isInit) {
            isInit = true;
            notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            RxBus.get().register(this);
        }
        createNotificationChannel();
        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // 通知渠道的id
            String id = "my_channel_01";
            // 用户可以看到的通知渠道的名字.
            CharSequence name = getString(R.string.app_name);
//         用户可以看到的通知渠道的描述
            String description = getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
//         配置通知渠道的属性
            mChannel.setDescription(description);
//         设置通知出现时的闪灯（如果 android 设备支持的话）
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
//         设置通知出现时的震动（如果 android 设备支持的话）
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
//         最后在notificationmanager中创建该通知渠道 //
            mNotificationManager.createNotificationChannel(mChannel);

            // 为该通知设置一个id
            int notifyID = 1;
            // 通知渠道的id
            String CHANNEL_ID = "my_channel_01";
            // Create a notification and set the notification channel.
            Notification notification = new Notification.Builder(this)
                    .setContentTitle("New Message").setContentText("You've received new messages.")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setChannelId(CHANNEL_ID)
                    .build();
            startForeground(1, notification);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void addNewTask(final List<DownloadChapterBean> newData) {
        isStartDownload = true;
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().insertOrReplaceInTx(newData);
                e.onNext(true);
                e.onComplete();
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        if (!isDownloading) {
                            toDownload();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private void toDownload() {
        isDownloading = true;
        if (isStartDownload) {
            Observable.create(new ObservableOnSubscribe<DownloadChapterBean>() {
                @Override
                public void subscribe(ObservableEmitter<DownloadChapterBean> e) throws Exception {
                    List<BookShelfBean> bookShelfBeanList = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder().orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
                    if (bookShelfBeanList != null && bookShelfBeanList.size() > 0) {
                        for (BookShelfBean bookItem : bookShelfBeanList) {
                            if (!bookItem.getTag().equals(BookShelfBean.LOCAL_TAG)) {
                                List<DownloadChapterBean> downloadChapterList = DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().queryBuilder().where(DownloadChapterBeanDao.Properties.NoteUrl.eq(bookItem.getNoteUrl())).orderAsc(DownloadChapterBeanDao.Properties.DurChapterIndex).limit(1).list();
                                if (downloadChapterList != null && downloadChapterList.size() > 0) {
                                    e.onNext(downloadChapterList.get(0));
                                    e.onComplete();
                                    return;
                                }
                            }
                        }
                        DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().deleteAll();
                        e.onNext(new DownloadChapterBean());
                    } else {
                        DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().deleteAll();
                        e.onNext(new DownloadChapterBean());
                    }
                    e.onComplete();
                }
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<DownloadChapterBean>() {
                        @Override
                        public void onNext(DownloadChapterBean value) {
                            if (value.getNoteUrl() != null && value.getNoteUrl().length() > 0) {
                                downloading(value, 0);
                            } else {
                                Observable.create(new ObservableOnSubscribe<Object>() {
                                    @Override
                                    public void subscribe(ObservableEmitter<Object> e) throws Exception {
                                        DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().deleteAll();
                                        e.onNext(new Object());
                                        e.onComplete();
                                    }
                                })
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new SimpleObserver<Object>() {
                                            @Override
                                            public void onNext(Object value) {
                                                isDownloading = false;
                                                finishDownload();
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                e.printStackTrace();
                                                isDownloading = false;
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            isDownloading = false;
                        }
                    });
        } else {
            isPause();
        }
    }

    private void downloading(final DownloadChapterBean data, final int durTime) {
        if (durTime < reTryTimes && isStartDownload) {
            isProgress(data);
            Observable.create(new ObservableOnSubscribe<BookContentBean>() {
                @Override
                public void subscribe(ObservableEmitter<BookContentBean> e) throws Exception {
                    List<BookContentBean> result = DbHelper.getInstance().getmDaoSession().getBookContentBeanDao().queryBuilder().where(BookContentBeanDao.Properties.DurChapterUrl.eq(data.getDurChapterUrl())).list();
                    if (result != null && result.size() > 0) {
                        e.onNext(result.get(0));
                    } else {
                        e.onNext(new BookContentBean());
                    }
                    e.onComplete();
                }
            }).flatMap(new Function<BookContentBean, ObservableSource<BookContentBean>>() {
                @Override
                public ObservableSource<BookContentBean> apply(final BookContentBean bookContentBean) throws Exception {
                    if (bookContentBean.getDurChapterUrl() == null || bookContentBean.getDurChapterUrl().length() <= 0) {
                        return WebBookModelImpl.getInstance().getBookContent(data.getDurChapterUrl(), data.getDurChapterIndex(), data.getTag()).map(new Function<BookContentBean, BookContentBean>() {
                            @Override
                            public BookContentBean apply(BookContentBean bookContentBean) throws Exception {
                                DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().delete(data);
                                if (bookContentBean.getRight()) {
                                    DbHelper.getInstance().getmDaoSession().getBookContentBeanDao().insertOrReplace(bookContentBean);
                                    DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().update(new ChapterListBean(data.getNoteUrl(), data.getDurChapterIndex(), data.getDurChapterUrl(), data.getDurChapterName(), data.getTag(), true));
                                }
                                return bookContentBean;
                            }
                        });
                    } else {
                        return Observable.create(new ObservableOnSubscribe<BookContentBean>() {
                            @Override
                            public void subscribe(ObservableEmitter<BookContentBean> e) throws Exception {
                                DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().delete(data);
                                e.onNext(bookContentBean);
                                e.onComplete();
                            }
                        });
                    }
                }
            })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new SimpleObserver<BookContentBean>() {
                        @Override
                        public void onNext(BookContentBean value) {
                            if (isStartDownload) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (isStartDownload) {
                                            toDownload();
                                        } else {
                                            isPause();
                                        }
                                    }
                                }, 800);
                            } else {
                                isPause();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            int time = durTime + 1;
                            downloading(data, time);
                        }
                    });
        } else {
            if (isStartDownload) {
                Observable.create(new ObservableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                        DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().delete(data);
                        e.onNext(true);
                        e.onComplete();
                    }
                })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new SimpleObserver<Boolean>() {
                            @Override
                            public void onNext(Boolean value) {
                                if (isStartDownload) {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (isStartDownload) {
                                                toDownload();
                                            } else {
                                                isPause();
                                            }
                                        }
                                    }, 800);
                                } else {
                                    isPause();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                if (!isStartDownload)
                                    isPause();
                            }
                        });
            } else
                isPause();
        }
    }

    public void startDownload() {
        isStartDownload = true;
        toDownload();
    }

    public void pauseDownload() {
        isStartDownload = false;
        notifyManager.cancelAll();
    }

    public void cancelDownload() {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().deleteAll();
                e.onNext(new Object());
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object value) {
                        pauseDownload();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void isPause() {
        isDownloading = false;
        Observable.create(new ObservableOnSubscribe<DownloadChapterBean>() {
            @Override
            public void subscribe(ObservableEmitter<DownloadChapterBean> e) throws Exception {
                List<BookShelfBean> bookShelfBeanList = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder().orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
                if (bookShelfBeanList != null && bookShelfBeanList.size() > 0) {
                    for (BookShelfBean bookItem : bookShelfBeanList) {
                        if (!bookItem.getTag().equals(BookShelfBean.LOCAL_TAG)) {
                            List<DownloadChapterBean> downloadChapterList = DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().queryBuilder().where(DownloadChapterBeanDao.Properties.NoteUrl.eq(bookItem.getNoteUrl())).orderAsc(DownloadChapterBeanDao.Properties.DurChapterIndex).limit(1).list();
                            if (downloadChapterList != null && downloadChapterList.size() > 0) {
                                e.onNext(downloadChapterList.get(0));
                                e.onComplete();
                                return;
                            }
                        }
                    }
                    DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().deleteAll();
                    e.onNext(new DownloadChapterBean());
                } else {
                    DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().deleteAll();
                    e.onNext(new DownloadChapterBean());
                }
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<DownloadChapterBean>() {
                    @Override
                    public void onNext(DownloadChapterBean value) {
                        if (value.getNoteUrl() != null && value.getNoteUrl().length() > 0) {
                            RxBus.get().post(RxBusTag.PAUSE_DOWNLOAD_LISTENER, new Object());
                        } else {
                            RxBus.get().post(RxBusTag.FINISH_DOWNLOAD_LISTENER, new Object());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private void isProgress(DownloadChapterBean downloadChapterBean) {
        RxBus.get().post(RxBusTag.PROGRESS_DOWNLOAD_LISTENER, downloadChapterBean);

        Intent mainIntent = new Intent(this, MainActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //创建 Notification.Builder 对象
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                //点击通知后自动清除
                .setAutoCancel(true)
                .setContentTitle("正在下载：" + downloadChapterBean.getBookName())
                .setContentText(downloadChapterBean.getDurChapterName() == null ? "  " : downloadChapterBean.getDurChapterName())
                .setContentIntent(mainPendingIntent);
        //发送通知
        notifyManager.notify(notifiId, builder.build());
    }

    private void finishDownload() {
        RxBus.get().post(RxBusTag.FINISH_DOWNLOAD_LISTENER, new Object());
        notifyManager.cancelAll();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "全部离线章节下载完成", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.PAUSE_DOWNLOAD)
            }
    )
    public void pauseTask(Object o) {
        pauseDownload();
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.START_DOWNLOAD)
            }
    )
    public void startTask(Object o) {
        startDownload();
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.CANCEL_DOWNLOAD)
            }
    )
    public void cancelTask(Object o) {
        cancelDownload();
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.ADD_DOWNLOAD_TASK)
            }
    )
    public void addTask(DownloadChapterListBean newData) {
        addNewTask(newData.getData());
    }
}