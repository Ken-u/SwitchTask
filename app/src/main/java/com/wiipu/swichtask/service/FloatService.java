package com.wiipu.swichtask.service;

/**
 * Created by Ken~Jc on 2016/4/3.
 */

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wiipu.swichtask.R;
import com.wiipu.swichtask.data.TaskData;
import com.wiipu.swichtask.utils.GetApplicationNameUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 悬浮窗Service 该服务会在后台一直运行一个悬浮的透明的窗体
 *
 * @author Administrator
 *
 */
public class FloatService extends Service {

    private static final int UPDATE_PIC = 0x100;
    private int statusBarHeight;// 状态栏高度
    private View view;// 透明窗体
    private TextView text = null;
    private Button hideBtn = null;
    private Button updateBtn = null;
    private Button nextBtn=null;
    private HandlerUI handler = null;
    private Thread updateThread = null;
    private boolean viewAdded = false;// 透明窗体是否已经显示
    private boolean viewHide = false; // 窗口隐藏
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    //任务管理器
    private ActivityManager activityManager;

    //创建存放id的列表
    ArrayList<TaskData> taskList=new ArrayList<TaskData>();

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        createFloatView();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
        System.out.println("------------------onStart");
        viewHide = false;
        refresh();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        removeView();
    }

    /**
     * 关闭悬浮窗
     */
    public void removeView() {
        if (viewAdded) {
            windowManager.removeView(view);
            viewAdded = false;
        }
    }

    private void createFloatView() {
        handler = new HandlerUI();
        UpdateUI update = new UpdateUI();
        updateThread = new Thread(update);
        updateThread.start(); // 开户线程

        view = LayoutInflater.from(this).inflate(R.layout.view_float_app, null);
        text = (TextView) view.findViewById(R.id.usage);
        hideBtn = (Button) view.findViewById(R.id.hideBtn);
        updateBtn = (Button) view.findViewById(R.id.updateBtn);
        nextBtn=(Button)view.findViewById(R.id.btn_next);

        windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
		/*
		 * LayoutParams.TYPE_SYSTEM_ERROR：保证该悬浮窗所有View的最上层
		 * LayoutParams.FLAG_NOT_FOCUSABLE:该浮动窗不会获得焦点，但可以获得拖动
		 * PixelFormat.TRANSPARENT：悬浮窗透明
		 */
        layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, LayoutParams.TYPE_SYSTEM_ERROR,
                LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
        // layoutParams.gravity = Gravity.RIGHT|Gravity.BOTTOM; //悬浮窗开始在右下角显示
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;

        /**
         * 监听窗体移动事件
         */
        view.setOnTouchListener(new OnTouchListener() {
            float[] temp = new float[] { 0f, 0f };

            public boolean onTouch(View v, MotionEvent event) {
                layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
                int eventaction = event.getAction();
                switch (eventaction) {
                    case MotionEvent.ACTION_DOWN: // 按下事件，记录按下时手指在悬浮窗的XY坐标值
                        temp[0] = event.getX();
                        temp[1] = event.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        refreshView((int) (event.getRawX() - temp[0]),
                                (int) (event.getRawY() - temp[1]));
                        break;

                }
                return true;
            }
        });

        hideBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                viewHide = true;
                removeView();
                System.out.println("----------hideBtn");
            }
        });

        updateBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(), "you click UpdateBtn",
                        Toast.LENGTH_SHORT).show();
                switchPreTask(getThisTaskId());
//                System.out.println("mom "
//                        + SysInfoUtils
//                        .getUsedPercentValue(getApplicationContext()));
            }
        });

        nextBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchNextTask(getThisTaskId());
