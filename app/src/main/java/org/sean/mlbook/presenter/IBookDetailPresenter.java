//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.sean.mlbook.presenter;

import com.monke.basemvplib.IPresenter;
import org.sean.mlbook.bean.BookShelfBean;
import org.sean.mlbook.bean.SearchBookBean;

public interface IBookDetailPresenter extends IPresenter {

    int getOpenfrom();

    SearchBookBean getSearchBook();

    BookShelfBean getBookShelf();

    Boolean getInBookShelf();

    void getBookShelfInfo();

    void addToBookShelf();

    void removeFromBookShelf();
}
