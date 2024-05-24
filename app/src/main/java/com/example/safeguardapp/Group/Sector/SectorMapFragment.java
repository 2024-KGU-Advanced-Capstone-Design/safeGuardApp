package com.example.safeguardapp.Group.Sector;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.safeguardapp.Group.GroupSettingFragment;
import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.UserRetrofitInterface;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PolygonOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;
import com.naver.maps.map.widget.CompassView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

public class SectorMapFragment extends Fragment implements OnMapReadyCallback {
    RetrofitClient retrofitClient;
    UserRetrofitInterface userRetrofitInterface;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1, mParam2;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private NaverMap mNaverMap;
    private String currentGroupUuid, childName;

    private List<LatLng> polygonPoints = new ArrayList<>();
    private HashMap<Integer, PolygonOverlay> greenPolygonOverlays = new HashMap<>();
    private HashMap<Integer, PolygonOverlay> redPolygonOverlays = new HashMap<>();
    private List<Marker> redMarkerList = new ArrayList<>();
    private List<Marker> greenMarkerList = new ArrayList<>();
    private HashMap<Integer, InfoWindow> greenInfoWindowList = new HashMap<>();
    private HashMap<Integer, InfoWindow> redInfoWindowList = new HashMap<>();
    private HashMap<String, PolygonOverlay> sectorPolygons = new HashMap<>();
    private int greenIndex = 1;
    private int redIndex = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public SectorMapFragment() {
    }

