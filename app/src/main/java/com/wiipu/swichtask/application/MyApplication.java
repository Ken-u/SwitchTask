package com.wiipu.swichtask.application;

import android.app.Application;
import android.content.Intent;

import com.wiipu.swichtask.service.FloatService;

/**
 * Created by Ken~Jc on 2016/4/4.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        Intent intent=new Intent(getApplicationContext(),FloatService.class);
        startService(intent);
    }
}
