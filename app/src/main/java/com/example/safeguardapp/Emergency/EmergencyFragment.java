package com.example.safeguardapp.Emergency;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.safeguardapp.Group.GetChildIDRequest;
import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.MainActivity;
import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.UserRetrofitInterface;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmergencyFragment extends Fragment {
    RetrofitClient retrofitClient;
    UserRetrofitInterface userRetrofitInterface;
    private Button addEmergencyBtn;
    private String selectedItem;
    private ArrayList<String> childList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_emergency, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeView(view);
        setupListeners();

    }

    private void initializeView(View view) {
        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

        addEmergencyBtn = view.findViewById(R.id.add_emergency_btn);
    }

    private void setupListeners() {
        loadChildList();
        sentEmergency();
        addEmergencyBtn.setOnClickListener(v -> addEmergency());
    }

    private void addEmergency() {
        int checknum = 0;

        final String[] items = new String[childList.size()];
        childList.toArray(items);

        final int[] selectedPosition = {checknum};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose an option")
                .setSingleChoiceItems(items, checknum, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 사용자가 항목을 선택할 때마다 호출됨
                        // 선택된 항목의 인덱스는 which에 저장됨
                        selectedPosition[0] = which;
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
                .setNegativeButton("Cancel", null);
        AlertDialog msgDlg = builder.create();
        msgDlg.show();
    }

    private void loadChildList() {
        String memberID = LoginPageFragment.saveID;
        GetChildIDRequest memberIDDTO = new GetChildIDRequest(memberID);
        Gson gson = new Gson();
        String memberInfo = gson.toJson(memberIDDTO);
        Log.e("JSON", memberInfo);

        Call<ResponseBody> call = userRetrofitInterface.getChildID(memberIDDTO);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("POST", "응답성공");
                    try {
                        // 응답 본문을 문자열로 변환
                        String responseBodyString = response.body().string();
                        JSONObject json = new JSONObject(responseBodyString);
                        Log.e("Response JSON", json.toString());
                        Log.e("POST", "응답성공");

                        // 각 키-값 쌍을 처리
                        for (Iterator<String> keys = json.keys(); keys.hasNext(); ) {
                            String key = keys.next();
                            String value = json.getString(key);
                            if (key.equals("status")) {
                            } else {
                                childList.add(value);
                            }

                            Log.e("Child ID", "Key: " + key + ", Value: " + value);
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
        double latitude = MainActivity.latitude;
        double logitude = MainActivity.longitude;

        EmergencyRequest emergencyRequest = new EmergencyRequest(parentId, selectedItem, latitude, logitude);
        Call<ResponseBody> call = userRetrofitInterface.sendEmergency(emergencyRequest);
        call.clone().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    sentEmergency();
                } else {
                    Log.e("POST", String.valueOf(response.code()));
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
        Call<ResponseBody> call = userRetrofitInterface.sentEmergency(sentEmergencyRequest);
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

                        // 최상위 키 순회
                        for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                            String topKey = it.next();

                            JSONObject innerJson = json.getJSONObject(topKey);

                            // 내부 키 순회
                            for (Iterator<String> innerIt = innerJson.keys(); innerIt.hasNext(); ) {
                                String innerKey = innerIt.next();
                                /*if(innerKey.equals(""))
                                String value = innerJson.getString(innerKey);

                                // memberList에 값 추가
                                helperList.add(value);

                                updateAideUi();
                                // 여기서 키와 값을 사용할 수 있습니다.
                                Log.e("Parsed Data", "TopKey: " + topKey + ", InnerKey: " + innerKey + ", Value: " + value);*/
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    //받은 emergency 조회(다른 사람이 보낸 emergency)
    private void receivedEmergency() {
        String memberId = LoginPageFragment.saveID;

        ReceivedEmergencyRequset receivedEmergencyRequset = new ReceivedEmergencyRequset(memberId);
        Call<ResponseBody> call = userRetrofitInterface.getEmergency(receivedEmergencyRequset);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}

