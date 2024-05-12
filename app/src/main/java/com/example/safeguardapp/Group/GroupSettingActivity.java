package com.example.safeguardapp.Group;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.safeguardapp.R;
import com.example.safeguardapp.data.model.Group;
import com.example.safeguardapp.data.repository.GroupRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GroupSettingActivity extends AppCompatActivity {
    private String uuid;
    private GroupRepository repository;
    private LiveData<Optional<Group>> groupStream;
    private ChipGroup aideGroup;

    public static void startActivity(Context context, String uuid) {
        Intent intent = new Intent(context, GroupSettingActivity.class);
        intent.putExtra("uuid", uuid);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null) {
            uuid = getIntent().getStringExtra("uuid");
        }

        if (savedInstanceState != null) {
            uuid = savedInstanceState.getString("uuid");
        }

        if (TextUtils.isEmpty(uuid)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_group_setting);

        repository = GroupRepository.getInstance(this);
        groupStream = Transformations.map(repository.getGroupListStream(), groups ->
                groups.stream().filter(e -> TextUtils.equals(e.getUuid(), uuid)).findFirst());

        groupStream.observe(this, group -> {
            if (!group.isPresent()) {
                finish();
                return;
            }

            updateAideUi();
        });

        initUi();
    }

    private void initUi() {
        ((MaterialToolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());

        findViewById(R.id.change_name_button).setOnClickListener(v -> edit());
        findViewById(R.id.add_aide_button).setOnClickListener(v -> addAide());
        findViewById(R.id.del_gourpbtn).setOnClickListener(v -> remove());

        aideGroup = findViewById(R.id.chip_group);
    }

    private void updateAideUi() {
        aideGroup.removeAllViews();

        Group group = groupStream.getValue().get();
        List<String> aideList = group.getAide();

        for (String id : aideList) {
            Chip chip = new Chip(this);
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("uuid", uuid);
        super.onSaveInstanceState(outState);
    }

    private void edit() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_group_name, null);
        EditText editText = dialogView.findViewById(R.id.name_edit_text);
        editText.setHint(groupStream.getValue().get().getName());

        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(this)
                .setTitle("그룹명 변경")
                .setView(dialogView)
                .setPositiveButton("수정", (dialogInterface, i) -> {
                    String name = editText.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) return;

                    Group group = groupStream.getValue().get();
                    group.setName(name);
                    repository.editGroup(group);

                    Toast.makeText(getApplicationContext(), "수정되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null);

        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }

    private void addAide() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_aide, null);
        EditText editText = dialogView.findViewById(R.id.id_edit_text);

        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(this)
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

                    Toast.makeText(getApplicationContext(), "추가되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null);

        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }

    private void remove() {
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(this)
                .setTitle("그룹 삭제")
                .setMessage("그룹을 삭제 하시겠습니까?")
                .setPositiveButton("삭제", (dialogInterface, i) -> {
                    GroupRepository.getInstance(GroupSettingActivity.this).removeGroup(uuid);

                    Toast.makeText(getApplicationContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null);
        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }
}
