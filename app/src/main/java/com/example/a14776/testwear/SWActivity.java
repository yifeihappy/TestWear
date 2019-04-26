package com.example.a14776.testwear;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

/**
 * Created by 14776 on 2019/2/20.
 */

public class SWActivity extends Activity implements SensorEventListener {
    private Button btnISS;
    private Button btnInput;
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sw_activity_main);

        btnInput = (Button)findViewById(R.id.btnInput);
        btnISS = (Button)findViewById(R.id.btnISS);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        btnISS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SWActivity.this, SWISSActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        btnInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SWActivity.this, SWInputActivity.class);
                startActivityForResult(intent, 0);
            }
        });

    }
}
