package com.example.safeguardapp;

import static com.naver.maps.map.NaverMap.MapType.Basic;
import static com.naver.maps.map.NaverMap.MapType.Hybrid;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.safeguardapp.Group.GetChildIDRequest;
import com.example.safeguardapp.Group.GroupFragment;
import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.Map.ChildLocationRequest;
import com.example.safeguardapp.Map.ChildLocationResponse;
import com.example.safeguardapp.Notice.NoticeFragment;
import com.example.safeguardapp.Setting.SettingFragment;
import com.example.safeguardapp.data.repository.GroupRepository;
import com.google.android.material.navigation.NavigationBarView;
import com.google.gson.Gson;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Align;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;
import com.naver.maps.map.widget.CompassView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    RetrofitClient retrofitClient;
    UserRetrofitInterface userRetrofitInterface;
    public MapFragment mapFragment;
    public GroupFragment groupFragment;
    public NoticeFragment noticeFragment;
    public SettingFragment settingFragment;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private NaverMap mNaverMap;
    private Boolean doubleBackToExitPressedOnce = false;

    private ArrayList<String> childList = new ArrayList<>();
    private Map<Integer, String> dynamicVariables = new HashMap<>();
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.ACCESS_NOTIFICATION_POLICY
    };
    private String markerName;

    private Handler handler;
    private Runnable updateMarkerRunnable;
    private Marker childMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        // -----v----- BottomNavigationView 구현 -----v-----
        mapFragment = new MapFragment();
        groupFragment = new GroupFragment();
        noticeFragment = new NoticeFragment();
        settingFragment = new SettingFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.containers, mapFragment).commit();

        NavigationBarView navigationBarView = findViewById(R.id.bottom_navigationview);
        navigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.map) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, mapFragment).commit();
                    mapFragment.getMapAsync(MainActivity.this);
                    // mapFragment를 실행시 mapModeNav 보이게 설정
                    LinearLayout mapModeNav = findViewById(R.id.mapModeNav);
                    mapModeNav.setVisibility(View.VISIBLE);
                    return true;
                } else if (item.getItemId() == R.id.setting) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, settingFragment).commit();
                    // SettingFragment로 실행시 mapModeNav를 사라지게 설정
                    LinearLayout mapModeNav = findViewById(R.id.mapModeNav);
                    mapModeNav.setVisibility(View.GONE);
                    return true;
                } else if (item.getItemId() == R.id.notice) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, noticeFragment).commit();
                    // NoticeFragment로 실행시 mapModeNav를 사라지게 설정
                    LinearLayout mapModeNav = findViewById(R.id.mapModeNav);
                    mapModeNav.setVisibility(View.GONE);
                    return true;
                } else if (item.getItemId() == R.id.group) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, groupFragment).commit();
                    // GroupFragment로 넘어가면 mapModeNav를 사라지게 설정
                    LinearLayout mapModeNav = findViewById(R.id.mapModeNav);
                    mapModeNav.setVisibility(View.GONE);
                    return true;
                }
                return false;
            }
        });
        // -----^----- BottomNavigationView 구현 -----^-----

        //getMapAsync 호출해 비동기로 onMapReady 콜백 메서드 호출
        //onMapReady에서 NaverMap 객체를 받음.
        mapFragment.getMapAsync(this);

        //위치를 반환하는 구현체인 FusedLocationSource 생성
        locationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);

        SharedPreferences sharedPreferences2 = getSharedPreferences("loginID", Context.MODE_PRIVATE);
        Boolean isAutoLogin = sharedPreferences2.getBoolean("autoLogin", false);

        handler = new Handler(Looper.getMainLooper());

        //childList Create
        String memberID = LoginPageFragment.saveID;
        GetChildIDRequest memberIDDTO = new GetChildIDRequest(memberID);
        Gson ggson = new Gson();
        String memberInfo = ggson.toJson(memberIDDTO);
        Log.e("JSON", memberInfo);

        Call<ResponseBody> childCall = userRetrofitInterface.getChildID(memberIDDTO);
        childCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("POST", "응답성공");
                    try {
                        // 응답 본문을 문자열로 변환
                        String responseBodyString = response.body().string();
                        JSONObject json = new JSONObject(responseBodyString);
                        Log.e("Response JSON", json.toString());
                        Log.e("POST", "응답성공");

                        // 각 키-값 쌍을 처리
                        for (Iterator<String> keys = json.keys(); keys.hasNext(); ) {
                            String key = keys.next();
                            String value = json.getString(key);
                            if(key.equals("status")){
                            }
                            else{
                                childList.add(value);
                            }

                            Log.e("Child ID", "Key: " + key + ", Value: " + value);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("getChildID", "Response body is null or request failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Log.e("getChildID", "Request failed", t);
            }
        });


        for(int i = 0; i< childList.size(); i++){
            dynamicVariables.put(i, childList.get(i));
            markerName = dynamicVariables.get(i);
            Marker markerName = new Marker();
        }

        childMarker = new Marker();

        // 마커를 업데이트하는 Runnable 정의
        updateMarkerRunnable = new Runnable() {
            @Override
            public void run() {
                getChildLocation();
                handler.postDelayed(this, 5000); // 5초마다 실행
            }
        };

        // 주기적인 마커 업데이트 시작
        handler.post(updateMarkerRunnable);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    if (isAutoLogin) {
                        finishAffinity(); // 현재 액티비티와 관련된 모든 액티비티를 종료
                        return;
                    } else {
                        GroupRepository groupRepository = GroupRepository.getInstance(MainActivity.this);
                        groupRepository.removeAllGroups();
                        Intent intent = new Intent(MainActivity.this, StartScreenActivity.class);
                        startActivity(intent);
                    }
                }

                doubleBackToExitPressedOnce = true;
                Toast.makeText(MainActivity.this, "앱을 종료하시려면 한번 더 눌러주세요", Toast.LENGTH_SHORT).show();

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000); // 2초 안에 두 번 눌러야 종료
            }
        });
    }

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

                if (mapType.equals("일반지도")) {
                    naverMap.setMapType(Basic);
                } else if (mapType.equals("위성지도")) {
                    naverMap.setMapType(Hybrid);
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

        // CompassView를 NaverMap에 바인딩
        CompassView compassView = findViewById(R.id.compass);
        uiSettings.setCompassEnabled(false); // 기본 나침반 비활성화
        compassView.setMap(naverMap); // 커스텀 나침반 설정

        // 권한 확인, 결과는 onRequestPermissionResult 콜백 메서드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
//        getChildLocation();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // request code와 권한 획득 여부 확인
        if(requestCode == PERMISSION_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
    }

    private void getChildLocation(){
        for(int i =0; i<childList.size(); i++){
            String getChildId = childList.get(i);
            String type = "Child";

            ChildLocationRequest childLocationRequest = new ChildLocationRequest(type, getChildId);
            Gson gson = new Gson();
            String childInfo = gson.toJson(childLocationRequest);

            Log.e("JSON", childInfo);

            Call<ChildLocationResponse> call = userRetrofitInterface.getChildLocation(childLocationRequest);
            int finalI = i;
            call.clone().enqueue(new Callback<ChildLocationResponse>() {
                @Override
                public void onResponse(Call<ChildLocationResponse> call, Response<ChildLocationResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.e("POST", "통신 성공");

                        ChildLocationResponse result = response.body();
                        Log.e("POST", "Response: " + gson.toJson(result)); // 전체 응답 로그

                        double latitude = result.getLatitude();
                        double longitude = result.getLongitude();

                        Log.e("JSON", "Coordinates: " + latitude + ", " + longitude);

                        dynamicVariables.put(finalI, childList.get(finalI));
                        markerName = dynamicVariables.get(finalI);
                        Marker markerName = new Marker();


                        markerName.setCaptionText(getChildId);
                        markerName.setCaptionAligns(Align.Top);
                        markerName.setCaptionOffset(10);
                        markerName.setIcon(MarkerIcons.BLACK);
                        markerName.setIconTintColor(Color.argb(0, 234, 234, 0));
                        markerName.setHideCollidedSymbols(true);
                        markerName.setCaptionTextSize(16);
                        markerName.setPosition(new LatLng(latitude, longitude));
                        markerName.setMap(mNaverMap);
                    }
                    else {
                        Log.e("POST", "응답 실패 또는 바디가 null: " + response.code() + " " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ChildLocationResponse> call, Throwable t) {
                    Log.e("POST", "통신 실패: " + t.getMessage());
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateMarkerRunnable); // 액티비티가 종료될 때 주기적인 업데이트를 중지
    }
}