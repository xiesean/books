//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.sean.mlbook.dao;

import android.database.sqlite.SQLiteDatabase;

import org.sean.mlbook.MApplication;

public class DbHelper {
    private static DbHelper instance;
    private final DaoMaster.DevOpenHelper mHelper;
    private final SQLiteDatabase db;
    private final DaoMaster mDaoMaster;
    private final DaoSession mDaoSession;

    private DbHelper() {
        mHelper = new DaoMaster.DevOpenHelper(MApplication.getInstance(), "monkebook_db", null);
        db = mHelper.getWritableDatabase();
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }

    public static DbHelper getInstance() {
        if (null == instance) {
            synchronized (DbHelper.class) {
                if (null == instance) {
                    instance = new DbHelper();
                }
            }
        }
        return instance;
    }

    public DaoSession getmDaoSession() {
        return mDaoSession;
    }

    public SQLiteDatabase getDb() {
        return db;
    }
}
