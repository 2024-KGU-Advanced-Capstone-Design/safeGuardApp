package com.example.safeguardapp.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.safeguardapp.data.model.OtherGroup;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OtherGroupRepository {
    private static OtherGroupRepository instance;
    private final SharedPreferences prefs;
    private final MutableLiveData<List<OtherGroup>> otherGroupList = new MutableLiveData<>();

    public static OtherGroupRepository getInstance(Context context) {
        if (instance == null) {
            instance = new OtherGroupRepository(context);
        }

        return instance;
    }

    private OtherGroupRepository(Context context){
        prefs = context.getSharedPreferences("com.example.safeGuardapp.group.2", Context.MODE_PRIVATE);
        otherGroupList.postValue(getOtherGroupList());
    }

    public List<OtherGroup> getOtherGroupList(){
        String json = prefs.getString("list1", "");
        if(TextUtils.isEmpty(json)){
            return Collections.emptyList();
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<OtherGroup>>(){
        }.getType();

        return  gson.fromJson(json, type);
    }

    public void addOtherGroup(OtherGroup otherGroup){
        List<OtherGroup> otherGroupList = new ArrayList<>(getOtherGroupList());
        otherGroupList.add(otherGroup);

        prefs.edit()
                .putString("list1", new Gson().toJson(otherGroupList))
                .apply();

        this.otherGroupList.postValue(otherGroupList);
    }

    public void editOtherGroup(OtherGroup otherGroup){
        List<OtherGroup> otherGroupList = new ArrayList<>(getOtherGroupList());
        if(otherGroupList.isEmpty()) return;

        for(int i = 0; i < otherGroupList.size(); i++){
            if(TextUtils.equals(otherGroup.getUuid(), otherGroupList.get(i).getUuid())){
                otherGroupList.set(i, otherGroup);
            }
        }
    }

    public boolean isOtherChildExists(String otherChild){
        List<OtherGroup> otherGroups = getOtherGroupList();
        for(OtherGroup otherGroup : otherGroups){
            if(TextUtils.equals(otherGroup.getId(), otherChild)){
                return true;
            }
        }
        return  false;
    }

    public void removeAllOtherGroups(){
        prefs.edit().remove("list1").apply();
        otherGroupList.postValue(Collections.emptyList());
    }

    public LiveData<List<OtherGroup>> getOtherGroupListStream(){return otherGroupList;}
}
