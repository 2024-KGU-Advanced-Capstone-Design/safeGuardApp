package com.example.safeguardapp.Group;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.safeguardapp.Group.Sector.ChildListAllRequest;
import com.example.safeguardapp.LogIn.LoginPageFragment;
import com.example.safeguardapp.MainActivity;
import com.example.safeguardapp.R;
import com.example.safeguardapp.RetrofitClient;
import com.example.safeguardapp.UserRetrofitInterface;
import com.example.safeguardapp.data.model.Group;
import com.example.safeguardapp.data.repository.GroupRepository;
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

public class GroupFragment extends Fragment {
    RetrofitClient retrofitClient;
    UserRetrofitInterface userRetrofitInterface;
    private GroupRepository repository;
    private RecyclerView groupListView;
    private Button addGroupBtn;
    private String currentGroupUuid;
    private GroupAdapter groupAdapter;
    private String childID;
    private ArrayList<String> memberChildList = new ArrayList<>();
    private ArrayList<String> helperChildList = new ArrayList<>();

    private static final String MEMBER_CHILD_LIST_KEY = "memberChildList";
    private static final String HELPER_CHILD_LIST_KEY = "helperChildList";

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(MEMBER_CHILD_LIST_KEY, memberChildList);
        outState.putStringArrayList(HELPER_CHILD_LIST_KEY, helperChildList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        if (savedInstanceState != null) {
            memberChildList = savedInstanceState.getStringArrayList(MEMBER_CHILD_LIST_KEY);
            helperChildList = savedInstanceState.getStringArrayList(HELPER_CHILD_LIST_KEY);
        } else {
            getChildID();
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = GroupRepository.getInstance(requireContext());
        initializeView(view);
        setupListeners();

        groupAdapter = new GroupAdapter(new ArrayList<>(), memberChildList, helperChildList, new GroupAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Group group) {
                currentGroupUuid = group.getUuid(); // 클릭한 그룹의 UUID를 저장
                childID = group.getId();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_bottom, R.anim.slide_in_bottom, R.anim.slide_out_top);
                transaction.replace(R.id.containers, GroupSettingFragment.newInstance(currentGroupUuid, childID));
                transaction.commit();
            }
        });

        groupListView.setAdapter(groupAdapter);

        repository.getGroupListStream().observe(getViewLifecycleOwner(), new Observer<List<Group>>() {
            @Override
            public void onChanged(List<Group> groupList) {
                groupAdapter.updateGroups(groupList);
            }
        });

        LinearLayout linearLayout = view.findViewById(R.id.groupScreen);
        YoYo.with(Techniques.FadeIn).duration(700).repeat(0).playOn(linearLayout);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 뒤로 가기 시 실행되는 코드
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
                transaction.replace(R.id.containers, ((MainActivity) requireActivity()).mapFragment);
                transaction.commit();

                BottomNavigationView navigationView = requireActivity().findViewById(R.id.bottom_navigationview);
                navigationView.setSelectedItemId(R.id.map);
            }
        });
    }

    private void initializeView(View view) {
        addGroupBtn = view.findViewById(R.id.add_group_btn);
        groupListView = view.findViewById(R.id.recycler_view);
        groupListView.setLayoutManager(new LinearLayoutManager(requireContext())); // 레이아웃 매니저 설정
    }

    private void setupListeners() {
        addGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top, R.anim.slide_in_top, R.anim.slide_out_bottom);
                fragmentTransaction.replace(R.id.containers, new AddGroupPopupFragment());
                fragmentTransaction.commit();
            }
        });
    }

    private void getChildID() {
        // childList 초기화
        memberChildList.clear();
        helperChildList.clear();

        retrofitClient = RetrofitClient.getInstance();
        userRetrofitInterface = RetrofitClient.getInstance().getUserRetrofitInterface();

        String memberID = LoginPageFragment.saveID;
        ChildListAllRequest memberIDDTO = new ChildListAllRequest(memberID);
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

                        for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                            String topKey = it.next();
                            if (topKey.equals("Parenting")) {  // 최상위 키가 "Parenting"인 경우만 처리
                                JSONObject innerJson = json.getJSONObject(topKey);

                                // 내부 키 순회
                                for (Iterator<String> innerIt = innerJson.keys(); innerIt.hasNext(); ) {
                                    String innerKey = innerIt.next();
                                    String value = innerJson.getString(innerKey);

                                    memberChildList.add(value);
                                    Log.e("POST", memberChildList.get(0));
                                }
                            } else if (topKey.equals("Helping")) {
                                JSONObject innerJson = json.getJSONObject(topKey);

                                // 내부 키 순회
                                for (Iterator<String> innerIt = innerJson.keys(); innerIt.hasNext(); ) {
                                    String innerKey = innerIt.next();
                                    String value = innerJson.getString(innerKey);

                                    helperChildList.add(value);
                                    Log.e("POST", helperChildList.get(0));

                                }
                            }
                        }
                        addMemberGroupFromServer();

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                groupAdapter.notifyDataSetChanged();
                            }
                        });
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

    private void addMemberGroupFromServer() {
        GroupRepository groupRepository = GroupRepository.getInstance(requireContext());
        for (String child : memberChildList) {
            if (!groupRepository.isChildExists(child)) {
                Group group = new Group(child, child);
                groupRepository.addGroup(group);
            }
        }
        for (String child : helperChildList) {
            if (!groupRepository.isChildExists(child)) {
                Group group = new Group(child, child);
                groupRepository.addGroup(group);
            }
        }
    }

    private static class GroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<Group> groupList;
        private final List<String> memberChildList;
        private final List<String> helperChildList;
        private final OnItemClickListener listener;
        private static final int TYPE_PARENTING = 0;
        private static final int TYPE_HELPING = 1;
        private static final int TYPE_HEADER = 2;

        public interface OnItemClickListener {
            void onItemClick(Group group);
        }

        public GroupAdapter(List<Group> groupList, List<String> memberChildList, List<String> helperChildList, OnItemClickListener listener) {
            this.groupList = groupList;
            this.memberChildList = memberChildList;
            this.helperChildList = helperChildList;
            this.listener = listener;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_HEADER; // Parenting Header
            } else if (position == memberChildList.size() + 1) {
                return TYPE_HEADER; // Helping Header
            } else if (position <= memberChildList.size()) {
                return TYPE_PARENTING;
            } else {
                return TYPE_HELPING;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_HEADER) {
                View view = inflater.inflate(R.layout.item_header, parent, false);
                return new HeaderViewHolder(view);
            } else {
                View view = inflater.inflate(R.layout.item_group, parent, false);
                return new GroupItemViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder.getItemViewType() == TYPE_HEADER) {
                HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
                if (position == 0) {
                    headerHolder.title.setText("Parenting");
                } else {
                    headerHolder.title.setText("Helping");
                }
            } else {
                Group group;
                if (position <= memberChildList.size()) {
                    group = new Group(memberChildList.get(position - 1), memberChildList.get(position - 1));
                } else {
                    int helperPosition = position - memberChildList.size() - 2;
                    group = new Group(helperChildList.get(helperPosition), helperChildList.get(helperPosition));
                }
                ((GroupItemViewHolder) holder).bind(group, listener);
            }
        }

        @Override
        public int getItemCount() {
            return memberChildList.size() + helperChildList.size() + 2; // Adjust for header counts
        }

        public void updateGroups(List<Group> groups) {
            groupList.clear();
            groupList.addAll(groups);
            notifyDataSetChanged();
        }

        static class HeaderViewHolder extends RecyclerView.ViewHolder {
            public TextView title;

            public HeaderViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.header_title);
            }
        }

        static class GroupItemViewHolder extends RecyclerView.ViewHolder {
            public MaterialButton button;

            public GroupItemViewHolder(@NonNull View itemView) {
                super(itemView);
                button = itemView.findViewById(R.id.button);
            }

            public void bind(final Group group, final OnItemClickListener listener) {
                button.setText(group.getName());
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(group);
                    }
                });
            }
        }
    }
}
