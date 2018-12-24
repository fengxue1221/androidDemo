package com.fx.androiddemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.fx.androiddemo.R;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UDPActivity extends AppCompatActivity {

    @BindView(R.id.text)
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udp);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                serverReceivedByUdp();
//                receiveServerSocketData();
            }
        }).start();
    }

    /**
     * 客户端连接服务端
     */
    private void connectServerWithUDPSocket() {
        DatagramSocket datagramSocket;

        try {
            //创建DatagramSocket对象并指定一个端口号，
            //如果客户端需要接收服务器的返回数据，还需要使用这个端口号来receive，所以一定要记住
            datagramSocket = new DatagramSocket(1988);
            //使用InetAddress(Inet4Address).getByName()把ip地址转换为网络地址
            InetAddress inetAddress = InetAddress.getByName("192.168.20.150");
            String str = "hello.world!";//设置要发送的报文
            byte data[] = str.getBytes();//转成byte数组
            //创建一个DatagramPacket对象，用于发送数据
            //参数一：要发送的数据    参数二：数据的长度   参数三：服务端的网络地址    参数四：服务端端口号
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress, 10025);
            datagramSocket.send(datagramPacket);
            Log.e("客户端连接服务端", "成功");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 客户端接收服务端数据
     */
    private void receiveServerSocketData() {
        DatagramSocket socket;
        try {
            //实例化的端口号要和发送时的socket一致，否则收不到data
            socket = new DatagramSocket(1988);
            byte data[] = new byte[4 * 1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            socket.receive(packet);
            //把接收到的data转化为string字符串
            String result = new String(packet.getData(), packet.getOffset(), packet.getLength());
            socket.close();
            Log.e("客户端接收到数据", result);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void serverReceivedByUdp() {
        DatagramSocket serverSocket;
        try {
            //创建DatagramSocket对象，并指定监听端口号
            serverSocket = new DatagramSocket(10025);
            byte data[] = new byte[4 * 1025];
            //创建一个DatagramPacket对象，并指定DatagramPacket对象的大小
            DatagramPacket packet = new DatagramPacket(data, data.length);
            serverSocket.receive(packet);
            final String result = new String(packet.getData(), packet.getOffset(), packet.getLength());
            Log.e("服务器接收到数据", result);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text.append(result);
                }
            });
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.send)
    public void onViewClicked() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                connectServerWithUDPSocket();
            }
        }).start();
    }
}
