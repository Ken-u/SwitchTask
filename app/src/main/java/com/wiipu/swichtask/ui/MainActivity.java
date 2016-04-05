package com.wiipu.swichtask.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wiipu.swichtask.R;

import java.util.List;

public class MainActivity extends Activity {

    private Button btn;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("123","十块");
        btn=(Button)findViewById(R.id.btn_switch);
        tv=(TextView)findViewById(R.id.hello);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "WTF", Toast.LENGTH_SHORT).show();
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "WTF", Toast.LENGTH_SHORT).show();
                getTask();

            }
        });
    }

    private void getTask() {
        ActivityManager activityManager;
        Log.e("1", "获取任务开始");
        try {
            activityManager = (ActivityManager) this
                    .getSystemService(ACTIVITY_SERVICE);
//            ArrayList arylistTask = new ArrayList<String>();

//            成功运行
            List<ActivityManager.RunningTaskInfo> mRunningTasks; //30表示获取的最大数
            mRunningTasks = activityManager.getRunningTasks(10);
            /* 以循环及baseActivity方式取得任务名称与ID */
            for (int i=0;i<mRunningTasks.size();i++){
                Log.e("TaskInfo", String.valueOf(mRunningTasks.get(i).id)+mRunningTasks.get(i).baseActivity.getClassName());
            }
            activityManager.moveTaskToFront(mRunningTasks.get(2).id,ActivityManager.MOVE_TASK_WITH_HOME);


//            测试6.0，当前活动的包名和信息，无id？
//            List<ActivityManager.AppTask> mRunningTasks;
//            mRunningTasks=activityManager.getAppTasks();
//            for (int ii=0;ii<mRunningTasks.size();ii++){
//                Log.e("TaskInfo", String.valueOf(mRunningTasks.get(ii).getTaskInfo().baseIntent.getComponent().getPackageName()));
//
//            }

//            List<ActivityManager.RecentTaskInfo> mRunningTasks;
//            mRunningTasks=activityManager.getRecentTasks(30,0);
//            for (int ii=0;ii<mRunningTasks.size();ii++){
//                Log.e("TaskInfo", String.valueOf(mRunningTasks.get(ii).id)+mRunningTasks.get(ii).baseActivity.getClassName());
//
//            }


//            for (ActivityManager.RunningAppProcessInfo amTask : mRunningTasks) {
//                Log.e("TaskInfo", amTask.processName+ "(" + amTask.toString()+ ")");
//            }
//            //获取服务
//            List<ActivityManager.RunningServiceInfo> mserviceTasks =
//                    activityManager.getRunningServices(30);
///* 以循环方式取得任务名称与ID */
//            for (ActivityManager.RunningServiceInfo serinfo : mserviceTasks) {
//                Log.e("TaskServerInfo", serinfo.process + "(" + serinfo.pid + ")");
//            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("获取任务失败","异常");
        }
    }
}
