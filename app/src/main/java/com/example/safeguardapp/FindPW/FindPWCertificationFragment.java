package com.example.safeguardapp.FindPW;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.SignUp.SignUpFragment;
import com.example.safeguardapp.UserRetrofitInterface;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindPWCertificationFragment extends Fragment {
    private RetrofitClient retrofitClient;
    private UserRetrofitInterface userRetrofitInterface;
    private EditText findPWToID, codeVerify;
    private Button cancel_btn, send_ANumber_btn;
    public static String sendID;

    int i = 1;

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
        cancel_btn = view.findViewById(R.id.cancel_btn);
        send_ANumber_btn = view.findViewById(R.id.send_ANumber_btn);
    }

    private void setupListeners() {
        //retrofit 생성
        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

        // 취소 버튼 클릭 시 로그인 화면으로 전환
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.start_activity, new LoginPageFragment());
                transaction.commit();
            }
        });

        findPWToID.addTextChangedListener(new SignUpFragment.SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() > 0)
                    send_ANumber_btn.setEnabled(true);
                else
                    send_ANumber_btn.setEnabled(false);
            }
        });

        // 비밀번호 찾기 버튼 클릭시 이메일을 통해서 비밀번호를 찾는 화면으로 전환 -------- 미구현
        // ID,Email을 통한 Email verification
        send_ANumber_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendID = findPWToID.getText().toString();
                EmailRequest emailRequest = new EmailRequest(sendID);

                Call<ResponseBody> call = userRetrofitInterface.sendCode(emailRequest);

                call.clone().enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.e("POST","통신 성공");

                        //통신 성공
                        if(response.isSuccessful()){
                            Toast.makeText(view.getContext(), "회원님의 메일함을 확인해주세요!", Toast.LENGTH_LONG).show();

                            //인증번호 입력 칸 생성

                            EditText newEditText = new EditText(getContext());
                            send_ANumber_btn.setEnabled(false);

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
                            newEditText.setInputType(InputType.TYPE_CLASS_NUMBER); // 예시로 입력 타입 지정

                            // 프래그먼트의 루트 레이아웃 가져오기
                            ViewGroup rootView = (ViewGroup) getView();

                            // 기존의 아이디 입력 EditText의 인덱스 확인하기
                            int index = rootView.indexOfChild(findPWToID);

                            // 새로운 EditText를 기존의 아이디 입력 EditText 위에 추가
                            rootView.addView(newEditText, index);
                            params.setMargins(0,dpToPx(15),0,0);
                            send_ANumber_btn.setText("다음");
                            newEditText.addTextChangedListener(new SignUpFragment.SimpleTextWatcher() {
                                @Override
                                public void afterTextChanged(Editable editable) {
                                    if(editable.length() > 0)
                                        send_ANumber_btn.setEnabled(true);
                                    else
                                        send_ANumber_btn.setEnabled(false);
                                    }
                            });
                            codeVerify = newEditText;
                        }else Toast.makeText(view.getContext(), "인증번호를 다시 확인해주세요!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(view.getContext(), "통신 실패", Toast.LENGTH_LONG).show();
                    }
                });

                send_ANumber_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String code = codeVerify.getText().toString();
                        CodeRequest codeRequest = new CodeRequest(sendID, code);
                        Call<ResponseBody> call = userRetrofitInterface.codeCheck(codeRequest);
                        call.clone().enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                                if(response.isSuccessful()) {
                                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                    transaction.replace(R.id.start_activity, new FindPWFragment());
                                    transaction.commit();
                                }else Toast.makeText(view.getContext(), "인증번호를 다시 확인해주세요!", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(view.getContext(), "통신 실패", Toast.LENGTH_LONG).show();
                            }
                        });
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
                transaction.replace(R.id.start_activity, new LoginPageFragment());
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

    private abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
    }
}
