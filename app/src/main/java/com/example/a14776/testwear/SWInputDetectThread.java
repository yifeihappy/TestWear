package com.example.a14776.testwear;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.a14776.testwear.libsvm.svm;
import com.example.a14776.testwear.libsvm.svm_model;
import com.example.a14776.testwear.libsvm.svm_node;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by 14776 on 2019/4/21.
 */

public class SWInputDetectThread extends Thread {
    LinkedList<DataItem> dataList = null;
    Handler handlerUI = null;
    private boolean tap_flag = false;
    svm_model model = null;

    public SWInputDetectThread(LinkedList<DataItem> dataList, Handler handler) {
        this.dataList = dataList;
        this.handlerUI = handler;
        try {
            model = svm.svm_load_model("svm_model_file");
        } catch (IOException e) {
            Log.e("Msg", "Load SVM model failed!");
            Message msg = Message.obtain();
            msg.obj = MSGCODE.MODEL_ERR;
            Bundle bundle = new Bundle();
            bundle.putString("model", "Load model fail!");
            msg.setData(bundle);
            handlerUI.sendMessage(msg);
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        LinkedList<DataItem> acc_win = new LinkedList<>();
        LinkedList<DataItem> mag_win = new LinkedList<>();
        try {
            while (true) {
                //判断是否终止
                if(this.isInterrupted()) {
                    Log.i("Msg", "stop input detection!");
                    throw new InterruptedException();
                }
                while(dataList.size() != 0) {
                    DataItem dataItem = dataList.removeFirst();
                    if(dataItem.getSensorType() == 10) {//线性加速度
                        acc_win.add(dataItem);
                        //如果数量达到一个窗口量
                        if(acc_win.getLast().getTimestamp() - acc_win.getFirst().getTimestamp() > Thresholds.ACC_TIME) {
                            //如果刚发生tap事件，则需等待magnetic数据收集完后再进行判断
                            if(!tap_flag){
                                tap_flag = Utils.isTap(acc_win, Thresholds.ACC_TH);
                            } else {
                                Log.d("iss", "wait for magnetic");
                            }
                            //窗口向前滑动，删除旧数据
                            Utils.slide_win(acc_win, Thresholds.ACC_TIME, 0.5f);
                        } else {//c维持窗口大小
                            Utils.slide_win(acc_win, Thresholds.ACC_TIME, 1.0f);
                        }
                    } else if(dataItem.getSensorType() == 2) {//磁场
                        mag_win.add(dataItem);
                        if(tap_flag) {
                            tap_flag = Utils.isFalseStroke(mag_win, Thresholds.FALSE_STROKE_TH_STD);
                            if(mag_win.getLast().getTimestamp() - mag_win.getFirst().getTimestamp() > 2*Thresholds.MAG_TIME) {
                                segmentation(mag_win, handlerUI);
                                tap_flag = false;
                                Utils.slide_win(mag_win, Thresholds.MAG_TIME, 0.1f);
                            }
                        } else {
                            Utils.slide_win(mag_win, Thresholds.MAG_TIME, 1.0f);
                        }
                    }
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
              e.printStackTrace();
        }
    }


    //分段
    public void segmentation(LinkedList<DataItem> mag_win, Handler handler) {
        //去除抬手情况？？

        //去除传感器受突变的情况
        DataItem firstItem = mag_win.getFirst();
        DataItem lastItem = mag_win.getLast();
        float dis = Math.abs(firstItem.getX() - lastItem.getX()) + Math.abs(firstItem.getY() - lastItem.getY()) + Math.abs(firstItem.getZ() - lastItem.getZ());
        if(dis < Thresholds.MAG_TH) {
           svm_node[] test = this.extractFeatureLabel(mag_win);
            String key = keyRecognizer(test);
            Message msg = Message.obtain();
            msg.obj = MSGCODE.KEY;
            Bundle bundle = new Bundle();
            bundle.putString("key", key);
            msg.setData(bundle);
            handlerUI.sendMessage(msg);
        } else {
            Log.d("iss", "bad stroke" + dis);
            //输出bad stroke 提示
            Message msg = Message.obtain();
            msg.obj = MSGCODE.BAD_STROKE;
            Bundle bundle = new Bundle();
            bundle.putString("msg", "Bad Stroke!");
            msg.setData(bundle);
            handlerUI.sendMessage(msg);
        }
    }

    //获取特征
    public svm_node[] extractFeatureLabel(LinkedList<DataItem> mag_win) {
        float max_x = Float.MIN_VALUE;
        float max_x_y = Float.MIN_VALUE;
        float max_sqrt_x2_y2_z2 = Float.MIN_VALUE;
        float min_x = Float.MAX_VALUE;
        float min_y = Float.MAX_VALUE;
        float min_y_z = Float.MAX_VALUE;
        for(DataItem dataItem: mag_win) {
            if(dataItem.getX() > max_x) {
                max_x = dataItem.getX();
            }
            if(dataItem.getX() - dataItem.getY() > max_x_y) {
                max_x_y = dataItem.getX() - dataItem.getY();
            }
            float sqrt_x2_y2_z2 = (float)Math.sqrt(dataItem.getX()*dataItem.getX() + dataItem.getY() * dataItem.getY() + dataItem.getZ() * dataItem.getZ());
            if(sqrt_x2_y2_z2 > max_sqrt_x2_y2_z2) {
                max_sqrt_x2_y2_z2 = sqrt_x2_y2_z2;
            }
            if(min_x > dataItem.getX()) {
                min_x = dataItem.getX();
            }
            if(min_y > dataItem.getY()) {
                min_y = dataItem.getY();
            }
            if(min_y_z > dataItem.getY() - dataItem.getZ()) {
                min_y_z = dataItem.getY() - dataItem.getZ();
            }
        }

        // 构建svm_node
        svm_node[] test = new svm_node[6];
        for(int i = 0; i < 6; i++) {
            test[i] = new svm_node();
        }
        test[0].index  = 1;
        test[0].value = max_x;
        test[1].index = 2;
        test[1].value = max_x_y;
        test[2].index = 3;
        test[2].value = max_sqrt_x2_y2_z2;
        test[3].index = 4;
        test[3].value = min_x;
        test[4].index = 5;
        test[4].value = min_y;
        test[5].index = 6;
        test[5].value = min_y_z;

        return test;
    }
    //调用模型
    public String keyRecognizer(svm_node[] test) {
        if(model == null) {
            Message msg = Message.obtain();
            msg.obj = MSGCODE.MODEL_ERR;
            Bundle bundle = new Bundle();
            bundle.putString("model", "Model is null!");
            msg.setData(bundle);
            handlerUI.sendMessage(msg);
            return "";
        } else {
            double result_normal = svm.svm_predict(model, test);
            if(Math.abs(10 - result_normal) < 0.0001) {
                return "*";
            }
            if(Math.abs(11 - result_normal) < 0.0001) {
                return "0";
            }
            if(Math.abs(12 - result_normal) < 0.0001) {
                return "#";
            }
            return ""+(int)result_normal;
        }
    }

}
