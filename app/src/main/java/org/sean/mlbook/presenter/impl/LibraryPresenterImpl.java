//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.sean.mlbook.presenter.impl;

import android.os.Handler;

import com.monke.basemvplib.impl.BasePresenterImpl;
import org.sean.mlbook.MApplication;
import org.sean.mlbook.base.observer.SimpleObserver;
import org.sean.mlbook.bean.LibraryBean;
import org.sean.mlbook.cache.ACache;
import org.sean.mlbook.model.impl.TxtSkyBookModelImpl;
import org.sean.mlbook.presenter.ILibraryPresenter;
import org.sean.mlbook.view.ILibraryView;

import java.util.LinkedHashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class LibraryPresenterImpl extends BasePresenterImpl<ILibraryView> implements ILibraryPresenter {
    public final static String LIBRARY_CACHE_KEY = "cache_library";
    private final LinkedHashMap<String, String> kinds = new LinkedHashMap<>();
    private ACache mCache;
    private Boolean isFirst = true;

    public LibraryPresenterImpl() {
        kinds.put("文学名著", "https://www.xstt5.com/mingzhu/");
        kinds.put("现代小说", "https://www.xstt5.com/dangdai/");
        kinds.put("世界名著", "https://www.xstt5.com/waiwen/");
        kinds.put("儿童文学", "https://www.xstt5.com/ertong/");
        kinds.put("古典名著", "https://www.xstt5.com/gudian/");
        kinds.put("散文随笔", "https://www.xstt5.com/sanwen/");
        kinds.put("青春校园", "https://www.xstt5.com/qingchun/");
        kinds.put("文学评论", "https://www.xstt5.com/pinglun/");
        kinds.put("玄幻仙侠", "https://www.xstt5.com/xuanhuan/");
        kinds.put("言情小说", "https://www.xstt5.com/yanqing/");
        kinds.put("武侠小说", "https://www.xstt5.com/wuxia/");
        kinds.put("穿越小说", "https://www.xstt5.com/chuanyue/");
        kinds.put("侦探悬疑", "https://www.xstt5.com/xuanyi/");
        kinds.put("科幻小说", "https://www.xstt5.com/kehuan/");
        kinds.put("网游小说", "https://www.xstt5.com/wangyou/");
        kinds.put("人文社科", "https://www.xstt5.com/renwen/");
        kinds.put("人物传记", "https://www.xstt5.com/zhuanji/");
        kinds.put("历史小说", "https://www.xstt5.com/lishi/");
        kinds.put("军事小说", "https://www.xstt5.com/junshi/");
        kinds.put("励志书籍", "https://www.xstt5.com/lizhi/");
        kinds.put("生活科普", "https://www.xstt5.com/shenghuo/");
        kinds.put("成人18+", "https://wap.maxreader.net/sort/dushi.html");

        mCache = ACache.get(MApplication.getInstance());
    }

    @Override
    public void detachView() {

    }

    @Override
    public void getLibraryData() {
        if (isFirst) {
            isFirst = false;
            Observable.create(new ObservableOnSubscribe<String>() {
                @Override
                public void subscribe(ObservableEmitter<String> e) throws Exception {
                    String cache = mCache.getAsString(LIBRARY_CACHE_KEY);
                    e.onNext(cache);
                    e.onComplete();
                }
            }).flatMap(new Function<String, ObservableSource<LibraryBean>>() {
                @Override
                public ObservableSource<LibraryBean> apply(String s) throws Exception {
                    return TxtSkyBookModelImpl.getInstance().analyLibraryData(s);
                }
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<LibraryBean>() {
                        @Override
                        public void onNext(LibraryBean value) {
                            //执行刷新界面
                            mView.updateUI(value);
                            getLibraryNewData();
                        }

                        @Override
                        public void onError(Throwable e) {
                            getLibraryNewData();
                        }
                    });
        } else {
            getLibraryNewData();
        }
    }

    private void getLibraryNewData() {
        TxtSkyBookModelImpl.getInstance().getLibraryData(mCache).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<LibraryBean>() {
                    @Override
                    public void onNext(final LibraryBean value) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mView.updateUI(value);
                                mView.finishRefresh();
                            }
                        }, 1000);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.finishRefresh();
                    }
                });
    }

    @Override
    public LinkedHashMap<String, String> getKinds() {
        return kinds;
    }
}