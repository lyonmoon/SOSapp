package administrator.example.com.sos_10;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    Intent ShakeIntent;
    Intent ScreamIntent;
    Intent ConfigIntent;

    TextView state;

    Intent SpeechIntent;
    SpeechRecognizer mRecognizer;
    String str="";
    MediaPlayer mp1;
    MediaPlayer mp2;
    MediaPlayer mp3;
    MediaPlayer mp4;

    int cnt; // 서비스 시작, 진행 유무를 파악

    ImageView ServiceImage; //서비스 시작 or 진행  그림

    Timer timer;
    TimerTask myTask;
    SmsManager smsManager = SmsManager.getDefault();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ShakeIntent = new Intent(this, ShakingSensor.class);
        ScreamIntent = new Intent(this, ScreamSensor.class);
        ConfigIntent = new Intent(this, ConfigActivity.class);

        SpeechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SpeechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        SpeechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);

        mp1 = MediaPlayer.create(this, R.raw.a);
        mp2 = MediaPlayer.create(this, R.raw.b);
        mp3 = MediaPlayer.create(this, R.raw.c);
        mp4 = MediaPlayer.create(this, R.raw.d);

        DBManager2 db2 = new DBManager2(this);
        String stringCnt = db2.getState();
        if(stringCnt == null || stringCnt.equals("0")) {
            cnt = 0;
        }
        else if(stringCnt.equals("1")){
            cnt = 1;
        }
        ServiceImage = (ImageView) findViewById(R.id.service);
        state = (TextView) findViewById(R.id.state);
        if(cnt == 1) {
            ServiceImage.setImageResource(R.drawable.alert);
            state.setText("경계 중");
        }



        timer = new Timer();

    }

    public void startServices(){
        startService(ShakeIntent); //여기에 GPS 포함
        startService(ScreamIntent);
    }

    public void stopServices(){
        stopService(ShakeIntent);
        stopService(ScreamIntent);
    }



    public void onClick(View view) {
        switch(view.getId()){
            case R.id.service:
                DBManager2 db2 = new DBManager2(this);
                String stringCnt = db2.getState();
                if(stringCnt == null || stringCnt.equals("0")) {
                    cnt = 0;
                }
                else if(stringCnt.equals("1")){
                    cnt = 1;
                }
                if(cnt==0) {
                    startServices();
                    Toast.makeText(MainActivity.this, "서비스가 시작되었습니다.", Toast.LENGTH_SHORT).show();
                    ServiceImage.setImageResource(R.drawable.alert);
                    state.setText("경계 중");
                    cnt++;
                    db2.addState("1");
                }
                else{
                    stopServices();
                    Toast.makeText(MainActivity.this, "서비스가 종료되었습니다.", Toast.LENGTH_SHORT).show();
                    ServiceImage.setImageResource(R.drawable.nonalert2);
                    state.setText("쉬는 중");
                    cnt--;
                    db2.addState("0");
                }
                db2.close();
                break;
            case R.id.help:
                Toast.makeText(MainActivity.this, "웹페이지 띄울 예정", Toast.LENGTH_SHORT).show();
                break;
            case R.id.config:
                startActivity(ConfigIntent);
                break;
            case R.id.stt:
                myTask = new TimerTask(){
                    @Override
                    public void run(){
                        startService(ScreamIntent);
                        myTask.cancel();
                    }
                };
                stopService(ScreamIntent);
                mRecognizer.startListening(SpeechIntent);
                timer.schedule(myTask, 8*1000);
                break;
            case R.id.send_msg:
                DBManager db = new DBManager(this);
                db.userData = new String[3][];
                db.userData[0] = new String[2];
                db.userData[1] = new String[2];
                db.userData[2] = new String[2];
                db.userData[0][0] = db.getPhoneNumber(1);db.userData[0][1] = db.getName(1);
                db.userData[1][0] = db.getPhoneNumber(2);db.userData[1][1] = db.getName(2);
                db.userData[2][0] = db.getPhoneNumber(3);db.userData[2][1] = db.getName(3);
                for (int i = 0; i < 3; i ++) {
                    String str = db.userData[i][0];
                    if(str == null) continue;
                    smsManager.sendTextMessage(str, null,
                            "[안전 지킴이]\n" + ShakingSensor.text + "\n도와주세요!!",
                            null, null);
                }
                db.close();
                Toast.makeText(this,"문자가 전송되었습니다.",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onRmsChanged(float rmsdB) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onResults(Bundle results) {
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            str=rs[0];
            if((str.contains("가고") && str.contains("있어")) || str.contains("가려고"))
                mp1.start();
            else if(str.contains("혼자") || str.contains("갈게"))
                mp2.start();
            else if(str.contains("먹고") || str.contains("사갈까"))
                mp3.start();
            else
                mp4.start();
        }
        @Override
        public void onReadyForSpeech(Bundle params) {
            // TODO Auto-generated method stub
        }
        @Override

        public void onPartialResults(Bundle partialResults) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onEvent(int eventType, Bundle params) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onError(int error) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onEndOfSpeech() {
            // TODO Auto-generated method stub
        }
        @Override
        public void onBufferReceived(byte[] buffer) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onBeginningOfSpeech() {
            // TODO Auto-generated method stub
        }
    };
}