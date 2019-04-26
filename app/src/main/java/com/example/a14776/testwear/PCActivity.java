package com.example.a14776.testwear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by 14776 on 2019/2/23.
 */

public class PCActivity extends Activity {
    private Button btnInput = null;
    private Button btnISS = null;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pc_activity_main);
        btnInput = (Button)findViewById(R.id.btnInput);
        btnISS = (Button)findViewById(R.id.btnISS);
        btnInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PCActivity.this, PCInputActivity.class);
                startActivityForResult(intent, 0);
            }
        });
        btnISS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PCActivity.this, PCISSActivity.class);
                startActivityForResult(intent, 0);
            }
        });
    }
}
