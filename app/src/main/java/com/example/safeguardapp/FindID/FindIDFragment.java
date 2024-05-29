package com.example.safeguardapp.FindID;

import android.os.Bundle;
import android.util.Log;
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
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindIDFragment extends Fragment {
    RetrofitClient retrofitClient;
    UserRetrofitInterface userRetrofitInterface;
    private TextView checkInfo;
    private EditText findIDToName, findIDToEmail;
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
        cancel_btn = view.findViewById(R.id.cancel_btn);
        find_btn = view.findViewById(R.id.find_btn);
        findIDToName = view.findViewById(R.id.findIDToName);
        findIDToEmail = view.findViewById(R.id.findIDToEmail);
        checkInfo = view.findViewById(R.id.Check_Info);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> previous());
    }

    private void setupListeners() {
        // 취소 버튼 클릭 시 로그인 화면으로 전환
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_bottom, R.anim.slide_in_bottom, R.anim.slide_out_top);
                transaction.replace(R.id.start_activity, new LoginPageFragment());
                transaction.commit();
            }
        });

        // 찾기 버튼 클릭 시 리스너
        find_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getName,getEmail;
                getName = findIDToName.getText().toString();
                getEmail = findIDToEmail.getText().toString();
                FindIDRequest idDTO = new FindIDRequest(getName,getEmail);
                Gson gson = new Gson();
                String userInfo = gson.toJson(idDTO);

                Log.e("JSON",userInfo);

                retrofitClient = RetrofitClient.getInstance();
                userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

                Call<FindIDResponse> call = userRetrofitInterface.findID(idDTO);
                call.clone().enqueue(new Callback<FindIDResponse>() {
                    @Override
                    public void onResponse(Call<FindIDResponse> call, Response<FindIDResponse> response) {
                        Log.e("POST","통신 성공");

                        FindIDResponse result = response.body();
                        if(result==null) {
                            checkInfo.setVisibility(View.VISIBLE);
                        }else {
                            checkInfo.setVisibility(View.INVISIBLE);

                            String state = result.getResultCode();
                            String memberId = result.getMemberId();

                            Log.e("JSON", state);
                            Log.e("JSON", memberId);

                            previous();
                            Toast.makeText(v.getContext(), "회원님의 ID는 " + memberId + " 입니다.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<FindIDResponse> call, Throwable t) {
                        checkInfo.setVisibility(View.VISIBLE);
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
                previous();
            }
        });
    }
    private void previous(){
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_bottom, R.anim.slide_in_bottom, R.anim.slide_out_top);
        transaction.replace(R.id.start_activity, new LoginPageFragment());
        transaction.commit();
    }
}
