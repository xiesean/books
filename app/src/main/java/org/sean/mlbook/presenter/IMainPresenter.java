//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.sean.mlbook.presenter;

import com.monke.basemvplib.IPresenter;

public interface IMainPresenter extends IPresenter {
    void queryBookShelf(Boolean needRefresh);
}
