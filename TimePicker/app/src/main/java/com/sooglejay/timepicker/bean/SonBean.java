package com.sooglejay.timepicker.bean;

/**
 * Created by JiangWei on 2015/10/20.
 */
public class SonBean {
    private String dateStr = "";//时间字符串，格式为 ：2015-1-1
    private String dayNumberString = "";//日期：天，字符串型
    private int status = 3;//标志位：0代表开始，1代表结束，2代表处于开始和结束之间的日期,3代表其他。这个标志位用来标志日期的显示

    public String getDateStr() {
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDayNumberString() {
        return dayNumberString;
    }

    public void setDayNumberString(String dayNumberString) {
        this.dayNumberString = dayNumberString;
    }


}
