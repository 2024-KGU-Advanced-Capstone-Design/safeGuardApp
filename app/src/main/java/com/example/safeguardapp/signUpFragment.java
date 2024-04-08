package com.example.safeguardapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


public class signUpFragment extends Fragment {
    private EditText inputName, inputId, inputEmail, inputPW, inputPW_re;
    private TextView O_email, X_email, X_PW, X_PW_re, X_PW2;
    private Button signUp_btn, cancel_btn;

    // 회원 가입 버튼 활성화 조건들의 변수 선언
    private boolean isEmailValid = false;
    private boolean isPasswordValid = false;
    private boolean isPasswordMatch = false;
    private boolean isNameValid = false;
    private boolean isIDValid = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        initializeViews(view);
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        inputName = view.findViewById(R.id.inputName);
        inputId = view.findViewById(R.id.inputId);
        inputEmail = view.findViewById(R.id.inputEmail);
        O_email = view.findViewById(R.id.O_email);
        X_email = view.findViewById(R.id.X_email);
        inputPW = view.findViewById(R.id.inputPW);
        X_PW = view.findViewById(R.id.X_PW);
        inputPW_re = view.findViewById(R.id.inputPW_re);
        X_PW_re = view.findViewById(R.id.X_PW_re);
        X_PW2 = view.findViewById(R.id.X_PW2);
        signUp_btn = view.findViewById(R.id.signUp_btn);
        cancel_btn = view.findViewById(R.id.cancel_btn);
    }

    private void setupListeners() {

        // 이름 입력 여부 확인
        inputName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                isNameValid = editable.length() > 0;
                updateSignUpButtonState();
            }
        });

        // ID 입력 여부 확인
        inputId.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                isIDValid = editable.length() > 0;
                updateSignUpButtonState();
            }
        });

        // 이메일 입력 시 조건 만족 확인 메서드
        inputEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                String email = editable.toString();
                isEmailValid = isValidEmail(email);
                X_email.setVisibility(isEmailValid ? View.INVISIBLE : View.VISIBLE);
                O_email.setVisibility(isEmailValid ? View.VISIBLE : View.INVISIBLE);
                updateSignUpButtonState();
            }
        });

        // 비밀번호 입력 시 조건 만족 확인 메서드
        inputPW.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                String password = editable.toString();
                if (password.length() >= 8){
                    X_PW.setVisibility(View.INVISIBLE);
                    if (PasswordValidFunc(password)){
                        X_PW2.setVisibility(View.INVISIBLE);
                        isPasswordValid = true;
                    }
                    else {
                        X_PW2.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    X_PW.setVisibility(View.VISIBLE);
                    X_PW2.setVisibility(View.INVISIBLE);
                }
                updateSignUpButtonState();
            }
        });

        // 비밀번호 재입력 시 조건 만족 확인 메서드
        inputPW_re.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                String pw = inputPW.getText().toString();
                String pwRe = editable.toString();
                isPasswordMatch = pw.equals(pwRe);
                X_PW_re.setVisibility(isPasswordMatch ? View.INVISIBLE : View.VISIBLE);
                updateSignUpButtonState();
            }
        });

        // 취소 버튼 클릭 시 로그인 화면으로 전환
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.activity_main, new loginPageFragment());
                transaction.commit();
            }
        });

        // 회원 가입 버튼 클릭 시 정보가 저장되며 로그인 화면으로 전환 -------- 미구현
        signUp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.activity_main, new loginPageFragment());
                transaction.commit();
            }
        });
    }

    // 회원 가입 버튼 활성화 및 비활성화 메서드
    private void updateSignUpButtonState() {
        signUp_btn.setEnabled(isEmailValid && isPasswordValid && isPasswordMatch && isNameValid && isIDValid);
        signUp_btn.setBackground(getResources().getDrawable(signUp_btn.isEnabled() ? R.drawable.signup_button_blue_version : R.drawable.signup_button_grey_version));
    }

    // 유효한 이메일 주소
    private boolean isValidEmail(String email) {
        return email.matches(".*@(naver\\.com|gmail\\.com|hanmail\\.net|daum\\.net|kyonggi\\.ac\\.kr)$");
    }

    // 비밀번호 입력 조건 (특수문자, 영문, 숫자 중 2가지 사용)
    private boolean PasswordValidFunc(String password) {
        return password.matches(".*[~!@#$%^&*?].*") && password.matches(".*[a-zA-Z].*") ||
                password.matches(".*[~!@#$%^&*?].*") && password.matches(".*\\d.*") ||
                password.matches(".*[a-zA-Z].*") && password.matches(".*\\d.*");
    }

    // TextWatcher 중복 제거
    private abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
    }

    // 뒤로가기 버튼 오류 해결 -> 뒤로 가기 눌렀을 때 어플이 꺼지는 것을 방지하고 이전 화면인 로그인 페이지로 넘어감
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 뒤로 가기 시 실행되는 코드
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.activity_main, new loginPageFragment());
                transaction.commit();
            }
        });
    }

}
