package com.example.safeguardapp.FindPW;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.UserRetrofitInterface;
import com.google.android.material.appbar.MaterialToolbar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindPWFragment extends Fragment {

    private EditText resetPW, resetPW_re;
    private TextView X_pw1, X_pw2, X_pw_re, spacePW;
    private Button cancel_btn, resettingPW_btn;
    private Boolean isPasswordValid = false;
    private Boolean isPasswordValid2 = false;
    private Boolean isSpacePWValid = false;
    private RetrofitClient retrofitClient;
    private UserRetrofitInterface userRetrofitInterface;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_pw, container, false);

        initializeView(view);
        setupListeners();

        return view;
    }

    private void initializeView(View view) {
        resetPW = view.findViewById(R.id.resetPW);
        resetPW_re = view.findViewById(R.id.resetPW_re);
        X_pw1 = view.findViewById(R.id.X_PW);
        X_pw2 = view.findViewById(R.id.X_PW2);
        X_pw_re = view.findViewById(R.id.X_PW_re);
        cancel_btn = view.findViewById(R.id.cancel_btn);
        resettingPW_btn = view.findViewById(R.id.resettingPW_btn);
        spacePW = view.findViewById(R.id.X_space_PW);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> previous());
    }

    private void setupListeners() {
        //retrofit 생성
        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

        // 취소 버튼 클릭 시 비밀번호 찾기 인증 화면으로 전환
        cancel_btn.setOnClickListener(v -> previous());

        resettingPW_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ID = FindPWCertificationFragment.sendID;
                String newPW = resetPW.getText().toString();
                ResetPwRequest pwRequest = new ResetPwRequest(ID, newPW);

                Call<ResponseBody> call = userRetrofitInterface.resetPw(pwRequest);
                call.clone().enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.isSuccessful()) {
                            Toast.makeText(v.getContext(), "비밀번호가 변경되었습니다. 다시 로그인 해주세요", Toast.LENGTH_LONG).show();

                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.start_activity, new LoginPageFragment());
                            transaction.commit();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(v.getContext(), "통신 오류", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        resetPW.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                String password = editable.toString();
                if(password.contains(" ")) {
                    isSpacePWValid = false;
                    spacePW.setVisibility(View.VISIBLE);
                    X_pw1.setVisibility(View.INVISIBLE);
                    X_pw2.setVisibility(View.INVISIBLE);
                }
                else{
                    isSpacePWValid = true;
                    spacePW.setVisibility(View.INVISIBLE);

                    if (password.length() >= 8) {
                        X_pw1.setVisibility(View.INVISIBLE);
                        if (PasswordValidFunc(password)) {
                            X_pw2.setVisibility(View.INVISIBLE);
                            isPasswordValid = true;
                        } else {
                            X_pw2.setVisibility(View.VISIBLE);
                            isPasswordValid = false;
                        }
                    } else {
                        X_pw1.setVisibility(View.VISIBLE);
                        X_pw2.setVisibility(View.INVISIBLE);
                        isPasswordValid = false;

                    }
                }
                updateSignUpButtonState();

                // 비밀번호를 다시 입력할 때 비밀번호 재입력칸 알림 메시지 수정
                String pw = editable.toString();
                String pwRe = resetPW_re.getText().toString();
                isPasswordValid2 = pwRe.equals(pw) && pw.equals(pwRe);
                X_pw_re.setVisibility(isPasswordValid2 ? View.INVISIBLE : View.VISIBLE);
                updateSignUpButtonState();
            }

        });

        resetPW_re.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                String pw = resetPW.getText().toString();
                String pwRe = editable.toString();
                isPasswordValid2 = pwRe.equals(pw) && pw.equals(pwRe);
                X_pw_re.setVisibility(isPasswordValid2 ? View.INVISIBLE : View.VISIBLE);
                updateSignUpButtonState();
            }
        });
    }

    // 뒤로 가기 버튼 구현
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 뒤로 가기 시 실행되는 코드
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.start_activity, new FindPWCertificationFragment());
                transaction.commit();
            }
        });
    }

    private boolean PasswordValidFunc(String password) {
        return password.matches(".*[~!@#$%^&*?].*") && password.matches(".*[a-zA-Z].*") ||
                password.matches(".*[~!@#$%^&*?].*") && password.matches(".*\\d.*") ||
                password.matches(".*[a-zA-Z].*") && password.matches(".*\\d.*");
    }

    private void updateSignUpButtonState() {
        resettingPW_btn.setEnabled(isPasswordValid && isPasswordValid2 && isSpacePWValid);
        resettingPW_btn.setTextColor(isPasswordValid && isPasswordValid2 && isSpacePWValid ? Color.parseColor("#5E53A6") : Color.parseColor("#FFFFFF"));
    }

    private void previous(){
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.start_activity, new LoginPageFragment());
        transaction.commit();
    }
}

