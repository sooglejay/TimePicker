package com.sooglejay.timepicker.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.sooglejay.timepicker.R;
import com.sooglejay.timepicker.bean.FatherBean;
import com.sooglejay.timepicker.bean.SonBean;
import com.sooglejay.timepicker.widget.MyGridView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by JammyQtheLab on 2015/10/20.
 */
public class CalenderAdapter extends BaseAdapter {

    public final static int START = 0;
    public final static int END = 1;
    public final static int BETWEEN = 2;
    public final static int OTHERS = 3;


    private static final int Data = 0;
    private static final int Header = 1;
    private static final int ViewTypeCount = 2;


    private Activity mContext;
    private List<Object> mDatas = new ArrayList<>();
    private LayoutInflater inflater;
    private int tvWhiteColorResId, tvBlackColorResId;//日历 数字本身的颜色值

    private int clickCount = -1;//用来标识日历是否是在选择开始时间，如果，用户已经选择了开始时间，用户再次点击日历控件时，若选中的时间在开始时间前，那么认为是在调整开始时间，反之就是在选择结束时间。注意，选择的时间物理上是不允许在今天的时间之前的
    private String startDateString = "";//这个变量的作用是，记住用户选择的开始时间，然后判断用户第二次选择的时间，如果是第一次选择的时间之前，那么，就认为用户是在调整开始时间，否则就是在选择结束时间


    private SonBean outerDayBean;//这个变量是记住上一次点击过的View，用来做背景色的变化
    private SimpleDateFormat dateFormat_yyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd");//日期格式化
    private String todayString_yyyy_m_d = new SimpleDateFormat("yyyy-MM-dd").format(new Date());


    private String timeStringForPostToServer = "";
    private long orderStartTimeFromServer = 0;
    private long orderEndTimeFromServer = 0;


    public void setOrderStartTimeFromServer(long orderStartTimeFromServer) {
        long today = new Date().getTime();
        this.orderStartTimeFromServer = orderStartTimeFromServer < today ? today : orderStartTimeFromServer;
    }

    public void setOrderEndTimeFromServer(long orderEndTimeFromServer) {
        this.orderEndTimeFromServer = orderEndTimeFromServer;
    }

