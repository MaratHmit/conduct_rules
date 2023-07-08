package com.example.conduct_rules.ui.list_rules;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class ListRulesViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ListRulesViewModel() {

    }

    public LiveData<String> getText() {
        return mText;
    }
}