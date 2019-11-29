package com.androidcodeman.simpleimagegallery;//package com.example.dbentrysearch.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.androidcodeman.simpleimagegallery.TupleTypes.TupStrInt;
import com.androidcodeman.simpleimagegallery.TupleTypes.TupStrStr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to interface with the SQLite DB.
 */
public class DBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "group_15";
    private static final String TABLE_NAME = "image_contents";
    private static final String URI = "uri";
    private static final String TIMESTAMP = "timestamp";
    private static final String CONTENTS = "contents";
    private TupleTypes tuples = new TupleTypes();

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + URI + " TEXT PRIMARY KEY NOT NULL," + CONTENTS + " TEXT,"
                + TIMESTAMP + " INTEGER" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void truncateOldEntries() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }

    /**
     * Method to add entry of meta-data of an image into the db.
     *
     * @param uri       Image uri on the device
     * @param contents  List of contents available in the image
     * @param timestamp Nano-second from epoch
     */
    public void addImageMeta(String uri, List<String> contents, Long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        StringBuilder contentBuilder = new StringBuilder();
        for (String s : contents) {
            contentBuilder.append(s.trim().toLowerCase());
        }
        String contentStr = contentBuilder.toString();
        ContentValues values = new ContentValues();
        String checkQuery = "SELECT " + CONTENTS + " FROM " + TABLE_NAME +
                " WHERE URI = '" + uri + "'";
        Cursor cursor = db.rawQuery(checkQuery, null);
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            String oldValue = cursor.getString(0);
            values.put(CONTENTS, oldValue + " " + contentStr);
            db.update(TABLE_NAME, values, URI + "='" + uri + "'", null);
        } else {
            values.put(URI, uri);
            values.put(CONTENTS, contentStr);
            values.put(TIMESTAMP, timestamp);
            db.insert(TABLE_NAME, null, values);
        }
        cursor.close();
        db.close();
    }

    public ArrayList<String> getImages(String query) {
        ArrayList<TupStrStr> dbRes = getAllMatching(query);
        return rankImages(dbRes, query);
    }

    private ArrayList<TupStrStr> getAllMatching(String query) {
        ArrayList<TupStrStr> res = new ArrayList<>();
        String[] qs = query.trim().split("\\s");

        StringBuilder dbQuery = new StringBuilder(
                "SELECT " + URI + ", " + CONTENTS +
                        " FROM " + TABLE_NAME + " WHERE " +
                        CONTENTS + " LIKE '%" + qs[0].trim() + "%'"
        );
        String[] nqs = Arrays.copyOfRange(qs, 1, qs.length);
        for (String q : nqs) {
            String additionalCondition = " OR " + CONTENTS + " LIKE '%" + q.trim() + "%'";
            dbQuery.append(additionalCondition);
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(dbQuery.toString(), null);

        while (cursor.moveToNext()) {
            res.add(
                    tuples.new TupStrStr(
                            cursor.getString(0),
                            cursor.getString(1)
                    )
            );
        }
        cursor.close();
        return res;
    }

    private ArrayList<String> rankImages(ArrayList<TupStrStr> inArr,
                                         String query) {
        ArrayList<String> res = new ArrayList<>();
        int inSize = inArr.size();
        String[] qs = query.toLowerCase().trim().split("\\s");
        TupStrInt[] values = new TupStrInt[inSize];
        int tempCount = 0;
        for (int i = 0; i < inSize; i++) {
            String curr = inArr.get(i).getSecond();
            tempCount = 0;
            for (String q : qs) {
                if (curr.contains(q))
                    tempCount += 1;
            }
//            values[i].setFirst(inArr.get(i).getFirst());
//            values[i].setSecond(tempCount);
            values[i] = tuples.new TupStrInt(inArr.get(i).getFirst(), tempCount);
        }
        Arrays.sort(values, tuples.new StrIntComp());
        for (TupStrInt e : values) {
            res.add(e.getFirst());
        }
        return res;
    }

}
