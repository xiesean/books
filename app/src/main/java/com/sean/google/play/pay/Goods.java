package com.sean.google.play.pay;

import java.util.ArrayList;
import java.util.List;

public final class Goods {

    public static  final List<String> INAPP = new ArrayList<>();

    public static  final List<String> SUBS = new ArrayList<>();

    // 加钱
    public static  final String ITEM_MONEY = "money_add_10000";
    // 存档
    public static  final String ITEM_FLY = "mod_fly";
    //去广告
    public static  final String ITEM_NO_AD = "no_ad";
    // 升级
    public static  final String ITEM_LEVEL_UP = "level_up";

    static {
        INAPP.add(ITEM_MONEY);
        INAPP.add(ITEM_FLY);
        INAPP.add(ITEM_NO_AD);
        INAPP.add(ITEM_LEVEL_UP);

        SUBS.add("vip");
    }
}
