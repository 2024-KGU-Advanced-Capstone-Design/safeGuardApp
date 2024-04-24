package com.example.safeguardapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.safeguardapp.data.model.Group;
import com.example.safeguardapp.data.repository.GroupRepository;

public class addGroupPopupFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_add_group_popup, null);
        EditText nameEditText = ((EditText) view.findViewById(R.id.name_edit_text));
        EditText idEditText = ((EditText) view.findViewById(R.id.id_edit_text));
        EditText passwordEditText = ((EditText) view.findViewById(R.id.password_edit_text));

        builder.setView(view)
                .setTitle("그룹 추가")
                .setPositiveButton("추가", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 추가 버튼이 클릭되었을 때의 동작
                        // 여기서 버튼을 추가합니다.
                        String name = nameEditText.getText().toString().trim();
                        String id = idEditText.getText().toString().trim();
                        String password = passwordEditText.getText().toString().trim();

                        addNewButton(name, id, password);
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 취소 버튼이 클릭되었을 때의 동작
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }

    private void addNewButton(String name, String id, String password) {
        Group group = new Group(name, id, password);
        GroupRepository.getInstance(requireContext()).addGroup(group);
        dismiss();
    }
}
