package com.example.safeguardapp;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PolygonOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SectorMapFragment extends Fragment implements OnMapReadyCallback {
    RetrofitClient retrofitClient;
    UserRetrofitInterface userRetrofitInterface;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private NaverMap mNaverMap;

    private List<LatLng> polygonPoints = new ArrayList<>();

    private PolygonManager polygonManager = new PolygonManager();
    private List<PolygonOverlay> polygonOverlays = new ArrayList<>();
    private List<Marker> redMarkerList = new ArrayList<>();
    private List<Marker> greenMarkerList = new ArrayList<>();

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

        drawAllPolygons();
    }

    private void drawAllPolygons() {

        // PolygonManager에 저장된 모든 폴리곤을 맵에 그리기
        for (List<LatLng> polygon : polygonManager.getPolygons()) {
            PolygonOverlay polygonOverlay = new PolygonOverlay();
            polygonOverlay.setCoords(polygon);
            polygonOverlay.setColor(Color.argb(75, 0, 100, 0)); // 색상 설정
            polygonOverlay.setMap(mNaverMap);
        }
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

                    for(Marker eraseMarker: redMarkerList){
                        eraseMarker.setMap(null);
                    }

                    mNaverMap.setOnMapLongClickListener((point, coord) -> {
                        if (polygonPoints.size() < 4) {
                            Marker marker = new Marker();
                            greenMarkerList.add(marker);
                            marker.setPosition(coord);
                            marker.setIcon(MarkerIcons.BLACK);
                            marker.setIconTintColor(Color.GREEN);
                            marker.setMap(mNaverMap);

                            polygonPoints.add(coord);
                            greenMarkerList.add(marker);
                        }
                        if (polygonPoints.size() == 4) {
                            sortPointsCounterClockwise(); // 점들을 정렬
                            PolygonOverlay polygonOverlay = new PolygonOverlay();
                            polygonOverlay.setCoords(polygonPoints);
                            polygonOverlay.setColor(Color.argb(75, 0, 100, 0));
                            polygonOverlay.setMap(mNaverMap);
                            Toast.makeText(getContext(), "안전구역이 지정되었습니다.", Toast.LENGTH_SHORT).show();

                            PolygonManager polygonManager = new PolygonManager();
                            polygonManager.addPolygon(new ArrayList<>(polygonPoints));
                            drawAllPolygons();
                            //retrofit 데이터 전송
                            double getXA, getYA, getXB, getYB, getXC, getYC, getXD, getYD;
                            getXA = polygonPoints.get(0).longitude;
                            getYA = polygonPoints.get(0).latitude;
                            getXB = polygonPoints.get(1).longitude;
                            getYB = polygonPoints.get(1).latitude;
                            getXC = polygonPoints.get(2).longitude;
                            getYC = polygonPoints.get(2).latitude;
                            getXD = polygonPoints.get(3).longitude;
                            getYD = polygonPoints.get(3).latitude;

                            SectorMapRequest MapDTO = new SectorMapRequest(getXA, getYA, getXB, getYB,
                                    getXC, getYC, getXD, getYD);
                            Gson gson = new Gson();
                            String mapInfo = gson.toJson(MapDTO);

                            Log.e("JSON", mapInfo);

                            retrofitClient = RetrofitClient.getInstance();
                            userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

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

                    for(Marker eraseMarker: greenMarkerList){
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

                            polygonPoints.clear();
                            redMarkerList.clear();
                        }
                    });
                }
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.containers, ((MainActivity) requireActivity()).settingFragment);
                transaction.commit();

                BottomNavigationView navigationView = requireActivity().findViewById(R.id.bottom_navigationview);
                navigationView.setSelectedItemId(R.id.setting);
            }
        });
    }

    public class PolygonManager {
        private List<List<LatLng>> polygons = new ArrayList<>();
        private static final int MAX_POLYGONS = 20;

        public boolean addPolygon(List<LatLng> polygon) {
            if (polygon.size() != 4) {
                throw new IllegalArgumentException("Each polygon must exactly contain 4 points.");
            }
            if (polygons.size() < MAX_POLYGONS) {
                polygons.add(new ArrayList<>(polygon));
                logPolygonAdded(polygon);  // 로그 출력
                return true;
            } else {
                Log.e("PolygonManager", "Cannot add more polygons. Maximum limit reached.");
                return false; // Can't add more polygons
            }
        }

        private void logPolygonAdded(List<LatLng> polygon) {
            StringBuilder coordinatesLog = new StringBuilder("Polygon added with coordinates: ");
            for (LatLng coord : polygon) {
                coordinatesLog.append(String.format("[Lat: %.5f, Lng: %.5f] ", coord.latitude, coord.longitude));
            }
            Log.e("PolygonManager", coordinatesLog.toString());
        }

        public List<List<LatLng>> getPolygons() {
            return polygons;
        }
    }
}