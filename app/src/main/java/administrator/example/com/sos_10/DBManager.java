package administrator.example.com.sos_10;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Administrator on 2015-03-09.
 */
public class DBManager extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME  = "SOS_DB_Manager";
    private static final String TABLE_SOS = "SOS";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE_NUMBER = "PhoneNumber";
    public String userData[][];

    public DBManager(Context context) {//, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {

        String CREATE_SOS_TABLE = "CREATE TABLE "+ TABLE_SOS + " ("
                + KEY_ID + " INTEGER PRIMARY KEY, "
                + KEY_NAME + " TEXT, "
                + KEY_PHONE_NUMBER + " TEXT)";
        db.execSQL(CREATE_SOS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SOS);
        onCreate(db);
    }

    public void addPhoneNumber(String phoneNumber, String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PHONE_NUMBER, phoneNumber);
        values.put(KEY_NAME, name);

        db.insert(TABLE_SOS, null, values);
        db.close();
    }

    public String getPhoneNumber(int id) {
        String phoneNumber = getAllPhone(id, 1);
        return phoneNumber;
    }

    public String getName(int id) {
        String name = getAllPhone(id, 0);
        return name;
    }

    public int updatePhoneNumber(int id, String name, String phoneNumber) {
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_PHONE_NUMBER, phoneNumber);

        return db.update(TABLE_SOS, values, KEY_ID + "=?", new String[] { String.valueOf(id)});
    }

    public void deleteAllPhone() {
        // Select All Query
        //String selectQuery = "SELECT  * FROM " + TABLE_SOS;

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SOS, null, null);
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

    public void deleteData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_SOS, KEY_ID + "=?", new String[] { String.valueOf(id)});
        db.close();
        deleteAllPhone();
        userData[id - 1][0] = null;
        userData[id - 1][1] = null;
        addAllPhone();
    }

    public void addAllPhone() {
        for(int i = 0; i < 3; i ++) {
            if(userData[i][0] != null && userData[i][1] != null){
                addPhoneNumber(userData[i][0], userData[i][1]);
                Log.e("DEBUG", "USERDATA"+i+"= "+userData[i][0]+"    "+userData[i][1]);
            }
        }
    }

    public String getAllPhone(int intId, int flag) {
        String returnName = null, returnPhone = null;
        //List<String> phoneList = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SOS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        try {
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(0);
                    String name = cursor.getString(1);
                    String phone = cursor.getString(2);
//                Log.e("DEBUG", "ID= "+id);
//                Log.e("DEBUG", "NAME= "+name);
//                Log.e("DEBUG", "PHONE= "+phone);
                    if (Integer.parseInt(id) == intId) {
                        returnName = name;
                        returnPhone = phone;
                    }
                } while (cursor.moveToNext());
            }
        }catch(Exception e) {
        }
        if(flag == 0){
            return returnName;
        }
        else if(flag == 1){
            return returnPhone;
        }
        else{
            return null;
        }
    }
}