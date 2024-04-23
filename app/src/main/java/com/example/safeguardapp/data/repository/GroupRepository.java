package com.example.safeguardapp.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.safeguardapp.data.model.Group;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupRepository {
    private static GroupRepository instance;
    private final SharedPreferences prefs;
    private final MutableLiveData<List<Group>> groupList = new MutableLiveData<>();

    public static GroupRepository getInstance(Context context) {
        if (instance == null) {
            instance = new GroupRepository(context);
        }

        return instance;
    }

    private GroupRepository(Context context) {
        prefs = context.getSharedPreferences("com.example.safeguardapp.group", Context.MODE_PRIVATE);
        groupList.postValue(getGroupList());
    }

    public List<Group> getGroupList() {
        String json = prefs.getString("list", "");
        if (TextUtils.isEmpty(json)) {
            return Collections.emptyList();
        }

        Gson gson = new Gson();
        Type type = new TypeToken<List<Group>>() {
        }.getType();

        return gson.fromJson(json, type);
    }

    public void addGroup(Group group) {
        List<Group> groupList = new ArrayList<>(getGroupList());
        groupList.add(group);

        prefs.edit()
                .putString("list", new Gson().toJson(groupList))
                .apply();

        this.groupList.postValue(groupList);
    }

    public LiveData<List<Group>> getGroupListStream() {
        return groupList;
    }
}
