package org.sean.mlbook.widget.libraryview;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.sean.mlbook.R;
import org.sean.mlbook.bean.LibraryNewBookBean;
import org.sean.mlbook.widget.flowlayout.FlowLayout;
import org.sean.mlbook.widget.flowlayout.TagAdapter;

import java.util.ArrayList;

public class LibraryNewBooksAdapter extends TagAdapter<LibraryNewBookBean> {
    private LibraryNewBooksView.OnClickAuthorListener clickNewBookListener;

    public LibraryNewBooksAdapter() {
        super(new ArrayList<LibraryNewBookBean>());
    }

    @Override
    public View getView(FlowLayout parent, int position, final LibraryNewBookBean libraryNewBookBean) {
        TextView tv = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_library_hotauthor_item,
                parent, false);
        tv.setText(libraryNewBookBean.getName());
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != clickNewBookListener) {
                    clickNewBookListener.clickNewBook(libraryNewBookBean);
                }
            }
        });
        return tv;
    }

    public LibraryNewBookBean getItemData(int position) {
        return mTagDatas.get(position);
    }

    public int getDataSize() {
        return mTagDatas.size();
    }

    public void setClickNewBookListener(LibraryNewBooksView.OnClickAuthorListener clickNewBookListener) {
        this.clickNewBookListener = clickNewBookListener;
    }
}
