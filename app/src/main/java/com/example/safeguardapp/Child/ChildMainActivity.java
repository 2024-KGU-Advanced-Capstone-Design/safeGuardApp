package com.example.safeguardapp.Child;

import static com.naver.maps.map.NaverMap.MapType.Basic;
import static com.naver.maps.map.NaverMap.MapType.Hybrid;

import static java.security.AccessController.getContext;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.safeguardapp.Group.GetMemberIDRequest;
import com.example.safeguardapp.Group.Sector.SectorDetails;
import com.example.safeguardapp.Group.Sector.SectorInquireRequest;
import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.Map.LocationRequest;
import com.example.safeguardapp.Map.LocationResponse;
import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.StartScreenActivity;
import com.example.safeguardapp.UserRetrofitInterface;
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
import com.naver.maps.map.overlay.PolygonOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;
import com.naver.maps.map.widget.CompassView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChildMainActivity extends AppCompatActivity implements OnMapReadyCallback {
    RetrofitClient retrofitClient;
    UserRetrofitInterface userRetrofitInterface;
    public MapFragment childMapFragment;
    public ChildSettingFragment childSettingFragment;
    private ImageButton fatalButton;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private NaverMap mNaverMap;
    public static double latitude, longitude;
    private boolean doubleBackToExitPressedOnce = false;
    private Intent serviceIntent;
    private ArrayList<String> memberList = new ArrayList<>();
    private Map<Integer, String> dynamicVariables = new HashMap<>();
    private Map<String, Marker> memberMarkers = new HashMap<>();
    private List<PolygonOverlay> polygonOverlays = new ArrayList<>();
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
    private String childID;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_main);

        // RetrofitClient 초기화
        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = retrofitClient.getUserRetrofitInterface();

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        // -----v----- BottomNavigationView 구현 -----v-----
        childMapFragment = new MapFragment();
        childSettingFragment = new ChildSettingFragment();
        fatalButton = findViewById(R.id.add_fatal_btn);
        fatalButton.setOnClickListener(v -> fatal());

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
                    // mapFragment를 실행시 나침반 보이게 설정
                    CompassView compassView = findViewById(R.id.compass);
                    compassView.setVisibility(View.VISIBLE);

                    return true;
                } else if (item.getItemId() == R.id.setting) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, childSettingFragment).commit();
                    // SettingFragment를 실행시 mapModeNav를 사라지게 설정
                    LinearLayout mapModeNav = findViewById(R.id.mapModeNav);
                    mapModeNav.setVisibility(View.GONE);
                    // SettingFragment를 실행시 나침반 안 보이게 설정
                    CompassView compassView = findViewById(R.id.compass);
                    compassView.setVisibility(View.GONE);
                    return true;
                }
                return false;
            }
        });

        childMapFragment.getMapAsync(this);

        //위치를 반환하는 구현체인 FusedLocationSource 생성
        locationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);

        SharedPreferences sharedPreferences2 = getSharedPreferences("loginID", Context.MODE_PRIVATE);
        Boolean isAutoLogin = sharedPreferences2.getBoolean("autoLogin", false);

        handler = new Handler(Looper.getMainLooper());

        childID = LoginPageFragment.saveID;
        GetMemberIDRequest childIDDTO = new GetMemberIDRequest(childID);
        Gson gson = new Gson();
        String childInfo = gson.toJson(childIDDTO);
        Log.e("JSON", childInfo + "Here");

        Call<ResponseBody> memberCall = userRetrofitInterface.getMemberID(childIDDTO);
        memberCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("POST", "응답성공");
                    try {
                        // 응답 본문을 문자열로 변환
                        String responseBodyString = response.body().string();
                        JSONObject json = new JSONObject(responseBodyString);
                        Log.e("Response JSON", json.toString());

                        // 최상위 키 순회
                        for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                            String topKey = it.next();
                            if (topKey.equals("Parenting")) {  // 최상위 키가 "Parenting"인 경우만 처리
                                JSONObject innerJson = json.getJSONObject(topKey);

                                // 내부 키 순회
                                for (Iterator<String> innerIt = innerJson.keys(); innerIt.hasNext(); ) {
                                    String innerKey = innerIt.next();
                                    String value = innerJson.getString(innerKey);

                                    // memberList에 값 추가
                                    memberList.add(value);
                                    // 여기서 키와 값을 사용할 수 있습니다.
                                    Log.e("Parsed Data", "TopKey: " + topKey + ", InnerKey: " + innerKey + ", Value: " + value);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("getMemberID", "Response body is null or request failed" + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Log.e("getMemberID", "Request failed", t);
            }
        });

        // 마커를 업데이트하는 Runnable 정의
        updateMarkerRunnable = new Runnable() {
            @Override
            public void run() {
                getMemberLocation();
                handler.postDelayed(this, 2000); // 2초마다 실행
            }
        };

        // 주기적인 마커 업데이트 시작
        handler.post(updateMarkerRunnable);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationService();
        } else
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    if (isAutoLogin) {
                        finishAffinity(); // 현재 액티비티와 관련된 모든 액티비티를 종료
                        return;
                    } else {
                        stopService(serviceIntent);
                        Intent intent = new Intent(ChildMainActivity.this, StartScreenActivity.class);
                        startActivity(intent);
                    }
                }

                doubleBackToExitPressedOnce = true;
                Toast.makeText(ChildMainActivity.this, "앱을 종료하시려면 한번 더 눌러주세요", Toast.LENGTH_SHORT).show();

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000); // 2초 안에 두 번 눌러야 종료
            }
        });
    }


    private void startLocationService() {
        Log.e("POST", "Latitude: " + latitude + ", Longitude: " + longitude);

        serviceIntent = new Intent(this, LocationService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
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

        mNaverMap.addOnLocationChangeListener(location -> {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            LocationService.transmitCoordinate(latitude, longitude);
        });
        sectorInquire();
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

    private void getMemberLocation() {
        for (int i = 0; i < memberList.size(); i++) {
            String getMemberId = memberList.get(i);
            String type = "Member";

            LocationRequest locationRequest = new LocationRequest(type, getMemberId);
            Gson gson = new Gson();
            String memberInfo = gson.toJson(locationRequest);

            /*Log.e("JSON", memberInfo);*/

            Call<LocationResponse> call = userRetrofitInterface.getLocation(locationRequest);
            int finalI = i;
            call.clone().enqueue(new Callback<LocationResponse>() {
                @Override
                public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LocationResponse result = response.body();

                        double latitude = result.getLatitude();
                        double longitude = result.getLongitude();

                        dynamicVariables.put(finalI, memberList.get(finalI));
                        markerName = dynamicVariables.get(finalI);

                        if (memberMarkers.get(markerName) == null) { // 해당 childMarker가 한번도 생성되지 않은 경우
                            Marker marker = new Marker();
                            memberMarkers.put(markerName, marker);

                            marker.setCaptionText(getMemberId);
                            marker.setCaptionAligns(Align.Top);
                            marker.setCaptionOffset(10);
                            marker.setIcon(MarkerIcons.BLACK);
                            marker.setIconTintColor(Color.argb(0, 234, 234, 0));
                            marker.setHideCollidedSymbols(true);
                            marker.setCaptionTextSize(16);
                            marker.setPosition(new LatLng(latitude, longitude));
                            marker.setMap(mNaverMap);
                        } else { // 해당 childMarker가 이미 생성되어 있는 경우
                            memberMarkers.get(markerName).setMap(null);

                            Marker marker = new Marker();
                            memberMarkers.replace(markerName, marker);

                            marker.setCaptionText(getMemberId);
                            marker.setCaptionAligns(Align.Top);
                            marker.setCaptionOffset(10);
                            marker.setIcon(MarkerIcons.BLACK);
                            marker.setIconTintColor(Color.argb(0, 234, 234, 0));
                            marker.setHideCollidedSymbols(true);
                            marker.setCaptionTextSize(16);
                            marker.setPosition(new LatLng(latitude, longitude));
                            marker.setMap(mNaverMap);
                        }
                    } else {
                        Log.e("POST", "응답 실패 또는 바디가 null: " + response.code() + " " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<LocationResponse> call, Throwable t) {
                    Log.e("POST", "통신 실패: " + t.getMessage());
                }
            });
        }
    }

    private void sectorInquire() {
        final Gson gson = new Gson();

        removeAllPolygons();

        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

        // 요청 JSON 로그 출력
        SectorInquireRequest sectorInquireDTO = new SectorInquireRequest(LoginPageFragment.saveID);
        String requestJson = gson.toJson(sectorInquireDTO);
        /*Log.e("Request JSON", requestJson);*/

        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), requestJson);
        Call<ResponseBody> call = userRetrofitInterface.getSectorLocation(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LinkedHashMap<String, SectorDetails> sectors = new LinkedHashMap<>();

                    try {
                        JSONObject json = new JSONObject(response.body().string());

                        for (Iterator<String> keys = json.keys(); keys.hasNext(); ) {
                            String key = keys.next();

                            SectorDetails sector = gson.fromJson(json.getJSONObject(key).toString(), SectorDetails.class);
                            sectors.put(key, sector);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }

                    if (sectors.isEmpty()) {
                        Log.e("SectorInquire", "No sectors found or sectorResponse is null");
                        return;
                    }

                    for (Map.Entry<String, SectorDetails> entry : sectors.entrySet()) {
                        String coordinateId = entry.getKey(); // 키 값 저장
                        SectorDetails details = entry.getValue();
                        boolean isLiving = Boolean.parseBoolean(details.getIsLiving());

                        // 로그 출력
                        Log.e("Coordinate ID", coordinateId);

                        // 좌표를 가져와서 LatLng 리스트를 생성합니다.
                        List<LatLng> polygonCoords = new ArrayList<>();
                        polygonCoords.add(new LatLng(details.getYofPointA(), details.getXofPointA()));
                        polygonCoords.add(new LatLng(details.getYofPointB(), details.getXofPointB()));
                        polygonCoords.add(new LatLng(details.getYofPointC(), details.getXofPointC()));
                        polygonCoords.add(new LatLng(details.getYofPointD(), details.getXofPointD()));

                        // 폴리곤 오버레이 생성
                        PolygonOverlay polygonOverlay = new PolygonOverlay();
                        polygonOverlay.setCoords(polygonCoords);

                        // 색상 설정
                        if (isLiving) {
                            polygonOverlay.setColor(Color.argb(75, 0, 100, 0)); // 초록색
                        } else {
                            polygonOverlay.setColor(Color.argb(75, 100, 0, 0)); // 빨간색
                        }

                        // 폴리곤을 지도에 추가
                        polygonOverlay.setMap(mNaverMap);
                        polygonOverlays.add(polygonOverlay);
                    }
                } else {
                    // 응답 본문이 null일 때 처리
                    Log.e("SectorInquire", "Response body is null");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 요청 실패 처리
                Log.e("SectorInquire", "Request failed", t);
            }
        });
    }

    private void removeAllPolygons() {
        for (PolygonOverlay overlay : polygonOverlays) {
            overlay.setMap(null);
        }
        polygonOverlays.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateMarkerRunnable); // 액티비티가 종료될 때 주기적인 업데이트를 중지
    }

    private void fatal() {
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(childMapFragment.getContext())
                .setTitle("위험")
                .setMessage("위험 알림을 보내시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 사용자가 OK 버튼을 클릭할 때 호출됨
                        // 선택된 항목의 인덱스를 가져옴

                        ChildFatalRequest childFatalRequest = new ChildFatalRequest(childID);
                        Call<ResponseBody> call = userRetrofitInterface.sendFatal(childFatalRequest);

                        call.clone().enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(childMapFragment.getContext(), "전송되었습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(childMapFragment.getContext(), "통신오류", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("취소", null);

        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }
}
