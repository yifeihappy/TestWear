package com.example.a14776.testwear;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.LinkedList;

/**
 * Created by 14776 on 2019/2/23.
 */

public class SWISSSensorClass implements SensorEventListener {
    private SensorManager sensorManager = null;
    private Handler handlerUI = null;
    private long startTime = 0;
    public LinkedList<DataItem> dataList = new  LinkedList<DataItem>();
    private boolean inputGestureb = false;


    public SWISSSensorClass(SensorManager sensorManager, Handler handlerUI){
        this.sensorManager = sensorManager;
        this.handlerUI = handlerUI;
    }
    public void register() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), sensorManager.SENSOR_DELAY_FASTEST);
        startTime = System.currentTimeMillis();
    }
    public void unRegister() {
        sensorManager.unregisterListener(this);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        //更新UI，显示时间
        Message msgUI = Message.obtain();
        Bundle bundleUI = new Bundle();
        msgUI.obj = MSGCODE.TIMESTAMP;
        long eventTime = System.currentTimeMillis() - startTime;
        bundleUI.putString("t", "" + eventTime);
        msgUI.setData(bundleUI);
        handlerUI.sendMessage(msgUI);
//        Log.i("Msg", ""+event.sensor.getType());
        if(event.sensor.getType() == 9) { //重力数据不放入，只是判断手的姿势
            if(event.values[2] > Thresholds.GRAVITY_TH || -event.values[2] > Thresholds.GRAVITY_TH) {//手水平放置，正向或者反向时
                inputGestureb = true;
            } else {//手不是水平放置，清空已经存储的数据
                inputGestureb = false;
                dataList.clear();
            }
        } else {
            if(inputGestureb) {
                //将数据放入队列中
                DataItem dataItem = new DataItem(event.sensor.getType(), eventTime, event.values[0], event.values[1], event.values[2]);
                dataList.add(dataItem);
            }
        }
    }




}
