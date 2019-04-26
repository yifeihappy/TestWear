package com.example.a14776.testwear;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


/**
 * Created by 14776 on 2019/2/23.
 */

public class PCInputActivity extends Activity {
    private Button btnStart = null;
    private EditText edtIP = null;
    private TextView txtTime = null;
    SharedPreferences sharedPreferences = null;
    SharedPreferences.Editor editor = null;
    private String IP;
    private boolean STATE = false;
    private final int port = 8123;
    private final String STR_STOP = "STOP";
    private final String STR_START = "START";

    SensorManager sensorManager = null;
    SocketClientThread socketClientThread = null;
    Handler issHandler = null;

    PCSensorClass sensorClass = null;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pc_input_activity_main);

        btnStart = (Button)findViewById(R.id.btnStart);
        edtIP = (EditText)findViewById(R.id.edtIP);
        txtTime = (TextView)findViewById(R.id.txtTime);
        sharedPreferences = getSharedPreferences("IP", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        IP = sharedPreferences.getString("IP", "192.168.0.1");
        edtIP.setText(IP);

        // keep the screen on
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        issHandler = new ISSHandler();

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(STATE) {
                    // to stop
                    sensorClass.setBFlag(false);//set the flag to "E"
                    sensorClass.unRegister();// unregister the sensors
                    try {
                        socketClientThread.os.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    socketClientThread.closeSocket();//disconnect the connection from server
                    socketClientThread.interrupt(); //stop the thread

                    //update UI
                    edtIP.setVisibility(View.VISIBLE);
                    txtTime.setVisibility(View.INVISIBLE);
                    btnStart.setText(STR_START);
                    STATE = false;

                } else {
                    // to start
                    IP = edtIP.getText().toString(); // get IP
                    if(null == IP || IP.equals("")) {
                        Toast.makeText(PCInputActivity.this, "IP is illegal!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // connect to server
                    socketClientThread = new SocketClientThread(IP, port, issHandler);
                    socketClientThread.start();
                    //update IP in the sharedpreference
                    editor.putString("IP", IP);
                    editor.commit();
                    //register sensors
                    sensorClass = new PCSensorClass(sensorManager, issHandler, socketClientThread.sockhandler);
                    Message msgPC = Message.obtain();
                    msgPC.obj = "PC";
                    socketClientThread.sockhandler.sendMessage(msgPC);
                    sensorClass.setBFlag(true);//set the flag to "S"
                    sensorClass.register();
                    // update UI
                    edtIP.setVisibility(View.INVISIBLE);
                    txtTime.setVisibility(View.VISIBLE);
                    btnStart.setText(STR_STOP);
                    STATE = true;

                }
            }
        });
    }


    class ISSHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            MSGCODE msgcode = (MSGCODE)msg.obj;
            switch(msgcode) {
                case SOCKET_CONNECT_ERROR:
                    Toast.makeText(PCInputActivity.this, "Fail to connect to Server!", Toast.LENGTH_SHORT).show();
                    break;
                case TIMESTAMP:
                    Bundle bundle = msg.getData();
                    txtTime.setText(bundle.getString("t"));
                    break;
                case SEND_FAILD:
                    Toast.makeText(PCInputActivity.this, "Send data error!", Toast.LENGTH_SHORT).show();
                    sensorClass.unRegister();
                    break;
            }

        }
    }
}
