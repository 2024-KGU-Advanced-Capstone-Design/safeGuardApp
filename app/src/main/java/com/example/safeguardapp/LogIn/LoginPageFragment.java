package com.example.safeguardapp.LogIn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.safeguardapp.FindID.FindIDFragment;
import com.example.safeguardapp.FindPW.FindPWCertificationFragment;
import com.example.safeguardapp.MainActivity;
import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.SignUp.SignUpFragment;
import com.example.safeguardapp.StartScreenActivity;
import com.example.safeguardapp.UserRetrofitInterface;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginPageFragment extends Fragment {
    public static String saveID;
    private StartScreenActivity startScreenActivity;
    private RetrofitClient retrofitClient;
    private UserRetrofitInterface userRetrofitInterface;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return inflater.inflate(R.layout.fragment_login_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText editTextID = view.findViewById(R.id.editTextID);
        EditText editTextPW = view.findViewById(R.id.editTextPW);
        TextView checkIDPW = view.findViewById(R.id.X_IDPW);
        CheckBox checkBox = view.findViewById(R.id.autoLogin);

        // 로그인 버튼에 대한 클릭 이벤트 처리
        view.findViewById(R.id.buttonLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = editTextID.getText().toString();
                String pw = editTextPW.getText().toString();

                LoginResponse(id,pw,checkIDPW);
            }
        });

        // 회원가입 버튼에 대한 클릭 이벤트 처리
        view.findViewById(R.id.buttonRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 회원가입 버튼을 클릭했을 때 fragmentSignUp로 화면 전환
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.start_activity, new SignUpFragment());
                transaction.commit();
            }
        });

        // 아이디 찾기 버튼에 대한 클릭 이벤트 처리
        view.findViewById(R.id.buttonFindID).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.start_activity, new FindIDFragment());
                transaction.commit();
            }
        });

        view.findViewById(R.id.buttonFindPW).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.start_activity, new FindPWCertificationFragment());
                transaction.commit();
            }
        });
    }

    public void LoginResponse(String id, String pw, TextView CheckIDPW){
        String loginType = "Member";

        //loginRequest에 id,pw 저장
        LoginRequest loginRequest = new LoginRequest(id,pw,loginType);
        LoginInfo.setLoginID(id);

        //retrofit 생성
        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

        Log.e("POST","client 생성");

        Gson gson = new Gson();
        String userInfo = gson.toJson(loginRequest);

        Log.e("JSON",userInfo);

        Call<LoginResponse> call = userRetrofitInterface.login(loginRequest);
        call.clone().enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                //통신 성공
                if(response.isSuccessful()){
                    Log.e("POST","통신 성공");
                    LoginResponse result = response.body();
                    String resultCode = result.getResultCode();

                    Log.e("POST",resultCode);

                    if(response.body()!=null) {
                        String success = "200"; //성공
                        if (resultCode.equals(success)) {
                            Log.e("POST", "로그인 성공");

                            saveID = id;

                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                        }
                    }
                }
                else{
                    Log.e("POST","통신 실패");
                    CheckIDPW.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("POST","에러");
            }
        });

    }
    //자동 로그인 유저
    public void checkAutoLogin(){
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }


}
