package com.example.safeguardapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class loginPageFragment extends Fragment {
    private startScreenActivity startScreenActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_page_fragment, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 로그인 버튼에 대한 클릭 이벤트 처리
        view.findViewById(R.id.buttonLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editTextID = view.findViewById(R.id.editTextID);
                String id = editTextID.getText().toString();

                EditText editTextPW = view.findViewById(R.id.editTextPW);
                String pw = editTextPW.getText().toString();
                // ------> 여기에 로그인 처리 코드 추가

                // 로그인 성공 했다 치고 startScreenActivity -> MainActivity로 이동 구현
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });

        // 회원가입 버튼에 대한 클릭 이벤트 처리
        view.findViewById(R.id.buttonRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 회원가입 버튼을 클릭했을 때 fragmentSignUp로 화면 전환
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.activity_main, new signUpFragment());
                transaction.commit();
            }
        });

        // 아이디 찾기 버튼에 대한 클릭 이벤트 처리
        view.findViewById(R.id.buttonFindID).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.activity_main, new findPWFragment());
                transaction.commit();
            }
        });

        // 비밀번호 찾기 버튼에 대한 클릭 이벤트 처리
        view.findViewById(R.id.buttonFindPW).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.activity_main, new findPWFragment());
                transaction.commit();
            }
        });

    }

}