    public static SectorMapFragment newInstance(String param1, String param2) {
        SectorMapFragment fragment = new SectorMapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            currentGroupUuid = getArguments().getString("UUID");
            childName = getArguments().getString("childID");
        }

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.sectorMapScreen);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getChildFragmentManager().beginTransaction().add(R.id.sectorMapScreen, mapFragment).commit();
        }

        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

        mapFragment.getMapAsync(this);

        return inflater.inflate(R.layout.fragment_sector_map, container, false);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        mNaverMap = naverMap;
        if (mNaverMap == null) {
            return;
        }
        mNaverMap.setLocationSource(locationSource);
        mNaverMap.setIndoorEnabled(true);

        UiSettings uiSettings = mNaverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        // CompassView를 NaverMap에 바인딩
        CompassView compassView = getView().findViewById(R.id.compass);
        uiSettings.setCompassEnabled(false); // 기본 나침반 비활성화
        compassView.setMap(naverMap); // 커스텀 나침반 설정

        // 권한 확인, 결과는 onRequestPermissionResult 콜백 메서드 호출
        ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, PERMISSION_REQUEST_CODE);

    }

    private LatLng computeCentroid() {
        double centerX = 0, centerY = 0;
        for (LatLng point : polygonPoints) {
            centerX += point.longitude;
            centerY += point.latitude;
        }
        return new LatLng(centerY / polygonPoints.size(), centerX / polygonPoints.size());
    }

    private void sortPointsCounterClockwise() {
        LatLng centroid = computeCentroid();
        Collections.sort(polygonPoints, new Comparator<LatLng>() {
            @Override
            public int compare(LatLng a, LatLng b) {
                double angleA = Math.atan2(a.latitude - centroid.latitude, a.longitude - centroid.longitude);
                double angleB = Math.atan2(b.latitude - centroid.latitude, b.longitude - centroid.longitude);
                return Double.compare(angleA, angleB);
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sectorInquire();

        RadioButton safeZoneRadioButton = view.findViewById(R.id.safeZone);
        safeZoneRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    polygonPoints.clear();

                    for (Marker eraseMarker : redMarkerList) {
                        eraseMarker.setMap(null);
                    }

                    if (mNaverMap == null) {
                        return;
                    }

                    mNaverMap.setOnMapClickListener((point, coord) -> {

                        Marker marker = new Marker();
                        greenMarkerList.add(marker);
                        marker.setPosition(coord);
                        marker.setIcon(MarkerIcons.BLACK);
                        marker.setIconTintColor(Color.GREEN);
                        marker.setMap(mNaverMap);

                        polygonPoints.add(coord);
                        greenMarkerList.add(marker);

                        if (polygonPoints.size() == 4) {
                            sortPointsCounterClockwise(); // 점들을 정렬
                            PolygonOverlay polygonOverlay = new PolygonOverlay();
                            polygonOverlay.setCoords(polygonPoints);
                            polygonOverlay.setColor(Color.argb(75, 0, 100, 0));
                            polygonOverlay.setMap(mNaverMap);
                            Toast.makeText(getContext(), "안전구역이 지정되었습니다.", Toast.LENGTH_SHORT).show();

                            InfoWindow infoWindow = new InfoWindow();
                            infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getContext()) {
                                @NonNull
                                @Override
                                public CharSequence getText(@NonNull InfoWindow infoWindow) {
                                    return "안전구역 " + greenIndex;
                                }
                            });
                            infoWindow.open(marker);
                            infoWindow.setPosition(new LatLng(polygonPoints.get(2).latitude, polygonPoints.get(2).longitude));
                            infoWindow.open(mNaverMap);
                            greenInfoWindowList.put(greenIndex, infoWindow);

                            for (Marker eraseMarker : greenMarkerList) {
                                eraseMarker.setMap(null);
                            }
                            greenPolygonOverlays.put(greenIndex, polygonOverlay);

                            //retrofit 데이터 전송
                            double xOfPointA, xOfPointB, xOfPointC, xOfPointD, yOfPointA, yOfPointB, yOfPointC, yOfPointD;

                            xOfPointA = polygonPoints.get(0).longitude;
                            yOfPointA = polygonPoints.get(0).latitude;
                            xOfPointB = polygonPoints.get(1).longitude;
                            yOfPointB = polygonPoints.get(1).latitude;
                            xOfPointC = polygonPoints.get(2).longitude;
                            yOfPointC = polygonPoints.get(2).latitude;
                            xOfPointD = polygonPoints.get(3).longitude;
                            yOfPointD = polygonPoints.get(3).latitude;

                            SafeSectorRequest SafeMapDTO = new SafeSectorRequest(xOfPointA, yOfPointA, xOfPointB, yOfPointB,
                                    xOfPointC, yOfPointC, xOfPointD, yOfPointD, childName);
                            Gson gson = new Gson();
                            String mapInfo = gson.toJson(SafeMapDTO);

                            Log.e("JSON", mapInfo);

                            Call<ResponseBody> call = userRetrofitInterface.sectorSafe(SafeMapDTO);
                            call.clone().enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    if (response.isSuccessful()) {
                                        Log.e("POST", "전달 성공");
                                        // 응답 본문 로그 추가
                                        try {
                                            String responseBody = response.body().string();
                                            Log.e("POST", "Response Body: " + responseBody);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Log.e("POST", "전달 실패, HTTP Status: " + response.code());
                                        Toast.makeText(getContext(), "서버와 통신에 실패하였습니다. 구역을 다시 지정해주세요.", Toast.LENGTH_SHORT).show();
                                        try {
                                            String responseBody = response.errorBody().string();
                                            Log.e("POST", "Error Body: " + responseBody);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Log.e("POST", "통신 실패", t);
                                }
                            });


                            greenIndex++;
                            polygonPoints.clear();
                            greenMarkerList.clear();
                        }
                    });
                }
            }
        });

        RadioButton dangerZoneRadioButton = view.findViewById(R.id.dangerZone);
        dangerZoneRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    polygonPoints.clear();

                    for (Marker eraseMarker : greenMarkerList) {
                        eraseMarker.setMap(null);
                    }

                    if (mNaverMap == null) {
                        return;
                    }

                    mNaverMap.setOnMapClickListener((point, coord) -> {
                        Marker marker = new Marker();

                        redMarkerList.add(marker);

                        marker.setPosition(coord);
                        marker.setIcon(MarkerIcons.BLACK);
                        marker.setIconTintColor(Color.RED);
                        marker.setMap(mNaverMap);

                        polygonPoints.add(coord);

                        if (polygonPoints.size() == 4) {
                            sortPointsCounterClockwise(); // 점들을 정렬
                            PolygonOverlay polygonOverlay = new PolygonOverlay();
                            polygonOverlay.setCoords(polygonPoints);
                            polygonOverlay.setColor(Color.argb(75, 100, 0, 0));
                            polygonOverlay.setMap(mNaverMap);
                            Toast.makeText(getContext(), "위험구역이 지정되었습니다.", Toast.LENGTH_SHORT).show();

                            InfoWindow infoWindow = new InfoWindow();
                            infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getContext()) {
                                @NonNull
                                @Override
                                public CharSequence getText(@NonNull InfoWindow infoWindow) {
                                    return "위험구역 " + redIndex;
                                }
                            });
                            infoWindow.open(marker);
                            infoWindow.setPosition(new LatLng(polygonPoints.get(2).latitude, polygonPoints.get(2).longitude));
                            infoWindow.open(mNaverMap);
                            redInfoWindowList.put(redIndex, infoWindow);

                            for (Marker eraseMarker : redMarkerList) {
                                eraseMarker.setMap(null);
                            }
                            redPolygonOverlays.put(redIndex, polygonOverlay);

                            //retrofit 데이터 전송
                            double xOfPointA, xOfPointB, xOfPointC, xOfPointD, yOfPointA, yOfPointB, yOfPointC, yOfPointD;

                            xOfPointA = polygonPoints.get(0).longitude;
                            yOfPointA = polygonPoints.get(0).latitude;
                            xOfPointB = polygonPoints.get(1).longitude;
                            yOfPointB = polygonPoints.get(1).latitude;
                            xOfPointC = polygonPoints.get(2).longitude;
                            yOfPointC = polygonPoints.get(2).latitude;
                            xOfPointD = polygonPoints.get(3).longitude;
                            yOfPointD = polygonPoints.get(3).latitude;

                            DangerSectorRequest DangerMapDTO = new DangerSectorRequest(xOfPointA, yOfPointA, xOfPointB, yOfPointB,
                                    xOfPointC, yOfPointC, xOfPointD, yOfPointD, childName);
                            Gson gson = new Gson();
                            String mapInfo = gson.toJson(DangerMapDTO);

                            Log.e("JSON", mapInfo);

                            Call<ResponseBody> call = userRetrofitInterface.sectorDanger(DangerMapDTO);
                            call.clone().enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    if (response.isSuccessful()) {
                                        Log.e("POST", "전달 성공");
                                        // 응답 본문 로그 추가
                                        try {
                                            String responseBody = response.body().string();
                                            Log.e("POST", "Response Body: " + responseBody);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Log.e("POST", "전달 실패, HTTP Status: " + response.code());
                                        Toast.makeText(getContext(), "서버와 통신에 실패하였습니다. 구역을 다시 지정해주세요.", Toast.LENGTH_SHORT).show();
                                        try {
                                            String responseBody = response.errorBody().string();
                                            Log.e("POST", "Error Body: " + responseBody);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Log.e("POST", "통신 실패", t);
                                }
                            });

                            redIndex++;
                            polygonPoints.clear();
                            redMarkerList.clear();
                        }
                    });
                }
            }
        });

        Button greenSectorDeleteBtn = view.findViewById(R.id.green_sector_delete_btn);
        greenSectorDeleteBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getContext(), v);
                popupMenu.getMenuInflater().inflate(R.menu.green_polygon_overlay_menu, popupMenu.getMenu());

                // LinkedHashMap의 키 목록을 가져옴
                List<String> keys = new ArrayList<>(sectorPolygons.keySet());
                Log.e("POST", keys.get(0));
                Log.e("POST", keys.get(1));
                // keys가 비어있지 않을 때 메뉴에 항목 추가
                if (keys != null && !keys.isEmpty()) {
                    for (int i = 0; i < keys.size(); i++) {
                        String key = keys.get(i);
                        Log.e("POST", "Key: " + key); // 각 key를 로그에 출력
                        if (sectorPolygons.containsKey(key)) {
                            // 고유한 ID 생성
                            popupMenu.getMenu().add(Menu.NONE, i, Menu.NONE, "안전구역 " + key);
                        }
                    }
                } else {
                    Log.e("POST", "No keys found in sectorPolygons.");
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int index = item.getItemId(); // 메뉴 항목의 인덱스 가져오기
                        String selectedKey = keys.get(index); // 인덱스를 사용하여 키 가져오기

                        // 지도에서 PolygonOverlay 제거
                        if (sectorPolygons.containsKey(selectedKey)) {
                            sectorPolygons.get(selectedKey).setMap(null);
                            sectorPolygons.remove(selectedKey);
                        }

                        // 지도에서 InfoWindow 제거
                        if (greenInfoWindowList.containsKey(selectedKey)) {
                            greenInfoWindowList.get(selectedKey).close();
                            greenInfoWindowList.remove(selectedKey);
                        }

                        Log.e("POST", "Removed key: " + selectedKey); // 삭제된 키를 로그에 출력
                        return true;
                    }
                });

                popupMenu.show();
            }
        });



        Button redSectorDeleteBtn = view.findViewById(R.id.red_sector_delete_btn);
        redSectorDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getContext(), v);
                popupMenu.getMenuInflater().inflate(R.menu.red_polygon_overlay_menu, popupMenu.getMenu());

                for (int i = 0; i < redIndex; i++) {
                    if (redPolygonOverlays.size() != 0) {
                        if (redPolygonOverlays.containsKey(i)) {
                            popupMenu.getMenu().add(Menu.NONE, i, Menu.NONE, "위험구역 " + i);
                        }
                    }
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int index = item.getItemId(); // 해당 PolygonOverlay를 선택하여 처리
                        redPolygonOverlays.get(index).setMap(null); // 지도에서 PolygonOverlay 제거
                        redPolygonOverlays.remove(index);
                        redInfoWindowList.get(index).close(); // 지도에서 infoWindow 닫기
                        redInfoWindowList.remove(index);
                        return true;
                    }
                });

                popupMenu.show();
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.containers, GroupSettingFragment.newInstance(currentGroupUuid, childName));
                transaction.commit();
            }
        });
    }

    private void sectorInquire() {
        final Gson gson = new Gson();

        // 요청 JSON 로그 출력
        SectorInquireRequest sectorInquireDTO = new SectorInquireRequest(childName);
        String requestJson = gson.toJson(sectorInquireDTO);
        Log.e("Request JSON", requestJson);

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
                        sectorPolygons.put(coordinateId, polygonOverlay);
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
}