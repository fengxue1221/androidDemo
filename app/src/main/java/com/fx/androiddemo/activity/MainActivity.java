package com.fx.androiddemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.fx.androiddemo.R;
import com.fx.androiddemo.activity.BaseActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    @Override
    public int getResourceId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {

    }

    @OnClick({R.id.bluetooth, R.id.socket})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bluetooth:
                startActivity(new Intent(this, BluetoothActivity.class));
                break;
            case R.id.socket:
                startActivity(new Intent(this,SocketActivity.class));
                break;
        }
    }
}
