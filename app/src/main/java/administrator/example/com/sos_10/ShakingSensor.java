package administrator.example.com.sos_10;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
public class ShakingSensor extends Service {
    static String text; // 위치정보
    static String location;
    LocationManager mlocManager;
    LocationListener mlocListener;

    Intent intent;
    PendingIntent pendingIntent;

    private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;
    private float x, y, z;

    private static String str1, str2, str3;

    private static final int SHAKE_THRESHOLD = 3500;
    private static final int DATA_X = SensorManager.DATA_X;
    private static final int DATA_Y = SensorManager.DATA_Y;
    private static final int DATA_Z = SensorManager.DATA_Z;

    public static SensorManager sensorManager;
    private Sensor accelerormeterSensor;

    NotificationManager notificationManager;
    Notification notification;
    Vibrator vibrator;

    SmsManager smsManager = SmsManager.getDefault();

 //   Timer timer;
 //   TimerTask myTask;
    int cnt=0;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() { //생성자
        super.onCreate();
        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor= sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(mySensorListener, accelerormeterSensor, SensorManager.SENSOR_DELAY_GAME);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        vibrator=(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        intent = new Intent(this, ConfigActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

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


    public SensorEventListener mySensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                long currentTime = System.currentTimeMillis();
                long gabOfTime = (currentTime - lastTime);
                if (gabOfTime > 100) {
                    lastTime = currentTime;
                    x = event.values[SensorManager.DATA_X];
                    y = event.values[SensorManager.DATA_Y];
                    z = event.values[SensorManager.DATA_Z];

                    speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000;

                    if (speed > SHAKE_THRESHOLD) {
                        cnt++;
                        if(cnt>=3) {
                            cnt=0;
                            sendMsg(); //background 테스트

                            notification = new Notification(R.drawable.horu1, "[안전 지킴이] 메시지 전송", System.currentTimeMillis());
                            notification.setLatestEventInfo(getApplicationContext(), "[안전 지킴이] 메시지 전송",
                                    "문자가 전송되었습니다.", pendingIntent);
                            notification.flags = Notification.FLAG_AUTO_CANCEL;
                            notificationManager.notify(1234, notification);
                            vibrator.vibrate(2 * 1000);
                            ScreamSensor.police.start();
                            //   timer.schedule(myTask, 5*1000);
                        }
                    }
                    lastX = event.values[DATA_X];
                    lastY = event.values[DATA_Y];
                    lastZ = event.values[DATA_Z];
                }
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    public void onDestroy(){
        sensorManager.unregisterListener(mySensorListener); //가속도 센서 해제
        mlocManager.removeUpdates(mlocListener);
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
            location =  getAddress(loc.getLatitude(), loc.getLongitude());
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

