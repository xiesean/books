//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.sean.mlbook.view.impl;

import android.content.Intent;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.sean.mlbook.BitIntentDataManager;
import org.sean.mlbook.R;
import org.sean.mlbook.base.MBaseActivity;
import org.sean.mlbook.bean.BookShelfBean;
import org.sean.mlbook.presenter.IMainPresenter;
import org.sean.mlbook.presenter.impl.BookDetailPresenterImpl;
import org.sean.mlbook.presenter.impl.MainPresenterImpl;
import org.sean.mlbook.presenter.impl.ReadBookPresenterImpl;
import org.sean.mlbook.view.IMainView;
import org.sean.mlbook.view.adapter.BookShelfAdapter;
import org.sean.mlbook.view.popupwindow.DownloadListPop;
import org.sean.mlbook.view.popupwindow.ProxyPop;
import org.sean.mlbook.widget.refreshview.OnRefreshWithProgressListener;
import org.sean.mlbook.widget.refreshview.RefreshRecyclerView;

import java.util.List;

public class MainActivity extends MBaseActivity<IMainPresenter> implements IMainView {
    private ImageView ivLogo;
    private ImageButton ibMoney;
    private ImageButton ibSettings;
    private ImageButton ibLibrary;
    private ImageButton ibAdd;
    private ImageButton ibDownload;

    private RefreshRecyclerView rfRvShelf;
    private BookShelfAdapter bookShelfAdapter;

    private FrameLayout flWarn;
    private ImageView ivWarnClose;

    private DownloadListPop downloadListPop;
    private ProxyPop proxyPop;
    private long exitTime = 0;

    @Override
    protected IMainPresenter initInjector() {
        return new MainPresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void initData() {
        bookShelfAdapter = new BookShelfAdapter();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void bindView() {
        proxyPop = new ProxyPop(MainActivity.this);
        downloadListPop = new DownloadListPop(MainActivity.this);

        ivLogo = findViewById(R.id.iv_logo);
        rfRvShelf = findViewById(R.id.rf_rv_shelf);

        ibMoney = findViewById(R.id.ib_money);
        ibSettings = findViewById(R.id.ib_settings);
        ibLibrary = findViewById(R.id.ib_library);
        ibAdd = findViewById(R.id.ib_add);
        ibDownload = findViewById(R.id.ib_download);

        rfRvShelf.setRefreshRecyclerViewAdapter(bookShelfAdapter, new LinearLayoutManager(this));

        flWarn = findViewById(R.id.fl_warn);
        ivWarnClose = findViewById(R.id.iv_warn_close);
    }

    @Override
    protected void bindEvent() {
        bindRvShelfEvent();
        ibSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proxyPop.showAsDropDown(ibSettings);
            }
        });
        ibDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadListPop.showAsDropDown(ibDownload);
            }
        });
        ibMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击打赏
            }
        });
        ibLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityByAnim(new Intent(MainActivity.this, LibraryActivity.class), 0, 0);
            }
        });
        ibAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击更多
                startActivityByAnim(new Intent(MainActivity.this, ImportBookActivity.class), 0, 0);
            }
        });
        bookShelfAdapter.setItemClickListener(new BookShelfAdapter.OnItemClickListener() {
            @Override
            public void toSearch() {
                //点击去选书
                startActivityByAnim(new Intent(MainActivity.this, LibraryActivity.class), 0, 0);
            }

            @Override
            public void onClick(BookShelfBean bookShelfBean, int index) {
                Intent intent = new Intent(MainActivity.this, ReadBookActivity.class);
                intent.putExtra("from", ReadBookPresenterImpl.OPEN_FROM_APP);
                String key = String.valueOf(System.currentTimeMillis());
                intent.putExtra("data_key", key);
                try {
                    BitIntentDataManager.getInstance().putData(key, bookShelfBean.clone());
                } catch (CloneNotSupportedException e) {
                    BitIntentDataManager.getInstance().putData(key, bookShelfBean);
                    e.printStackTrace();
                }
                startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);
            }

            @Override
            public void onLongClick(View animView, BookShelfBean bookShelfBean, int index) {
                Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
                intent.putExtra("from", BookDetailPresenterImpl.FROM_BOOKSHELF);
                String key = String.valueOf(System.currentTimeMillis());
                intent.putExtra("data_key", key);
                BitIntentDataManager.getInstance().putData(key, bookShelfBean);
                startActivityByAnim(intent, animView, "img_cover", android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        ivWarnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flWarn.setVisibility(View.GONE);
            }
        });
    }

    private void bindRvShelfEvent() {
        rfRvShelf.setBaseRefreshListener(new OnRefreshWithProgressListener() {
            @Override
            public int getMaxProgress() {
                return bookShelfAdapter.getBooks().size();
            }

            @Override
            public void startRefresh() {
                mPresenter.queryBookShelf(true);
            }
        });
    }

    @Override
    protected void firstRequest() {
        mPresenter.queryBookShelf(false);
    }

    @Override
    public void refreshBookShelf(List<BookShelfBean> bookShelfBeanList) {
        bookShelfAdapter.replaceAll(bookShelfBeanList);
    }

    @Override
    public void activityRefreshView() {
        //执行刷新响应
        rfRvShelf.startRefresh();
    }

    @Override
    public void refreshFinish() {
        rfRvShelf.finishRefresh(false, true);
    }

    @Override
    public void refreshError(String error) {
        refreshFinish();
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void refreshRecyclerViewItemAdd() {
        rfRvShelf.getRpb().setDurProgress(rfRvShelf.getRpb().getDurProgress() + 1);
    }

    @Override
    public void setRecyclerMaxProgress(int x) {
        rfRvShelf.getRpb().setMaxProgress(x);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        downloadListPop.onDestroy();
    }

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }
}