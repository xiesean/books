//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.sean.mlbook.view.impl;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import org.sean.mlbook.R;
import org.sean.mlbook.base.MBaseActivity;
import org.sean.mlbook.bean.SearchBookBean;
import org.sean.mlbook.presenter.IChoiceBookPresenter;
import org.sean.mlbook.presenter.impl.BookDetailPresenterImpl;
import org.sean.mlbook.presenter.impl.ChoiceBookPresenterImpl;
import org.sean.mlbook.utils.NetworkUtil;
import org.sean.mlbook.view.IChoiceBookView;
import org.sean.mlbook.view.adapter.ChoiceBookAdapter;
import org.sean.mlbook.widget.refreshview.BaseRefreshListener;
import org.sean.mlbook.widget.refreshview.OnLoadMoreListener;
import org.sean.mlbook.widget.refreshview.RefreshRecyclerView;

import java.util.List;

public class ChoiceBookActivity extends MBaseActivity<IChoiceBookPresenter> implements IChoiceBookView {
    private ImageButton ivReturn;
    private TextView tvTitle;
    private RefreshRecyclerView rfRvSearchBooks;
    private ChoiceBookAdapter searchBookAdapter;

    public static void startChoiceBookActivity(Context context, String title, String url) {
        Intent intent = new Intent(context, ChoiceBookActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        context.startActivity(intent);
    }

    @Override
    protected IChoiceBookPresenter initInjector() {
        return new ChoiceBookPresenterImpl(this,getIntent());
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_bookchoice);
    }

    @Override
    protected void initData() {
        searchBookAdapter = new ChoiceBookAdapter();
    }

    @Override
    protected void bindView() {
        ivReturn = (ImageButton) findViewById(R.id.iv_return);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTitle.setText(mPresenter.getTitle());
        rfRvSearchBooks = (RefreshRecyclerView) findViewById(R.id.rfRv_search_books);
        rfRvSearchBooks.setRefreshRecyclerViewAdapter(searchBookAdapter, new LinearLayoutManager(this));

        View viewRefreshError = LayoutInflater.from(this).inflate(R.layout.view_searchbook_refresherror, null);
        viewRefreshError.findViewById(R.id.tv_refresh_again).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBookAdapter.replaceAll(null);
                //刷新失败 ，重试
                mPresenter.initPage();
                mPresenter.toSearchBooks(null);
                startRefreshAnim();
            }
        });
        rfRvSearchBooks.setNoDataAndrRefreshErrorView(LayoutInflater.from(this).inflate(R.layout.view_searchbook_nodata, null),
                viewRefreshError);
    }

    @Override
    protected void bindEvent() {
        ivReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        searchBookAdapter.setItemClickListener(new ChoiceBookAdapter.OnItemClickListener() {
            @Override
            public void clickAddShelf(View clickView, int position, SearchBookBean searchBookBean) {
                mPresenter.addBookToShelf(searchBookBean);
            }

            @Override
            public void clickItem(View animView, int position, SearchBookBean searchBookBean) {
                Intent intent = new Intent(ChoiceBookActivity.this, BookDetailActivity.class);
                intent.putExtra("from", BookDetailPresenterImpl.FROM_SEARCH);
                intent.putExtra("data", searchBookBean);
                startActivityByAnim(intent, animView, "img_cover", android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        rfRvSearchBooks.setBaseRefreshListener(new BaseRefreshListener() {
            @Override
            public void startRefresh() {
                mPresenter.initPage();
                mPresenter.toSearchBooks(null);
                startRefreshAnim();
            }
        });
        rfRvSearchBooks.setLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void startLoadmore() {
                mPresenter.toSearchBooks(null);
            }

            @Override
            public void loadMoreErrorTryAgain() {
                mPresenter.toSearchBooks(null);
            }
        });
    }

    @Override
    public void refreshSearchBook(List<SearchBookBean> books) {
        searchBookAdapter.replaceAll(books);
    }

    @Override
    public void refreshFinish(Boolean isAll) {
        rfRvSearchBooks.finishRefresh(isAll, true);
    }

    @Override
    public void loadMoreFinish(Boolean isAll) {
        rfRvSearchBooks.finishLoadMore(isAll, true);
    }

    @Override
    public void loadMoreSearchBook(final List<SearchBookBean> books) {
        searchBookAdapter.addAll(books);
    }

    @Override
    public void searchBookError() {
        if (mPresenter.getPage() > 1) {
            rfRvSearchBooks.loadMoreError();
        } else {
            //刷新失败
            rfRvSearchBooks.refreshError();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void addBookShelfSuccess(List<SearchBookBean> datas) {
        searchBookAdapter.notifyDataSetChanged();
    }

    @Override
    public void addBookShelfFailed(int code) {
        Toast.makeText(this, NetworkUtil.getErrorTip(code), Toast.LENGTH_SHORT).show();
    }

    @Override
    public ChoiceBookAdapter getSearchBookAdapter() {
        return searchBookAdapter;
    }

    @Override
    public void updateSearchItem(int index) {
        int tempIndex = index;
        if (tempIndex < searchBookAdapter.getItemcount()) {
            int startIndex = ((LinearLayoutManager) rfRvSearchBooks.getRecyclerView().getLayoutManager()).findFirstVisibleItemPosition();
            TextView tvAddShelf = (TextView) ((ViewGroup) rfRvSearchBooks.getRecyclerView()).getChildAt(tempIndex - startIndex).findViewById(R.id.tv_addshelf);
            if (tvAddShelf != null) {
                if (searchBookAdapter.getSearchBooks().get(index).getAdd()) {
                    tvAddShelf.setText("已添加");
                    tvAddShelf.setEnabled(false);
                } else {
                    tvAddShelf.setText("+添加");
                    tvAddShelf.setEnabled(true);
                }
            }
        }
    }

    @Override
    public void startRefreshAnim() {
        rfRvSearchBooks.startRefresh();
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
    }
}