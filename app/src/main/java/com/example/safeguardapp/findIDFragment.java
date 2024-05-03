package com.example.safeguardapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class findIDFragment extends Fragment {
    private EditText findIDToEmail;
    private Button cancel_btn, find_btn;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_id, container, false);

        initializeView(view);
        setupListeners();

        return view;
    }

    private void initializeView(View view) {
        findIDToEmail = view.findViewById(R.id.findIDToEmail);
        cancel_btn = view.findViewById(R.id.cancel_btn);
        find_btn = view.findViewById(R.id.find_btn);
    }

    private void setupListeners() {
        // 취소 버튼 클릭 시 로그인 화면으로 전환
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.start_activity, new loginPageFragment());
                transaction.commit();
            }
        });

        // 비밀번호 찾기 버튼 클릭시 이메일로 아이디 전송 및 로그인 화면으로 전환-------- 미구현
        find_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.start_activity, new loginPageFragment());
                transaction.commit();
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
                transaction.replace(R.id.start_activity, new loginPageFragment());
                transaction.commit();
            }
        });
    }

}
