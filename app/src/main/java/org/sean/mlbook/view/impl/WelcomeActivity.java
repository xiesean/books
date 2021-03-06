//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.sean.mlbook.view.impl;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;

import com.monke.basemvplib.IPresenter;
import org.sean.mlbook.R;
import org.sean.mlbook.base.MBaseActivity;

public class WelcomeActivity extends MBaseActivity {

    private ImageView ivBg;
    private ImageView ivIcon;
    private TextView tvIntro;

    private ValueAnimator welAnimator;

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_welcome);
    }

    @Override
    protected void initData() {
        welAnimator = ValueAnimator.ofFloat(1f, 0f).setDuration(800);
        welAnimator.setStartDelay(500);
    }

    @Override
    protected void bindView() {
        ivBg = findViewById(R.id.iv_bg);
        ivIcon = findViewById(R.id.iv_icon);
        tvIntro = findViewById(R.id.tv_intro);
    }

    @Override
    protected void bindEvent() {
        welAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (Float) animation.getAnimatedValue();
                ivBg.setAlpha(alpha);
                ivIcon.setAlpha(alpha);
                tvIntro.setAlpha(1f - alpha);
            }
        });
        welAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                startActivityByAnim(new Intent(WelcomeActivity.this, MainActivity.class), android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @Override
    protected void firstRequest() {
        welAnimator.start();
    }

}
