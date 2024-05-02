package com.example.safeguardapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safeguardapp.data.model.Group;
import com.example.safeguardapp.data.repository.GroupRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class GroupFragment extends Fragment {
    private GroupRepository repository;
    private RecyclerView groupListView;
    private Button addGroupBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = GroupRepository.getInstance(requireContext());

        // Inflate the layout for this fragment
        initializeView(view);
        setupListeners();

        repository.getGroupListStream().observe(getViewLifecycleOwner(), groupList -> {
            groupListView.setAdapter(new GroupAdapter(groupList, new GroupAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Group group) {
                    GroupSettingActivity.startActivity(requireContext(), group.getUuid());
                }
            }));
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 뒤로 가기 시 실행되는 코드
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                // 이동 시에는 이미 생성된 mapFragment를 사용하여 교체
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
    }

    private void setupListeners() {
        addGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addGroupPopupFragment addGroupPopupFragment = new addGroupPopupFragment();

                // DialogFragment를 보여줍니다.
                addGroupPopupFragment.show(getFragmentManager(), "addGroupPopupFragment");
            }
        });
    }

    private static class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupItemViewHolder> {
        private final List<Group> groupList;
        private final OnItemClickListener listener;

        public interface OnItemClickListener {
            void onItemClick(Group group);
        }

        public GroupAdapter(List<Group> groupList, OnItemClickListener listener) {
            this.groupList = groupList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public GroupItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_group, parent, false);
            return new GroupItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GroupItemViewHolder holder, int position) {
            Group group = groupList.get(position);
            holder.bind(group, listener);
        }

        @Override
        public int getItemCount() {
            return groupList.size();
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
