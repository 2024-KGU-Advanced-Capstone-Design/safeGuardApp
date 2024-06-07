package com.example.safeguardapp.Group;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.MainActivity;
import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.UserRetrofitInterface;
import com.example.safeguardapp.data.model.OtherGroup;
import com.example.safeguardapp.data.repository.OtherGroupRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class HelpChildGroupFragment extends Fragment{
    RetrofitClient retrofitClient;
    UserRetrofitInterface userRetrofitInterface;
    private OtherGroupRepository repository;
    private RecyclerView groupListView;
    private String currentGroupUuid;
    private String childID;
    private ImageButton transformBtn;
    private ArrayList<String> otherChildList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getOtherChildID();

        View view = inflater.inflate(R.layout.fragment_helpchild_group, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = OtherGroupRepository.getInstance(requireContext());
        initializeView(view);

        repository.getOtherGroupListStream().observe(getViewLifecycleOwner(), groupList -> {
            groupListView.setAdapter(new OtherGroupAdapter(groupList, new OtherGroupAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(OtherGroup otherGroup) {
                    currentGroupUuid = otherGroup.getUuid(); // 클릭한 그룹의 UUID를 저장
                    childID = otherGroup.getId();
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_bottom, R.anim.slide_in_bottom, R.anim.slide_out_top);
                    transaction.replace(R.id.containers, GroupHelperSettingFragment.newInstance(currentGroupUuid, childID));
                    transaction.commit();
                }
            }));
        });

        LinearLayout linearLayout = view.findViewById(R.id.groupScreen);
        YoYo.with(Techniques.FadeIn).duration(700).repeat(0).playOn(linearLayout);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 뒤로 가기 시 실행되는 코드
                previous();
            }
        });
    }

    private void initializeView(View view) {
        groupListView = view.findViewById(R.id.recycler_view);
        transformBtn = view.findViewById(R.id.toolbar_image_button);

        transformBtn.setOnClickListener(v -> transScreenToMy());
    }

    private void getOtherChildID() {
        // childList 초기화
        otherChildList.clear();

        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

        String memberID = LoginPageFragment.saveID;
        GetOtherChildIDRequest memberIDDTO = new GetOtherChildIDRequest(memberID);
        Gson gson = new Gson();
        String memberInfo = gson.toJson(memberIDDTO);
        Log.e("JSON", memberInfo);

        Call<ResponseBody> call = userRetrofitInterface.getChildAllList(memberIDDTO);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // 응답 본문을 문자열로 변환
                        String responseBodyString = response.body().string();
                        JSONObject json = new JSONObject(responseBodyString);

                        // 각 키-값 쌍을 처리
                        for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                            String topKey = it.next();

                            if(topKey.equals("Helping")) {
                                JSONObject innerJson = json.getJSONObject(topKey);

                                for (Iterator<String> innerIt = innerJson.keys(); innerIt.hasNext(); ) {
                                    String innerKey = innerIt.next();
                                    String value = innerJson.getString(innerKey);
                                    otherChildList.add(value);
                                }
                            }
                        }
                        addGroupFromServer();
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

    private void addGroupFromServer() {
        OtherGroupRepository groupRepository = OtherGroupRepository.getInstance(requireContext());
        String name = "1";
        int number = Integer.parseInt(name);

        for (String child : otherChildList) {
            if (!groupRepository.isOtherChildExists(child)) {
                OtherGroup otherGroup = new OtherGroup(child, child);
                groupRepository.addOtherGroup(otherGroup);

                number++;
                name = Integer.toString(number);
            } else {
                Log.e("addGroupFromServer", "Group for child " + child + " already exists.");
            }
        }
    }

    private void previous(){
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
        transaction.replace(R.id.containers, ((MainActivity) requireActivity()).mapFragment);
        transaction.commit();

        BottomNavigationView navigationView = requireActivity().findViewById(R.id.bottom_navigationview);
        navigationView.setSelectedItemId(R.id.map);
    }

    private void transScreenToMy(){
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
        transaction.replace(R.id.containers, new GroupFragment());
        transaction.commit();
    }

    private static class OtherGroupAdapter extends RecyclerView.Adapter<OtherGroupAdapter.OtherGroupItemViewHolder>{
        private final List<OtherGroup> otherGroupList;
        private final OnItemClickListener listener;

        public interface OnItemClickListener {
            void onItemClick(OtherGroup otherGroup);
        }

        public OtherGroupAdapter(List<OtherGroup> otherGroupList, OnItemClickListener listener) {
            this.otherGroupList = otherGroupList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public OtherGroupItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_group, parent, false);
            return new OtherGroupItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OtherGroupItemViewHolder holder, int position) {
            OtherGroup otherGroup = otherGroupList.get(position);
            holder.bind(otherGroup, listener);
        }

        @Override
        public int getItemCount() {
            return otherGroupList.size();
        }

        static class OtherGroupItemViewHolder extends RecyclerView.ViewHolder {
            public MaterialButton button;

            public OtherGroupItemViewHolder(@NonNull View itemView) {
                super(itemView);
                button = itemView.findViewById(R.id.button);
            }

            public void bind(final OtherGroup otherGroup, final OnItemClickListener listener) {
                button.setText(otherGroup.getName());
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(otherGroup);
                    }
                });
            }
        }
    }
}
