package com.fx.androiddemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.fx.androiddemo.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SocketActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.tcp, R.id.udp})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tcp:
                startActivity(new Intent(this,TCPActivity.class));
                break;
            case R.id.udp:
                startActivity(new Intent(this,UDPActivity.class));
                break;
        }
    }
}
