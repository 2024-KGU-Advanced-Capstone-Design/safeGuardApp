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

public class findPWFragment extends Fragment {

    private EditText resetPW, resetPW_re;
    private TextView X_pw1, X_pw2, X_pw_re;
    private Button cancel_btn, resettingPW_btn;
    private Boolean isPasswordValid = false;
    private Boolean isPasswordValid2 = false;

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
    }

    private void setupListeners() {
        // 취소 버튼 클릭 시 비밀번호 찾기 인증 화면으로 전환
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.start_activity, new loginPageFragment());
                transaction.commit();
            }
        });

        resettingPW_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.start_activity, new loginPageFragment());
                transaction.commit();
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
                if (password.length() >= 8){
                    X_pw1.setVisibility(View.INVISIBLE);
                    if (PasswordValidFunc(password)){
                        X_pw2.setVisibility(View.INVISIBLE);
                        isPasswordValid = true;
                    }
                    else {
                        X_pw2.setVisibility(View.VISIBLE);
                        isPasswordValid = false;
                    }
                }
                else {
                    X_pw1.setVisibility(View.VISIBLE);
                    X_pw2.setVisibility(View.INVISIBLE);
                    isPasswordValid = false;

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
                transaction.replace(R.id.start_activity, new findPWCertificationFragment());
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
        resettingPW_btn.setEnabled(isPasswordValid && isPasswordValid2);
    }
}

