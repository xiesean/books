//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.sean.mlbook.view;

import com.monke.basemvplib.IView;
import org.sean.mlbook.bean.LibraryBean;

public interface ILibraryView extends IView {

    /**
     * 书城书籍获取成功  更新UI
     *
     * @param library
     */
    void updateUI(LibraryBean library);

    /**
     * 书城数据刷新成功 更新UI
     */
    void finishRefresh();
}
