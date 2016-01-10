package com.sooglejay.timepicker.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;



import com.sooglejay.timepicker.R;
import com.sooglejay.timepicker.adapter.CalenderAdapter;
import com.sooglejay.timepicker.widget.TitleBar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 首页-选择预约停保-时间选择器
 */
public class TimePickerActivity extends Activity {
    private TitleBar mTitleBar;
    private ListView listView;

    private List<Object> mDatas = new ArrayList<>();
    private CalenderAdapter mAdapter;
    private Calendar calendar = Calendar.getInstance(Locale.CHINA);//全局日历对象

    private SimpleDateFormat dateFormat_yyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd");//日期格式化
    private String todayString_yyyy_m_d = new SimpleDateFormat("yyyy-MM-dd").format(new Date());


    private String startTimeStr;
    private String endTimeStr;


    private Long startTimeLong;
    private Long endTimeLong;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aaa_activity_time_picker);
        setUpView();
        setUpLisenter();
    }

    private void setUpLisenter() {
        mTitleBar.setOnTitleBarClickListener(new TitleBar.OnTitleBarClickListener() {
            @Override
            public void onLeftButtonClick(View v) {
                TimePickerActivity.this.finish();
            }

            @Override
            public void onRightButtonClick(View v) {
            }
        });
    }

    private void setUpView() {

        startTimeStr = getIntent().getExtras().getString(StringConstant.orderStartTimeFromServerString);
        endTimeStr = getIntent().getExtras().getString(StringConstant.orderEndTimeFromServerString);

        startTimeLong = getIntent().getLongExtra(StringConstant.orderStartTimeFromServerLong, 0);
        endTimeLong = getIntent().getLongExtra(StringConstant.orderEndTimeFromServerLong, 0);


        mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.initTitleBarInfo("选择开始结束时间", R.drawable.arrow_left, -1, "", "");


        listView = (ListView) findViewById(R.id.list_item);
        setmAdapterDatas(mDatas);
        mAdapter = new CalenderAdapter(this, mDatas);
        mAdapter.setOrderEndTimeFromServer(endTimeLong);
        mAdapter.setOrderStartTimeFromServer(startTimeLong);
        listView.setAdapter(mAdapter);

    }

    /**
     * 设置日历需要的数据集
     *
     * @param listData
     */
    public void setmAdapterDatas(List<Object> listData) {
        int thisMonthNumber = calendar.get(Calendar.MONTH) + 1;
        int thisYearNumber = calendar.get(Calendar.YEAR);
        for (int i = 0; i < 2; i++) {
            if (thisMonthNumber + i <= 12) {//在一年以内
                listData.add(thisYearNumber + "年" + (thisMonthNumber + i) + "月");
                FatherBean calenderListBean = new FatherBean();
                List<SonBean> daysList = new ArrayList<>();//当月的天对象

                calendar.set(Calendar.DATE, 1);//cal设置当前day为当前月第一天
                int daysInTheMonth = calendar.getActualMaximum(Calendar.DATE);
                int firstDay_NumberOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                for (int j = 0; j < firstDay_NumberOfWeek; j++) {
                    SonBean dayBean = new SonBean();
                    dayBean.setDateStr("");
                    daysList.add(dayBean);
                }
                for (int k = 1; k <= daysInTheMonth; k++) {
                    String dayS = k + "";
                    if (k < 10) {
                        dayS = "0" + k;
                    }
                    SonBean dayBean = new SonBean();
                    dayBean.setDateStr(thisYearNumber + "-" + (thisMonthNumber + i) + "-" + dayS);


                    //设置 时间状态
                    dayBean.setStatus(genStatus(startTimeStr, endTimeStr, thisYearNumber + "-" + (thisMonthNumber + i) + "-" + dayS));


                    dayBean.setDayNumberString(k + "");
                    daysList.add(dayBean);
                }
                calenderListBean.setDaysList(daysList);
                listData.add(calenderListBean);
                calendar.add(Calendar.MONTH, 1);
            } else {

            }
        }

    }


    /**
     * 生成 status ，标识是开始0 、结束1  还是 在它们之间2
     *
     * @param startDateString
     * @param endDateString
     * @param dateString
     * @return
     */
    public int genStatus(String startDateString, String endDateString, String dateString) {
        try {
            if (dateString.equals(startDateString)) {
                return CalenderAdapter.START;

            } else if (dateString.equals(endDateString)) {
                return CalenderAdapter.END;
            } else if (dateFormat_yyyy_MM_dd.parse(dateString).getTime() > dateFormat_yyyy_MM_dd.parse(startDateString).getTime()
                    && dateFormat_yyyy_MM_dd.parse(dateString).getTime() < dateFormat_yyyy_MM_dd.parse(endDateString).getTime()) {
                return CalenderAdapter.BETWEEN;
            } else return CalenderAdapter.OTHERS;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return CalenderAdapter.OTHERS;
    }
}
