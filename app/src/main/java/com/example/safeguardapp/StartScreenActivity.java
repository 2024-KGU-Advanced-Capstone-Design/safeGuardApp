package com.example.safeguardapp;

import static android.app.PendingIntent.getActivity;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.safeguardapp.Child.ChildMainActivity;
import com.example.safeguardapp.LogIn.LoginInfo;
import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.LogIn.LoginRequest;
import com.example.safeguardapp.LogIn.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StartScreenActivity extends AppCompatActivity {
    String loginID, loginPW, loginType;
    private RetrofitClient retrofitClient;
    private UserRetrofitInterface userRetrofitInterface;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        SharedPreferences auto = getSharedPreferences("appdata", Activity.MODE_PRIVATE);

        loginID = auto.getString("inputID", null);
        loginPW = auto.getString("inputPW", null);
        loginType = auto.getString("loginType", null);

        if (loginID != null && loginPW != null && loginType != null) {
            LoginRequest loginRequest = new LoginRequest(loginID, loginPW, loginType);
            LoginInfo.setLoginID(loginID);

            //retrofit 생성
            retrofitClient = RetrofitClient.getInstance();
            userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

            Call<LoginResponse> call = userRetrofitInterface.login(loginRequest);
            call.clone().enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    //통신 성공
                    if (response.isSuccessful()) {
                        Log.e("POST", "통신 성공");
                        LoginResponse result = response.body();
                        String resultCode = result.getResultCode();

                        Log.e("POST", resultCode);

                        if (response.body() != null) {
                            String success = "200"; //성공
                            if (resultCode.equals(success)) {
                                Log.e("POST", "로그인 성공");

                                LoginPageFragment.saveID = loginID;

                                if (loginType.equals("Member")) {
                                    Toast.makeText(StartScreenActivity.this, "자동 로그인 성공", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(StartScreenActivity.this, MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(StartScreenActivity.this, "자동 로그인 성공", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(StartScreenActivity.this, ChildMainActivity.class);
                                    startActivity(intent);
                                }

                            }
                        }
                    } else
                        Toast.makeText(StartScreenActivity.this, "자동 로그인 실패", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Log.e("POST", "에러");
                }
            });
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.start_activity, new LoginPageFragment());
            transaction.commit();
        }
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity(); // 현재 액티비티와 관련된 모든 액티비티를 종료
            }
        });
    }
}