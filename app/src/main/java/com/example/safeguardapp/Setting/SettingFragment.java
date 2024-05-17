package com.example.safeguardapp.Setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.safeguardapp.Group.MemberWithdrawRequest;
import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.MainActivity;
import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.StartScreenActivity;
import com.example.safeguardapp.UserRetrofitInterface;
import com.google.gson.Gson;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingFragment extends Fragment {

    private Button changeName, changePW, logout, withdraw;
    private RetrofitClient retrofitClient;
    private UserRetrofitInterface userRetrofitInterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        initializeView(view);
        setupListeners();

        return view;
    }

    private void initializeView(View view){
        changeName = view.findViewById(R.id.editName_btn);
        changePW = view.findViewById(R.id.changePW_btn);
        logout = view.findViewById(R.id.logout_btn);
        withdraw = view.findViewById(R.id.withdraw_btn);

        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();
    }

    private void setupListeners(){
        changePW.setOnClickListener(v -> changePWMethod());
        logout.setOnClickListener(v -> logoutMethod());
        withdraw.setOnClickListener(v -> withdrawMethod());
    }

    private void changePWMethod(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.containers, new ChangePWFragment());

        fragmentTransaction.commit();
    }

    private void logoutMethod(){
        new AlertDialog.Builder(requireContext())
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        transScreen();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void withdrawMethod(){
        new AlertDialog.Builder(requireContext())
                .setTitle("회원탈퇴")
                .setMessage("정말로 회원 탈퇴 하시겠습니까?\n탈퇴 후 복구는 불가능합니다" )
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userID = LoginPageFragment.saveID;
                        MemberWithdrawRequest withdrawDTO = new MemberWithdrawRequest(userID);
                        Gson gson = new Gson();
                        String userInfo = gson.toJson(withdrawDTO);

                        Log.e("JSON", userInfo);

                        Call<ResponseBody> call = userRetrofitInterface.withdrawMember(withdrawDTO);
                        call.clone().enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if(response.isSuccessful()){
                                    Log.e("POST", "탈퇴 성공");
                                    transScreen();
                                } else {
                                    Log.e("POST", "Server Response: " + response.code() + " " + response.message());
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.e("POST", "not success");
                                Toast.makeText(getContext(), "탈퇴에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void transScreen(){
        Intent intent = new Intent(getActivity(), StartScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // SettingFragment에서 뒤로 갔을 때 MapFragment로 이동
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 뒤로 가기 시 실행되는 코드
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                // 이동 시에는 이미 생성된 mapFragment를 사용하여 교체
                transaction.replace(R.id.containers, ((MainActivity) requireActivity()).mapFragment);
                transaction.commit();

            }
        });
    }
}