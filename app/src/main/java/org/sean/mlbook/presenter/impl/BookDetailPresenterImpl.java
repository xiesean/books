//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.sean.mlbook.presenter.impl;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.IView;
import com.monke.basemvplib.impl.BaseActivity;
import com.monke.basemvplib.impl.BasePresenterImpl;
import org.sean.mlbook.BitIntentDataManager;
import org.sean.mlbook.MApplication;
import org.sean.mlbook.base.observer.SimpleObserver;
import org.sean.mlbook.bean.BookShelfBean;
import org.sean.mlbook.bean.SearchBookBean;
import org.sean.mlbook.common.RxBusTag;
import org.sean.mlbook.dao.DbHelper;
import org.sean.mlbook.listener.OnGetChapterListListener;
import org.sean.mlbook.model.impl.WebBookModelImpl;
import org.sean.mlbook.presenter.IBookDetailPresenter;
import org.sean.mlbook.view.IBookDetailView;
import org.sean.mlbook.view.impl.BookDetailActivity;

import com.sean.google.admob.GAD;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.ArrayList;
import java.util.Collections;
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

public class BookDetailPresenterImpl extends BasePresenterImpl<IBookDetailView> implements IBookDetailPresenter {
    public final static int FROM_BOOKSHELF = 1;
    public final static int FROM_SEARCH = 2;

    private int openfrom;
    private SearchBookBean searchBook;
    private BookShelfBean bookShelf;
    private Boolean inBookShelf = false;
    private Activity activity;

    private List<BookShelfBean> bookShelfs = Collections.synchronizedList(new ArrayList<BookShelfBean>());   //用来比对搜索的书籍是否已经添加进书架

    public BookDetailPresenterImpl(Activity activity, Intent intent) {
        this.activity = activity;
        openfrom = intent.getIntExtra("from", FROM_BOOKSHELF);
        if (openfrom == FROM_BOOKSHELF) {
            String key = intent.getStringExtra("data_key");
            bookShelf = (BookShelfBean) BitIntentDataManager.getInstance().getData(key);
            BitIntentDataManager.getInstance().cleanData(key);
            inBookShelf = true;
        } else {
            searchBook = intent.getParcelableExtra("data");
            inBookShelf = searchBook.getAdd();
        }
    }

