package com.fx.androiddemo.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fx.androiddemo.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BluetoothActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    @BindView(R.id.tvDevices)
    TextView tvDevices;
    @BindView(R.id.search)
    Button search;
    @BindView(R.id.lvDevices)
    ListView lvDevices;

    private BluetoothAdapter bluetoothAdapter;
    private List<String> devicesList = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");//随便输入
    private final String NAME = "Blurtooth_Socket";
    private BluetoothSocket clientSocket;
    private BluetoothDevice device;
    private OutputStream outputStream;//输出流
    private AcceptThread acceptThread;

    private final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 100;


    @Override
    public int getResourceId() {
        return R.layout.activity_bluetooth;
    }

    @Override
    public void initView() {

        //得到BluetoothAdapter对象
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {//判断蓝牙权限是否开启
            bluetoothAdapter.enable();//强制开启蓝牙
        }

        //获得已绑定的BluetoothDevice，之前绑定的有记录
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                tvDevices.append(device.getName() + ":" + device.getAddress());
                //加入list
                devicesList.add(device.getName() + ":" + device.getAddress());
            }
        }

        //通过广播接收到搜索到设备,注册广播接收器----同一个接收器处理两个广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(receiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(receiver, filter);

        filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//设备配对
        this.registerReceiver(receiver, filter);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1, devicesList);
        lvDevices.setAdapter(arrayAdapter);
        lvDevices.setOnItemClickListener(this);

        acceptThread = new AcceptThread();
        acceptThread.start();
    }


    @OnClick(R.id.search)
    public void onViewClicked() {
        requestPermission();
    }

    //广播接收器
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //现在有两个广播，先进行判断是哪一个广播
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //判断设备是否被绑定
                if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                    if (bluetoothDevice.getName() != null) {
                        //添加显示
//                        tvDevices.append(bluetoothDevice.getName() + ":" + bluetoothDevice.getAddress() + "\n");
                        //添加到列表中
                        devicesList.add(bluetoothDevice.getName() + ":" + bluetoothDevice.getAddress());
                        lvDevices.setAdapter(arrayAdapter);
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {//扫描完成
                setTitle("已搜索完成");
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (bluetoothDevice.getBondState()) {
                    case BluetoothDevice.BOND_NONE:
                        Log.e(getPackageName(), "取消配对");
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.e(getPackageName(), "配对中");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.e(getPackageName(), "配对成功");
                        break;
                }
            }
        }
    };

    private void searchBluetoothDevice() {
        setTitle("正在扫描");
        if (bluetoothAdapter.isDiscovering()) {//如果正在搜索，先取消搜索，不能开启两次
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();//开始搜索
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkAccessFinePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (checkAccessFinePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSION_ACCESS_FINE_LOCATION);
                return;
            } else {
                //开始搜索
                searchBluetoothDevice();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    searchBluetoothDevice();
                } else {
                    Toast.makeText(this, "请先开启定位权限", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    //客户端的Socket设置
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        String s = arrayAdapter.getItem(position);
        String address = s.substring(s.indexOf(":") + 1).trim();//蓝牙地址,ip

        //主动连接服务端
        try {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            if (device == null) {
                device = bluetoothAdapter.getRemoteDevice(address);//获得远程蓝牙设备
                //进行配对
//                device.createBond();
            }
            if (clientSocket == null) {
                try {
                    clientSocket = device.createRfcommSocketToServiceRecord(MY_UUID);//指定uuid
                } catch (IOException e) {
                    e.printStackTrace();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            clientSocket.connect();//进行连接
                            outputStream = clientSocket.getOutputStream();//获得输出流
                            if (outputStream != null) {
                                byte[] buffer = "发送".getBytes("utf-8");
                                outputStream.write(buffer, 0, buffer.length);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            Toast.makeText(BluetoothActivity.this, String.valueOf(message.obj), Toast.LENGTH_SHORT).show();
            super.handleMessage(message);
        }
    };

    //线程的服务类
    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;
        private BluetoothSocket socket;
        private InputStream is;
        private OutputStream os;

        public AcceptThread() {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (socket != null) {
                    try {
                        is = socket.getInputStream();
                        byte[] buffer = new byte[128];
                        int count = is.read(buffer);
                        Message message = new Message();
                        message.obj = new String(buffer, 0, count, "utf-8");
                        handler.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

//            try {
//                socket = serverSocket.accept();
//                is = socket.getInputStream();
//                os = socket.getOutputStream();
//
//                while (true) {
//                    byte[] buffer = new byte[128];
//                    int count = is.read(buffer);
//                    Message message = new Message();
//                    message.obj = new String(buffer, 0, count, "utf-8");
//                    handler.sendMessage(message);
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    }
}
