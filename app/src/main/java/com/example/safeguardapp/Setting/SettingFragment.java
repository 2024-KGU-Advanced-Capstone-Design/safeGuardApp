package com.example.safeguardapp.Setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.safeguardapp.MainActivity;
import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.UserRetrofitInterface;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private RetrofitClient retrofitClient;
    private UserRetrofitInterface userRetrofitInterface;

    public SettingFragment() {
        // Required empty public constructor
    }

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
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.containers, new ChangePWFragment());

                fragmentTransaction.commit();

            }
        });

        // 위험구역 및 안전구역 설정 버튼에 대한 클릭 이벤트 처리
        view.findViewById(R.id.selectZone_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SectorMapFragment sectorMapFragment = new SectorMapFragment();
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.containers, sectorMapFragment);
                fragmentTransaction.commit();
            }
        });

        // 내 정보 수정 버튼에 대한 클릭 이벤트 처리
        view.findViewById(R.id.editName_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        // 로그아웃 버튼에 대한 클릭 이벤트 처리
        view.findViewById(R.id.logout_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("로그아웃")
                        .setMessage("로그아웃 하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                String token = "";
//                                LogoutResponse(token);
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
            }
        });

        // SettingFragment에서 뒤로 갔을 때 MapFragment로 이동
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
/*
    public void LogoutResponse(String token) {
        String logout = "logout";
        String logoutType = "Member";

        LogoutRequest logoutRequest = new LogoutRequest(token);

        retrofitClient = RetrofitClient.getInstance(logout);
        userRetrofitInterface = RetrofitClient.getInstance(logout).getUserRetrofitInterface();

        Log.e("POST","client 생성");

        Gson gson = new Gson();
        String userInfo = gson.toJson(logoutRequest);

        Call<LogoutResponse> call = userRetrofitInterface.logout(logoutRequest);
        call.clone().enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(retrofit2.Call<LoginResponse> call, Response<LoginResponse> response) {
                //통신 성공
                if(response.isSuccessful()){
                    Log.e("POST","통신 성공");
                    LoginResponse result = response.body();
                    String resultCode = result.getResultCode();

                    Log.e("POST",resultCode);

                    if(response.body()!=null) {
                        String success = "200"; //성공
                        if (resultCode.equals(success)) {
                            Log.e("POST", "로그아웃 성공");
                            Intent intent = new Intent(getActivity(), startScreenActivity.class);
                            startActivity(intent);
                        }
                    }
                }
                else{
                    Log.e("POST","통신 실패");
                }
            }
            @Override
            public void onFailure(retrofit2.Call<LoginResponse> call, Throwable t) {
                Log.e("POST","에러");
            }
        });
    }
    */
}