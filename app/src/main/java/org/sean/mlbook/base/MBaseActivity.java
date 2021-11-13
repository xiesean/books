//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.sean.mlbook.base;

import com.monke.basemvplib.IPresenter;
import com.monke.basemvplib.impl.BaseActivity;

public abstract class MBaseActivity<T extends IPresenter> extends BaseActivity<T> {
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
