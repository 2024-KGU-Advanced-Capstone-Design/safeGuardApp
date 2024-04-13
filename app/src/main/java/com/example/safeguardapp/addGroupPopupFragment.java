package com.example.safeguardapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class addGroupPopupFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_add_group_popup, null);
        builder.setView(view)
                .setTitle("그룹 추가")
                .setPositiveButton("추가", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 추가 버튼이 클릭되었을 때의 동작
                        // 여기서 버튼을 추가합니다.
                        addNewButton();
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

    private void addNewButton() {
        Button Groupbtn = new Button(getContext());
        Groupbtn.setText("새로운 버튼");

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // 너비
                LinearLayout.LayoutParams.WRAP_CONTENT  // 높이
        );

        // fragment_group.xml에서 버튼을 추가할 레이아웃을 찾습니다.
        LinearLayout containerLayout = requireActivity().findViewById(R.id.group_setting);

        // 레이아웃에 버튼과 파라미터를 추가합니다.
        containerLayout.addView(Groupbtn, layoutParams);
    }

}
