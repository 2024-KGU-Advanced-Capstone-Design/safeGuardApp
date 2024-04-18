package com.example.safeguardapp;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 비밀번호 변경 버튼에 대한 클릭 이벤트 처리
        view.findViewById(R.id.changePW_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // 위험구역 및 안전구역 설정 버튼에 대한 클릭 이벤트 처리
        view.findViewById(R.id.selectZone_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // 내 정보 수정 버튼에 대한 클릭 이벤트 처리
        view.findViewById(R.id.editMyInfo_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 뒤로 가기 시 실행되는 코드
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                // 이동 시에는 이미 생성된 mapFragment를 사용하여 교체
                transaction.replace(R.id.containers, ((MainActivity) requireActivity()).mapFragment);
                transaction.commit();

                BottomNavigationView navigationView = requireActivity().findViewById(R.id.bottom_navigationview);
                navigationView.setSelectedItemId(R.id.map);
            }
        });
    }
}