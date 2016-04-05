package com.wiipu.swichtask.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.wiipu.swichtask.service.FloatService;

/**
 * Created by Ken~Jc on 2016/4/3.
 */
public class FloatingViewApp extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent=new Intent(FloatingViewApp.this,FloatService.class);
        startService(intent);
        finish();
    }
}