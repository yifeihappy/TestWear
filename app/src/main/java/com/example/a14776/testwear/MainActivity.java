package com.example.a14776.testwear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
// 击键输入，PC+smartwatch 和 smartwatch独立的应用
public class MainActivity extends Activity {

    private Button btnPC;
    private Button btnSW;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        btnPC = (Button)findViewById(R.id.btnPC);
        btnSW = (Button)findViewById(R.id.btnSW);

        if(null == btnPC) {
            Log.d("PC", "btnPC is null");
        } else {
            Log.d("PC", "btnPC is not null");
        }

        btnPC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PCActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        btnSW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SWActivity.class);
                startActivityForResult(intent, 0);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}
