package administrator.example.com.sos_10;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2015-03-09.
 */
public class DBManager2 extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION2 = 1;
    private static final String DATABASE_NAME2  = "SOS_DB_Manager2";
    private static final String TABLE_SOS2 = "SOS2";
    private static final String KEY_ID2 = "id";
    private static final String KEY_CNT2 = "cnt";

    public DBManager2(Context context) {//, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, DATABASE_NAME2, null, DATABASE_VERSION2);
    }
    public void onCreate(SQLiteDatabase db2) {

        String CREATE_SOS_TABLE2 = "CREATE TABLE "+ TABLE_SOS2 + " ("
                + KEY_ID2 + " INTEGER PRIMARY KEY, "
                + KEY_CNT2 + " TEXT)";
        db2.execSQL(CREATE_SOS_TABLE2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db2, int oldVersion, int newVersion) {
        db2.execSQL("DROP TABLE IF EXISTS " + TABLE_SOS2);
        onCreate(db2);
    }

    public void addState(String state) {
        SQLiteDatabase db2 = this.getWritableDatabase();

        deleteData2();
        ContentValues values = new ContentValues();
        values.put(KEY_CNT2, state);

        db2.insert(TABLE_SOS2, null, values);
        db2.close();
    }

    public String getState() {
        String returnState = null;
        //List<String> phoneList = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SOS2;

        SQLiteDatabase db2 = this.getWritableDatabase();
        Cursor cursor = db2.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        try {
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(0);
                    returnState = cursor.getString(1);
                } while (cursor.moveToNext());
            }
        }catch(Exception e) {
        }
        return returnState;
    }

    public void deleteAllState() {
        // Select All Query
        //String selectQuery = "SELECT  * FROM " + TABLE_SOS;

        SQLiteDatabase db2 = this.getWritableDatabase();
        db2.delete(TABLE_SOS2, null, null);
        /*
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(0);
                String name = cursor.getString(1);
                String phone = cursor.getString(2);
                //Log.e("DEBUG", "ID= " + id+"   NAME= "+name+"    PHONE= "+phone);
                if (id == null || name == null || phone == null){
                    return;
                }
                deleteData(Integer.parseInt(id));

            } while (cursor.moveToNext());
        }*/
    }

    public void deleteData2() {
        SQLiteDatabase db2 = this.getReadableDatabase();
        db2.delete(TABLE_SOS2, KEY_ID2 + "=?", new String[]{String.valueOf(0)});
        deleteAllState();
    }
}