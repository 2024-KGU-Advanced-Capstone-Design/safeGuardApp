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
import java.util.regex.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.content.res.Configuration;


public class fragmentSignUp extends Fragment {
    private EditText inputName;
    private EditText inputEmail;
    private TextView O_email;
    private TextView X_email;
    private EditText inputPW;
    private TextView X_PW;
    private EditText inputPW_re;
    private TextView X_PW_re;
    private TextView X_PW2;
    private Button signUp_btn;
    private Button cancel_btn;

    // 각 조건에 대한 상태를 저장하는 변수 -> 회원가입 버튼 활성화
    private boolean isEmailValid = false;
    private boolean isPasswordValid = false;
    private boolean isPasswordMatch = false;
    private boolean isNameValid = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        inputName = view.findViewById(R.id.inputName); // 이름 입력 창
        inputEmail = view.findViewById(R.id.inputEmail); // 이메일 입력 창
        O_email = view.findViewById(R.id.O_email); // 이메일 주소 형식 옳바르지 않을 경우 visible
        X_email = view.findViewById(R.id.X_email); // 이메일 주소 사용 가능할 경우 visible
        inputPW = view.findViewById(R.id.inputPW); // 비밀번호 입력 창
        X_PW = view.findViewById(R.id.X_PW); // 비밀번호 8자리 이하일 경우 visible
        inputPW_re = view.findViewById(R.id.inputPW_re); // 비밀번호 재입력 창
        X_PW_re = view.findViewById(R.id.X_PW_re); // 비밀번호가 같지 않을 경우 visible
        X_PW2 = view.findViewById(R.id.X_PW2); // 비밀번호 생성 시 조건 충족(특수문자, 영문, 숫자)
        signUp_btn = view.findViewById(R.id.signUp_btn); // 회원가입 조건 만족 시킬 시 색변환 및 클릭 가능
        cancel_btn = view.findViewById(R.id.cancel_btn); // 정보 저장 없이 뒤로가기 기능(로그인 화면으로 전환)

        // 이름의 입력이 변환될 때마다 확인할 수 있는 메서드
        inputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                String name = editable.toString();

                // 이름 입력 칸이 빈칸인지 확인
                if(name!=null){
                    isNameValid = true;
                }
                else{
                    isNameValid = false;
                }
                updateSignUpButtonState();
            }
        });

        // 이메일의 입력이 변환될 때마다 이메일의 string 호출
        inputEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                // 입력된 이메일 주소를 문자열로 가져옴
                String email = s.toString();

                // 이메일 주소가 특정 패턴에 맞지 않는 경우 처리
                if (!isValidEmail(email)) {
                    // 예: X_email -> visible, O_email -> invisible
                    X_email.setVisibility(View.VISIBLE);
                    O_email.setVisibility(View.INVISIBLE);
                    isEmailValid = false;
                } else {
                    // 이메일 주소가 특정 패턴에 맞는 경우 처리
                    X_email.setVisibility(View.INVISIBLE);
                    O_email.setVisibility(View.VISIBLE);
                    isEmailValid = true;
                }
                updateSignUpButtonState();
            }
        });

        // 비밀번호를 입력 할 때마다 비밀번호 string 호출
        inputPW.addTextChangedListener(new TextWatcher() {
            String pattern = "[~!@#$%^&*?]";
            String english = "[a-zA-Z]";
            String number = "\\d";
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                String password = inputPW.getText().toString();

                Pattern patternPattern = Pattern.compile(pattern);
                Pattern englishPattern = Pattern.compile(english);
                Pattern numberPattern = Pattern.compile(number);

                Matcher patternMatcher = patternPattern.matcher(password);
                Matcher englishMatcher = englishPattern.matcher(password);
                Matcher numberMatcher = numberPattern.matcher(password);

                // 비밀번호가 8자리 이상인지 확인
                if(inputPW.length() < 8){
                    X_PW.setVisibility(View.VISIBLE);
                    X_PW2.setVisibility(View.INVISIBLE);
                    isPasswordValid = false;
                }
                else{   // 비밀번호 8자리 이상일 때 특수문자, 영문, 숫자 중 2가지 이상을 사용했는지 확인
                    X_PW.setVisibility(View.INVISIBLE);
                    if((patternMatcher.find()&&englishMatcher.find()) || (patternMatcher.find()&&numberMatcher.find()) || (numberMatcher.find()&&englishMatcher.find())){
                        X_PW2.setVisibility(View.INVISIBLE);
                        isPasswordValid = true;
                    }
                    else{
                        X_PW2.setVisibility(View.VISIBLE);
                        isPasswordValid = false;
                    }
                }
                updateSignUpButtonState();
            }
        });

        // 비밀번호 재입력 칸에 입력 할 때마다 비밀번호 string 호출
        inputPW_re.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            // 비밀번호와 비밀번호 확인 후 텍스트 visible 설정
            public void afterTextChanged(Editable editable) {
                String pw = inputPW.getText().toString(); // 비밀번호 입력란의 텍스트 가져오기
                String pwRe = editable.toString(); // 비밀번호 재입력란의 텍스트 가져오기

                if(pw.equals(pwRe)){ // 비밀번호와 재입력한 비밀번호가 같은지 확인
                    X_PW_re.setVisibility(View.INVISIBLE);
                    isPasswordMatch = true;
                } else {
                    X_PW_re.setVisibility(View.VISIBLE);
                    isPasswordMatch = false;
                }
                updateSignUpButtonState();
            }
        });

        // ---------- 취소 버튼 onclick 리스너 구현 해야됨 -----------*************
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // ---------- 회원 가입 버튼을 누르면 모든 정보가 서버에 보내지면서 로그인 창으로 이동 -----------***************
        signUp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }

    // 이메일 주소가 특정 패턴에 맞는지 확인하는 메서드
    private boolean isValidEmail(String email) {
        // 여기에 이메일 주소의 패턴을 확인하는 코드를 작성
        // 예: email 문자열이 "@naver.com", "@gmail.com",
        //     "@hanmail.net", "@daum.net" 중 하나로 끝나는지 확인 -> 여기서 이메일 주소 형식 등록 가능
        return email.endsWith("@naver.com") ||
                email.endsWith("@gmail.com") ||
                email.endsWith("@hanmail.net") ||
                email.endsWith("@daum.net");
    }

    // 모든 조건을 만족 할 때 회원가입 버튼 활성화 및 만족 불충분 시 버튼 비활성
    private void updateSignUpButtonState() {
        if (isEmailValid && isPasswordValid && isPasswordMatch && isNameValid) {
            signUp_btn.setEnabled(true);
            signUp_btn.setBackground(getResources().getDrawable(R.drawable.signup_button_blue_version));
        } else {
            signUp_btn.setEnabled(false);
            signUp_btn.setBackground(getResources().getDrawable(R.drawable.signup_button_grey_version));
        }
    }

}
