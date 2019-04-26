package com.example.a14776.testwear;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;


/**
 * Created by 14776 on 2019/2/20.
 */

public class SocketClientThread extends Thread {

    private String host;
    private int port;
    public Socket socket;
    public OutputStream os;
    public Handler sockhandler;
    public Handler pcHandler;

    public SocketClientThread(String host, int port, Handler handler) {
        this.host = host;
        this.port = port;
        pcHandler = handler;
        sockhandler = new SocketHanlder();

    }

    @Override
    public void run() {
        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), 6000);
            os = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            Message msgUI = Message.obtain();
            msgUI.obj = MSGCODE.SOCKET_CONNECT_ERROR;
            pcHandler.sendMessage(msgUI);
        };
    }

    public void closeSocket() {
        try {
            if(null != os) {
                os.close();
            }
            if(null != socket) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setHost(String IP) {
        host = IP;
    }

    class SocketHanlder extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                if(null != os ) {
                    os.write(msg.obj.toString().getBytes("utf-8"));
                }
            } catch (IOException e) {
                e.printStackTrace();
                Message msgUI = Message.obtain();
                msgUI.obj =  MSGCODE.SEND_FAILD;
                pcHandler.sendMessage(msgUI);
            }


        }
    }
}
