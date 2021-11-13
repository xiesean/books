//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.sean.mlbook.listener;

import org.sean.mlbook.bean.BookShelfBean;

public interface OnGetChapterListListener {
    public void success(BookShelfBean bookShelfBean);

    public void error();
}
