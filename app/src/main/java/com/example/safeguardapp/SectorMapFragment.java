package com.example.safeguardapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.model.Marker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.PolygonOverlay;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.Arrays;

public class SectorMapFragment extends Fragment implements OnMapReadyCallback{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private NaverMap mNaverMap;
//    private static final int PERMISSION_REQUEST_CODE = 100;
//    private static final String[] PERMISSIONS = {
//            android.Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//    };

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

        mNaverMap.setOnMapLongClickListener((point, coord) ->
                Toast.makeText(getContext(), coord.latitude + ", " + coord.longitude, Toast.LENGTH_SHORT).show());

//        Marker marker = new Marker();
//        mNaverMap.setOnMapLongClickListener((point, coord) ->
//                marker.setPosition(new LatLng(coord.latitude, coord.longitude)));
//        marker.setMap(mNaverMap);

        CircleOverlay circleOverlay = new CircleOverlay();
        circleOverlay.setCenter(new LatLng(37.5182, 126.9077));
        circleOverlay.setRadius(150);
        circleOverlay.setColor(Color.argb(75, 100, 0 , 0));
        circleOverlay.setMap(naverMap);

        PolygonOverlay polygonOverlay = new PolygonOverlay();
        polygonOverlay.setCoords(Arrays.asList(
                new LatLng(37.515594, 126.902706),
                new LatLng(37.515320, 126.903029),
                new LatLng(37.515929, 126.904795),
                new LatLng(37.516353, 126.904509)
        ));
        polygonOverlay.setColor(Color.argb(75, 100, 0, 0));
        polygonOverlay.setMap(naverMap);

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