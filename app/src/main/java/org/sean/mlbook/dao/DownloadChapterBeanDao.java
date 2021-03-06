package org.sean.mlbook.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import org.sean.mlbook.bean.DownloadChapterBean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DOWNLOAD_CHAPTER_BEAN".
*/
public class DownloadChapterBeanDao extends AbstractDao<DownloadChapterBean, String> {

    public static final String TABLENAME = "DOWNLOAD_CHAPTER_BEAN";

    /**
     * Properties of entity DownloadChapterBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property NoteUrl = new Property(0, String.class, "noteUrl", false, "NOTE_URL");
        public final static Property DurChapterIndex = new Property(1, int.class, "durChapterIndex", false, "DUR_CHAPTER_INDEX");
        public final static Property DurChapterUrl = new Property(2, String.class, "durChapterUrl", true, "DUR_CHAPTER_URL");
        public final static Property DurChapterName = new Property(3, String.class, "durChapterName", false, "DUR_CHAPTER_NAME");
        public final static Property Tag = new Property(4, String.class, "tag", false, "TAG");
        public final static Property BookName = new Property(5, String.class, "bookName", false, "BOOK_NAME");
        public final static Property CoverUrl = new Property(6, String.class, "coverUrl", false, "COVER_URL");
    }


    public DownloadChapterBeanDao(DaoConfig config) {
        super(config);
    }
    
    public DownloadChapterBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DOWNLOAD_CHAPTER_BEAN\" (" + //
                "\"NOTE_URL\" TEXT," + // 0: noteUrl
                "\"DUR_CHAPTER_INDEX\" INTEGER NOT NULL ," + // 1: durChapterIndex
                "\"DUR_CHAPTER_URL\" TEXT PRIMARY KEY NOT NULL ," + // 2: durChapterUrl
                "\"DUR_CHAPTER_NAME\" TEXT," + // 3: durChapterName
                "\"TAG\" TEXT," + // 4: tag
                "\"BOOK_NAME\" TEXT," + // 5: bookName
                "\"COVER_URL\" TEXT);"); // 6: coverUrl
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DOWNLOAD_CHAPTER_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DownloadChapterBean entity) {
        stmt.clearBindings();
 
        String noteUrl = entity.getNoteUrl();
        if (noteUrl != null) {
            stmt.bindString(1, noteUrl);
        }
        stmt.bindLong(2, entity.getDurChapterIndex());
 
        String durChapterUrl = entity.getDurChapterUrl();
        if (durChapterUrl != null) {
            stmt.bindString(3, durChapterUrl);
        }
 
        String durChapterName = entity.getDurChapterName();
        if (durChapterName != null) {
            stmt.bindString(4, durChapterName);
        }
 
        String tag = entity.getTag();
        if (tag != null) {
            stmt.bindString(5, tag);
        }
 
        String bookName = entity.getBookName();
        if (bookName != null) {
            stmt.bindString(6, bookName);
        }
 
        String coverUrl = entity.getCoverUrl();
        if (coverUrl != null) {
            stmt.bindString(7, coverUrl);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DownloadChapterBean entity) {
        stmt.clearBindings();
 
        String noteUrl = entity.getNoteUrl();
        if (noteUrl != null) {
            stmt.bindString(1, noteUrl);
        }
        stmt.bindLong(2, entity.getDurChapterIndex());
 
        String durChapterUrl = entity.getDurChapterUrl();
        if (durChapterUrl != null) {
            stmt.bindString(3, durChapterUrl);
        }
 
        String durChapterName = entity.getDurChapterName();
        if (durChapterName != null) {
            stmt.bindString(4, durChapterName);
        }
 
        String tag = entity.getTag();
        if (tag != null) {
            stmt.bindString(5, tag);
        }
 
        String bookName = entity.getBookName();
        if (bookName != null) {
            stmt.bindString(6, bookName);
        }
 
        String coverUrl = entity.getCoverUrl();
        if (coverUrl != null) {
            stmt.bindString(7, coverUrl);
        }
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2);
    }    

    @Override
    public DownloadChapterBean readEntity(Cursor cursor, int offset) {
        DownloadChapterBean entity = new DownloadChapterBean( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // noteUrl
            cursor.getInt(offset + 1), // durChapterIndex
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // durChapterUrl
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // durChapterName
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // tag
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // bookName
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6) // coverUrl
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DownloadChapterBean entity, int offset) {
        entity.setNoteUrl(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setDurChapterIndex(cursor.getInt(offset + 1));
        entity.setDurChapterUrl(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setDurChapterName(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setTag(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setBookName(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setCoverUrl(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
     }
    
    @Override
    protected final String updateKeyAfterInsert(DownloadChapterBean entity, long rowId) {
        return entity.getDurChapterUrl();
    }
    
    @Override
    public String getKey(DownloadChapterBean entity) {
        if(entity != null) {
            return entity.getDurChapterUrl();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(DownloadChapterBean entity) {
        return entity.getDurChapterUrl() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
