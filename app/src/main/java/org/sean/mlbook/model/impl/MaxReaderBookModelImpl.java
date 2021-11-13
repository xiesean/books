//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.sean.mlbook.model.impl;

import android.text.Html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.sean.mlbook.ErrorAnalyContentManager;
import org.sean.mlbook.base.MBaseModelImpl;
import org.sean.mlbook.base.observer.SimpleObserver;
import org.sean.mlbook.bean.BookContentBean;
import org.sean.mlbook.bean.BookInfoBean;
import org.sean.mlbook.bean.BookShelfBean;
import org.sean.mlbook.bean.ChapterListBean;
import org.sean.mlbook.bean.LibraryBean;
import org.sean.mlbook.bean.LibraryKindBookListBean;
import org.sean.mlbook.bean.SearchBookBean;
import org.sean.mlbook.bean.WebChapterBean;
import org.sean.mlbook.cache.ACache;
import org.sean.mlbook.common.api.IGxwztvApi;
import org.sean.mlbook.listener.OnGetChapterListListener;
import org.sean.mlbook.model.IGxwztvBookModel;
import org.sean.mlbook.presenter.impl.LibraryPresenterImpl;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MaxReaderBookModelImpl extends MBaseModelImpl implements IGxwztvBookModel {
    public static final String TAG = "https://wap.maxreader.net/";

    public static MaxReaderBookModelImpl getInstance() {
        return new MaxReaderBookModelImpl();
    }


    /**
     * 获取主页信息
     */
    @Override
    public Observable<LibraryBean> getLibraryData(final ACache aCache) {
        return getRetrofitObject(TAG).create(IGxwztvApi.class).getLibraryData("sort.html").flatMap(new Function<String, ObservableSource<LibraryBean>>() {
            @Override
            public ObservableSource<LibraryBean> apply(String s) throws Exception {
                if (s != null && s.length() > 0 && aCache != null) {
                    aCache.put(LibraryPresenterImpl.LIBRARY_CACHE_KEY, s);
                }
                return analyLibraryData(s);
            }
        });
    }

    /**
     * 解析主页数据
     */
    @Override
    public Observable<LibraryBean> analyLibraryData(final String data) {
        return Observable.create(new ObservableOnSubscribe<LibraryBean>() {
            @Override
            public void subscribe(ObservableEmitter<LibraryBean> e) throws Exception {
                LibraryBean result = new LibraryBean();
                Document doc = Jsoup.parse(data);
                Element contentE = doc.getElementsByClass("norl yli").get(0);
                //解析最新
//                Elements newBookEs = contentE.getElementsByTag("a");
//                List<LibraryNewBookBean> libraryNewBooks = new ArrayList<LibraryNewBookBean>();
//                for (int i = 0; i < newBookEs.size(); i++) {
//                    Element itemE = newBookEs.get(i);
//                    String title = itemE.attr("title");
//                    if(TextUtils.isEmpty(title)){
//                        continue;
//                    }
//                    LibraryNewBookBean item = new LibraryNewBookBean(itemE.attr("title"), TAG + itemE.attr("href"), TAG, "");
//                    libraryNewBooks.add(item);
//                }
//                result.setLibraryNewBooks(libraryNewBooks);
                //////////////////////////////////////////////////////////////////////
                List<LibraryKindBookListBean> kindBooks = new ArrayList<LibraryKindBookListBean>();
                //分类推荐
                Elements hotEs = doc.getElementsByClass("bkj zl1");
                for (int i = 1; i < hotEs.size(); i++) {
                    LibraryKindBookListBean kindItem = new LibraryKindBookListBean();
                    kindItem.setKindName(hotEs.get(i).getElementsByTag("h2").get(0).text());
                    Element inbkcon = hotEs.get(i).getElementsByClass("inbkcon").get(0);
                    Elements bookEs =inbkcon.getElementsByTag("a");
                    List<SearchBookBean> books = new ArrayList<SearchBookBean>();
                    // 帶圖的書
                    SearchBookBean searchBookBean = new SearchBookBean();
                    searchBookBean.setOrigin("");
                    searchBookBean.setTag(TAG);
                    searchBookBean.setName(bookEs.get(0).getElementsByTag("img").get(0).attr("alt"));
                    searchBookBean.setNoteUrl(TAG + bookEs.get(0).attr("href"));
                    searchBookBean.setCoverUrl(bookEs.get(0).getElementsByTag("img").get(0).attr("src"));
                    books.add(searchBookBean);
                    // 不帶圖的书
                    bookEs =inbkcon.getElementsByClass("norl2").get(0).getElementsByTag("li");
                    for (int j = 0; j < bookEs.size(); j++) {
                         searchBookBean = new SearchBookBean();
                        searchBookBean.setOrigin("");
                        searchBookBean.setTag(TAG);
                        searchBookBean.setName(bookEs.get(j).getElementsByTag("a").get(0).text());
                        searchBookBean.setAuthor(bookEs.get(j).getElementsByTag("a").get(1).text());
                        searchBookBean.setNoteUrl(bookEs.get(j).getElementsByTag("a").get(0).attr("href"));
                        searchBookBean.setCoverUrl("");
                        books.add(searchBookBean);
                    }
                    kindItem.setBooks(books);
                    kindBooks.add(kindItem);
                }
                //解析部分分类推荐
                Elements kindEs = contentE.getElementsByClass("panel panel-info index-category-qk");
                for (int i = 0; i < kindEs.size(); i++) {
                    LibraryKindBookListBean kindItem = new LibraryKindBookListBean();
                    kindItem.setKindName(kindEs.get(i).getElementsByClass("panel-title").get(0).text());
                    kindItem.setKindUrl(TAG + kindEs.get(i).getElementsByClass("listMore").get(0).getElementsByTag("a").get(0).attr("href"));

                    List<SearchBookBean> books = new ArrayList<SearchBookBean>();
                    Element firstBookE = kindEs.get(i).getElementsByTag("dl").get(0);
                    SearchBookBean firstBook = new SearchBookBean();
                    firstBook.setTag(TAG);
                    firstBook.setOrigin("");
                    firstBook.setName(firstBookE.getElementsByTag("a").get(1).text());
                    firstBook.setNoteUrl(TAG + firstBookE.getElementsByTag("a").get(0).attr("href"));
                    firstBook.setCoverUrl(firstBookE.getElementsByTag("a").get(0).getElementsByTag("img").get(0).attr("src"));
                    firstBook.setKind(kindItem.getKindName());
                    books.add(firstBook);

                    Elements otherBookEs = kindEs.get(i).getElementsByClass("book_textList").get(0).getElementsByTag("li");
                    for (int j = 0; j < otherBookEs.size(); j++) {
                        SearchBookBean item = new SearchBookBean();
                        item.setTag(TAG);
                        item.setOrigin("");
                        item.setKind(kindItem.getKindName());
                        item.setNoteUrl(TAG + otherBookEs.get(j).getElementsByTag("a").get(0).attr("href"));
                        item.setName(otherBookEs.get(j).getElementsByTag("a").get(0).text());
                        books.add(item);
                    }
                    kindItem.setBooks(books);
                    kindBooks.add(kindItem);
                }
                //////////////
                result.setKindBooks(kindBooks);
                e.onNext(result);
                e.onComplete();
            }
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        return getRetrofitObject(TAG).create(IGxwztvApi.class).searchBook(content, page).flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
            @Override
            public ObservableSource<List<SearchBookBean>> apply(String s) throws Exception {
                return analySearchBook(s);
            }
        });
    }

    public Observable<List<SearchBookBean>> analySearchBook(final String s) {
        return Observable.create(new ObservableOnSubscribe<List<SearchBookBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<SearchBookBean>> e) throws Exception {
                try {
                    Document doc = Jsoup.parse(s);
                    Elements booksE = doc.getElementById("zuo").getElementsByClass("bbox");
                    if (null != booksE && booksE.size() >= 2) {
                        List<SearchBookBean> books = new ArrayList<SearchBookBean>();
                        for (int i = 1; i < booksE.size(); i++) {
                            SearchBookBean item = new SearchBookBean();
                            item.setTag(TAG);
                            item.setAuthor(booksE.get(i).getElementsByClass("bpic").get(0).getElementsByTag("img").attr("alt"));
                            item.setKind("");
                            item.setLastChapter("");
                            item.setOrigin("");
                            item.setName(booksE.get(i).getElementsByClass("bpic").get(0).getElementsByTag("img").attr("alt"));
                            item.setNoteUrl(TAG + booksE.get(i).getElementsByClass("bintro").get(0).getElementsByTag("a").get(0).attr("href"));
                            item.setCoverUrl(booksE.get(i).getElementsByClass("bpic").get(0).getElementsByTag("img").attr("src"));
                            books.add(item);
                        }
                        e.onNext(books);
                    } else {
                        e.onNext(new ArrayList<SearchBookBean>());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    e.onNext(new ArrayList<SearchBookBean>());
                }
                e.onComplete();
            }
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Observable<BookShelfBean> getBookInfo(final BookShelfBean bookShelfBean) {
        return getRetrofitObject(TAG).create(IGxwztvApi.class).getBookInfo(bookShelfBean.getNoteUrl().replace(TAG, "")).flatMap(new Function<String, ObservableSource<BookShelfBean>>() {
            @Override
            public ObservableSource<BookShelfBean> apply(String s) throws Exception {
                return analyBookInfo(s, bookShelfBean);
            }
        });
    }

    private Observable<BookShelfBean> analyBookInfo(final String s, final BookShelfBean bookShelfBean) {
        return Observable.create(new ObservableOnSubscribe<BookShelfBean>() {
            @Override
            public void subscribe(ObservableEmitter<BookShelfBean> e) throws Exception {
                bookShelfBean.setTag(TAG);
                bookShelfBean.setBookInfoBean(analyBookinfo(s, bookShelfBean.getNoteUrl()));
                e.onNext(bookShelfBean);
                e.onComplete();
            }
        });
    }

    private BookInfoBean analyBookinfo(String s, String novelUrl) {
        BookInfoBean bookInfoBean = new BookInfoBean();
        bookInfoBean.setNoteUrl(novelUrl);   //id
        bookInfoBean.setTag(TAG);
        Document doc = Jsoup.parse(s);
        String downloadHtml = "";
        try {
            downloadHtml = doc.getElementsByAttributeValue("src", "https://statics.weiyuedu.cc/skin/v3/im/txtdown.gif")
                    .parents().get(0)
                    .getElementsByTag("a").get(0)
                    .attr("href");
        } catch (Exception e) {
            e.printStackTrace();
        }
        bookInfoBean.setDownloadUrl(downloadHtml);
        Element resultE = doc.getElementsByClass("zhaungtai").get(0);
        bookInfoBean.setCoverUrl(doc.getElementsByClass("img1").get(0).attr("src"));
        bookInfoBean.setName(doc.getElementsByClass("img1").get(0).attr("alt"));
        bookInfoBean.setAuthor(resultE.getElementsByTag("li").get(0).getElementsByTag("a").get(0).text());
        Element introduceE = doc.getElementsByClass("neir").get(0).getElementById("aboutbook");
        String introduce = "";
        if (introduceE.getElementsByTag("p") != null) {
            introduce = introduceE.getElementsByTag("p").get(0).text();
        } else {
            introduce = "";
        }
        bookInfoBean.setIntroduce("\u3000\u3000" + introduce);
        bookInfoBean.setChapterUrl(novelUrl);
        bookInfoBean.setOrigin("");
        return bookInfoBean;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void getChapterList(final BookShelfBean bookShelfBean, final OnGetChapterListListener getChapterListListener) {
        getRetrofitObject(TAG).create(IGxwztvApi.class).getChapterList(bookShelfBean.getBookInfoBean().getChapterUrl().replace(TAG, "")).flatMap(new Function<String, ObservableSource<WebChapterBean<BookShelfBean>>>() {
            @Override
            public ObservableSource<WebChapterBean<BookShelfBean>> apply(String s) throws Exception {
                return analyChapterList(s, bookShelfBean);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<WebChapterBean<BookShelfBean>>() {
                    @Override
                    public void onNext(WebChapterBean<BookShelfBean> value) {
                        if (getChapterListListener != null) {
                            getChapterListListener.success(value.getData());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (getChapterListListener != null) {
                            getChapterListListener.error();
                        }
                    }
                });
    }

    private Observable<WebChapterBean<BookShelfBean>> analyChapterList(final String s, final BookShelfBean bookShelfBean) {
        return Observable.create(new ObservableOnSubscribe<WebChapterBean<BookShelfBean>>() {
            @Override
            public void subscribe(ObservableEmitter<WebChapterBean<BookShelfBean>> e) throws Exception {
                bookShelfBean.setTag(TAG);
                WebChapterBean<List<ChapterListBean>> temp = analyChapterlist(s, bookShelfBean.getNoteUrl());
                bookShelfBean.getBookInfoBean().setChapterlist(temp.getData());
                e.onNext(new WebChapterBean<BookShelfBean>(bookShelfBean, temp.getNext()));
                e.onComplete();
            }
        });
    }

    private void createChapterBean(Elements ulElements) {
    }

    private WebChapterBean<List<ChapterListBean>> analyChapterlist(String s, String novelUrl) {
        Document doc = Jsoup.parse(s);
        Elements chapterlist = doc.getElementById("chapterList_1").getElementsByTag("table").get(0).getElementsByTag("ul");
        List<ChapterListBean> chapterBeans = new ArrayList<ChapterListBean>();
        int index = 0;
        ChapterListBean[][] array = new ChapterListBean[chapterlist.size()][];
        for (int i = 0; i < chapterlist.size(); i++) {
            Element ul = chapterlist.get(i);
            Elements aElements = ul.getElementsByTag("a");
            for (int j = 0; j < aElements.size(); j++) {
                if (array[i] == null) {
                    array[i] = new ChapterListBean[aElements.size()];
                }
                Element a = aElements.get(j);
                ChapterListBean temp = new ChapterListBean();
                temp.setDurChapterUrl(TAG + a.attr("href"));   //id
                temp.setDurChapterIndex(index++);
                temp.setDurChapterName(a.attr("title"));
                temp.setNoteUrl(novelUrl);
                temp.setTag(TAG);
                array[i][j] = temp;
            }
        }
        for (int i = 0;i<array[0].length;i++){
            for (int j=0;j<array.length;j++){
                if (i<array[j].length) {
                    chapterBeans.add(array[j][i]);
                }
            }
        }
        Boolean next = false;
        return new WebChapterBean<List<ChapterListBean>>(chapterBeans, next);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Observable<BookContentBean> getBookContent(final String durChapterUrl, final int durChapterIndex) {
        return getRetrofitObject(TAG).create(IGxwztvApi.class).getBookContent(durChapterUrl.replace(TAG, "")).flatMap(new Function<String, ObservableSource<BookContentBean>>() {
            @Override
            public ObservableSource<BookContentBean> apply(String s) throws Exception {
                return analyBookContent(s, durChapterUrl, durChapterIndex);
            }
        });
    }

    private Observable<BookContentBean> analyBookContent(final String s, final String durChapterUrl, final int durChapterIndex) {
        return Observable.create(new ObservableOnSubscribe<BookContentBean>() {
            @Override
            public void subscribe(ObservableEmitter<BookContentBean> e) throws Exception {
                BookContentBean bookContentBean = new BookContentBean();
                bookContentBean.setDurChapterIndex(durChapterIndex);
                bookContentBean.setDurChapterUrl(durChapterUrl);
                bookContentBean.setTag(TAG);
                try {
                    Document doc = Jsoup.parse(s);
                    String contentEs = Html.fromHtml(doc.getElementsByClass("zw").get(0).toString()).toString();
                    contentEs = contentEs.replaceAll(" ", "")
                                .replaceAll("小.*说.*堂", "")
                                .replaceAll("[wW][wW].*\\..*T", "")
                                .replaceAll("T@xt.*小.*说.*堂", "")
                                .replaceAll("[wW].*xia.*ＯＳhuＯtxＴ.*neＴ", "")
                                .replaceAll("Txt.*说.*堂", "")
                                .replaceAll("  ", " ");
                    bookContentBean.setDurCapterContent(contentEs.toString());
                    bookContentBean.setRight(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    ErrorAnalyContentManager.getInstance().writeNewErrorUrl(durChapterUrl);
                    bookContentBean.setDurCapterContent(durChapterUrl.substring(0, durChapterUrl.indexOf('/', 8)) + "站点暂时不支持解析");
                    bookContentBean.setRight(false);
                }
                e.onNext(bookContentBean);
                e.onComplete();
            }
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 获取分类书籍
     */
    @Override
    public Observable<List<SearchBookBean>> getKindBook(String url, int page) {
//        index_4.html
        if (page > 1) {
            url = url + "index_" + page + ".html";
        }
        return getRetrofitObject(MaxReaderBookModelImpl.TAG).create(IGxwztvApi.class).getKindBooks(url.replace(MaxReaderBookModelImpl.TAG, "")).flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
            @Override
            public ObservableSource<List<SearchBookBean>> apply(String s) throws Exception {
                return analySearchBook(s);
            }
        });
    }
}
