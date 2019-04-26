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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a14776.testwear.libsvm.svm;

import java.io.IOException;


/**
 * Created by 14776 on 2019/4/21.
 */

public class SWInputActivity extends Activity {
    private TextView txtTime = null;
    private EditText editText = null;
    private Button btnStart = null;
    private Handler handlerUI = null;
    private SensorManager sensorManager = null;
    private final String STR_STOP = "STOP";
    private final String STR_START = "START";
    private boolean btnState = false;
    private SWInputSensorClass swInputSensorClass = null;
    private SWInputDetectThread swInputDetectThread = null;
    private TextView txtMDial = null;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sw_input_activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        txtTime = (TextView)findViewById(R.id.txtTime);
        editText = (EditText)findViewById(R.id.editText);
        btnStart = (Button)findViewById(R.id.btnStart);
        handlerUI = new HandlerUI();
        txtMDial = (TextView)findViewById(R.id.txtMDial);

        txtMDial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        swInputSensorClass = new SWInputSensorClass(sensorManager, handlerUI);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnState == false) { //to start
                    //注册
                    swInputSensorClass.register();
                    //改变状态
                    btnState = true;
                    btnStart.setText(STR_STOP);
                    //启动线程进行输入监听
                    swInputDetectThread = new SWInputDetectThread(swInputSensorClass.dataList, handlerUI);
                    swInputDetectThread.start();

                } else {
                    //取消传感器监听
                    swInputSensorClass.unRegister();
                    //改变状态
                    btnState = false;
                    btnStart.setText(STR_START);
                    //关闭输入事件监听线程
                    swInputDetectThread.interrupt();
                }
            }
        });

    }

    class HandlerUI extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MSGCODE msgcode = (MSGCODE)msg.obj;
            Bundle bundle = msg.getData();
            switch (msgcode) {
                case TIMESTAMP:
                    txtTime.setText(bundle.getString("t"));
                    break;
                case BAD_STROKE:
                    Toast.makeText(SWInputActivity.this, bundle.getString("msg"), Toast.LENGTH_SHORT).show();
                    break;
                case KEY:
                    editText.setText(editText.getText()+bundle.getString("key"));
                    break;
                case MODEL_ERR:
                    Toast.makeText(SWInputActivity.this, bundle.getString("model"), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
