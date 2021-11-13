//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.sean.mlbook.view.impl;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.AppActivityManager;
import org.sean.mlbook.R;
import org.sean.mlbook.base.MBaseActivity;
import org.sean.mlbook.bean.DownloadChapterBean;
import org.sean.mlbook.bean.DownloadChapterListBean;
import org.sean.mlbook.common.RxBusTag;
import org.sean.mlbook.presenter.IBookReadPresenter;
import org.sean.mlbook.presenter.impl.ReadBookPresenterImpl;
import org.sean.mlbook.utils.DensityUtil;
import org.sean.mlbook.utils.PremissionCheck;
import org.sean.mlbook.view.IBookReadView;
import org.sean.mlbook.view.popupwindow.CheckAddShelfPop;
import org.sean.mlbook.view.popupwindow.FontPop;
import org.sean.mlbook.view.popupwindow.MoreSettingPop;
import org.sean.mlbook.view.popupwindow.ReadBookMenuMorePop;
import org.sean.mlbook.view.popupwindow.WindowLightPop;
import org.sean.mlbook.widget.ChapterListView;
import org.sean.mlbook.widget.contentswitchview.BookContentView;
import org.sean.mlbook.widget.contentswitchview.ContentSwitchView;
import org.sean.mlbook.widget.modialog.MoProgressHUD;
import com.monke.mprogressbar.MHorProgressBar;
import com.monke.mprogressbar.OnProgressListener;

import java.util.ArrayList;
import java.util.List;

import me.grantland.widget.AutofitTextView;

public class ReadBookActivity extends MBaseActivity<IBookReadPresenter> implements IBookReadView {

    private FrameLayout flContent;

    private ContentSwitchView csvBook;

    //主菜单
    private FrameLayout flMenu;
    private View vMenuBg;
    private LinearLayout llMenuTop;
    private LinearLayout llMenuBottom;
    private ImageButton ivReturn;
    private ImageView ivMenuMore;
    private AutofitTextView atvTitle;
    private TextView tvPre;
    private TextView tvNext;
    private MHorProgressBar hpbReadProgress;
    private LinearLayout llCatalog;
    private LinearLayout llLight;
    private LinearLayout llFont;
    private LinearLayout llSetting;
    //主菜单动画
    private Animation menuTopIn;
    private Animation menuTopOut;
    private Animation menuBottomIn;
    private Animation menuBottomOut;

    private CheckAddShelfPop checkAddShelfPop;
    private ChapterListView chapterListView;
    private WindowLightPop windowLightPop;
    private ReadBookMenuMorePop readBookMenuMorePop;
    private FontPop fontPop;
    private MoreSettingPop moreSettingPop;

    private MoProgressHUD moProgressHUD;
    private Boolean showCheckPremission = false;

