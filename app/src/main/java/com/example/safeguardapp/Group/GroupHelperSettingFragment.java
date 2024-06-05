package com.example.safeguardapp.Group;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;


import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.MainActivity;
import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.UserRetrofitInterface;
import com.example.safeguardapp.data.model.Group;
import com.example.safeguardapp.data.model.OtherGroup;
import com.example.safeguardapp.data.repository.GroupRepository;
import com.example.safeguardapp.data.repository.OtherGroupRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;


import java.util.ArrayList;
import java.util.Optional;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupHelperSettingFragment extends Fragment {
    private String uuid;
    private String childID;
    private String selectedItem;
    private OtherGroupRepository repository;
    private LiveData<Optional<OtherGroup>> groupStream;
    private ChipGroup aideGroup;
    private RetrofitClient retrofitClient;
    private ArrayList<String> typeList =new ArrayList<>();
    private UserRetrofitInterface userRetrofitInterface;

    public static GroupHelperSettingFragment newInstance(String uuid, String childID) {
        GroupHelperSettingFragment fragment = new GroupHelperSettingFragment();
        Bundle args = new Bundle();
        args.putString("uuid", uuid);
        args.putString("childID", childID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        typeList.add("도착");
        typeList.add("출발");
        typeList.add("?");

        if (getArguments() != null) {
            uuid = getArguments().getString("uuid");
            childID = getArguments().getString("childID");
        }

        repository = OtherGroupRepository.getInstance(requireContext());
        groupStream = Transformations.map(repository.getOtherGroupListStream(), groups ->
                groups.stream().filter(e -> TextUtils.equals(e.getUuid(), uuid)).findFirst());

        if (TextUtils.isEmpty(uuid)) {
            previous();
            return;
        }
    }


    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_helper_group_setting, container, false);

        TextView groupNameTextView = view.findViewById(R.id.groupName);
        groupStream.observe(getViewLifecycleOwner(), groupOptional -> {
            if (groupOptional.isPresent()) {
                OtherGroup group = groupOptional.get();
                groupNameTextView.setText(group.getName());
            }
        });

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> previous());

        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

        view.findViewById(R.id.del_group_btn).setOnClickListener(v -> remove());
        view.findViewById(R.id.confirm_button).setOnClickListener(v->confirm());
        view.findViewById(R.id.change_name_button).setOnClickListener(v -> changeName());

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 뒤로 가기 시 실행되는 코드
                previous();
            }
        });

        LinearLayout linearLayout = view.findViewById(R.id.group_setting_screen);
        YoYo.with(Techniques.FadeIn).duration(700).repeat(0).playOn(linearLayout);

        return view;
    }

    //이전 화면으로 이동
    private void previous(){
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top, R.anim.slide_in_top, R.anim.slide_out_bottom);
        fragmentTransaction.replace(R.id.containers, new HelpChildGroupFragment());
        fragmentTransaction.commit();
    }

    // 그룹명 변경
    private void changeName(){
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_group_name, null);
        EditText editText = dialogView.findViewById(R.id.name_edit_text);
        editText.setHint(groupStream.getValue().get().getName());

        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(getContext())
                .setTitle("그룹명 변경")
                .setView(dialogView)
                .setPositiveButton("수정", (dialogInterface, i) -> {
                    String name = editText.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) return;

                    OtherGroup group = groupStream.getValue().get();
                    group.setName(name);
                    repository.editOtherGroup(group);

                    Toast.makeText(getContext(), "수정되었습니다.", Toast.LENGTH_SHORT).show();
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

    //확인 버튼
    private void confirm(){
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirm, null);

        int checknum = 0;

        final String[] items = new String[typeList.size()];
        typeList.toArray(items);

        final int[] selectedPosition = {checknum};
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(getContext())
                .setTitle("확인")
                .setSingleChoiceItems(items, checknum, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 사용자가 항목을 선택할 때마다 호출됨
                        // 선택된 항목의 인덱스는 which에 저장됨
                        selectedPosition[0] = which;
                    }
                })
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 사용자가 OK 버튼을 클릭할 때 호출됨
                        // 선택된 항목의 인덱스를 가져옴
                        if (selectedPosition[0] != -1) {
                            selectedItem = items[selectedPosition[0]];

                            ConfirmRequest confirmRequest = new ConfirmRequest(LoginPageFragment.saveID, childID, selectedItem);
                            Call<ResponseBody> call = userRetrofitInterface.sendConfirm(confirmRequest);

                            call.clone().enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    if(response.isSuccessful()){
                                        Toast.makeText(getContext(), "전송되었습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Toast.makeText(getContext(), "통신오류", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                })
                .setNegativeButton("취소", null)
                .setView(dialogView);
        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }

    private void transmitRemove(){
        RemoveHelperRequest RemoveDTO = new RemoveHelperRequest(LoginPageFragment.saveID, childID);
        Gson gson = new Gson();
        String removeInfo = gson.toJson(RemoveDTO);

        Log.e("JSON", removeInfo);

        Call<ResponseBody> call = userRetrofitInterface.removeHelper(RemoveDTO);
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
