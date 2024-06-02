package com.example.safeguardapp.Setting;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.safeguardapp.Group.GroupSettingFragment;
import com.example.safeguardapp.Group.MemberWithdrawRequest;
import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.MainActivity;
import com.example.safeguardapp.PreferenceManager;
import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.StartScreenActivity;
import com.example.safeguardapp.UserRetrofitInterface;
import com.example.safeguardapp.data.repository.GroupRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.checkerframework.checker.units.qual.C;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingFragment extends Fragment {

    private Button changeName, changePW, logout, withdraw;
    private CircleImageView choiceImage;
    private String newNickname;
    private RetrofitClient retrofitClient;
    private UserRetrofitInterface userRetrofitInterface;
    private static final int REQUEST_IMAGE_SELECT = 1;


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
        choiceImage = view.findViewById(R.id.imageView);

        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();
    }

    private void setupListeners(){
        changeName.setOnClickListener(v -> changeNameMethod());
        changePW.setOnClickListener(v -> changePWMethod());
        logout.setOnClickListener(v -> logoutMethod());
        withdraw.setOnClickListener(v -> withdrawMethod());
        choiceImage.setOnClickListener(v -> choiceImageMethod());
    }

    private void changeNameMethod(){
        LayoutInflater inflater2 = getLayoutInflater();
        View dialogView2 = inflater2.inflate(R.layout.edit_update_nickname, null);

        EditText boxInDialog2 = dialogView2.findViewById(R.id.nickname_editText);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("작성할 내용을 입력해주세요.")
                .setView(dialogView2) // 커스텀 레이아웃 설정
                .setPositiveButton("확인", (dialog, which) -> {

                    // OK 버튼 클릭 시 처리할 코드
                    newNickname = boxInDialog2.getText().toString();
                    nicknameChange();
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    // Cancel 버튼 클릭 시 처리할 코드
                    dialog.dismiss();
                });

        // 다이얼로그 표시
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void nicknameChange(){
        ChangeNicknameRequest changeNicknameRequest = new ChangeNicknameRequest(LoginPageFragment.saveID, newNickname);
        Call<ResponseBody> call = userRetrofitInterface.changeNickname(changeNicknameRequest);

        call.clone().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    Toast.makeText(getContext(), "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "통신 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changePWMethod(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top, R.anim.slide_in_top, R.anim.slide_out_bottom);
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
                        GroupSettingFragment fragmentB = (GroupSettingFragment) getFragmentManager().findFragmentById(R.id.group_activity);
                        if (fragmentB != null) {
                            fragmentB.clearAideGroup();
                        }
                        PreferenceManager.clear(getContext());
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
                                    PreferenceManager.clear(getContext());
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

    private void choiceImageMethod(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_SELECT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_SELECT && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                CropImage.activity(selectedImageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .setAspectRatio(1, 1)
                        .start(getContext(), this);
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                // 자른 이미지 결과를 사용하여 추가 작업 수행
                choiceImage.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                // 오류 처리
                Toast.makeText(getContext(), "이미지 자르기 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void transScreen(){
        Intent intent = new Intent(getActivity(), StartScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView IDtextView = view.findViewById(R.id.memberId);
        IDtextView.setText("아이디 : " + LoginPageFragment.saveID);
        TextView NicknameTextView = view.findViewById(R.id.memberNickname);

        ReturnNicknameRequest returnNicknameRequest = new ReturnNicknameRequest(LoginPageFragment.saveID);
        Call<ResponseBody> call = userRetrofitInterface.getNickname(returnNicknameRequest);

        call.clone().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful() && response.body() != null){
                    ResponseBody body = response.body();
                    try {
                        String nickname = body.string();
                        NicknameTextView.setText("닉네임 : " + nickname);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                NicknameTextView.setText("닉네임을 불러오지 못했습니다.");
            }
        });


        // SettingFragment에서 뒤로 갔을 때 MapFragment로 이동
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 뒤로 가기 시 실행되는 코드
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
                transaction.replace(R.id.containers, ((MainActivity) requireActivity()).mapFragment);
                transaction.commit();

                BottomNavigationView navigationView = requireActivity().findViewById(R.id.bottom_navigationview);
                navigationView.setSelectedItemId(R.id.map);
            }
        });

        LinearLayout linearLayout = view.findViewById(R.id.settingScreen);
        YoYo.with(Techniques.FadeIn).duration(700).repeat(0).playOn(linearLayout);
    }
}