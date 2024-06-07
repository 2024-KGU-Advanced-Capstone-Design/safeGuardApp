package com.example.safeguardapp.Emergency;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.UserRetrofitInterface;
import com.example.safeguardapp.data.model.EmergencyCommentItem;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtherNoticeBoardFragment extends Fragment {
    private RetrofitClient retrofitClient;
    private UserRetrofitInterface userRetrofitInterface;
    private String uuid, childName, date, inputText, emergencyId, senderId, currentCommentId;
    private Button emergModify;
    private MaterialToolbar title;
    private TextView dateSet, textSet;
    private RecyclerView recyclerView;
    private Map<Integer, List<String>> commentDataMap = new HashMap<>();
    private List<EmergencyCommentItem> commentItemList = new ArrayList<>();
    private OtherCommentAdapter otherCommentAdapter;


    public static OtherNoticeBoardFragment newInstance(String uuid, String childName, String date, String emergencyId, String senderId){
        OtherNoticeBoardFragment otherNoticeBoardFragment = new OtherNoticeBoardFragment();
        Bundle args = new Bundle();
        args.putString("uuid", uuid);
        args.putString("childName", childName);
        args.putString("date", date);
        args.putString("emergencyId", emergencyId);
        args.putString("senderId", senderId);
        otherNoticeBoardFragment.setArguments(args);
        return otherNoticeBoardFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            uuid = getArguments().getString("uuid");
            childName = getArguments().getString("childName");
            date = getArguments().getString("date");
            emergencyId = getArguments().getString("emergencyId");
            senderId = getArguments().getString("senderId");
        }

        if (TextUtils.isEmpty(uuid)) {
            previous();
            return;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_other_emergency_notice_board, container, false);

        initializeView(view);
        setupListeners();

        return view;
    }

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

    private void initializeView(View view){
        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

        emergModify = view.findViewById(R.id.comment_write);
        title = view.findViewById(R.id.toolbar);
        dateSet = view.findViewById(R.id.emergency_date);
        textSet = view.findViewById(R.id.emergency_text);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        otherCommentAdapter = new OtherCommentAdapter(commentItemList, senderId, new OtherCommentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(EmergencyCommentItem item) {
                currentCommentId = item.getTopkey();
                delCommentMethod();
            }
        });
        recyclerView.setAdapter(otherCommentAdapter);
    }

    private void setupListeners(){
        emergModify.setOnClickListener(v -> emergModifyMethod());
        title.setTitle(childName + " 긴급 알림 게시판");

        dateSet.setText(date);

        textSet.setText(childName + "의 아이의 위치가 확인되지 않고 있습니다. 근처에 계신 분들의 힘이 필요합니다");

        title.setNavigationOnClickListener(v -> previous());
        loadComment();
    }

    private void emergModifyMethod() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_emergency_notice, null);

        EditText editTextInDialog = dialogView.findViewById(R.id.comment_editText);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("작성할 내용을 입력해주세요.")
                .setView(dialogView) // 커스텀 레이아웃 설정
                .setPositiveButton("OK", (dialog, which) -> {
                    // OK 버튼 클릭 시 처리할 코드
                    inputText = editTextInDialog.getText().toString();
                    Log.e("POST", inputText);

                    if (!TextUtils.isEmpty(inputText)) {
                        commentSendToServer();
                    } else {
                        Log.e("InputText", "입력된 텍스트가 없습니다.");
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Cancel 버튼 클릭 시 처리할 코드
                    dialog.dismiss();
                });

        // 다이얼로그 표시
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void commentSendToServer(){
        String commentId = LoginPageFragment.saveID;

        CommentSendRequest commentSendRequest = new CommentSendRequest(commentId, inputText, emergencyId);
        Call<ResponseBody> call = userRetrofitInterface.sendComment(commentSendRequest);
        call.clone().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    Log.e("POST", "댓글 전송 성공");
                    loadComment();
                }
                else
                    Log.e("POST", String.valueOf(response.code()));
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void loadComment(){
        CommentLoadRequest commentLoadRequest = new CommentLoadRequest(emergencyId);
        Call<ResponseBody> call = userRetrofitInterface.loadComment(commentLoadRequest);
        call.clone().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful() && response.body() != null){
                    try {
                        // 응답 본문을 문자열로 변환
                        String responseBodyString = response.body().string();
                        JSONObject json = new JSONObject(responseBodyString);
                        int i = 0;

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                otherCommentAdapter.clearAllItems();
                            }
                        });

                        commentItemList.clear();
                        commentDataMap.clear();
                        // 최상위 키 순회
                        for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                            String topKey = it.next();
                            JSONObject innerJson = json.getJSONObject(topKey);

                            // 내부 키 순회
                            List<String> list = commentDataMap.get(i);
                            if (list == null) {
                                list = new ArrayList<>();
                                commentDataMap.put(i, list);
                            }

                            for (Iterator<String> innerIt = innerJson.keys(); innerIt.hasNext(); ) {
                                String innerKey = innerIt.next();
                                String value = innerJson.getString(innerKey);
                                list.add(value);
                            }
                            commentItemList.add(new EmergencyCommentItem(topKey, list.get(0), list.get(1), list.get(2)));
                            i += 1;
                        }

                        // 어댑터에 데이터 변경 알림
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                otherCommentAdapter.notifyDataSetChanged();
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("POST", String.valueOf(response.code()));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void delCommentMethod() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("댓글 삭제")
                .setMessage("정말로 댓글을 삭제하시겠습니까?\n삭제하시려면 '확인'버튼을 눌러주세요") // 커스텀 레이아웃 설정
                .setPositiveButton("확인", (dialog, which) -> {
                    DeleteCommentRequest deleteCommentRequest = new DeleteCommentRequest(currentCommentId);
                    Call<ResponseBody> call = userRetrofitInterface.deleteComment(deleteCommentRequest);
                    call.clone().enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()){
                                loadComment();
                            }
                            else
                                Log.e("POST", String.valueOf(response.code()));
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                        }
                    });
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    // Cancel 버튼 클릭 시 처리할 코드
                    dialog.dismiss();
                });

        // 다이얼로그 표시
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void previous(){
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_bottom, R.anim.slide_in_bottom, R.anim.slide_out_top);
        transaction.replace(R.id.containers, new OtherEmergencyFragment());
        transaction.commit();
    }

    private static class OtherCommentAdapter extends RecyclerView.Adapter<OtherCommentAdapter.EmergencyViewHolder> {
        private final List<EmergencyCommentItem> emergencyCommentList;
        private final OtherCommentAdapter.OnItemClickListener listener;
        private final String senderId;

        public interface OnItemClickListener {
            void onItemClick(EmergencyCommentItem item);
        }

        public OtherCommentAdapter(List<EmergencyCommentItem> emergencyCommentList, String senderId, OtherCommentAdapter.OnItemClickListener listener) {
            this.emergencyCommentList = emergencyCommentList;
            this.senderId = senderId;
            this.listener = listener;
        }

        @NonNull
        @Override
        public OtherCommentAdapter.EmergencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_comment_style, parent, false);
            return new OtherCommentAdapter.EmergencyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OtherCommentAdapter.EmergencyViewHolder holder, int position) {
            EmergencyCommentItem item = emergencyCommentList.get(position);
            holder.bind(item, listener, senderId);
        }

        @Override
        public int getItemCount() {
            return emergencyCommentList.size();
        }
        public void clearAllItems() {
            emergencyCommentList.clear();
            notifyDataSetChanged();
        }

        static class EmergencyViewHolder extends RecyclerView.ViewHolder {
            public TextView commentId, commentDate, commentText;
            public Button commentDelete;

            public EmergencyViewHolder(@NonNull View itemView) {
                super(itemView);
                commentId = itemView.findViewById(R.id.comment_id);
                commentDate = itemView.findViewById(R.id.comment_date);
                commentText = itemView.findViewById(R.id.comment_text);
                commentDelete = itemView.findViewById(R.id.comment_delete);
            }

            public void bind(final EmergencyCommentItem item, final OtherCommentAdapter.OnItemClickListener listener, final String senderId) {
                if(item.getCommentator().equals(senderId)){
                    commentId.setText("글쓴이");
                }
                else{
                    commentDelete.setVisibility(View.GONE);
                    commentId.setText(item.getCommentator());
                }
                commentDate.setText(item.getCommentdate());
                commentText.setText(item.getContent());
            }
        }
    }
}
