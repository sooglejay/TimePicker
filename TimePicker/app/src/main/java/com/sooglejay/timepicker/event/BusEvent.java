package com.sooglejay.timepicker.event;


public class BusEvent {
    public static final int MSG_INT_TIME = 1000;//选择时间控件  投保开始和结束的时间
    private String start_time;//开始时间
    private String end_time;//结束时间
    private String interval_time;//时间间隔

    public String getTimeStringFroServer() {
        return timeStringFroServer;
    }

    public void setTimeStringFroServer(String timeStringFroServer) {
        this.timeStringFroServer = timeStringFroServer;
    }

    private String timeStringFroServer;//时间控件 选择时间，服务端要求的时间格式

    private int msg = 0;

    public int getMsg() {
        return msg;
    }

    public void setMsg(int msg) {
        this.msg = msg;
    }

    public BusEvent(int msg) {
        this.msg = msg;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getInterval_time() {
        return interval_time;
    }

    public void setInterval_time(String interval_time) {
        this.interval_time = interval_time;
    }
}
