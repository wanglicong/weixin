package com.riso.weixin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.riso.selectdate.mian.SelectDate2;

public class MainActivity extends AppCompatActivity {

    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootView = findViewById(R.id.activity_main);
        Button btn = (Button) findViewById(R.id.btn);
    }


    public void showSelectDate(View view) {
        System.out.println("clickle");
        new SelectDate2(this, "2017-08-28 19:20", "2017-09-12 16:00", rootView, new SelectDate2.SelectDateListener() {
            @Override
            public void OnOk(String selectDate) {
                Toast.makeText(MainActivity.this, selectDate, Toast.LENGTH_SHORT).show();
            }
        }).show();
    }
}
