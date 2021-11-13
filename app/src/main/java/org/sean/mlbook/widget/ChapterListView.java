package org.sean.mlbook.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.sean.mlbook.R;
import org.sean.mlbook.bean.BookShelfBean;
import org.sean.mlbook.view.adapter.ChapterListAdapter;

public class ChapterListView extends FrameLayout {
    private TextView tvName;
    private TextView tvListCount;
    private RecyclerView rvList;
    private RecyclerViewBar rvbSlider;

    private FrameLayout flBg;
    private LinearLayout llContent;

    private ChapterListAdapter chapterListAdapter;

    private Animation animIn;
    private Animation animOut;
    private OnItemClickListener itemClickListener;
    private BookShelfBean bookShelfBean;

    public ChapterListView( Context context) {
        this(context, null);
    }

    public ChapterListView( Context context,  AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChapterListView( Context context,  AttributeSet attrs,  int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ChapterListView( Context context,  AttributeSet attrs,  int defStyleAttr,  int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setVisibility(INVISIBLE);
        LayoutInflater.from(getContext()).inflate(R.layout.view_chapterlist, this, true);
        initData();
        initView();
    }

    private void initData() {
        animIn = AnimationUtils.loadAnimation(getContext(), R.anim.anim_pop_chapterlist_in);
        animIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                flBg.setOnClickListener(null);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                flBg.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dimissChapterList();
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animOut = AnimationUtils.loadAnimation(getContext(), R.anim.anim_pop_chapterlist_out);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                flBg.setOnClickListener(null);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                llContent.setVisibility(INVISIBLE);
                setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void show(int durChapter) {
        chapterListAdapter.setIndex(durChapter);
        ((LinearLayoutManager) rvList.getLayoutManager()).scrollToPositionWithOffset(durChapter, 0);
        if (getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
            animOut.cancel();
            animIn.cancel();
            llContent.setVisibility(VISIBLE);
            llContent.startAnimation(animIn);
        }
    }

    private void initView() {
        flBg = (FrameLayout) findViewById(R.id.fl_bg);
        llContent = (LinearLayout) findViewById(R.id.ll_content);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvListCount = (TextView) findViewById(R.id.tv_listcount);
        rvList = (RecyclerView) findViewById(R.id.rv_list);
        rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvList.setItemAnimator(null);
        rvbSlider = (RecyclerViewBar) findViewById(R.id.rvb_slider);
    }

    public void setData(BookShelfBean bookShelfBean, OnItemClickListener clickListener) {
        this.itemClickListener = clickListener;
        this.bookShelfBean = bookShelfBean;
        tvName.setText(bookShelfBean.getBookInfoBean().getName());
        tvListCount.setText("共" + bookShelfBean.getBookInfoBean().getChapterlist().size() + "章");
        chapterListAdapter = new ChapterListAdapter(bookShelfBean, new OnItemClickListener() {
            @Override
            public void itemClick(int index) {
                if (itemClickListener != null) {
                    itemClickListener.itemClick(index);
                    rvbSlider.scrollToPositionWithOffset(index);
                }
            }
        });
        rvList.setAdapter(chapterListAdapter);
        rvbSlider.setRecyclerView(rvList);
    }

    public Boolean dimissChapterList() {
        if (getVisibility() != VISIBLE) {
            return false;
        } else {
            animOut.cancel();
            animIn.cancel();
            llContent.startAnimation(animOut);
            return true;
        }
    }

    public interface OnItemClickListener {
        public void itemClick(int index);
    }
}