package com.example.a14776.testwear;

import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.a14776.testwear.libsvm.svm;
import com.example.a14776.testwear.libsvm.svm_model;
import com.example.a14776.testwear.libsvm.svm_node;
import com.example.a14776.testwear.libsvm.svm_parameter;
import com.example.a14776.testwear.libsvm.svm_problem;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by 14776 on 2019/4/22.
 */

public class SWISSThread extends Thread {
    private LinkedList<DataItem> dataList = null;
    private boolean tap_flag = false;
    private LinkedList<Float[]> featuresList;
    private LinkedList<Integer> labelsList;
    private Handler handlerUI = null;

    public SWISSThread(LinkedList<DataItem> dataList, Handler handler) {
        this.dataList = dataList;
        featuresList = new LinkedList<Float[]>();
        labelsList = new LinkedList<>();
        this.handlerUI = handler;
    }

    @Override
    public void run() {
        super.run();
        //获取特征
        LinkedList<DataItem> acc_win = new LinkedList<>();
        LinkedList<DataItem> mag_win = new LinkedList<>();
        int label = 1;
        for(DataItem dataItem: dataList) {
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
                        if(segmentation(mag_win, label)) {
                            label += 1;
                        }
                        tap_flag = false;
                        Utils.slide_win(mag_win, Thresholds.MAG_TIME, 0.1f);
                    }
                } else {
                    Utils.slide_win(mag_win, Thresholds.MAG_TIME, 1.0f);
                }
            }
        }
        //训练模型
        trainModelISS();
    }

    //分段
    public boolean segmentation(LinkedList<DataItem> mag_win,  int label) {
        //去除抬手情况？？

        //去除传感器受突变的情况
        DataItem firstItem = mag_win.getFirst();
        DataItem lastItem = mag_win.getLast();
        float dis = Math.abs(firstItem.getX() - lastItem.getX()) + Math.abs(firstItem.getY() - lastItem.getY()) + Math.abs(firstItem.getZ() - lastItem.getZ());
        if(dis < Thresholds.MAG_TH) {
            this.extractFeatureLabel(mag_win, label);
            return true;
        } else {
            Log.d("iss", "bad stroke" + dis);
            return false;
        }

    }

    //获取特征
    public void extractFeatureLabel(LinkedList<DataItem> mag_win, int label) {
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
        Float[] featureItem = new Float[6];
        featureItem[0] = max_x;
        featureItem[1] = max_x_y;
        featureItem[2] = max_sqrt_x2_y2_z2;
        featureItem[3] = min_x;
        featureItem[4] = min_y;
        featureItem[5] = min_y_z;
        featuresList.add(featureItem);
        labelsList.add(label);
    }

    public void trainModelISS() {
        Message strainMsg = Message.obtain();
        strainMsg.obj = MSGCODE.KEYNUM;
        Bundle strainBundle = new Bundle();
        strainBundle.putString("keynum", "trainstart...");
        strainMsg.setData(strainBundle);
        handlerUI.sendMessage(strainMsg);
        int nClass = featuresList.size();

        if(nClass != 0) {
            Log.i("Msg", "nClass=" + nClass);
            System.out.print("nC = " + nClass);
            svm_node[][] x = new svm_node[nClass][6];
            for(int i = 0; i < nClass; i++) {
                for(int j=0; j<6; j++) {
                    x[i][j] = new svm_node();
                }
            }
            //特征
            int iC = 0;
            for (Float[] fs : featuresList) {
                for (int i = 0; i < 6; i++) {
                    x[iC][i].index = i + 1;
                    x[iC][i].value = fs[i];
                }
                iC++;
            }
            //标签
            double[] labels = new double[nClass];
            int iL = 0;
            for (Integer l : labelsList) {
                labels[iL] = l;
                iL++;
            }
            svm_problem sp = new svm_problem();
            sp.x = x; // the training data
            sp.y = labels; // the labels of training data;
            sp.l = nClass; // the number of training data.这里设计为每一类一个数据

            svm_parameter prm = new svm_parameter();
            prm.svm_type = svm_parameter.C_SVC;
            prm.kernel_type = svm_parameter.LINEAR;
            prm.C = 1000; //the cost of contraints violation
            prm.eps = 0.001; // the stopping criterion
            prm.probability = 1;
            prm.cache_size = 1;  // cache_size is the size of the kernel cache, specified in megabytes.
            svm_model model = svm.svm_train(sp, prm);

            //训练分类
            try {
                svm.svm_save_model("svm_model_file", model);
            } catch (IOException e) {
                Log.e("Msg", "Save SVM model failed");
                e.printStackTrace();
            }
        }
        Message resultMsg = Message.obtain();
        resultMsg.obj = MSGCODE.KEYNUM;
        Bundle resultBundle = new Bundle();
        resultBundle.putString("keynum", "" + nClass);
        resultMsg.setData(resultBundle);
        handlerUI.sendMessage(resultMsg);
    }
}
