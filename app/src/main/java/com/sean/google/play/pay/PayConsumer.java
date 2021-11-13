package com.sean.google.play.pay;

import android.widget.Toast;

import com.sean.google.BaseApplication;
import com.sean.google.admob.GAD;


public class PayConsumer implements Consumer<String> {

    @Override
    public void accept(String s, ErrorCode code) {
        if (s == null) {
            return;
        }
        if (ErrorCode.SUCCESS != code) {
            Toast.makeText(BaseApplication.getContext(), "支付失败", Toast.LENGTH_LONG).show();
            return;
        }
        switch (s) {
            case Goods.ITEM_FLY:
                break;
            case Goods.ITEM_LEVEL_UP:
                break;
            case Goods.ITEM_MONEY:
                break;
            case Goods.ITEM_NO_AD:
                GAD.setNoAd();
                Toast.makeText(BaseApplication.getContext(), "已升级为专业版，马上去使用吧", Toast.LENGTH_LONG).show();
                break;
        }
    }


}