//                getTask();
            }
        });
    }

    /**
     * 刷新悬浮窗
     *
     * @param x
     *            拖动后的X轴坐标
     * @param y
     *            拖动后的Y轴坐标
     */
    private void refreshView(int x, int y) {
        // 状态栏高度不能立即取，不然得到的值是0
        if (statusBarHeight == 0) {
            View rootView = view.getRootView();
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            statusBarHeight = r.top;
        }

        layoutParams.x = x;
        // y轴减去状态栏的高度，因为状态栏不是用户可以绘制的区域，不然拖动的时候会有跳动
        layoutParams.y = y - statusBarHeight;// STATUS_HEIGHT;
        refresh();
    }

    /**
     * 添加悬浮窗或者更新悬浮窗 如果悬浮窗还没添加则添加 如果已经添加则更新其位置
     */
    private void refresh() {
        // 如果已经添加了就只更新view
        if (viewAdded) {
            windowManager.updateViewLayout(view, layoutParams);
        } else {
            windowManager.addView(view, layoutParams);
            viewAdded = true;
        }
    }

    /**
     * 获取正在运行的任务,保存到taskList中
     * 实体类为TaskData:
     * id；packageName
     * 获取任务列表时忽略自身，以包名为届
     */
    private void updateTaskList() {
        activityManager = (ActivityManager) this
                .getSystemService(ACTIVITY_SERVICE);
        //            成功运行
        //创建列表
        List<ActivityManager.RunningTaskInfo> mRunningTasks;

        //10表示获取的最大数，获取所有运行的任务，若需过滤则需新的数组存放，不需过滤则不用
        mRunningTasks = activityManager.getRunningTasks(10);
        try {
            /* 以循环及baseActivity方式取得任务名称与ID */
            for (ActivityManager.RunningTaskInfo temp:mRunningTasks){
                TaskData taskData=new TaskData();
                int t=0;
                taskData.setId(temp.id);
                taskData.setName(temp.baseActivity.getPackageName());

//                listId.add(t,taskData);

                if ((!isHad(taskData.getId()))&&(!taskData.getName().equals("com.wiipu.swichtask"))){
                    taskList.add(t,taskData);
                    t++;
                }
            }
//            Log.e("自定义任务列表：",taskList.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("获取任务失败","异常");
        }
    }

    /**
     * 获取正在运行的任务中最前的一个
     * 返回最前任务的id
     */
    private int getThisTaskId(){
        activityManager = (ActivityManager) this
                .getSystemService(ACTIVITY_SERVICE);
        //创建列表
        List<ActivityManager.RunningTaskInfo> mRunningTasks;
        mRunningTasks=activityManager.getRunningTasks(30);
        //默认为1的是启动器的id
        int thisId=1;
        try {
            thisId=mRunningTasks.get(0).id;
//            Log.e("当前任务的id为：",String.valueOf(thisId));
//            Log.e("当前任务在任务栈中的位置（标号）：",String.valueOf(getIndexFromId(thisId)));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("获取当前任务失败","异常");
        }
        return thisId;
    }

    /**
     * 判断任务是否存在于列表中，存在则返回true，不存在则返回false
     */
    private boolean isHad(int id){
        for (int i=0;i<taskList.size();i++){
            if (id==taskList.get(i).getId()){
                return true;
            }
        }
        return false;
    }

    /**
     * 切换到上一个任务
     */
    private void switchPreTask(int thisId){
        int thisIndex=getIndexFromId(thisId);
//        Log.e("当前任务的索引",String.valueOf(thisIndex));
        int id=1,index=0;
        if (thisIndex>0){
//            如果索引大于0，则直接-1，即可取上个任务
            index=thisIndex-1;
        }else {
//            如果索引为0，则取最后一个任务，相当于上个任务,小于0的情况不考虑
            index=taskList.size()-1;
        }
//        Log.e("上一个任务的索引",String.valueOf(index));
        id=taskList.get(index).getId();
        switchTheTask(id);
    }

    /**
     * 切换到下一个任务
     */
    private void switchNextTask(int thisId){
        int thisIndex=getIndexFromId(thisId);
//        Log.e("当前任务的索引",String.valueOf(thisIndex));
        int id=1,index=0;
        if (thisIndex<taskList.size()-1){
//            如果索引不是最后一个任务，则索引加1
            index=thisIndex+1;
        }else{
//            如果索引为最后一个任务，则索引减1，即为第一个任务
            index=0;
        }
        id=taskList.get(index).getId();
        switchTheTask(id);
    }

    /**
     * 获取指定id的任务标号
     */
    private int getIndexFromId(int thisId){
        int index=0;
        for (int i=0;i<taskList.size();i++){
            if (taskList.get(i).getId()==thisId){
                index=i;
            }
        }
        return index;
    }

    /**
     * 切换到指定id的任务
     */
    private void switchTheTask(int id){
        activityManager.moveTaskToFront(id,ActivityManager.MOVE_TASK_WITH_HOME);
    }

    /**
     * 获取指定正在运行的任务的信息
     * 显示在当前程序一栏
     */
    private String getThisTaskInfo(){
        String taskInfo=null;
        try {
            activityManager = (ActivityManager) this
                    .getSystemService(ACTIVITY_SERVICE);
//            成功运行
            List<ActivityManager.RunningTaskInfo> mRunningTasks; //30表示获取的最大数
            mRunningTasks = activityManager.getRunningTasks(10);
            /* 以循环及baseActivity方式取得任务名称与ID */
            for (int i=0;i<mRunningTasks.size();i++){
                //Log.e("TaskInfo", String.valueOf(mRunningTasks.get(i).id)+mRunningTasks.get(i).baseActivity.getClassName());
            }
            taskInfo=mRunningTasks.get(0).baseActivity.getPackageName();
            taskInfo=GetApplicationNameUtil.getProgramNameByPackageName(getApplicationContext(),taskInfo);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("获取任务失败","异常");
        }
        return taskInfo;
    }

    /**
     * 获取所有任务的信息,暂时显示在下面，换行符显示一条任务信息
     */
    private String getAllTaskInfo(){
        updateTaskList();
        String taskInfo="";
        ArrayList<TaskData> list=new ArrayList<TaskData>();
        list=taskList;
        for (TaskData temp:list){
            taskInfo+=GetApplicationNameUtil.getProgramNameByPackageName(getApplicationContext(),temp.getName())+"\n";
        }
        return taskInfo;
    }


    /**
     * 接受消息和处理消息
     *
     * @author Administrator
     *
     */
    class HandlerUI extends Handler {
        public HandlerUI() {

        }

        public HandlerUI(Looper looper) {
            super(looper);
        }

        /**
         * 接收消息
         */
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            // 根据收到的消息分别处理
            if (msg.what == UPDATE_PIC) {
                text.setText("当前程序：\n"+getThisTaskInfo()+"\n任务队列：\n"+getAllTaskInfo());
                if (!viewHide)
                    refresh();
            } else {
                super.handleMessage(msg);
            }

        }

    }

    /**
     * 更新悬浮窗的信息
     *
     * @author Administrator
     *
     */
    class UpdateUI implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            // 如果没有中断就一直运行
            while (!Thread.currentThread().isInterrupted()) {
                Message msg = handler.obtainMessage();
                msg.what = UPDATE_PIC; // 设置消息标识
                handler.sendMessage(msg);
                // 休眠1s
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}