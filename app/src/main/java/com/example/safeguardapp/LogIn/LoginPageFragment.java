package com.example.safeguardapp.LogIn;

import android.app.Activity;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.safeguardapp.Child.ChildMainActivity;
import com.example.safeguardapp.FindID.FindIDFragment;
import com.example.safeguardapp.FindPW.FindPWCertificationFragment;
import com.example.safeguardapp.MainActivity;
import com.example.safeguardapp.PreferenceManager;
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
    private TextView checkIDPW;
    private EditText editTextID, editTextPW;
    private RadioButton isMember, isChild;
    private CheckBox checkBox;
    private ScrollView scrollView;
    private RetrofitClient retrofitClient;
    private UserRetrofitInterface userRetrofitInterface;

    private String loginType;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return inflater.inflate(R.layout.fragment_login_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences auto = getActivity().getSharedPreferences("auto", Activity.MODE_PRIVATE);
        SharedPreferences.Editor autoLogin = auto.edit();

        editTextID = view.findViewById(R.id.editTextID);
        editTextPW = view.findViewById(R.id.editTextPW);
        checkIDPW = view.findViewById(R.id.X_IDPW);
        isMember = view.findViewById(R.id.setMember);
        isChild =view.findViewById(R.id.setChild);
        checkBox = view.findViewById(R.id.autoLogin);
        scrollView = view.findViewById(R.id.login_scrollview);

        ImageView safeGuardLogoImageView = view.findViewById(R.id.SafeGuard_logo_image);
        YoYo.with(Techniques.FadeIn).duration(1500).repeat(0).playOn(safeGuardLogoImageView);

        ImageView safeGuardWordLogoImageView = view.findViewById(R.id.SafeGuard_word_logo_image);
        YoYo.with(Techniques.FadeIn).duration(1500).repeat(0).playOn(safeGuardWordLogoImageView);

        editTextID.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    scrollView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.smoothScrollTo(0, editTextID.getBottom());
                        }
                    }, 200);
                }
            }
        });

        editTextPW.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    scrollView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.smoothScrollTo(0, editTextPW.getBottom());
                        }
                    }, 200);
                }
            }
        });


        // 로그인 버튼에 대한 클릭 이벤트 처리
        view.findViewById(R.id.buttonLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = editTextID.getText().toString();
                String pw = editTextPW.getText().toString();
                if(isMember.isChecked()){
                    loginType="Member";
                }else loginType="Child";

                ToLogin(v,id,pw,getContext());

            }
        });

        // 회원가입 버튼에 대한 클릭 이벤트 처리
        view.findViewById(R.id.buttonRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 회원가입 버튼을 클릭했을 때 fragmentSignUp로 화면 전환
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top, R.anim.slide_in_top, R.anim.slide_out_bottom);
                transaction.replace(R.id.start_activity, new SignUpFragment());
                transaction.commit();
            }
        });

        // 아이디 찾기 버튼에 대한 클릭 이벤트 처리
        view.findViewById(R.id.buttonFindID).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top, R.anim.slide_in_top, R.anim.slide_out_bottom);
                transaction.replace(R.id.start_activity, new FindIDFragment());
                transaction.commit();
            }
        });

        view.findViewById(R.id.buttonFindPW).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top, R.anim.slide_in_top, R.anim.slide_out_bottom);
                transaction.replace(R.id.start_activity, new FindPWCertificationFragment());
                transaction.commit();
            }
        });
    }
    public void ToLogin(View v, String id, String pw, Context context){
        //loginRequest에 id,pw 저장
        LoginRequest loginRequest = new LoginRequest(id, pw, loginType, StartScreenActivity.token);
        LoginInfo.setLoginID(id);

        //retrofit 생성
        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

        Gson gson = new Gson();

        Call<LoginResponse> call = userRetrofitInterface.login(loginRequest);
        call.clone().enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                //통신 성공
                if(response.isSuccessful()){
                    LoginResponse result = response.body();
                    String resultCode = result.getResultCode();

                    if(response.body()!=null) {
                        String success = "200"; //성공
                        if (resultCode.equals(success)) {
                            Context context = getActivity();
                            if (context != null) {
                                SharedPreferences sharedPreferences = context.getSharedPreferences("loginID", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("saveID", id);
                                editor.putBoolean("autoLogin", checkBox.isChecked());
                                editor.apply();
                            } else {
                                Log.e("LoginPageFragment", "Context is null, cannot save ID");
                            }

                            saveID = id;
                            if(checkBox.isChecked()) PreferenceManager.setPreference(context,id,pw,loginType,StartScreenActivity.token);

                            if(loginType.equals("Member")) {
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                            }else{
                                Intent intent = new Intent(getActivity(), ChildMainActivity.class);
                                startActivity(intent);
                            }
                        }
                    }
                }
                else{
                    Log.e("POST","통신 실패");
                    checkIDPW.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("POST","에러");
            }
        });
    }
}

