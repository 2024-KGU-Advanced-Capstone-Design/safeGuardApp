package com.example.safeguardapp.Child;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.safeguardapp.FindPW.EmailRequest;
import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.MainActivity;
import com.example.safeguardapp.PreferenceManager;
import com.example.safeguardapp.R;
import com.example.safeguardapp.StartScreenActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChildSettingFragment extends Fragment {
    private Button changeName, logout;
    private CircleImageView changeImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_child_setting, container, false);

        initializeView(view);
        setupListeners();

        return view;
    }

    private void initializeView(View view) {
        changeName = view.findViewById(R.id.editName_btn);
        logout = view.findViewById(R.id.logout_btn);
        changeImage = view.findViewById(R.id.imageView);
    }

    private void setupListeners() {
        changeName.setOnClickListener(v -> changeNameMethod());
        logout.setOnClickListener(v -> logoutMethod());
        changeImage.setOnClickListener(v -> changeImageMethod());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textView = view.findViewById(R.id.childId);
        textView.setText(LoginPageFragment.saveID);

        // SettingFragment에서 뒤로 갔을 때 MapFragment로 이동
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 뒤로 가기 시 실행되는 코드
                previous();
            }
        });
    }

    private void changeNameMethod() {
        //구현 후순위
    }

    private void logoutMethod() {
        new AlertDialog.Builder(requireContext())
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Context context = getActivity();
                        if (context != null) {
                            Intent serviceIntent = new Intent(context, LocationService.class);
                            context.stopService(serviceIntent);
                        } else {
                            Log.e("MyFragment", "Context is null, cannot stop service");
                        }
                        PreferenceManager.clear(getContext());
                        transScreen();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void changeImageMethod(){

    }

    private void previous(){
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        // 이동 시에는 이미 생성된 mapFragment를 사용하여 교체
        transaction.replace(R.id.containers, ((ChildMainActivity) requireActivity()).childMapFragment);
        transaction.commit();
    }

    private void transScreen(){
        Intent intent = new Intent(getActivity(), StartScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