    public static void main(String[] args) {
        final BookShelfBean bookShelfResult = new BookShelfBean();
        bookShelfResult.setNoteUrl("http://www.gxwztv.com/ba598.shtml");
        bookShelfResult.setFinalDate(System.currentTimeMillis());
        bookShelfResult.setDurChapter(0);
        bookShelfResult.setDurChapterPage(0);
        bookShelfResult.setTag("http://www.gxwztv.com");
        WebBookModelImpl.getInstance().getBookInfo(bookShelfResult).subscribe(new Observer<BookShelfBean>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("-------------subscribe");
            }

            @Override
            public void onNext(BookShelfBean bookShelfBean) {
                System.out.println("-------------next");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("-------------error");
            }

            @Override
            public void onComplete() {
                System.out.println("-------------complete");
            }
        });
    }

    public Boolean getInBookShelf() {
        return inBookShelf;
    }

    public void setInBookShelf(Boolean inBookShelf) {
        this.inBookShelf = inBookShelf;
    }

    public int getOpenfrom() {
        return openfrom;
    }

    public SearchBookBean getSearchBook() {
        return searchBook;
    }

    public BookShelfBean getBookShelf() {
        return bookShelf;
    }

    @Override
    public void getBookShelfInfo() {
        Observable.create(new ObservableOnSubscribe<List<BookShelfBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<BookShelfBean>> e) throws Exception {
                List<BookShelfBean> temp = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder().list();
                if (temp == null)
                    temp = new ArrayList<BookShelfBean>();
                e.onNext(temp);
                e.onComplete();
            }
        }).flatMap(new Function<List<BookShelfBean>, ObservableSource<BookShelfBean>>() {
            @Override
            public ObservableSource<BookShelfBean> apply(List<BookShelfBean> bookShelfBeen) throws Exception {
                bookShelfs.addAll(bookShelfBeen);

                final BookShelfBean bookShelfResult = new BookShelfBean();
                bookShelfResult.setNoteUrl(searchBook.getNoteUrl());
                bookShelfResult.setFinalDate(System.currentTimeMillis());
                bookShelfResult.setDurChapter(0);
                bookShelfResult.setDurChapterPage(0);
                bookShelfResult.setTag(searchBook.getTag());
                return WebBookModelImpl.getInstance().getBookInfo(bookShelfResult);
            }
        }).map(new Function<BookShelfBean, BookShelfBean>() {
            @Override
            public BookShelfBean apply(BookShelfBean bookShelfBean) throws Exception {
                for (int i = 0; i < bookShelfs.size(); i++) {
                    if (bookShelfs.get(i).getNoteUrl().equals(bookShelfBean.getNoteUrl())) {
                        inBookShelf = true;
                        bookShelfBean.setDurChapter(bookShelfs.get(i).getDurChapter());
                        bookShelfBean.setDurChapterPage(bookShelfs.get(i).getDurChapterPage());
                        break;
                    }
                }
                return bookShelfBean;
            }
        }).subscribeOn(Schedulers.io())
                .compose(((BaseActivity) mView.getContext()).<BookShelfBean>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean value) {
                        WebBookModelImpl.getInstance().getChapterList(value, new OnGetChapterListListener() {
                            @Override
                            public void success(BookShelfBean bookShelfBean) {
                                bookShelf = bookShelfBean;
                                mView.updateView();
                            }

                            @Override
                            public void error() {
                                bookShelf = null;
                                mView.getBookShelfError();
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        bookShelf = null;
                        mView.getBookShelfError();
                    }
                });
    }

    @Override
    public void addToBookShelf() {
        // TODO 加广告
        Toast.makeText(mView.getContext(), "此功能需要解锁,请观看广告支持一下",Toast.LENGTH_SHORT).show();
        GAD.showCover(activity);
        if (bookShelf != null) {
            Observable.create(new ObservableOnSubscribe<Boolean>() {
                @Override
                public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                    DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().insertOrReplaceInTx(bookShelf.getBookInfoBean().getChapterlist());
                    DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().insertOrReplace(bookShelf.getBookInfoBean());
                    //网络数据获取成功  存入BookShelf表数据库
                    DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelf);
                    e.onNext(true);
                    e.onComplete();
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(((BaseActivity) mView.getContext()).<Boolean>bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean value) {
                            if (value) {
                                RxBus.get().post(RxBusTag.HAD_ADD_BOOK, bookShelf);
                            } else {
                                Toast.makeText(MApplication.getInstance(), "放入书架失败!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Toast.makeText(MApplication.getInstance(), "放入书架失败!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void removeFromBookShelf() {
        if (bookShelf != null) {
            Observable.create(new ObservableOnSubscribe<Boolean>() {
                @Override
                public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                    DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().deleteByKey(bookShelf.getNoteUrl());
                    DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().deleteByKey(bookShelf.getBookInfoBean().getNoteUrl());
                    List<String> keys = new ArrayList<String>();
                    if (bookShelf.getBookInfoBean().getChapterlist().size() > 0) {
                        for (int i = 0; i < bookShelf.getBookInfoBean().getChapterlist().size(); i++) {
                            keys.add(bookShelf.getBookInfoBean().getChapterlist().get(i).getDurChapterUrl());
                        }
                    }
                    DbHelper.getInstance().getmDaoSession().getBookContentBeanDao().deleteByKeyInTx(keys);
                    DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().deleteInTx(bookShelf.getBookInfoBean().getChapterlist());
                    e.onNext(true);
                    e.onComplete();
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(((BaseActivity) mView.getContext()).<Boolean>bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean value) {
                            if (value) {
                                RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, bookShelf);
                            } else {
                                Toast.makeText(MApplication.getInstance(), "移出书架失败!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Toast.makeText(MApplication.getInstance(), "移出书架失败!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void attachView( IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.HAD_ADD_BOOK)
            }
    )
    public void hadAddBook(BookShelfBean value) {
        if ((null != bookShelf && value.getNoteUrl().equals(bookShelf.getNoteUrl())) || (null != searchBook && value.getNoteUrl().equals(searchBook.getNoteUrl()))) {
            inBookShelf = true;
            if (null != searchBook) {
                searchBook.setAdd(inBookShelf);
            }
            mView.updateView();
        }
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.HAD_REMOVE_BOOK)
            }
    )
    public void hadRemoveBook(BookShelfBean value) {
        if (bookShelfs != null) {
            for (int i = 0; i < bookShelfs.size(); i++) {
                if (bookShelfs.get(i).getNoteUrl().equals(value.getNoteUrl())) {
                    bookShelfs.remove(i);
                    break;
                }
            }
        }
        if ((null != bookShelf && value.getNoteUrl().equals(bookShelf.getNoteUrl())) || (null != searchBook && value.getNoteUrl().equals(searchBook.getNoteUrl()))) {
            inBookShelf = false;
            if (null != searchBook) {
                searchBook.setAdd(false);
            }
            mView.updateView();
        }
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.HAD_ADD_BOOK),
            }
    )
    public void hadBook(BookShelfBean value) {
        bookShelfs.add(value);
        if ((null != bookShelf && value.getNoteUrl().equals(bookShelf.getNoteUrl())) || (null != searchBook && value.getNoteUrl().equals(searchBook.getNoteUrl()))) {
            inBookShelf = true;
            if (null != searchBook) {
                searchBook.setAdd(true);
            }
            mView.updateView();
        }
    }
}
