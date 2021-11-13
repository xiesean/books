//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.sean.mlbook.model.impl;

import org.sean.mlbook.bean.BookContentBean;
import org.sean.mlbook.bean.BookShelfBean;
import org.sean.mlbook.bean.SearchBookBean;
import org.sean.mlbook.listener.OnGetChapterListListener;
import org.sean.mlbook.model.IWebBookModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class WebBookModelImpl implements IWebBookModel {

    public static WebBookModelImpl getInstance() {
        return new WebBookModelImpl();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 网络请求并解析书籍信息
     * return BookShelfBean
     */
    @Override
    public Observable<BookShelfBean> getBookInfo(BookShelfBean bookShelfBean) {
        if (bookShelfBean.getTag().equals(TxtSkyBookModelImpl.TAG)) {
            return TxtSkyBookModelImpl.getInstance().getBookInfo(bookShelfBean);
        } else if (bookShelfBean.getTag().equals(LingdiankanshuStationBookModelImpl.TAG)) {
            return LingdiankanshuStationBookModelImpl.getInstance().getBookInfo(bookShelfBean);
        } else {
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 网络解析图书目录
     * return BookShelfBean
     */
    @Override
    public void getChapterList(final BookShelfBean bookShelfBean, OnGetChapterListListener getChapterListListener) {
        if (bookShelfBean.getTag().equals(TxtSkyBookModelImpl.TAG)) {
            TxtSkyBookModelImpl.getInstance().getChapterList(bookShelfBean, getChapterListListener);
        } else if (bookShelfBean.getTag().equals(LingdiankanshuStationBookModelImpl.TAG)) {
            LingdiankanshuStationBookModelImpl.getInstance().getChapterList(bookShelfBean, getChapterListListener);
        } else {
            if (getChapterListListener != null)
                getChapterListListener.success(bookShelfBean);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 章节缓存
     */
    @Override
    public Observable<BookContentBean> getBookContent(String durChapterUrl, int durChapterIndex, String tag) {
        if (tag.equals(TxtSkyBookModelImpl.TAG)) {
            return TxtSkyBookModelImpl.getInstance().getBookContent(durChapterUrl, durChapterIndex);
        } else if (tag.equals(LingdiankanshuStationBookModelImpl.TAG)) {
            return LingdiankanshuStationBookModelImpl.getInstance().getBookContent(durChapterUrl, durChapterIndex);
        } else
            return Observable.create(new ObservableOnSubscribe<BookContentBean>() {
                @Override
                public void subscribe(ObservableEmitter<BookContentBean> e) throws Exception {
                    e.onNext(new BookContentBean());
                    e.onComplete();
                }
            });
    }

    /**
     * 其他站点集合搜索
     */
    @Override
    public Observable<List<SearchBookBean>> searchOtherBook(String content, int page, String tag) {
        if (tag.equals(TxtSkyBookModelImpl.TAG)) {
            return TxtSkyBookModelImpl.getInstance().searchBook(content, page);
        } else if (tag.equals(LingdiankanshuStationBookModelImpl.TAG)) {
            return LingdiankanshuStationBookModelImpl.getInstance().searchBook(content, page);
        } else {
            return Observable.create(new ObservableOnSubscribe<List<SearchBookBean>>() {
                @Override
                public void subscribe(ObservableEmitter<List<SearchBookBean>> e) throws Exception {
                    e.onNext(new ArrayList<SearchBookBean>());
                    e.onComplete();
                }
            });
        }
    }

    /**
     * 获取分类书籍
     */
    @Override
    public Observable<List<SearchBookBean>> getKindBook(String url, int page) {
        if (url.contains(MaxReaderBookModelImpl.TAG)) {
            return MaxReaderBookModelImpl.getInstance().getKindBook(url, page);
        }
        return TxtSkyBookModelImpl.getInstance().getKindBook(url, page);
    }
}
