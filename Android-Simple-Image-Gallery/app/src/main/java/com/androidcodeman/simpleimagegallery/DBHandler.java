package com.androidcodeman.simpleimagegallery;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.androidcodeman.simpleimagegallery.TupleTypes.TupStrLong;

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
    private static final String LABEL = "label";
    private static final String CONFIDENCE = "confidence";
    private TupleTypes tuples = new TupleTypes();

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + URI + " TEXT," + LABEL + " TEXT,"
                + CONFIDENCE + " INTEGER,"
                + TIMESTAMP + " INTEGER" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Method to truncate all the previous entries from DB.
     */
    public void truncateOldEntries() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }

    /**
     * Method to insert meta-information about an image
     * from ML model into in the DB
     *
     * @param uri URI of the image
     * @param labels labels recognized by the ML models.
     * @param confidence Confidence score of the ML models
     *                   for the given label.
     * @param timestamp Timestamp of the images.
     */
    public void addImageMeta(String uri, List<String> labels,
                             List<Double> confidence, Long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        String colNames = " (" + URI + "," + LABEL + ","
                + CONFIDENCE + "," + TIMESTAMP + ")";
        StringBuilder insertQuery = new StringBuilder(
                "INSERT INTO " + TABLE_NAME + colNames + " VALUES "
        );
        for (int i = 0; i < labels.size(); i++) {
            int scaledConfidence = (int) (10000 * confidence.get(i));
            if (i == 0) {
                insertQuery.append(
                        String.format("('%s','%s',%s,%s)",
                                uri, labels.get(i).toLowerCase().trim(),
                                Integer.toString(scaledConfidence),
                                Long.toString(timestamp))
                );
            } else {
                insertQuery.append(
                        String.format(",('%s','%s',%s,%s)",
                                uri, labels.get(i), Integer.toString(scaledConfidence),
                                Long.toString(timestamp))
                );
            }
        }
        Cursor cursor = db.rawQuery(insertQuery.toString(), null);
        cursor.close();
        db.close();
    }

    /**
     * Method to get the URI of all the images matching the query.
     *
     * @param query String to search the labels of the images.
     * @return List of URIs in string format.
     */
    public ArrayList<String> getImages(String query) {
        ArrayList<TupStrLong> dbRes = getAllMatching(query);
        return orderImages(dbRes);
    }

    /**
     * Method to query DB for the labels matching the query
     *
     * @param query String to search the labels of the images.
     * @return Tuple of image URI with a score
     */
    private ArrayList<TupStrLong> getAllMatching(String query) {
        ArrayList<TupStrLong> res = new ArrayList<>();
        String[] qs = query.toLowerCase().trim().split("\\s");

        StringBuilder dbQuery = new StringBuilder(
                "SELECT " + URI + ", " + "SUM(" + CONFIDENCE + ") AS score"
                        + " FROM " + TABLE_NAME + " WHERE "
                        + LABEL + " LIKE '%" + qs[0].trim() + "%'"
        );
        String[] nqs = Arrays.copyOfRange(qs, 1, qs.length);
        for (String q : nqs) {
            String additionalCondition = " OR " + LABEL + " LIKE '%" + q.trim() + "%'";
            dbQuery.append(additionalCondition);
        }
        String aggregation = " GROUP BY " + URI;
        dbQuery.append(aggregation);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(dbQuery.toString(), null);

        while (cursor.moveToNext()) {
            res.add(
                    tuples.new TupStrLong(
                            cursor.getString(0),
                            cursor.getLong(1)
                    )
            );
        }
        cursor.close();
        return res;
    }

    private ArrayList<String> orderImages(ArrayList<TupStrLong> inArr) {
        ArrayList<String> res = new ArrayList<>();
        int inSize = inArr.size();
        TupStrLong[] values = new TupStrLong[inSize];
        for (int i = 0; i < inSize; i++) {
            values[i] = tuples.new TupStrLong(
                    inArr.get(i).getFirst(), inArr.get(i).getSecond()
            );
        }
        Arrays.sort(values, tuples.new StrLongComp());
        for (TupStrLong e : values) {
            res.add(e.getFirst());
        }
        return res;
    }

}
