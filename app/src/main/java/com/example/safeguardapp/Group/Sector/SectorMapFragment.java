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
import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.UserRetrofitInterface;
import com.google.gson.Gson;
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
    private Marker marker;

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
    private HashMap<String, PolygonOverlay> safeSectorPolygons = new HashMap<>();
    private HashMap<String, PolygonOverlay> dangerSectorPolygons = new HashMap<>();
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

                        marker = new Marker();
                        greenMarkerList.add(marker);
                        marker.setPosition(coord);
                        marker.setIcon(MarkerIcons.BLACK);
                        marker.setIconTintColor(Color.GREEN);
                        marker.setMap(mNaverMap);

                        polygonPoints.add(coord);
                        greenMarkerList.add(marker);

                        if (polygonPoints.size() == 4) {
                            for (Marker eraseMarker : greenMarkerList) {
                                eraseMarker.setMap(null);
                            }
                            greenMarkerList.clear();
                            sortPointsCounterClockwise(); // 점들을 정렬
                            PolygonOverlay polygonOverlay = new PolygonOverlay();
                            polygonOverlay.setCoords(polygonPoints);
                            polygonOverlay.setColor(Color.argb(75, 0, 100, 0));
                            polygonOverlay.setMap(mNaverMap);
                            Toast.makeText(getContext(), "안전구역이 지정되었습니다.", Toast.LENGTH_SHORT).show();


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
                                        sectorInquire();
                                        try {
                                            String responseBody = response.body().string();
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
                            polygonPoints.clear();
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
                            for (Marker eraseMarker : redMarkerList) {
                                eraseMarker.setMap(null);
                            }
                            redMarkerList.clear();
                            sortPointsCounterClockwise(); // 점들을 정렬
                            PolygonOverlay polygonOverlay = new PolygonOverlay();
                            polygonOverlay.setCoords(polygonPoints);
                            polygonOverlay.setColor(Color.argb(75, 100, 0, 0));
                            polygonOverlay.setMap(mNaverMap);
                            Toast.makeText(getContext(), "위험구역이 지정되었습니다.", Toast.LENGTH_SHORT).show();

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
                                        // 응답 본문 로그 추가
                                        sectorInquire();
                                        try {
                                            String responseBody = response.body().string();
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

                            polygonPoints.clear();
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
                List<String> keys = new ArrayList<>(safeSectorPolygons.keySet());

                // 키 목록이 비어 있지 않을 때만 메뉴에 항목 추가
                if (!keys.isEmpty()) {
                    for (int i = 0; i < keys.size(); i++) {
                        String key = keys.get(i);
                        if (safeSectorPolygons.containsKey(key)) {
                            popupMenu.getMenu().add(Menu.NONE, i, Menu.NONE, "안전구역 " + key);
                        }
                    }
                } else {
                    Log.e("POST", "No keys found in safeSectorPolygons.");
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int index = item.getItemId(); // 메뉴 항목의 인덱스 가져오기
                        if (index >= 0 && index < keys.size()) {
                            String selectedKey = keys.get(index); // 인덱스를 사용하여 키 가져오기

                            DeleteSectorRequest deleteSectorDTO = new DeleteSectorRequest(selectedKey, childName, LoginPageFragment.saveID);
                            Call<ResponseBody> call = userRetrofitInterface.deleteSector(deleteSectorDTO);

                            call.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    if (response.isSuccessful()) {
                                        // 지도에서 PolygonOverlay 제거
                                        if (safeSectorPolygons.containsKey(selectedKey)) {
                                            safeSectorPolygons.get(selectedKey).setMap(null);
                                            safeSectorPolygons.remove(selectedKey);
                                        }

                                        // 지도에서 InfoWindow 제거
                                        if (greenInfoWindowList.containsKey(selectedKey)) {
                                            greenInfoWindowList.get(selectedKey).close();
                                            greenInfoWindowList.remove(selectedKey);
                                        }
                                    } else {
                                        Log.e("POST", "Failed to delete sector on server." + response.code());
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Log.e("POST", "Failed to communicate with server: " + t.getMessage());
                                }
                            });

                            return true;
                        } else {
                            Log.e("POST", "Invalid index: " + index);
                            return false;
                        }
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

                // LinkedHashMap의 키 목록을 가져옴
                List<String> keys = new ArrayList<>(dangerSectorPolygons.keySet());

                // 키 목록이 비어 있지 않을 때만 메뉴에 항목 추가
                if (!keys.isEmpty()) {
                    for (int i = 0; i < keys.size(); i++) {
                        String key = keys.get(i);
                        if (dangerSectorPolygons.containsKey(key)) {
                            popupMenu.getMenu().add(Menu.NONE, i, Menu.NONE, "위험구역 " + key);
                        }
                    }
                } else {
                    Log.e("POST", "No keys found in dangerSectorPolygons.");
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int index = item.getItemId(); // 메뉴 항목의 인덱스 가져오기
                        if (index >= 0 && index < keys.size()) {
                            String selectedKey = keys.get(index); // 인덱스를 사용하여 키 가져오기

                            DeleteSectorRequest deleteSectorDTO = new DeleteSectorRequest(selectedKey, childName, LoginPageFragment.saveID);
                            Call<ResponseBody> call = userRetrofitInterface.deleteSector(deleteSectorDTO);

                            call.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    if (response.isSuccessful()) {
                                        // 지도에서 PolygonOverlay 제거
                                        if (dangerSectorPolygons.containsKey(selectedKey)) {
                                            dangerSectorPolygons.get(selectedKey).setMap(null);
                                            dangerSectorPolygons.remove(selectedKey);
                                        }

                                        // 지도에서 InfoWindow 제거
                                        if (redInfoWindowList.containsKey(selectedKey)) {
                                            redInfoWindowList.get(selectedKey).close();
                                            redInfoWindowList.remove(selectedKey);
                                        }
                                    } else {
                                        Log.e("POST", "Failed to delete sector on server." + response.code());
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Log.e("POST", "Failed to communicate with server: " + t.getMessage());
                                }
                            });

                            return true;
                        } else {
                            Log.e("POST", "Invalid index: " + index);
                            return false;
                        }
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
                        Log.e("SectorInquire", "JSON Parsing Error", e);
                        return;
                    }

                    if (sectors.isEmpty()) {
                        Log.e("SectorInquire", "No sectors found or sectorResponse is null");
                        return;
                    }

                    getActivity().runOnUiThread(() -> {
                        for (Map.Entry<String, SectorDetails> entry : sectors.entrySet()) {
                            String coordinateId = entry.getKey();
                            SectorDetails details = entry.getValue();
                            boolean isLiving = Boolean.parseBoolean(details.getIsLiving());

                            List<LatLng> polygonCoords = new ArrayList<>();
                            polygonCoords.add(new LatLng(details.getYofPointA(), details.getXofPointA()));
                            polygonCoords.add(new LatLng(details.getYofPointB(), details.getXofPointB()));
                            polygonCoords.add(new LatLng(details.getYofPointC(), details.getXofPointC()));
                            polygonCoords.add(new LatLng(details.getYofPointD(), details.getXofPointD()));

                            PolygonOverlay polygonOverlay = new PolygonOverlay();
                            polygonOverlay.setCoords(polygonCoords);

                            InfoWindow infoWindow = new InfoWindow();
                            infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getContext()) {
                                @NonNull
                                @Override
                                public CharSequence getText(@NonNull InfoWindow infoWindow) {
                                    return (isLiving ? "안전구역 " : "위험구역 ") + coordinateId;
                                }
                            });

                            if (isLiving) {
                                polygonOverlay.setColor(Color.argb(75, 0, 100, 0)); // 초록색
                                infoWindow.setPosition(new LatLng(details.getYofPointC(), details.getXofPointC()));
                                infoWindow.open(mNaverMap);
                                greenInfoWindowList.put(Integer.valueOf(coordinateId), infoWindow);
                                greenPolygonOverlays.put(Integer.valueOf(coordinateId), polygonOverlay);
                                safeSectorPolygons.put(coordinateId, polygonOverlay);
                            } else {
                                polygonOverlay.setColor(Color.argb(75, 100, 0, 0)); // 빨간색
                                infoWindow.setPosition(new LatLng(details.getYofPointC(), details.getXofPointC()));
                                infoWindow.open(mNaverMap);
                                redInfoWindowList.put(Integer.valueOf(coordinateId), infoWindow);
                                redPolygonOverlays.put(Integer.valueOf(coordinateId), polygonOverlay);
                                dangerSectorPolygons.put(coordinateId, polygonOverlay);
                            }

                            // 폴리곤을 지도에 추가
                            polygonOverlay.setMap(mNaverMap);
                        }
                    });

                } else {
                    Log.e("SectorInquire", "Response body is null or response unsuccessful");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("SectorInquire", "Request failed", t);
            }
        });
    }
}
