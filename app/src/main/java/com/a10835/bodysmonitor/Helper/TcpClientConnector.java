package com.a10835.bodysmonitor.Helper;

/**
 * Created by 10835 on 2018/5/1.
 */


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by hadisi on 2016/6/28.
 */

public class TcpClientConnector {
    private static TcpClientConnector mTcpClientConnector;
    private Socket mClient;
    private ConnectLinstener mListener;
    private Thread mConnectThread;
    private OnCreateSuccessListner onCreateSuccess;

    public interface ConnectLinstener {
        void onReceiveData(String data);
    }

    public interface OnCreateSuccessListner{
        void onSuccess();
    }

    public void setOnConnectLinstener(ConnectLinstener linstener) {
        this.mListener = linstener;
    }
    public void setOnCreateSuccessListner(OnCreateSuccessListner createSuccessListner){
        this.onCreateSuccess = createSuccessListner;
    }

    private TcpClientConnector(){
    }

    public static TcpClientConnector getInstance() {
        if (mTcpClientConnector == null)
            mTcpClientConnector = new TcpClientConnector();
        return mTcpClientConnector;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 100:
                    if (mListener != null) {
                        mListener.onReceiveData(msg.getData().getString("data"));
                    }
                    break;
                case 200:
                    if (onCreateSuccess != null){
                        onCreateSuccess.onSuccess();
                    }
                    break;
            }
        }
    };

    public void creatConnect(final String mSerIP, final int mSerPort) {
        if (mConnectThread == null) {
            mConnectThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        connect(mSerIP, mSerPort);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            mConnectThread.start();
        }
    }

    /**
     * 与服务端进行连接
     *
     * @throws IOException
     */
    private void connect(String mSerIP, int mSerPort) throws IOException {
        if (mClient == null) {
            mClient = new Socket(mSerIP, mSerPort);
        }
        InputStream inputStream = mClient.getInputStream();
        if (inputStream != null){
            Message message = new Message();
            message.what = 200;
            mHandler.sendMessage(message);
        }
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = inputStream.read(buffer)) != -1) {
            String data = new String(buffer, 0, len);
            Message message = new Message();
            message.what = 100;
            Bundle bundle = new Bundle();
            bundle.putString("data", data);
            message.setData(bundle);
            mHandler.sendMessage(message);
        }
    }

    /**
     * 发送数据
     *
     * @param data 需要发送的内容
     */
    public void send(String data) throws IOException {
        OutputStream outputStream = mClient.getOutputStream();
        outputStream.write(data.getBytes());
    }

    /**
     * 断开连接
     *
     * @throws IOException
     */
    public void disconnect() throws IOException {
        if (mClient != null) {
            mClient.close();
            mClient = null;
            if (mTcpClientConnector != null){
                mTcpClientConnector = null;
            }
        }
    }
}
