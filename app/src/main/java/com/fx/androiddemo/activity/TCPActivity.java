package com.fx.androiddemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.fx.androiddemo.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 手机做服务端，也做客户端
 * ip为手机在局域网中的ip地址，端口号随意输入
 */
public class TCPActivity extends AppCompatActivity {

    @BindView(R.id.info)
    TextView info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                serverReceivedByTcp();
            }
        }).start();

    }

    //客户端
    protected void connectServerWithTCPSocket() {
        Socket socket;

        try {
            //创建一个Socket对象，并指定服务端的IP及端口号
            socket = new Socket("192.168.20.150", 1989);
            //创建一个InputStream用户读取要发送的文件
            InputStream inputStream = this.getAssets().open("a.txt");
            //获取socket的OutputStream对象用于发送数据
            OutputStream outputStream = socket.getOutputStream();
//            outputStream.write("hello".getBytes());
            //创建一个byte类型的buffer字节数组，用于存放读取的本地文件
            byte buffer[] = new byte[4 * 1024];
            int temp = 0;
            //循环读取文件
            while ((temp = inputStream.read(buffer)) != -1) {
                //把数据写入到outputStream对象中
                outputStream.write(buffer, 0, temp);
            }
            //发送读取的数据到服务端
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //服务端
    public void serverReceivedByTcp() {
        ServerSocket serverSocket = null;
        try {
            //创建一个ServerSocket对象，并让这个Socket在1989端口监听
            serverSocket = new ServerSocket(1989);
            Log.e("HHH", "服务器监听开始了");
            //调用ServerSocket的accept()方法，接受客户端所发送的请求
            //如果客户端没有发送数据，那么线程就停滞不继续
            Socket socket = serverSocket.accept();
            //从Socket当中得到InputStream
            InputStream inputStream = socket.getInputStream();
            byte buffer[] = new byte[1024 * 4];
            int temp = 0;
            //从InputStream当中读取客户端所发送的数据
            while ((temp = inputStream.read(buffer)) != -1) {
                System.out.println(new String(buffer, 0, temp));
                Log.e("数据", new String(buffer, 0, temp));
                final String str=new String(buffer,0,temp);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        info.setText(str);
                    }
                });
            }
            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.send)
    public void onViewClicked() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                connectServerWithTCPSocket();
            }
        }).start();

    }
}