    @Override
    protected IBookReadPresenter initInjector() {
        return new ReadBookPresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_bookread);
    }

    @Override
    protected void initData() {
        mPresenter.saveProgress();
        menuTopIn = AnimationUtils.loadAnimation(this, R.anim.anim_readbook_top_in);
        menuTopIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                vMenuBg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        llMenuTop.startAnimation(menuTopOut);
                        llMenuBottom.startAnimation(menuBottomOut);
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        menuBottomIn = AnimationUtils.loadAnimation(this, R.anim.anim_readbook_bottom_in);

        menuTopOut = AnimationUtils.loadAnimation(this, R.anim.anim_readbook_top_out);
        menuTopOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                vMenuBg.setOnClickListener(null);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                flMenu.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        menuBottomOut = AnimationUtils.loadAnimation(this, R.anim.anim_readbook_bottom_out);
    }

    @Override
    protected void bindView() {
        moProgressHUD = new MoProgressHUD(this);

        flContent = (FrameLayout) findViewById(R.id.fl_content);
        csvBook = (ContentSwitchView) findViewById(R.id.csv_book);
        initCsvBook();

        flMenu = (FrameLayout) findViewById(R.id.fl_menu);
        vMenuBg = findViewById(R.id.v_menu_bg);
        llMenuTop = (LinearLayout) findViewById(R.id.ll_menu_top);
        llMenuBottom = (LinearLayout) findViewById(R.id.ll_menu_bottom);
        ivReturn = (ImageButton) findViewById(R.id.iv_return);
        ivMenuMore = (ImageView) findViewById(R.id.iv_more);
        atvTitle = (AutofitTextView) findViewById(R.id.atv_title);

        tvPre = (TextView) findViewById(R.id.tv_pre);
        tvNext = (TextView) findViewById(R.id.tv_next);
        hpbReadProgress = (MHorProgressBar) findViewById(R.id.hpb_read_progress);
        llCatalog = (LinearLayout) findViewById(R.id.ll_catalog);
        llLight = (LinearLayout) findViewById(R.id.ll_light);
        llFont = (LinearLayout) findViewById(R.id.ll_font);
        llSetting = (LinearLayout) findViewById(R.id.ll_setting);

        chapterListView = (ChapterListView) findViewById(R.id.clp_chapterlist);
    }

    @Override
    public void setHpbReadProgressMax(int count) {
        hpbReadProgress.setMaxProgress(count);
    }

    private void initCsvBook() {
        csvBook.bookReadInit(new ContentSwitchView.OnBookReadInitListener() {
            @Override
            public void success() {
                mPresenter.initData(ReadBookActivity.this);
            }
        });
    }

    @Override
    public void initPop() {
        checkAddShelfPop = new CheckAddShelfPop(this, mPresenter.getBookShelf().getBookInfoBean().getName(), new CheckAddShelfPop.OnItemClickListener() {
            @Override
            public void clickExit() {
                finish();
            }

            @Override
            public void clickAddShelf() {
                mPresenter.addToShelf(null);
                checkAddShelfPop.dismiss();
            }
        });
        chapterListView.setData(mPresenter.getBookShelf(), new ChapterListView.OnItemClickListener() {
            @Override
            public void itemClick(int index) {
                csvBook.setInitData(index, mPresenter.getBookShelf().getBookInfoBean().getChapterlist().size(), BookContentView.DURPAGEINDEXBEGIN);
            }
        });

        windowLightPop = new WindowLightPop(this);
        windowLightPop.initLight();

        fontPop = new FontPop(this, new FontPop.OnChangeProListener() {
            @Override
            public void textChange(int index) {
                csvBook.changeTextSize();
            }

            @Override
            public void bgChange(int index) {
                csvBook.changeBg();
            }
        });

        readBookMenuMorePop = new ReadBookMenuMorePop(this);
        readBookMenuMorePop.setOnClickDownload(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readBookMenuMorePop.dismiss();
                if (flMenu.getVisibility() == View.VISIBLE) {
                    llMenuTop.startAnimation(menuTopOut);
                    llMenuBottom.startAnimation(menuBottomOut);
                }
                //弹出离线下载界面
                int endIndex = mPresenter.getBookShelf().getDurChapter() + 50;
                if (endIndex >= mPresenter.getBookShelf().getBookInfoBean().getChapterlist().size()) {
                    endIndex = mPresenter.getBookShelf().getBookInfoBean().getChapterlist().size() - 1;
                }
                moProgressHUD.showDownloadList(mPresenter.getBookShelf().getDurChapter(), endIndex, mPresenter.getBookShelf().getBookInfoBean().getChapterlist().size(), new MoProgressHUD.OnClickDownload() {
                    @Override
                    public void download(final int start, final int end) {
                        moProgressHUD.dismiss();
                        mPresenter.addToShelf(new ReadBookPresenterImpl.OnAddListner() {
                            @Override
                            public void addSuccess() {
                                List<DownloadChapterBean> result = new ArrayList<DownloadChapterBean>();
                                for (int i = start; i <= end; i++) {
                                    DownloadChapterBean item = new DownloadChapterBean();
                                    item.setNoteUrl(mPresenter.getBookShelf().getNoteUrl());
                                    item.setDurChapterIndex(mPresenter.getBookShelf().getBookInfoBean().getChapterlist().get(i).getDurChapterIndex());
                                    item.setDurChapterName(mPresenter.getBookShelf().getBookInfoBean().getChapterlist().get(i).getDurChapterName());
                                    item.setDurChapterUrl(mPresenter.getBookShelf().getBookInfoBean().getChapterlist().get(i).getDurChapterUrl());
                                    item.setTag(mPresenter.getBookShelf().getTag());
                                    item.setBookName(mPresenter.getBookShelf().getBookInfoBean().getName());
                                    item.setCoverUrl(mPresenter.getBookShelf().getBookInfoBean().getCoverUrl());
                                    result.add(item);
                                }
                                RxBus.get().post(RxBusTag.ADD_DOWNLOAD_TASK, new DownloadChapterListBean(result));
                            }
                        });

                    }
                });
            }
        });

        moreSettingPop = new MoreSettingPop(this);
    }

    @Override
    protected void bindEvent() {
        hpbReadProgress.setProgressListener(new OnProgressListener() {
            @Override
            public void moveStartProgress(float dur) {

            }

            @Override
            public void durProgressChange(float dur) {

            }

            @Override
            public void moveStopProgress(float dur) {
                int realDur = (int) Math.ceil(dur);
                if (realDur < 1) {
                    realDur = 1;
                }
                if ((realDur - 1) != mPresenter.getBookShelf().getDurChapter()) {
                    csvBook.setInitData(realDur - 1, mPresenter.getBookShelf().getBookInfoBean().getChapterlist().size(), BookContentView.DURPAGEINDEXBEGIN);
                }
                if (hpbReadProgress.getDurProgress() != realDur)
                    hpbReadProgress.setDurProgress(realDur);
            }

            @Override
            public void setDurProgress(float dur) {
                if (hpbReadProgress.getMaxProgress() == 1) {
                    tvPre.setEnabled(false);
                    tvNext.setEnabled(false);
                } else {
                    if (dur == 1) {
                        tvPre.setEnabled(false);
                        tvNext.setEnabled(true);
                    } else if (dur == hpbReadProgress.getMaxProgress()) {
                        tvPre.setEnabled(true);
                        tvNext.setEnabled(false);
                    } else {
                        tvPre.setEnabled(true);
                        tvNext.setEnabled(true);
                    }
                }
            }
        });
        ivReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ivMenuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readBookMenuMorePop.showAsDropDown(ivMenuMore, 0, DensityUtil.dp2px(ReadBookActivity.this, -3.5f));
            }
        });
        csvBook.setLoadDataListener(new ContentSwitchView.LoadDataListener() {
            @Override
            public void loaddata(BookContentView bookContentView, long qtag, int chapterIndex, int pageIndex) {
                mPresenter.loadContent(bookContentView, qtag, chapterIndex, pageIndex);
            }

            @Override
            public void updateProgress(int chapterIndex, int pageIndex) {
                mPresenter.updateProgress(chapterIndex, pageIndex);

                if (mPresenter.getBookShelf().getBookInfoBean().getChapterlist().size() > 0)
                    atvTitle.setText(mPresenter.getBookShelf().getBookInfoBean().getChapterlist().get(mPresenter.getBookShelf().getDurChapter()).getDurChapterName());
                else
                    atvTitle.setText("无章节");
                if (hpbReadProgress.getDurProgress() != chapterIndex + 1)
                    hpbReadProgress.setDurProgress(chapterIndex + 1);
            }

            @Override
            public String getChapterTitle(int chapterIndex) {
                return mPresenter.getChapterTitle(chapterIndex);
            }

            @Override
            public void initData(int lineCount) {
                mPresenter.setPageLineCount(lineCount);
                mPresenter.initContent();
            }

            @Override
            public void showMenu() {
                flMenu.setVisibility(View.VISIBLE);
                llMenuTop.startAnimation(menuTopIn);
                llMenuBottom.startAnimation(menuBottomIn);
            }
        });

        tvPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                csvBook.setInitData(mPresenter.getBookShelf().getDurChapter() - 1, mPresenter.getBookShelf().getBookInfoBean().getChapterlist().size(), BookContentView.DURPAGEINDEXBEGIN);
            }
        });
        tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                csvBook.setInitData(mPresenter.getBookShelf().getDurChapter() + 1, mPresenter.getBookShelf().getBookInfoBean().getChapterlist().size(), BookContentView.DURPAGEINDEXBEGIN);
            }
        });

        llCatalog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llMenuTop.startAnimation(menuTopOut);
                llMenuBottom.startAnimation(menuBottomOut);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        chapterListView.show(mPresenter.getBookShelf().getDurChapter());
                    }
                }, menuTopOut.getDuration());
            }
        });

        llLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llMenuTop.startAnimation(menuTopOut);
                llMenuBottom.startAnimation(menuBottomOut);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        windowLightPop.showAtLocation(flContent, Gravity.BOTTOM, 0, 0);
                    }
                }, menuTopOut.getDuration());
            }
        });

        llFont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llMenuTop.startAnimation(menuTopOut);
                llMenuBottom.startAnimation(menuBottomOut);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fontPop.showAtLocation(flContent, Gravity.BOTTOM, 0, 0);
                    }
                }, menuTopOut.getDuration());
            }
        });

        llSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llMenuTop.startAnimation(menuTopOut);
                llMenuBottom.startAnimation(menuBottomOut);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        moreSettingPop.showAtLocation(flContent, Gravity.BOTTOM, 0, 0);
                    }
                }, menuTopOut.getDuration());
            }
        });
    }

    @Override
    public Paint getPaint() {
        return csvBook.getTextPaint();
    }

    @Override
    public int getContentWidth() {
        return csvBook.getContentWidth();
    }

    @Override
    public void initContentSuccess(int durChapterIndex, int chapterAll, int durPageIndex) {
        csvBook.setInitData(durChapterIndex, chapterAll, durPageIndex);
    }

    @Override
    public void startLoadingBook() {
        csvBook.startLoading();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.saveProgress();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean mo = moProgressHUD.onKeyDown(keyCode, event);
        if (mo)
            return mo;
        else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (flMenu.getVisibility() == View.VISIBLE) {
                    llMenuTop.startAnimation(menuTopOut);
                    llMenuBottom.startAnimation(menuBottomOut);
                    return true;
                } else if (!mPresenter.getAdd() && checkAddShelfPop != null && !checkAddShelfPop.isShowing()) {
                    checkAddShelfPop.showAtLocation(flContent, Gravity.CENTER, 0, 0);
                    return true;
                } else {
                    Boolean temp2 = chapterListView.dimissChapterList();
                    if (temp2)
                        return true;
                    else {
                        finish();
                        return true;
                    }
                }
            } else {
                Boolean temp = csvBook.onKeyDown(keyCode, event);
                if (temp)
                    return true;
            }
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Boolean temp = csvBook.onKeyUp(keyCode, event);
        if (temp)
            return true;
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void showLoadBook() {
        moProgressHUD.showLoading("文本导入中...");
    }

    @Override
    public void dimissLoadBook() {
        moProgressHUD.dismiss();
    }

    @Override
    public void loadLocationBookError() {
        csvBook.loadError();
    }

    @Override
    public void showDownloadMenu() {
        ivMenuMore.setVisibility(View.VISIBLE);
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0x11) {
            if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && PremissionCheck.checkPremission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                mPresenter.openBookFromOther(ReadBookActivity.this);
            } else {
                if (!this.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showCheckPremission = true;
                    moProgressHUD.showTwoButton("去系统设置打开SD卡读写权限？", "取消", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    }, "设置", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PremissionCheck.requestPermissionSetting(ReadBookActivity.this);
                        }
                    });
                } else {
                    Toast.makeText(this, "未获取SD卡读取权限", Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (showCheckPremission && mPresenter.getOpen_from() == ReadBookPresenterImpl.OPEN_FROM_OTHER && !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PremissionCheck.checkPremission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            showCheckPremission = true;
            mPresenter.openBookFromOther(this);
        }
    }

    @Override
    public void finish() {
        if (!AppActivityManager.getInstance().isExist(MainActivity.class)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        super.finish();
    }

}