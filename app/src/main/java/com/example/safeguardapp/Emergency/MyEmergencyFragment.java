package com.example.safeguardapp.Emergency;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safeguardapp.Group.GetChildIDRequest;
import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.MainActivity;
import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.UserRetrofitInterface;
import com.example.safeguardapp.data.model.SentEmergencyItem;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

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

public class MyEmergencyFragment extends Fragment {
    RetrofitClient retrofitClient;
    UserRetrofitInterface userRetrofitInterface;
    private FloatingActionButton addEmergencyBtn;
    private ImageButton transformBtn;
    private String selectedItem, currentEmergencyUuid;
    private ArrayList<String> childList = new ArrayList<>();
    private Map<Integer, List<String>> emergencyDataMap = new HashMap<>();
    private RecyclerView recyclerView;
    private MyEmergencyAdapter  myEmergencyAdapter;

    private List<SentEmergencyItem> emergencyItemList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_emergency, container, false);

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

    private void initializeView(View view) {
        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        myEmergencyAdapter = new MyEmergencyAdapter(emergencyItemList, new MyEmergencyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(SentEmergencyItem item) {
                currentEmergencyUuid = item.getMyEmergencyUuid();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top, R.anim.slide_in_top, R.anim.slide_out_bottom);
                transaction.replace(R.id.containers, MyNoticeBoardFragment.newInstance(currentEmergencyUuid, item.getChildName(), item.getDate(), item.getTopkey(), item.getMemberId()));
                transaction.commit();
            }
        });
        recyclerView.setAdapter(myEmergencyAdapter);

        addEmergencyBtn = view.findViewById(R.id.add_emergency_btn);
        transformBtn = view.findViewById(R.id.toolbar_image_button);
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> previous());
    }

    private void setupListeners() {
        loadChildList();
        sentEmergency();
        addEmergencyBtn.setOnClickListener(v -> addEmergency());
        transformBtn.setOnClickListener(v -> transScreenToOther());
    }

    private void addEmergency() {
        int checknum = 0;

        final String[] items = new String[childList.size()];
        childList.toArray(items);

        final int[] selectedPosition = {checknum};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("긴급 알림 전송")
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
                            sendEmergency();
                        }
                    }
                })
                .setNegativeButton("취소", null);
        AlertDialog msgDlg = builder.create();
        msgDlg.show();

        msgDlg.getButton(AlertDialog.BUTTON_POSITIVE).post(() -> {
            msgDlg.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
            msgDlg.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        });
    }

    private void loadChildList() {
        String memberID = LoginPageFragment.saveID;
        GetChildIDRequest memberIDDTO = new GetChildIDRequest(memberID);
        Gson gson = new Gson();
        String memberInfo = gson.toJson(memberIDDTO);

        Call<ResponseBody> call = userRetrofitInterface.getChildID(memberIDDTO);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // 응답 본문을 문자열로 변환
                        String responseBodyString = response.body().string();
                        JSONObject json = new JSONObject(responseBodyString);

                        // 각 키-값 쌍을 처리
                        for (Iterator<String> keys = json.keys(); keys.hasNext(); ) {
                            String key = keys.next();
                            String value = json.getString(key);
                            if (key.equals("status")) {
                            } else {
                                childList.add(value);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("getChildID", "Response body is null or request failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Log.e("getChildID", "Request failed", t);
            }
        });
    }

    //emergency 보내기
    private void sendEmergency() {
        String parentId = LoginPageFragment.saveID;
        Log.e("POST", "sendEmergency 실행");

        EmergencyRequest emergencyRequest = new EmergencyRequest(parentId, selectedItem);
        Call<ResponseBody> call = userRetrofitInterface.sendEmergency(emergencyRequest);
        call.clone().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.e("POST", "emergency 전송 성공");
                    refreshFragment();
                } else {
                    refreshFragment();
                    Log.e("POST", "응답 코드: " + response.code() + ", 오류 메시지 없음");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    //내가 보낸 emergency 조회
    private void sentEmergency() {
        String memberId = LoginPageFragment.saveID;

        SentEmergencyRequest sentEmergencyRequest = new SentEmergencyRequest(memberId);
        Gson gson = new Gson();
        String dto = gson.toJson(sentEmergencyRequest);

        Call<ResponseBody> call = userRetrofitInterface.sentEmergency(sentEmergencyRequest);
        call.clone().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // 응답 본문을 문자열로 변환
                        String responseBodyString = response.body().string();
                        JSONObject json = new JSONObject(responseBodyString);
                        int i = 0;
                        String alertText = "긴급 알림";

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myEmergencyAdapter.clearAllItems();
                            }
                        });

                        // 최상위 키 순회
                        for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                            String topKey = it.next();
                            JSONObject innerJson = json.getJSONObject(topKey);

                            // 내부 키 순회
                            List<String> list = emergencyDataMap.get(i);
                            if (list == null) {
                                list = new ArrayList<>();
                                emergencyDataMap.put(i, list);
                            }

                            for (Iterator<String> innerIt = innerJson.keys(); innerIt.hasNext(); ) {
                                String innerKey = innerIt.next();
                                String value = innerJson.getString(innerKey);
                                list.add(value);
                            }
                            emergencyItemList.add(new SentEmergencyItem(topKey, list.get(0), list.get(1), list.get(2), list.get(3), list.get(4), alertText));
                            i += 1;
                        }

                        // 어댑터에 데이터 변경 알림
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myEmergencyAdapter.notifyDataSetChanged();
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
                t.printStackTrace();
            }
        });
    }

    private void transScreenToOther(){
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.replace(R.id.containers, new OtherEmergencyFragment());
        transaction.commit();
    }

    private void previous(){
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        // 이동 시에는 이미 생성된 mapFragment를 사용하여 교체
        transaction.replace(R.id.containers, ((MainActivity) requireActivity()).mapFragment);
        transaction.commit();

        BottomNavigationView navigationView = requireActivity().findViewById(R.id.bottom_navigationview);
        navigationView.setSelectedItemId(R.id.map);
    }

    private void refreshFragment() {
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.containers, new MyEmergencyFragment()); // 여기서 R.id.containers는 프래그먼트가 들어있는 컨테이너의 ID입니다.
        fragmentTransaction.commit();
    }

    //recyclerView adapter 설정
    private static class MyEmergencyAdapter extends RecyclerView.Adapter<MyEmergencyAdapter.EmergencyViewHolder> {
        private final List<SentEmergencyItem> emergencyItemList;
        private final OnItemClickListener listener;

        public interface OnItemClickListener {
            void onItemClick(SentEmergencyItem item);
        }

        public MyEmergencyAdapter(List<SentEmergencyItem> emergencyItemList, OnItemClickListener listener) {
            this.emergencyItemList = emergencyItemList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public EmergencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_group, parent, false);
            return new EmergencyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull EmergencyViewHolder holder, int position) {
            SentEmergencyItem item = emergencyItemList.get(position);
            holder.bind(item, listener);
        }

        @Override
        public int getItemCount() {
            return emergencyItemList.size();
        }
        public void clearAllItems() {
            emergencyItemList.clear();
            notifyDataSetChanged();
        }

        static class EmergencyViewHolder extends RecyclerView.ViewHolder {
            public MaterialButton button;

            public EmergencyViewHolder(@NonNull View itemView) {
                super(itemView);
                button = itemView.findViewById(R.id.button);
            }

            public void bind(final SentEmergencyItem item, final OnItemClickListener listener) {
                button.setText(item.getTopkey() + " " + item.getChildName() + " " + item.getAlertText());
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(item);
                    }
                });
            }
        }
    }
}