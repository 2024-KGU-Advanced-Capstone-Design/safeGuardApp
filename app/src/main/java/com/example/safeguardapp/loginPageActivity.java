package com.example.safeguardapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class loginPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
    }

    public void onClickButtonLogin(View v) {
        EditText editTextID = (EditText) findViewById(R.id.editTextID);
        String id = editTextID.getText().toString();

        EditText editTextPW = (EditText) findViewById(R.id.editTextPW);
        String pw = editTextPW.getText().toString();

    }

    public void onClickButtonRegister(View v) {
        // 프래그먼트 트랜잭션을 시작합니다.
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_login_page, new fragmentSignUp());
        transaction.addToBackStack(null); //     백 스택에 추가하여 뒤로가기 버튼 동작을 처리할 수 있도록 함
        transaction.commit();

    }

    public void onClickButtonFindIDPW(View v) {

    }
}