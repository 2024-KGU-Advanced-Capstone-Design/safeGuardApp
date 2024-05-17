package com.example.safeguardapp.Group;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.safeguardapp.FindPW.EmailRequest;
import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.MainActivity;
import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.UserRetrofitInterface;
import com.example.safeguardapp.data.model.Group;
import com.example.safeguardapp.data.repository.GroupRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupSettingFragment extends Fragment {
    private String uuid;
    private String childID;
    private GroupRepository repository;
    private LiveData<Optional<Group>> groupStream;
    private ChipGroup aideGroup;
    private RetrofitClient retrofitClient;
    private UserRetrofitInterface userRetrofitInterface;

    public static GroupSettingFragment newInstance(String uuid, String childID) {
        GroupSettingFragment fragment = new GroupSettingFragment();
        Bundle args = new Bundle();
        args.putString("uuid", uuid);
        args.putString("childID", childID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            uuid = getArguments().getString("uuid");
            childID = getArguments().getString("childID");
        }

        if (TextUtils.isEmpty(uuid)) {
            previous();
            return;
        }

        repository = GroupRepository.getInstance(requireContext());
        groupStream = Transformations.map(repository.getGroupListStream(), groups ->
                groups.stream().filter(e -> TextUtils.equals(e.getUuid(), uuid)).findFirst());

        groupStream.observe(this, group -> {
            if (!group.isPresent()) {
                previous();
                return;
            }

            updateAideUi();
        });
    }


    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_setting, container, false);

        TextView groupNameTextView = view.findViewById(R.id.groupName);
        groupStream.observe(getViewLifecycleOwner(), groupOptional -> {
            if (groupOptional.isPresent()) {
                Group group = groupOptional.get();
                groupNameTextView.setText(group.getName());
            }
        });

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> previous());

        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

        view.findViewById(R.id.change_name_button).setOnClickListener(v -> edit());
        view.findViewById(R.id.add_aide_button).setOnClickListener(v -> addAide());
        view.findViewById(R.id.selectZone_btn).setOnClickListener(v -> mapSectorSet());
        view.findViewById(R.id.child_id_find_button).setOnClickListener(v -> findChildID());
        view.findViewById(R.id.child_pw_find_button).setOnClickListener(v -> findChildPW());
        view.findViewById(R.id.del_group_btn).setOnClickListener(v -> remove());

        aideGroup = view.findViewById(R.id.chip_group);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 뒤로 가기 시 실행되는 코드
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                // 이동 시에는 이미 생성된 mapFragment를 사용하여 교체
                transaction.replace(R.id.containers, ((MainActivity) requireActivity()).groupFragment);
                transaction.commit();

                BottomNavigationView navigationView = requireActivity().findViewById(R.id.bottom_navigationview);
                navigationView.setSelectedItemId(R.id.group);
            }
        });

        return view;
    }

    private void updateAideUi() {
        if (aideGroup != null) {
            aideGroup.removeAllViews();
        }

        Group group = groupStream.getValue().get();
        List<String> aideList = group.getAide();

        for (String id : aideList) {
            Chip chip = new Chip(getContext());
            chip.setCloseIconVisible(true);
            chip.setCheckable(false);
            chip.setText(id);
            chip.setOnCloseIconClickListener(v -> {
                aideList.remove(id);
                repository.editGroup(group);
            });

            aideGroup.addView(chip);
        }
    }

    //이전 화면으로 이동
    private void previous(){
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.containers, new GroupFragment());
        fragmentTransaction.commit();
    }

    //그룹 명 변경
    private void edit() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_group_name, null);
        EditText editText = dialogView.findViewById(R.id.name_edit_text);
        editText.setHint(groupStream.getValue().get().getName());

        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(getContext())
                .setTitle("그룹명 변경")
                .setView(dialogView)
                .setPositiveButton("수정", (dialogInterface, i) -> {
                    String name = editText.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) return;

                    Group group = groupStream.getValue().get();
                    group.setName(name);
                    repository.editGroup(group);

                    Toast.makeText(getContext(), "수정되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null);

        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }

    //아이 조력자 추가
    private void addAide() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_aide, null);
        EditText editText = dialogView.findViewById(R.id.id_edit_text);

        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(getContext())
                .setTitle("조력자 추가")
                .setView(dialogView)
                .setPositiveButton("추가", (dialogInterface, i) -> {
                    String id = editText.getText().toString().trim();
                    if (TextUtils.isEmpty(id)) return;

                    Group group = groupStream.getValue().get();
                    if (group.getAide().contains(id)) return;

                    ArrayList<String> aideList = new ArrayList<>(group.getAide());
                    aideList.add(id);
                    group.setAide(aideList);

                    repository.editGroup(group);

                    Toast.makeText(getContext(), "추가되었습니다.", Toast.LENGTH_SHORT).show();


                })
                .setNegativeButton("취소", null);

        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }

    //안전 구역 및 위험 구역 설정
    private void mapSectorSet() {
        Bundle args = new Bundle();
        args.putString("UUID", uuid);
        args.putString("childID", childID);
        SectorMapFragment SectorMapFragment = new SectorMapFragment();
        SectorMapFragment.setArguments(args);
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.containers, SectorMapFragment);
        fragmentTransaction.commit();
    }

    //그룹 아이디 찾기
    private void findChildID() {
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(getContext())
                .setTitle("그룹 아이디")
                .setMessage("그룹의 아이디는 " + childID + "입니다")
                .setPositiveButton("확인", (dialogInterface, i) -> {
                });
        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }

    //아이 비밀번호 찾기
    private void findChildPW() {

        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(getContext())
                .setTitle("인증번호 요청")
                .setMessage("가입하신 이메일로 인증번호를 보내시겠습니까?")
                .setPositiveButton("확인", (dialogInterface, i) -> {
                    sendEmail();
                })
                .setNegativeButton("취소", null);
        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }

    //그룹 삭제
    private void remove() {
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(getContext())
                .setTitle("그룹 삭제")
                .setMessage("그룹을 삭제 하시겠습니까?")
                .setPositiveButton("삭제", (dialogInterface, i) -> {
                    transmitRemove();
                })
                .setNegativeButton("취소", null);

        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }

    private void sendEmail(){
        String sendID = LoginPageFragment.saveID;
        EmailRequest emailRequest = new EmailRequest(sendID);

        Log.e("POST", sendID);
        Call<ResponseBody> call = userRetrofitInterface.sendCode(emailRequest);

        call.clone().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.e("POST","통신 성공");

                if (response.isSuccessful()) {
                    Bundle args = new Bundle();
                    args.putString("UUID", uuid);
                    args.putString("childID", childID);
                    FindChildPWCertFragment FindChildPWCertFragment = new FindChildPWCertFragment();
                    FindChildPWCertFragment.setArguments(args);
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.containers, FindChildPWCertFragment);
                    fragmentTransaction.commit();
                } else {
                    Log.e("POST", "Error: " + response.errorBody().toString());
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("POST", "통신 실패", t);
            }
        });
    }
    private void transmitRemove(){
        GroupRemoveRequest RemoveDTO = new GroupRemoveRequest(childID);
        Gson gson = new Gson();
        String removeInfo = gson.toJson(RemoveDTO);

        Log.e("JSON", removeInfo);

        Call<ResponseBody> call = userRetrofitInterface.removeGroup(RemoveDTO);
        call.clone().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.e("POST", "전달 성공");
                    previous();
                    GroupRepository.getInstance(getContext()).removeGroup(uuid);
                    Toast.makeText(getContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    // 응답 본문 로그 추가
                } else {
                    Log.e("POST", "전달 실패, HTTP Status: " + response.code());

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("POST", "통신 실패", t);
            }
        });
    }
}
