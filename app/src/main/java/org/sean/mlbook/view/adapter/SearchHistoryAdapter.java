//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.sean.mlbook.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.sean.mlbook.R;
import org.sean.mlbook.bean.SearchHistoryBean;
import org.sean.mlbook.widget.flowlayout.FlowLayout;
import org.sean.mlbook.widget.flowlayout.TagAdapter;

import java.util.ArrayList;

public class SearchHistoryAdapter extends TagAdapter<SearchHistoryBean> {
    private SearchHistoryAdapter.OnItemClickListener onItemClickListener;

    public SearchHistoryAdapter() {
        super(new ArrayList<SearchHistoryBean>());
    }

    public OnItemClickListener getListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Override
    public View getView(FlowLayout parent, int position, final SearchHistoryBean searchHistoryBean) {
        TextView tv = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_searchhistory_item,
                parent, false);
        tv.setText(searchHistoryBean.getContent());
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != onItemClickListener) {
                    onItemClickListener.itemClick(searchHistoryBean);
                }
            }
        });
        return tv;
    }

    public SearchHistoryBean getItemData(int position) {
        return mTagDatas.get(position);
    }

    public int getDataSize() {
        return mTagDatas.size();
    }

    public interface OnItemClickListener {
        void itemClick(SearchHistoryBean searchHistoryBean);
    }
}
