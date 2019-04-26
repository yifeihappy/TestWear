package com.example.a14776.testwear;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by 14776 on 2019/2/23.
 */

public class PCSensorClass implements SensorEventListener {
    private SensorManager sensorManager = null;
    private Handler handlerUI = null;
    private Handler socketHandler = null;
    private volatile boolean bFlag = false;

    public void setBFlag(boolean b) {
        bFlag = b;
    }

    public PCSensorClass(SensorManager sensorManager, Handler handlerUI, Handler socketHandler){
        this.sensorManager = sensorManager;
        this.handlerUI = handlerUI;
        this.socketHandler = socketHandler;
    }
    public void register() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), sensorManager.SENSOR_DELAY_FASTEST);
    }
    public void unRegister() {
        sensorManager.unregisterListener(this);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Message msgUI = Message.obtain();
        Bundle bundleUI = new Bundle();
        msgUI.obj = MSGCODE.TIMESTAMP;
        long eventTime = System.currentTimeMillis();
        bundleUI.putString("t", "" + eventTime);
        msgUI.setData(bundleUI);
        handlerUI.sendMessage(msgUI);

        Message msgSocket = Message.obtain();
        StringBuffer stringBuffer = new StringBuffer();
        if(bFlag) {
            stringBuffer.append("S,");
        } else {
            stringBuffer.append("E,");
        }
        stringBuffer.append(event.sensor.getType() + "," + eventTime);
        for (float v:event.values) {
            stringBuffer.append("," + v);
        }
        stringBuffer.append("\n");
        msgSocket.obj = stringBuffer;
        Log.d("msgS", stringBuffer.toString());
        socketHandler.sendMessage(msgSocket);
    }
}
