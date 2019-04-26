package com.example.a14776.testwear;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.LinkedList;


/**
 * Created by 14776 on 2019/4/21.
 */

public class SWISSActivity extends Activity {
    private TextView txtMsg;
    private Button btnStart;
    private boolean btnState = false;
    private HandlerUI handerUI = null;
    private SWISSSensorClass SWISSSensorClass = null;
    private SensorManager sensorManager = null;
    private final String STR_STOP = "STOP";
    private final String STR_START = "START";
//    private LinkedList<DataItem> acc_list;
//    private LinkedList<DataItem> mag_list;
    private SWISSThread swissThread = null;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sw_iss_activity_main);
        txtMsg = (TextView)findViewById(R.id.txtMsg);
        btnStart = (Button)findViewById(R.id.btnStart);
        handerUI = new HandlerUI();
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        SWISSSensorClass = new SWISSSensorClass(sensorManager, handerUI);
//        acc_list = new LinkedList<>();
//        mag_list = new LinkedList<>();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // to start
                if(btnState == false) {
                    //传感器监听
                    SWISSSensorClass.register();
                    //改变状态
                    btnState = true;
                    btnStart.setText(STR_STOP);
                } else { // to stop
                    //取消传感器监听
                    SWISSSensorClass.unRegister();
                    //改变状态
                    btnState = false;
                    btnStart.setText(STR_START);
                    Log.i("Msg", "Stop dataList_len = " + SWISSSensorClass.dataList.size());
                    //获取数据, 训练模型
                    swissThread = new SWISSThread(SWISSSensorClass.dataList, handerUI);
                    swissThread.start();
                }
            }
        });

    }

    class HandlerUI extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            MSGCODE msgcode = (MSGCODE)msg.obj;
            Bundle bundle = msg.getData();
            switch (msgcode) {
                case TIMESTAMP:
                    txtMsg.setText(bundle.getString("t"));
                    break;
                case KEYNUM:
                    txtMsg.setText(bundle.getString("keynum"));
                    break;
            }

        }
    }
}
