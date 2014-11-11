package ru.ifmo.md.colloquium2;

/**
 * Created by Евгения on 11.11.2014.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.inputmethodservice.KeyboardView;
import android.util.Log;

public class VotesDBAdapter {

    private static final String LOG_TAG = "VotesDBAdapter";

    public static final String KEY_ID              = "_id";
    public static final String CANDIDATES_TABLE_NAME = "candidats";

    //Subjects table
    public static final String KEY_CANDIDATE                 = "candidate";
    public static final String KEY_VOTES = "votes";
    public static final String CANDIDATES_TABLE_CREATE_QUERY = "CREATE TABLE " + CANDIDATES_TABLE_NAME + " (" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            KEY_CANDIDATE + " TEXT, " +
            KEY_VOTES + " INTEGER DEFAULT 0, "+
            "UNIQUE (" + KEY_CANDIDATE + ") ON CONFLICT IGNORE)";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME    = "votes.db";
    private static final int    DATABASE_VERSION = 13;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        @Override
        public void onOpen(SQLiteDatabase db) {
            db.execSQL("PRAGMA foreign_keys=ON");
        }

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(CANDIDATES_TABLE_CREATE_QUERY);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + CANDIDATES_TABLE_NAME);
            onCreate(db);
        }
    }

    public VotesDBAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public VotesDBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public long createCandidate(String name) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CANDIDATE, name);
        return mDb.insert(CANDIDATES_TABLE_NAME, null, initialValues);
    }


    public static final String KEY_VOTES_SUM = "votes_sum";
    public int totalVotesCount() {
        Cursor c = mDb.rawQuery("SELECT SUM("+KEY_VOTES+") AS "+KEY_VOTES_SUM+" FROM "+CANDIDATES_TABLE_NAME, null);
        c.moveToFirst();
        return c.getInt(c.getColumnIndex(KEY_VOTES_SUM));
    }

    public boolean deleteCandidate(long id) {
        return mDb.delete(CANDIDATES_TABLE_NAME, KEY_ID + "=" + id, null) > 0;
    }

    public static final String KEY_POINTS_SUM = "points_sum";

    public Cursor fetchCandidates(boolean sortByVotes) {
        return mDb.query(
                CANDIDATES_TABLE_NAME
                , new String[]{KEY_ID, KEY_CANDIDATE, KEY_VOTES}
                , null, null, null, null,
                sortByVotes ? (KEY_VOTES + " DESC") : (KEY_CANDIDATE + " ASC"));
    }

    public Cursor fetchCandidateById(long id) {
        return mDb.query(
                CANDIDATES_TABLE_NAME
                , new String[]{KEY_ID, KEY_CANDIDATE, KEY_VOTES}
                , KEY_ID +"="+id, null, null, null, null);
    }

    public boolean updateCandidate(long id, String newName) {
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_CANDIDATE, newName);
        return mDb.update(CANDIDATES_TABLE_NAME, newValues, KEY_ID + "=" + id, null) > 0;
    }

    public void resetVotes() {
        ContentValues cv = new ContentValues();
        cv.put(KEY_VOTES, 0);
        mDb.update(CANDIDATES_TABLE_NAME, cv, null, null);
    }

    public boolean addVote(long candidateId) {
        Cursor c = fetchCandidateById(candidateId);
        if (!c.moveToFirst())
            return false;
        int votes = c.getInt(c.getColumnIndex(KEY_VOTES));
        ContentValues cv = new ContentValues();
        cv.put(KEY_VOTES, votes + 1);
        return mDb.update(CANDIDATES_TABLE_NAME, cv, KEY_ID + "=" + candidateId, null) == 1;
    }
}


