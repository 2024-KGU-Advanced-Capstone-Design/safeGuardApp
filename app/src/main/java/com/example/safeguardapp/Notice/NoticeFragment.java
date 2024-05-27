package com.example.safeguardapp.Notice;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safeguardapp.Group.GroupFragment;
import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.UserRetrofitInterface;
import com.example.safeguardapp.data.model.Group;
import com.example.safeguardapp.data.model.Notice;
import com.google.android.material.button.MaterialButton;

import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.ResponseBody;

public class NoticeFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private RetrofitClient retrofitClient;
    private UserRetrofitInterface userRetrofitInterface;

    private RecyclerView recyclerView;
    private NoticeAdapter noticeAdapter;
    private ArrayList<Notice> noticeList = new ArrayList<>();

    public NoticeFragment() {
        // Required empty public constructor
    }
    public static NoticeFragment newInstance(String param1, String param2) {
        NoticeFragment fragment = new NoticeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 프래그먼트 레이아웃을 인플레이트
        View view = inflater.inflate(R.layout.fragment_notice, container, false);
        getNoti();
        // 여기서 RecyclerView 초기화
        recyclerView = view.findViewById(R.id.recyclerView);

        // Null check for recyclerView
        if (recyclerView != null) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

            noticeAdapter = new NoticeAdapter(getContext(), noticeList);
            recyclerView.setAdapter(noticeAdapter);
        } else {
            Log.e("NoticeFragment", "RecyclerView is null");
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 이 부분은 더 이상 필요 없음, onCreateView에서 이미 처리됨
        /*
        recyclerView = view.findViewById(R.id.recyclerView);

        // Null check for recyclerView
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            noticeAdapter = new NoticeAdapter(getContext(), noticeList);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setAdapter(noticeAdapter);
            recyclerView.setLayoutManager(linearLayoutManager);
        } else {
            Log.e("NoticeFragment", "RecyclerView is null");
        }
        */
    }

    public void getNoti() {
        GetNotificationRequest getNotificationRequest = new GetNotificationRequest(LoginPageFragment.saveID);

        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

        Call<ResponseBody> call = userRetrofitInterface.getNotification(getNotificationRequest);
        Log.e("STRING","hi");
        call.clone().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("POST", "응답성공");
                    try {
                        // 응답 본문을 문자열로 변환
                        String responseBodyString = response.body().string();
                        JSONObject json = new JSONObject(responseBodyString);
                        Log.e("Response JSON", json.toString());
                        Log.e("POST","대체 뭐임");
                        // 최상위 키 순회
                        for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                            String topKey = it.next();
                            if (topKey.equals("ARRIVED") || topKey.equals("UNCONFIRMED") || topKey.equals("DEPART")) {  // 최상위 키가 "Parenting"인 경우만 처리
                                JSONObject innerJson = json.getJSONObject(topKey);

                                String title = innerJson.getString("title");
                                String content = innerJson.getString("content");
                                String date = innerJson.getString("date");
                                String type = innerJson.getString("type");
                                String child = innerJson.getString("child");

                                Notice newNotice = new Notice(title, content, date, type, child);

                                noticeList.add(newNotice);
                                Log.e("POST","add 완료");
                            }
                        }
                        // 어댑터에 변경 사항 알림
                        noticeAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Log.e("POST", String.valueOf(response.code()));
                }
                Log.e("POST","대체 뭐임");

            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("POST","왜 실패함");
            }
        });

    }
}
