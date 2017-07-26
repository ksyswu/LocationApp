package com.example.pc.locationapp;


import android.*;
import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mGoogleMap;
    private static boolean mIsFirstMap = false; //신호를 한번만 주겠다. 반복해서 신호를 주면 움직여도 제자리에 돌아가기에
    //근접경보 intent 키 정의
    public static final String PROXI_INTENT_KEY = "proximity";
    //근접경보 위치정보를 저장하고 있는 List
    private ArrayList<PendingIntent> mPendingIntentList = new ArrayList<PendingIntent>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //권한이 있는지 없는지 체크해서 권한이 없으면 요청
        String[] permissions = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(this, permissions, 1);


        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        startLocationService();

        //근접경보 위치 등록
        register(1000, 37.629285, 127.090435, 1000, 1000*60*60); //1키로 근방 , 1시간후에 해제
        register(1000, 37.621787, 127.087897, 1000, 1000*60*60);

        ProxBroadcast pb = new ProxBroadcast(PROXI_INTENT_KEY);
        registerReceiver(pb, pb.getFilter());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true); //나의 위치
        googleMap.getUiSettings().setZoomControlsEnabled(true); //확대 축소 버튼

        LatLng SEOUL = new LatLng(37.56, 126.97);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(SEOUL);
        markerOptions.title("서울");
        markerOptions.snippet("한국의 수도");
        googleMap.addMarker(markerOptions);

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(20));


    }

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
                    != PackageManager.PERMISSION_GRANTED) {
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
            if (lastLocation != null) {
                Double lat = lastLocation.getLatitude();
                Double lon = lastLocation.getLongitude();
                String msg = "마지막 위치: " + lat + ", " + lon;
                ToastUtil.showToast(MapActivity.this, msg);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }//end startLocation Service

    //GPS가 동작할 경우, GPS정보가 계속적으로 콜백 처리
    private class GPSListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            String msg = "Latitude(위도) : " + latitude + "\nLongitude(경도): " + longitude;
            Log.i("TEST", msg);
            ToastUtil.showToast(MapActivity.this, msg);

            if (mGoogleMap != null) {
                if (!mIsFirstMap) { //한번만 위치를 띄우도록
                    LatLng latlng = new LatLng(latitude, longitude);
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                    mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    mIsFirstMap = true;
                }
            }
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
    }//end GPSListener

    //근접경보 등록
    private void register(int id, double lat, double lng, float radius, long expirtaion) {
        Intent pIntent = new Intent(PROXI_INTENT_KEY); //경보울리기
        pIntent.putExtra("id", id);
        pIntent.putExtra("latitude", lat);
        pIntent.putExtra("longitude", lng);

        PendingIntent pi =
                PendingIntent.getBroadcast(MapActivity.this, id, pIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        LocationManager Im = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Im.addProximityAlert(lat, lng, radius, expirtaion, pi);
        mPendingIntentList.add(pi);

    }
}
