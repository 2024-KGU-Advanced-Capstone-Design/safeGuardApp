package com.example.safeguardapp.Group;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.MainActivity;
import com.example.safeguardapp.R;
import com.example.safeguardapp.data.model.Group;
import com.example.safeguardapp.data.repository.GroupRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GroupSettingFragment extends Fragment {
    private String uuid;
    private String childID;
    private GroupRepository repository;
    private LiveData<Optional<Group>> groupStream;
    private ChipGroup aideGroup;

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

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> previous());

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
    private void mapSectorSet() {
        Bundle args = new Bundle();
        args.putString("UUID", uuid);
        args.putString("childID", childID);
        SectorMapFragment sectorMapfragment = new SectorMapFragment();
        sectorMapfragment.setArguments(args);
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.containers, sectorMapfragment);
        fragmentTransaction.commit();
    }

    //아이 아이디 찾기
    private void findChildID() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.containers, new FindChildIDFragment());
        fragmentTransaction.commit();
    }

    //아이 비밀번호 찾기
    private void findChildPW() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.containers, new FindChildPWFragment());
        fragmentTransaction.commit();
    }

    //그룹 삭제
    private void remove() {
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(getContext())
                .setTitle("그룹 삭제")
                .setMessage("그룹을 삭제 하시겠습니까?")
                .setPositiveButton("삭제", (dialogInterface, i) -> {
                    previous();
                    GroupRepository.getInstance(getContext()).removeGroup(uuid);
                    Toast.makeText(getContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null);

        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }
}
