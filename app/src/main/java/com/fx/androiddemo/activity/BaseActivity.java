package com.fx.androiddemo.activity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;

/**
 * Created by changxin on 2018/12/13.
 */

public abstract class BaseActivity extends AppCompatActivity {

    public abstract int getResourceId();

    public abstract void initView();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResourceId());
        ButterKnife.bind(this);
        initView();
    }
}
