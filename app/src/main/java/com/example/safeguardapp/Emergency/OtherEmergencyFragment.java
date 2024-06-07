package com.example.safeguardapp.Emergency;

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
import com.example.safeguardapp.data.model.ReceivedEmergencyItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

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

public class OtherEmergencyFragment extends Fragment {
    RetrofitClient retrofitClient;
    UserRetrofitInterface userRetrofitInterface;
    private ImageButton transformBtn;
    private String selectedItem, currentEmergencyUuid;
    private ArrayList<String> childList = new ArrayList<>();
    private Map<Integer, List<String>> otherEmergencyDataMap = new HashMap<>();
    private RecyclerView recyclerView;
    private OtherEmergencyAdapter otherEmergencyAdapter;
    private List<ReceivedEmergencyItem> otherEmergencyItemList = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_other_emergency, container, false);

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

        otherEmergencyAdapter = new OtherEmergencyAdapter(otherEmergencyItemList, new OtherEmergencyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ReceivedEmergencyItem item) {
                currentEmergencyUuid = item.getOtherEmergencyUuid();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top, R.anim.slide_in_top, R.anim.slide_out_bottom);
                transaction.replace(R.id.containers, OtherNoticeBoardFragment.newInstance(currentEmergencyUuid, item.getChildName(), item.getDate(), item.getTopkey(), item.getMemberId()));
                transaction.commit();
            }
        });
        recyclerView.setAdapter(otherEmergencyAdapter);

        transformBtn = view.findViewById(R.id.toolbar_image_button);
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> previous());
    }

    private void setupListeners() {
        loadChildList();
        receivedEmergency();
        transformBtn.setOnClickListener(v -> transScreenToMy());
    }

    private void loadChildList() {
        String memberID = LoginPageFragment.saveID;
        GetChildIDRequest memberIDDTO = new GetChildIDRequest(memberID);

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

    private void sendEmergency() {
        String parentId = LoginPageFragment.saveID;

        EmergencyRequest emergencyRequest = new EmergencyRequest(parentId, selectedItem);
        Call<ResponseBody> call = userRetrofitInterface.sendEmergency(emergencyRequest);
        call.clone().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    refreshFragment();
                } else {
                    refreshFragment();
                    Log.e("POST", String.valueOf(response.code()));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void receivedEmergency() {
        String memberId = LoginPageFragment.saveID;

        ReceivedEmergencyRequset receivedEmergencyRequset = new ReceivedEmergencyRequset(memberId);
        Call<ResponseBody> call = userRetrofitInterface.getEmergency(receivedEmergencyRequset);
        call.enqueue(new Callback<ResponseBody>() {
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
                                otherEmergencyAdapter.clearAllItems();
                            }
                        });

                        // 최상위 키 순회
                        for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                            String topKey = it.next();
                            JSONObject innerJson = json.getJSONObject(topKey);

                            // 내부 키 순회
                            List<String> list = otherEmergencyDataMap.get(i);
                            if (list == null) {
                                list = new ArrayList<>();
                                otherEmergencyDataMap.put(i, list);
                            }

                            for (Iterator<String> innerIt = innerJson.keys(); innerIt.hasNext(); ) {
                                String innerKey = innerIt.next();
                                String value = innerJson.getString(innerKey);
                                list.add(value);
                            }

                            otherEmergencyItemList.add(new ReceivedEmergencyItem(topKey, list.get(0), list.get(1), list.get(2), list.get(3), list.get(4), alertText));
                            i += 1;
                        }

                        // 어댑터에 데이터 변경 알림
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                otherEmergencyAdapter.notifyDataSetChanged();
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

    private void transScreenToMy(){
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
        transaction.replace(R.id.containers, new MyEmergencyFragment());
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
        fragmentTransaction.replace(R.id.containers, new OtherEmergencyFragment()); // 여기서 R.id.containers는 프래그먼트가 들어있는 컨테이너의 ID입니다.
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    private static class OtherEmergencyAdapter extends RecyclerView.Adapter<OtherEmergencyAdapter.EmergencyViewHolder> {
        private final List<ReceivedEmergencyItem> otherEmergencyItemList;
        private final OnItemClickListener listener;

        public interface OnItemClickListener {
            void onItemClick(ReceivedEmergencyItem item);
        }

        public OtherEmergencyAdapter(List<ReceivedEmergencyItem> otherEmergencyItemList, OnItemClickListener listener) {
            this.otherEmergencyItemList = otherEmergencyItemList;
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
            ReceivedEmergencyItem item = otherEmergencyItemList.get(position);
            holder.bind(item, listener);
        }

        @Override
        public int getItemCount() {
            return otherEmergencyItemList.size();
        }

        public void clearAllItems() {
            otherEmergencyItemList.clear();
            notifyDataSetChanged();
        }

        static class EmergencyViewHolder extends RecyclerView.ViewHolder {
            public MaterialButton button;

            public EmergencyViewHolder(@NonNull View itemView) {
                super(itemView);
                button = itemView.findViewById(R.id.button);
            }

            public void bind(final ReceivedEmergencyItem item, final OnItemClickListener listener) {
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
