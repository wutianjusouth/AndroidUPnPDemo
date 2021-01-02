package com.zane.androidupnpdemo.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.zane.androidupnpdemo.R;

import org.fourthline.cling.TestMain;

public class TestActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TestMain.upnpDiscover(null);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });

    }



}
