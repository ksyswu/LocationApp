package com.example.pc.locationapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //권한이 있는지 없는지 체크해서 권한이 없으면 요청
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(this, permissions, 1);

        findViewById(R.id.btnLocStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationService();
            }
        });

    }//end onCreate

    //위치정보 확인 셋팅 시작
    private void startLocationService() {
        LocationManager Im = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        GPSListener gpsListener = new GPSListener();
        long minTime = 800; //5초
        float minDistance = 0;

        try {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
            {
                return;
            }
            //point!! GPS를 이용한 위치 요청 실내에서는 사용 못함 오차범위 적음
            Im.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    minTime,
                    minDistance,
                    gpsListener);//정보갱신을 최소 5초마다 하겠다

            //point 2!! 네트워크  실내에서 사용 가능 오차범위 넓음
            Im.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    minTime,
                    minDistance,
                    gpsListener); //정보,신호가 잡히면 gpsListener로 콜백하는것

            //마지막 사용했던 위치 정보를 확인한다.
            Location lastLocation = Im.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(lastLocation != null){
                Double lat = lastLocation.getLatitude();
                Double lon = lastLocation.getLongitude();
                String msg = "마지막 위치: "+lat + ", "+lon;
                ToastUtil.showToast(MainActivity.this, msg);
            }


        }catch ( Exception e){
            e.printStackTrace();
        }


    }//end startLocation Service

    //GPS가 동작할 경우, GPS정보가 계속적으로 콜백 처리
    private class GPSListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            String msg = "Latitude(위도) : " + latitude + "\nLongitude(경도): " +longitude;
            Log.i("TEST", msg);
            ToastUtil.showToast(MainActivity.this, msg);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }
}
