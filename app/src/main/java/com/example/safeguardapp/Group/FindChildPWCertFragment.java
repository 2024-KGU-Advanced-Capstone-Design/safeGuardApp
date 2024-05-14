package com.example.safeguardapp.Group;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.safeguardapp.FindPW.CodeRequest;
import com.example.safeguardapp.FindPW.FindPWFragment;
import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.UserRetrofitInterface;
import com.google.android.material.appbar.MaterialToolbar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindChildPWCertFragment extends Fragment {

    private EditText certNum;
    private Button cancel_btn, positive_btn;
    private String currentGroupUuid, childName;
    private RetrofitClient retrofitClient;
    private UserRetrofitInterface userRetrofitInterface;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_child_pw_cert, container, false);

        initializeView(view);
        setupListeners(view);

        return view;
    }

    private void initializeView(View view){
        certNum = view.findViewById(R.id.input_cert_num);
        cancel_btn = view.findViewById(R.id.cancel_btn);
        positive_btn = view.findViewById(R.id.positive_btn);
        currentGroupUuid = getArguments().getString("UUID");
        childName = getArguments().getString("childID");

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> previous());
    }

    private void setupListeners(View view){
        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();
        positive_btn.setOnClickListener(v -> clickOK(view));
    }

    private void clickOK(View view){
        String sendID = LoginPageFragment.saveID;
        String code = certNum.getText().toString().trim();
        CodeRequest codeRequest = new CodeRequest(sendID, code);
        Call<ResponseBody> call = userRetrofitInterface.codeCheck(codeRequest);
        call.clone().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    transScreen(new FindChildPWFragment());
                }
                else Toast.makeText(view.getContext(), "인증번호를 다시 확인해주세요!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("POST", "통신 실패");
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 뒤로 가기 시 실행되는 코드
                previous();
            }
        });
    }

    private void transScreen(Fragment fragment){
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containers, fragment);
        transaction.commit();
    }

    private void previous(){
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containers, GroupSettingFragment.newInstance(currentGroupUuid, childName));
        transaction.commit();
    }
}