    public CalenderAdapter(Activity mContext, List<Object> mDatas) {
        this.mContext = mContext;
        this.mDatas = mDatas;
        inflater = LayoutInflater.from(mContext);
        tvWhiteColorResId = mContext.getResources().getColor(R.color.white_color);
        tvBlackColorResId = mContext.getResources().getColor(R.color.black_color);
    }


    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return ViewTypeCount;
    }

    @Override
    public int getItemViewType(int position) {
        return (mDatas.get(position) instanceof String) ? Header
                : Data;
    }


    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != Header;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final int type = getItemViewType(position);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            final int layoutID = type == Header ? R.layout.aaa_item_calender_list_view_header : R.layout.aaa_item_calender_list_view_data;
            convertView = inflater.inflate(layoutID, parent, false);
            if (type == Header) {
                holder.tvHeaderString = (TextView) convertView.findViewById(R.id.tv_type_header);
            } else {
                holder.myGridView = (MyGridView) convertView.findViewById(R.id.gv_calendar);
                holder.innerGridViewAdapter = new InnerGridViewAdapter();
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Object obj = getItem(position);
        if (type == Header) {//如果是标题栏
            String yyyymm = (String) obj;//比如 ：yyyymm 是 2015年3月
            holder.tvHeaderString.setText(yyyymm + "");
        } else if (obj instanceof FatherBean) {//如果是数据
            FatherBean bean = (FatherBean) obj;
            holder.innerGridViewAdapter.setYmDatas(bean);
            holder.myGridView.setAdapter(holder.innerGridViewAdapter);
        }
        return convertView;
    }

    private static class ViewHolder {
        private TextView tvHeaderString;
        private MyGridView myGridView;
        private InnerGridViewAdapter innerGridViewAdapter;
    }

    private class InnerGridViewAdapter extends BaseAdapter {
        private List<SonBean> dayListBean = new ArrayList<>();

        @Override
        public int getCount() {
            return dayListBean.size();
        }

        @Override
        public Object getItem(int position) {
            return dayListBean.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            CalenderAdapter.this.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final GrideViewHolder innerGridViewHolder;
            if (convertView == null) {
                innerGridViewHolder = new GrideViewHolder();
                convertView = inflater.inflate(R.layout.item_calendar_gridview, parent, false);
                innerGridViewHolder.tv_bottom = (TextView) convertView.findViewById(R.id.tv_bottom);
                innerGridViewHolder.tv_up = (TextView) convertView.findViewById(R.id.tv_up);
                innerGridViewHolder.item = (LinearLayout) convertView.findViewById(R.id.item);
                innerGridViewHolder.onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SonBean bean = (SonBean) v.getTag();
                        if (TextUtils.isEmpty(bean.getDateStr())) {
                            return;//说明是该位置没有日期
                        }
                        switch (v.getId()) {
                            case R.id.item:
                                try {
                                    if (dateFormat_yyyy_MM_dd.parse(bean.getDateStr()).getTime() < orderStartTimeFromServer) {
                                        Toast.makeText(mContext, "日期无效，请重新选择！", Toast.LENGTH_SHORT).show();
                                        return;
                                    } else if (clickCount < 0) {//选择开始时间
                                        for (int i = 0; i < mDatas.size(); i++) {
                                            Object obj = mDatas.get(i);
                                            if (obj instanceof String) {
                                                continue;
                                            } else if (obj instanceof FatherBean) {    //如果是数据
                                                FatherBean ymBean = (FatherBean) obj;
                                                List<SonBean> dayBeanList = ymBean.getDaysList();
                                                for (SonBean b : dayBeanList) {
                                                    if (TextUtils.isEmpty(b.getDateStr()))
                                                        continue;
                                                    b.setStatus(OTHERS);
                                                }
                                            }
                                        }
                                        bean.setStatus(START);
                                        timeStringForPostToServer = bean.getDateStr() + ",";
                                        clickCount = 1;
                                        outerDayBean = bean;
                                        startDateString = bean.getDateStr();

                                    } else if (clickCount > 0)//如果不是第一次点击
                                    {
                                        long minus = dateFormat_yyyy_MM_dd.parse(bean.getDateStr()).getTime() - dateFormat_yyyy_MM_dd.parse(startDateString).getTime();
                                        if (minus < 0)//点击的时间在上一次的前面，就认为是在调整开始时间
                                        {
                                            outerDayBean.setStatus(OTHERS);
                                            bean.setStatus(START);
                                            timeStringForPostToServer = bean.getDateStr() + ",";
                                            outerDayBean = bean;
                                            startDateString = bean.getDateStr();
                                        } else if (minus == 0)//点击的时间与上一次的一样，就认为是在调整开始时间
                                        {
                                            return;//不做处理
                                        } else {
                                            //服务端需要的时间格式 “2015-10-22,2015-10-23 ,...，2015-10-26,2015-10-27”


                                            //开始和结束时间在同一个月，就不做全部的数据更新了
                                            String[] endDateArray = bean.getDateStr().split("-");
                                            String[] startDateArray = startDateString.split("-");
                                            if (endDateArray[1].equals(startDateArray[1])) {
                                                for (SonBean b : dayListBean) {
                                                    //如果时间在 开始和结束之间，则 设置状态量为 BETWEEN
                                                    if (TextUtils.isEmpty(b.getDateStr())) {
                                                        continue;
                                                    }
                                                    if (dateFormat_yyyy_MM_dd.parse(b.getDateStr()).getTime() > dateFormat_yyyy_MM_dd.parse(startDateString).getTime()
                                                            &&
                                                            dateFormat_yyyy_MM_dd.parse(b.getDateStr()).getTime() < dateFormat_yyyy_MM_dd.parse(bean.getDateStr()).getTime()
                                                            ) {
                                                        b.setStatus(BETWEEN);
                                                        timeStringForPostToServer = timeStringForPostToServer + b.getDateStr() + ",";
                                                    }
                                                }
                                            } else {
                                                for (int j = 0; j < mDatas.size(); j++) {
                                                    Object obj = mDatas.get(j);
                                                    if (obj instanceof String) {
                                                        continue;
                                                    } else if (obj instanceof FatherBean) {
                                                        FatherBean ymBean = (FatherBean) obj;
                                                        List<SonBean> dayBeanList = ymBean.getDaysList();
                                                        for (SonBean b : dayBeanList) {
                                                            //如果时间在 开始和结束之间，则 设置状态量为 BETWEEN
                                                            if (TextUtils.isEmpty(b.getDateStr())) {
                                                                continue;
                                                            }
                                                            if (dateFormat_yyyy_MM_dd.parse(b.getDateStr()).getTime() > dateFormat_yyyy_MM_dd.parse(startDateString).getTime()
                                                                    &&
                                                                    dateFormat_yyyy_MM_dd.parse(b.getDateStr()).getTime() < dateFormat_yyyy_MM_dd.parse(bean.getDateStr()).getTime()
                                                                    ) {
                                                                b.setStatus(BETWEEN);
                                                                timeStringForPostToServer = timeStringForPostToServer + b.getDateStr() + ",";
                                                            }
                                                        }

                                                    }
                                                }
                                            }

                                            bean.setStatus(END);
                                            timeStringForPostToServer = timeStringForPostToServer + bean.getDateStr();
                                            int e_s = (int) ((dateFormat_yyyy_MM_dd.parse(bean.getDateStr()).getTime() - dateFormat_yyyy_MM_dd.parse(startDateString).getTime()) / IntConstant.milliSecondInADay);
                                            BusEvent intEvent = new BusEvent(BusEvent.MSG_INT_TIME);
                                            intEvent.setStart_time(startDateString);
                                            intEvent.setEnd_time(bean.getDateStr());
                                            intEvent.setTimeStringFroServer(timeStringForPostToServer);
                                            intEvent.setInterval_time(e_s + "天");
                                            EventBus.getDefault().post(intEvent);
                                            mContext.finish();
                                        }

                                    }
                                    break;
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                        //要手动去通知!
                        notifyDataSetChanged();
                    }
                };
                convertView.setTag(innerGridViewHolder);
            } else {
                innerGridViewHolder = (GrideViewHolder) convertView.getTag();
            }


            String dayNumberString = dayListBean.get(position).getDayNumberString();
            int status = dayListBean.get(position).getStatus();
            innerGridViewHolder.tv_up.setText(dayNumberString + "");
            switch (status) {
                case START:
                    innerGridViewHolder.tv_bottom.setText("开始");
                    innerGridViewHolder.tv_bottom.setTextColor(tvBlackColorResId);
                    innerGridViewHolder.tv_up.setTextColor(tvWhiteColorResId);
                    innerGridViewHolder.item.setBackgroundResource(R.drawable.shape_circle_base_color);
                    break;
                case END:
                    innerGridViewHolder.tv_bottom.setText("结束");
                    innerGridViewHolder.tv_bottom.setTextColor(tvBlackColorResId);
                    innerGridViewHolder.tv_up.setTextColor(tvWhiteColorResId);
                    innerGridViewHolder.item.setBackgroundResource(R.drawable.shape_circle_base_color);
                    break;
                case BETWEEN:
                    innerGridViewHolder.tv_bottom.setText("");
                    innerGridViewHolder.tv_bottom.setTextColor(tvBlackColorResId);
                    innerGridViewHolder.tv_up.setTextColor(tvWhiteColorResId);
                    innerGridViewHolder.item.setBackgroundResource(R.drawable.shape_circle_base_color);
                    break;
                case OTHERS:
                    innerGridViewHolder.tv_bottom.setText("");
                    innerGridViewHolder.tv_bottom.setTextColor(tvBlackColorResId);
                    innerGridViewHolder.tv_up.setTextColor(tvBlackColorResId);
                    innerGridViewHolder.item.setBackgroundResource(R.drawable.btn_select_for_white_to_circle_base_color);
                    break;
                default:
                    break;
            }


            try {
                if (dateFormat_yyyy_MM_dd.parse(dayListBean.get(position).getDateStr()).getTime() < orderStartTimeFromServer) {
                    innerGridViewHolder.tv_up.setTextColor(mContext.getResources().getColor(R.color.light_gray_color));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }


            //如果是周末
            if ((position + 1) % 7 == 0 || (position) % 7 == 0) {
                innerGridViewHolder.tv_up.setTextColor(mContext.getResources().getColor(R.color.red_color));
            }


            //如果是今天
            String dateString = dayListBean.get(position).getDateStr();
            if (dateString.equals(todayString_yyyy_m_d)) {
                innerGridViewHolder.tv_up.setTextColor(mContext.getResources().getColor(R.color.orange_color));
                innerGridViewHolder.tv_up.setText("今天");
            }

            innerGridViewHolder.item.setTag(dayListBean.get(position));
            innerGridViewHolder.item.setOnClickListener(innerGridViewHolder.onClickListener);

            return convertView;
        }

        public void setYmDatas(FatherBean ymDatas) {
            this.dayListBean.clear();
            this.dayListBean.addAll(ymDatas.getDaysList());
        }
    }

    static class GrideViewHolder {
        private TextView tv_up, tv_bottom;
        private LinearLayout item;
        private View.OnClickListener onClickListener;
    }

}
