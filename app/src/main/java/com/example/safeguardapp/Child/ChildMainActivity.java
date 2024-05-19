package com.example.safeguardapp.Child;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.safeguardapp.R;
import com.google.android.material.navigation.NavigationBarView;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.util.FusedLocationSource;

public class ChildMainActivity extends AppCompatActivity implements OnMapReadyCallback {
    public MapFragment childMapFragment;
    public ChildSettingFragment childSettingFragment;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private NaverMap mNaverMap;
    private double latitude, longitude;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_main);
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        // -----v----- BottomNavigationView 구현 -----v-----
        childMapFragment = new MapFragment();
        childSettingFragment = new ChildSettingFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.containers, childMapFragment).commit();

        NavigationBarView navigationBarView = findViewById(R.id.child_bottom_navigationview);
        navigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.map) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, childMapFragment).commit();
                    childMapFragment.getMapAsync(ChildMainActivity.this);
                    // mapFragment를 실행시 mapModeNav 보이게 설정
                    LinearLayout mapModeNav = findViewById(R.id.mapModeNav);
                    mapModeNav.setVisibility(View.VISIBLE);
                    return true;
                } else if (item.getItemId() == R.id.setting) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, childSettingFragment).commit();
                    // SettingFragment로 실행시 mapModeNav를 사라지게 설정
                    LinearLayout mapModeNav = findViewById(R.id.mapModeNav);
                    mapModeNav.setVisibility(View.GONE);
                    return true;
                }
                return false;
            }
        });

        childMapFragment.getMapAsync(this);

        //위치를 반환하는 구현체인 FusedLocationSource 생성
        locationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
/*
            startLocationService();
*/
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
        }
    }

    /*private void startLocationService() {
        Log.e("POST", "Latitude: " + latitude + ", Longitude: " + longitude);

        Intent serviceIntent = new Intent(this, LocationService.class);
        serviceIntent.putExtra("latitude", latitude);
        serviceIntent.putExtra("longitude", longitude);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }*/

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.map_types,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = findViewById(R.id.map_type);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CharSequence mapType = adapter.getItem(position);
                if (mapType != null) {
                    naverMap.setMapType(NaverMap.MapType.valueOf(mapType.toString()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // NaverMap 객체 받아서 NaverMap 객체에 위치 소스 지정
        mNaverMap = naverMap;
        mNaverMap.setLocationSource(locationSource);
        mNaverMap.setIndoorEnabled(true);

        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        // 권한 확인, 결과는 onRequestPermissionResult 콜백 메서드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);

        mNaverMap.addOnLocationChangeListener(location -> {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            SharedPreferences sharedPreferences = getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("latitude_v2", Double.doubleToLongBits(latitude));
            editor.putLong("longitude_v2", Double.doubleToLongBits(longitude));
            editor.apply();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // request code와 권한 획득 여부 확인
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
    }
}
