package com.example.conduct_rules.ui.practice;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class PracticeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public PracticeViewModel() {

    }

    public LiveData<String> getText() {
        return mText;
    }
}