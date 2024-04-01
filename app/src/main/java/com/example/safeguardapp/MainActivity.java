package com.example.safeguardapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // fragmentSignUp을 추가
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new fragmentSignUp());
        transaction.addToBackStack(null); // 백 스택에 추가하여 뒤로가기 버튼 동작을 처리할 수 있도록 함
        transaction.commit();
    }
}
