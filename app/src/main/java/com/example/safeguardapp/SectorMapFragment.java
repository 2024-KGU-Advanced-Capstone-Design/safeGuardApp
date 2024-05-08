package com.example.safeguardapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PolygonOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import java.util.ArrayList;
import java.util.List;

public class SectorMapFragment extends Fragment implements OnMapReadyCallback{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private NaverMap mNaverMap;

    // 4개 좌표의 위도, 경도 값 저장하는 리스트
    private List<LatLng> polygonPoints = new ArrayList<>();

    public SectorMapFragment() {
        // Required empty public constructor
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
        // Inflate the layout for this fragment
        // MapFragment 추가
        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.sectorMapScreen);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getChildFragmentManager().beginTransaction().add(R.id.sectorMapScreen, mapFragment).commit();
        }

        // MapFragment에서 네이버지도 설정
        mapFragment.getMapAsync(this);
        return inflater.inflate(R.layout.fragment_sector_map, container, false);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        mNaverMap = naverMap;
        mNaverMap.setLocationSource(locationSource);
        mNaverMap.setIndoorEnabled(true);

        mNaverMap.setOnMapLongClickListener((point, coord) -> {
            double latitude = coord.latitude;
            double longitude = coord.longitude;

            Marker marker = new Marker();
            marker.setPosition(new LatLng(latitude, longitude));
            marker.setIcon(MarkerIcons.BLACK);
            marker.setIconTintColor(Color.RED);
            marker.setWidth(Marker.SIZE_AUTO);
            marker.setHeight(Marker.SIZE_AUTO);
            marker.setMap(mNaverMap);

            // PolygonOverlay에 추가할 점을 리스트에 추가합니다.
            polygonPoints.add(new LatLng(latitude, longitude));

            // 사용자가 4개의 좌표를 입력하면 polygon을 생성하고 지도에 표시합니다.
            if (polygonPoints.size() == 4) {
                PolygonOverlay polygonOverlay = new PolygonOverlay();
                polygonOverlay.setCoords(polygonPoints);
                polygonOverlay.setColor(Color.argb(75, 100, 0, 0));
                polygonOverlay.setMap(mNaverMap);
                Toast.makeText(getContext(), "위험구역이 지정되었습니다.", Toast.LENGTH_SHORT).show();

                // 다음을 위해 리스트를 초기화합니다.
                polygonPoints.clear();
            }
        });

        // 네이버지도 UI 설정
        UiSettings uiSettings = mNaverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        // request code와 권한 획득 여부 확인
//        if(requestCode == PERMISSION_REQUEST_CODE){
//            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
//            }
//        }
//    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // SectorMapFragment에서 뒤로 갔을 때 SettingFragment로 이동
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 뒤로 가기 시 실행되는 코드
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                // 이동 시에는 이미 생성된 settingFragment를 사용하여 교체
                transaction.replace(R.id.containers, ((MainActivity) requireActivity()).settingFragment);
                transaction.commit();

                BottomNavigationView navigationView = requireActivity().findViewById(R.id.bottom_navigationview);
                navigationView.setSelectedItemId(R.id.setting);
            }
        });
    }
}