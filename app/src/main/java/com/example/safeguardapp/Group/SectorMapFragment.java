package com.example.safeguardapp.Group;

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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.UserRetrofitInterface;
import com.google.gson.Gson;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PolygonOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    private List<PolygonOverlay> greenPolygonOverlays = new ArrayList<>();
    private List<PolygonOverlay> redPolygonOverlays = new ArrayList<>();
    private List<Marker> redMarkerList = new ArrayList<>();
    private List<Marker> greenMarkerList = new ArrayList<>();
    private List<InfoWindow> greenInfoWindowList = new ArrayList<>();
    private List<InfoWindow> redInfoWindowList = new ArrayList<>();
    private int greenIndex = 0;
    private int redIndex = 0;

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

        mapFragment.getMapAsync(this);

        return inflater.inflate(R.layout.fragment_sector_map, container, false);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        mNaverMap = naverMap;
        mNaverMap.setLocationSource(locationSource);
        mNaverMap.setIndoorEnabled(true);

        UiSettings uiSettings = mNaverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);
        mNaverMap.getUiSettings().setLocationButtonEnabled(true);

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

        RadioButton safeZoneRadioButton = view.findViewById(R.id.safeZone);
        safeZoneRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    polygonPoints.clear();

                    for (Marker eraseMarker : redMarkerList) {
                        eraseMarker.setMap(null);
                    }

                    mNaverMap.setOnMapLongClickListener((point, coord) -> {

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
                            greenInfoWindowList.add(infoWindow);

                            for(Marker eraseMarker: greenMarkerList){
                                eraseMarker.setMap(null);
                            }
                            greenPolygonOverlays.add(polygonOverlay);

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

                            retrofitClient = RetrofitClient.getInstance();
                            userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

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

                    mNaverMap.setOnMapLongClickListener((point, coord) -> {
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
                            redInfoWindowList.add(infoWindow);

                            for(Marker eraseMarker: redMarkerList){
                                eraseMarker.setMap(null);
                            }
                            redPolygonOverlays.add(polygonOverlay);

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

                            retrofitClient = RetrofitClient.getInstance();
                            userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

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

                for(int i = 0; i < greenPolygonOverlays.size(); i++){
                    popupMenu.getMenu().add(Menu.NONE, i, Menu.NONE, "안전구역 " + i);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int index = item.getItemId(); // 해당 PolygonOverlay를 선택하여 처리
                        greenPolygonOverlays.get(index).setMap(null); // Polygon 제거
                        greenInfoWindowList.get(index).close(); // 지도에서 infoWindow 닫기
//                        greenInfoWindowList.remove(index);
                        popupMenu.getMenu().removeItem(index); // 메뉴에서 항목 제거
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

                for(int i = 0; i < redPolygonOverlays.size(); i++){
                    popupMenu.getMenu().add(Menu.NONE, i, Menu.NONE, "위험구역 " + i);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int index = item.getItemId(); // 해당 PolygonOverlay를 선택하여 처리
                        redPolygonOverlays.get(index).setMap(null); // Polygon 제거
                        redInfoWindowList.get(index).close(); // 지도에서 infoWindow 닫기
//                        redInfoWindowList.remove(index);
                        popupMenu.getMenu().removeItem(index); // 메뉴에서 항목 제거

                        return true;
                    }
                });

                popupMenu.show();
            }

            // PolygonOverlay 제거 시 호출되는 메서드
            private void removePolygonOverlay(int index) {
                redPolygonOverlays.get(index).setMap(null); // PolygonOverlay 제거하는 코드
                redPolygonOverlays.remove(index);
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
}