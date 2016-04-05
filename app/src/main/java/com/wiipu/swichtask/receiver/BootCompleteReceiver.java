package com.wiipu.swichtask.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.wiipu.swichtask.service.FloatService;

/**
 * Created by Ken~Jc on 2016/4/5.
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context,Intent intent){
        //开机启动
        Toast.makeText(context,"Boot Complete,Start TaskSwitcher!",Toast.LENGTH_SHORT).show();
        Intent service=new Intent(context,FloatService.class);
        context.startService(intent);
    }
}
