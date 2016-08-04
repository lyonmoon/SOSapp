package administrator.example.com.sos_10;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2015-03-02.
 */
public class ScreamSensor extends Service {
    String text; // 위치정보
    LocationManager mlocManager;
    LocationListener mlocListener;
    static MediaPlayer police;

    private AudioReader audioReader;
    private int sampleRate = 8000;
    private int inputBlockSize = 256;
    private int sampleDecimate = 1;
    private static String str1, str2, str3;

    NotificationManager notificationManager;
    Notification notification;
    Vibrator vibrator;

    Intent intent;
    PendingIntent pendingIntent;

    static int decibel;

    SmsManager smsManager = SmsManager.getDefault();

   // Timer timer;
   // TimerTask myTask;
    int cnt=0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        vibrator=(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        audioReader = new AudioReader();
        intent = new Intent(this, ConfigActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        police = MediaPlayer.create(this, R.raw.police);
        police.setLooping(true);

        audioReader.startReader(sampleRate, inputBlockSize * sampleDecimate, new AudioReader.Listener(){
            @Override
            public final void onReadComplete(int dB) {
                receiveDecibel(dB);
                decibel=dB+73;
                //text.setText(String.format(dB+73 + "dB", dB));  1번
                //text.setText(dB+73 +" dB");       2번
            }
            @Override
            public void onReadError(int error)
            {
            }
        });

        mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60*1000, 0, mlocListener);
/*
        timer = new Timer();
        myTask = new TimerTask(){
            @Override
            public void run(){
            }
        };
        */
    }
    private void receiveDecibel(final int dB) {
        //Log.e("###", dB + 73 + " dB");  // Logcat에 데시벨이  -70~ -73으로 출력이되어 73을더한다.
        if(dB+77>70){
            cnt++;
            if(cnt>=3) {
                sendMsg();
                cnt = 0;
            }
            /*
            if(!str1.equals("") && !str1.equals(null)
                    && (str2.equals("") || str2.equals(null))
                    && (str3.equals("") || str3.equals(null))) {
                Log.d("##",str1);
                sendMsg(str1);
                Log.d("1", "1");
            }
            else if(!str1.equals("") && !str1.equals(null)
                    && !str2.equals("") && !str2.equals(null)
                    && (str3.equals("") || str3.equals(null))){
                sendMsg(str1,str2);
                Log.d("2","2");
            }
            else if(!str1.equals("") && !str1.equals(null)
                    && !str2.equals("") && !str2.equals(null)
                    && !str3.equals("") && !str3.equals(null)){
                sendMsg(str1,str2,str3);
                Log.d("3","3");
            }
            //     cnt = 0;
            //   }
            */
            notification = new Notification(R.drawable.horu1, "[안전 지킴이] 메시지 전송", System.currentTimeMillis());
            notification.setLatestEventInfo(getApplicationContext(), "[안전 지킴이] 메시지 전송",
                    "문자가 전송되었습니다.", pendingIntent);
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(1234, notification);
            vibrator.vibrate(2 * 1000);
            police.start();
          //timer.schedule(myTask, 5*1000);
        }
    }

    public static int getDecibel(){
        return decibel;
    }

    public static void setNumber(String str11, String str22, String str33){
        str1=str11;
        str2=str22;
        str3=str33;
    }
    @Override
    public void onDestroy(){
        audioReader.stopReader();
        mlocManager.removeUpdates(mlocListener);
        police.stop();
    }

    public void sendMsg(){
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
                    "[안전 지킴이]\n" + text + "\n도와주세요!!",
                    null, null);
        }
        db.close();
    }

    public class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            loc.getLatitude();
            loc.getLongitude();
            text = "현재 위치\n" +
                    getAddress(loc.getLatitude(), loc.getLongitude());
            /*
                    "\n위도 : " + loc.getLatitude() +
                    "\n경도 : " + loc.getLongitude();
                    */
            /*
            Toast.makeText(getApplicationContext(),
                    Text,
                    Toast.LENGTH_SHORT).show();
                    */
        }
        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(),
                    "Gps Disabled",
                    Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(),
                    "Gps Enabled",
                    Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }/* End of Class MyLocationListener */
    /** 위도와 경도 기반으로 주소를 리턴하는 메서드*/
    public String getAddress(double lat, double lng){
        String address = null;

        //위치정보를 활용하기 위한 구글 API 객체
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        //주소 목록을 담기 위한 HashMap
        List<Address> list = null;

        try{
            list = geocoder.getFromLocation(lat, lng, 1);
        } catch(Exception e){
            e.printStackTrace();
        }

        if(list == null){
            Log.e("getAddress", "주소 데이터 얻기 실패");
            return null;
        }
        if(list.size() > 0){
            Address addr = list.get(0);
            address = addr.getLocality() + " "
                    + addr.getThoroughfare() + " "
                    + addr.getFeatureName();
        }
        return address;
    }
}
