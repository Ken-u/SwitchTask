package com.wiipu.swichtask.data;

/**
 * Created by Ken~Jc on 2016/4/4.
 * 任务的细节存放data
 */
public class TaskData {
    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString(){
        return "(id:"+getId()+",name:"+getName()+")";
    }
}
