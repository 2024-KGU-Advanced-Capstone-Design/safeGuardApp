package com.example.safeguardapp.Group;

import android.text.Editable;
import android.text.TextWatcher;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.safeguardapp.LogIn.LoginInfo;
import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.UserRetrofitInterface;
import com.example.safeguardapp.data.model.Group;
import com.example.safeguardapp.data.repository.GroupRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddGroupPopupFragment extends Fragment {

    RetrofitClient retrofitClient;
    UserRetrofitInterface userRetrofitInterface;
    private TextView X_space_ID, X_PW, X_PW2, X_space_PW, X_PW_re;
    private boolean isSpaceIDValid, isIDValid, isPWValid, isPWMatch, isSpacePWValid = false;
    private Button signUp_btn, cancel_btn;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_group_popup, container, false);
        EditText nameEditText = ((EditText) view.findViewById(R.id.name_edit_text));
        EditText idEditText = ((EditText) view.findViewById(R.id.id_edit_text));
        EditText passwordEditText = ((EditText) view.findViewById(R.id.password_edit_text));
        EditText rePWEditText = ((EditText) view.findViewById(R.id.re_password_edit_text));
        signUp_btn = view.findViewById(R.id.signUp_btn);
        cancel_btn = view.findViewById(R.id.cancel_btn);
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);

        IDValid(idEditText);
        PWValid(passwordEditText, rePWEditText);

        signUp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = nameEditText.getText().toString().trim();
                String userid = idEditText.getText().toString().trim();
                String userpw = passwordEditText.getText().toString().trim();
                transmitChildData(username, userid,userpw);
            }
        });

        cancel_btn.setOnClickListener(v -> previous());
        toolbar.setNavigationOnClickListener(v -> previous());

        return view;
    }

    private void IDValid(EditText idEditText) {
        idEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                X_space_ID = getView().findViewById(R.id.X_space_ID);
                String ID = editable.toString().trim();
                if (ID.contains(" ")) {
                    isSpaceIDValid = false;
                    X_space_ID.setVisibility(View.VISIBLE);
                } else {
                    isSpaceIDValid = true;
                    X_space_ID.setVisibility(View.INVISIBLE);
                }
                isIDValid = editable.length() > 0;
                updateSignUpButtonState();
            }
        });
    }

    private void PWValid(EditText passwordEditText, EditText rePWEditText) {
        passwordEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                String password = editable.toString();
                X_PW = getView().findViewById(R.id.X_PW);
                X_PW2 = getView().findViewById(R.id.X_PW2);
                X_space_PW = getView().findViewById(R.id.X_space_PW);
                X_PW_re = getView().findViewById(R.id.X_PW_re);
                if (password.contains(" ")) {
                    isSpacePWValid = false;
                    X_space_PW.setVisibility(View.VISIBLE);
                    X_PW.setVisibility(View.INVISIBLE);
                    X_PW2.setVisibility(View.INVISIBLE);
                } else {
                    isSpacePWValid = true;
                    X_space_PW.setVisibility(View.INVISIBLE);

                    if (password.length() >= 8) {
                        X_PW.setVisibility(View.INVISIBLE);
                        if (PasswordValidFunc(password)) {
                            X_PW2.setVisibility(View.INVISIBLE);
                            isPWValid = true;
                        } else {
                            X_PW2.setVisibility(View.VISIBLE);
                            isPWValid = false;
                        }
                    } else {
                        X_PW.setVisibility(View.VISIBLE);
                        X_PW2.setVisibility(View.INVISIBLE);
                        isPWValid = false;
                    }
                }

                updateSignUpButtonState();

                // 비밀번호를 다시 입력할 때 비밀번호 재입력칸 알림 메시지 수정
                String pw = editable.toString();
                String pwRe = rePWEditText.getText().toString().trim();
                isPWMatch = pwRe.equals(pw) && pw.equals(pwRe);
                X_PW_re.setVisibility(isPWMatch ? View.INVISIBLE : View.VISIBLE);
                updateSignUpButtonState();
            }
        });
        rePWEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                X_PW_re = getView().findViewById(R.id.X_PW_re);
                String pw = passwordEditText.getText().toString();
                String pwRe = editable.toString();
                isPWMatch = pw.equals(pwRe) && pwRe.equals(pw);
                X_PW_re.setVisibility(isPWMatch ? View.INVISIBLE : View.VISIBLE);
                updateSignUpButtonState();
            }
        });
    }


    private void addNewButton(String name, String id, String password) {
        Group group = new Group(name, id, password);
        GroupRepository.getInstance(requireContext()).addGroup(group);
    }

    public abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
    }

    private boolean PasswordValidFunc(String password) {
        return password.matches(".*[~!@#$%^&*?].*") && password.matches(".*[a-zA-Z].*") ||
                password.matches(".*[~!@#$%^&*?].*") && password.matches(".*\\d.*") ||
                password.matches(".*[a-zA-Z].*") && password.matches(".*\\d.*");
    }

    private void updateSignUpButtonState() {
        signUp_btn.setEnabled(isSpaceIDValid && isIDValid && isPWValid && isPWMatch && isSpacePWValid);
        signUp_btn.setBackground(getResources().getDrawable(signUp_btn.isEnabled() ? R.drawable.signup_button_blue_version : R.drawable.signup_button_grey_version));
    }

    private void transmitChildData(String name, String id, String password) {
        //retrofit 생성
        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

        String GuardID = LoginInfo.getLoginID();
        Log.e("POST", "child 생성");
        ChildDTO childDTO = new ChildDTO(id, password, GuardID);
        Gson gson = new Gson();
        String userInfo = gson.toJson(childDTO);

        Log.e("JSON", userInfo);

        Call<ResponseBody> call = userRetrofitInterface.child(childDTO);
        call.clone().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.e("POST", "성공");
                    addNewButton(name, id, password);
                    previous();
                }
                else {
                    Log.e("POST", "not success");
                    Toast.makeText(getContext(), "이미 존재하는 아이디입니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("POST", "실패");
            }
        });
    }

    private void previous(){
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containers, new GroupFragment());
        transaction.commit();
    }
}