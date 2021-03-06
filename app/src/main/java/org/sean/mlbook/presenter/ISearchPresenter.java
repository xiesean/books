//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.sean.mlbook.presenter;

import com.monke.basemvplib.IPresenter;
import org.sean.mlbook.bean.SearchBookBean;

public interface ISearchPresenter extends IPresenter {

    Boolean getHasSearch();

    void setHasSearch(Boolean hasSearch);

    void insertSearchHistory();

    void querySearchHistory();

    void cleanSearchHistory();

    int getPage();

    void initPage();

    void toSearchBooks(String key, Boolean fromError);

    void addBookToShelf(final SearchBookBean searchBookBean);

    Boolean getInput();

    void setInput(Boolean input);
}
