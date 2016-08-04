package administrator.example.com.sos_10;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class ConfigActivity extends Activity {
    Intent PhoneIntent;

    TextView dB_text;
    TextView loc_text;

    Clock clock;

    /* 전화번호부 부분 */
    String name;//전화번호부에서 받아올 이름
    String number;//전화번호부에서 받아올 번호
    TextView[] name_text = new TextView[3];
    String[] phone_str = new String[3];
    String[] name_str = new String[3];
    int lastClick = 0;

    DBManager db;

    public Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            if(msg.what == 0){
                dB_text.setText(ScreamSensor.getDecibel() + " DB");
                loc_text.setText(ShakingSensor.location);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.activity_config);

        PhoneIntent = new Intent(Intent.ACTION_PICK);
        PhoneIntent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);

        dB_text = (TextView) findViewById(R.id.decibel);
        loc_text = (TextView) findViewById(R.id.location);

        clock = new Clock();
        clock.setDaemon(true);
        clock.start();

        name_text[0]=(TextView) findViewById(R.id.name1);
        name_text[1]=(TextView) findViewById(R.id.name2);
        name_text[2]=(TextView) findViewById(R.id.name3);

        db = new DBManager(this);
        db.userData = new String[3][];
        db.userData[0] = new String[2];
        db.userData[1] = new String[2];
        db.userData[2] = new String[2];
        db.userData[0][0] = db.getPhoneNumber(1);db.userData[0][1] = db.getName(1);
        db.userData[1][0] = db.getPhoneNumber(2);db.userData[1][1] = db.getName(2);
        db.userData[2][0] = db.getPhoneNumber(3);db.userData[2][1] = db.getName(3);

        Log.e("DEBUG", "GETNAME0="+db.userData[0][1]);
        Log.e("DEBUG", "GETNAME1="+db.userData[1][1]);
        Log.e("DEBUG", "GETNAME2="+db.userData[2][1]);

        if(db.userData[0][1] == null)
            name_text[0].setText("눌러서 추가하세요.");
        else
            name_text[0].setText(db.userData[0][1]);

        if(db.userData[1][1] == null)
            name_text[1].setText("눌러서 추가하세요.");
        else
            name_text[1].setText(db.userData[1][1]);

        if(db.userData[2][1] == null)
            name_text[2].setText("눌러서 추가하세요.");
        else
            name_text[2].setText(db.userData[2][1]);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onClick(View view) {
        switch(view.getId()) {
            /* 주소록  추가  부분*/
            case R.id.name1:
                startActivityForResult(PhoneIntent, 0);
                lastClick = 0;
                break;
            case R.id.name2:
                startActivityForResult(PhoneIntent, 0);
                lastClick = 1;
                break;
            case R.id.name3:
                startActivityForResult(PhoneIntent, 0);
                lastClick = 2;
                break;
            /*주소록 삭제 부분 */
            case R.id.delete_1:
                name_text[0].setText("눌러서 추가하세요.");
                db.deleteData(1);
                break;
            case R.id.delete_2:
                name_text[1].setText("눌러서 추가하세요.");
                db.deleteData(2);
                break;
            case R.id.delete_3:
                name_text[2].setText("눌러서 추가하세요.");
                db.deleteData(3);
                break;
            /* 전화걸기 */
            case R.id.call_1:
                if(db.userData[0][0] == null)
                    Toast.makeText(this, "번호를 먼저 추가하세요.", Toast.LENGTH_SHORT).show();
                else
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tel:".concat(db.userData[0][0]))));
                break;
            case R.id.call_2:
                if(db.userData[1][0] == null)
                    Toast.makeText(this, "번호를 먼저 추가하세요.", Toast.LENGTH_SHORT).show();
                else
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tel:".concat(db.userData[1][0]))));
                break;
            case R.id.call_3:
                if(db.userData[2][0] == null)
                    Toast.makeText(this, "번호를 먼저 추가하세요.", Toast.LENGTH_SHORT).show();
                else
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tel:".concat(db.userData[2][0]))));
                break;
        }
    }



    class Clock extends Thread{
        public void run(){
            while(true){
                mHandler.sendEmptyMessage(0);
                try{
                    Thread.sleep(500);
                }
                catch(InterruptedException e){
                    Log.e("Error", e.toString());
                }
            }
        }
    }

    /* 전화번호부 눌렀을 때의 동작 */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            Cursor cursor = getContentResolver().query(data.getData(),
                    new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
            cursor.moveToFirst();
            name = cursor.getString(0);        //0은 이름을 얻어옵니다.
            number = cursor.getString(1);   //1은 번호를 받아옵니다.
            db.deleteAllPhone();
            name_text[lastClick].setText(name);
//            Log.e("DEBUG", "LASTCLCIK= "+lastClick);
            db.userData[lastClick][0] = number;
            db.userData[lastClick][1] = name;
            db.addAllPhone();
            cursor.close();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}