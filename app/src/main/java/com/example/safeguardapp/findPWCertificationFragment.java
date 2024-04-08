package com.example.safeguardapp;

import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class findPWCertificationFragment extends Fragment {
    private EditText findPWToEmail, findPWToID;
    private Button cancel_btn, send_ANumber_btn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_pw_certification, container, false);

        initializeView(view);
        setupListeners();

        return view;
    }

    private void initializeView(View view) {
        findPWToID = view.findViewById(R.id.findPWToID);
        findPWToEmail = view.findViewById(R.id.findPWToEmail);
        cancel_btn = view.findViewById(R.id.cancel_btn);
        send_ANumber_btn = view.findViewById(R.id.send_ANumber_btn);
    }

    private void setupListeners() {
        // 취소 버튼 클릭 시 로그인 화면으로 전환
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.activity_main, new loginPageFragment());
                transaction.commit();
            }
        });

        // 비밀번호 찾기 버튼 클릭시 이메일을 통해서 비밀번호를 찾는 화면으로 전환 -------- 미구현
        send_ANumber_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText newEditText = new EditText(getContext());

                // LayoutParams 설정
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                LinearLayout.LayoutParams findPWToIDparams = (LinearLayout.LayoutParams) findPWToID.getLayoutParams();
                findPWToIDparams.setMargins(0, dpToPx(15), 0, 0);

                // EditText에 LayoutParams 적용
                newEditText.setLayoutParams(params);

                // 추가된 EditText의 속성 설정
                newEditText.setHint("인증번호를 입력해주세요");
                newEditText.setInputType(InputType.TYPE_CLASS_TEXT); // 예시로 입력 타입 지정

                // 프래그먼트의 루트 레이아웃 가져오기
                ViewGroup rootView = (ViewGroup) getView();

                // 기존의 아이디 입력 EditText의 인덱스 확인하기
                int index = rootView.indexOfChild(findPWToID);

                // 새로운 EditText를 기존의 아이디 입력 EditText 위에 추가
                rootView.addView(newEditText, index);
                params.setMargins(0,dpToPx(15),0,0);
                send_ANumber_btn.setText("다음");

                send_ANumber_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.activity_main, new findPWFragment());
                        transaction.commit();

                    }
                });
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
                transaction.replace(R.id.activity_main, new loginPageFragment());
                transaction.commit();
            }
        });
    }
    public int dpToPx(int sizeInDP) {
        int pxVal = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, sizeInDP, getResources().getDisplayMetrics()
                );
        return pxVal;
    }
}
