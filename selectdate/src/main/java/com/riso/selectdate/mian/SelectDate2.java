package com.riso.selectdate.mian;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;

import com.riso.selectdate.R;
import com.riso.selectdate.bean.IntDateBean;
import com.riso.selectdate.bean.SelectMonthBean;
import com.riso.selectdate.holder.SelectDateViewHolder;
import com.riso.selectdate.utils.SelectDateUtils;
import com.riso.selectdate.views.NoScrollGridView3;

import java.util.List;


/**
 * 创建者:  王黎聪  创建时间: 2017/7/15.
 * <p>
 * <p>
 * 有两个更好的想法
 * 1种.固定GridView高度, 通过MarginBottom值, 如果大于35个设置marginBottom为0,反之 设置为 负数值 -**dp
 * 2种.固定GridView高度, 通过动画 动画上下移动
 */

public class SelectDate2 {


    //默认时间 格式  '2017-7-11 16:36' "yyyy-MM-dd HH:mm"
    private View mRootView;//从下方弹出的父View
    private Activity context;//上下文
    private String startTime;//开始选择的时间  null 就是可以选择过去
    private String defaultTime;//默认选中的时间  ,就是第一页展示的时间
    public SelectDateUtils selectDateUtils = new SelectDateUtils();   //选择时间的工具类
    private PagerAdapter pagerAdapter;  //竖 ViewPager  的适配器
    private int lastMouthDaySize; //记录上次选中的月的格数
    private SelectDateListener selectDateListener;   //确定按钮的回调监听
    private SelectMonthBean defaultSelectMonthBean;//默认展示的页面
    private IntDateBean defaultIntDateBean;//选择中的对象
    private IntDateBean todayIntDateBean;  //今天的展示对象
    private IntDateBean startIntDateBean;  //开始选择的日期展示的对象
    private SelectMonthBean currentSelectMonthBean;
    private boolean isCanClick = true;  //防止快速重复点击
    private SelectDateViewHolder vh;

    /**
     * 构造器
     *
     * @param context            上下文
     * @param startTime          开始时间(如果传null==可以选择过去时间, 不传入空==只能选择开始时间之后的时间,)
     * @param defaultTime        默认选中的时间 也是 展示的月份(如果传null==默认是今天,建议默认时间要大于开始时间)
     * @param mRootView          需要弹出popwindow 的跟布局
     * @param selectDateListener 选择时间的回调监听
     */
    public SelectDate2(Activity context, String startTime, String defaultTime, View mRootView, SelectDateListener selectDateListener) {
        this.context = context;
        this.startTime = startTime;
        this.defaultTime = defaultTime;
        this.mRootView = mRootView;
        this.selectDateListener = selectDateListener;
        initView();
        initAdapter();
    }

    private void initAdapter() {

        //初始化点击事件
        vh.tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != selectDateListener) {
                    defaultIntDateBean.tHour = vh.ctp_time.getCurrentHour();
                    defaultIntDateBean.tMinute = vh.ctp_time.getCurrentMinute();
                    selectDateListener.OnOk(defaultIntDateBean.toString());
                }
            }
        });

        pagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return Integer.MAX_VALUE;
            }

            @Override
            public View instantiateItem(ViewGroup container, int positions) {
                final SelectMonthBean selectMonthBean;
                int flag = currentSelectMonthBean.id - positions;
                switch (flag) {
                    case -2:
                        currentSelectMonthBean = currentSelectMonthBean.getNextMonthBean();
                    case -1:
                        selectMonthBean = currentSelectMonthBean.getNextMonthBean();
                        break;
                    case 2:
                        currentSelectMonthBean = currentSelectMonthBean.getPreviousMonthBean();
                    case 1:
                        selectMonthBean = currentSelectMonthBean.getPreviousMonthBean();
                        break;
                    default:
                        selectMonthBean = currentSelectMonthBean;
                        break;
                }

                final NoScrollGridView3 noScrollGridView3 = new NoScrollGridView3(context);
                noScrollGridView3.setNumColumns(7);
                noScrollGridView3.setTag(selectMonthBean);
                final BaseAdapter baseAdapter = new BaseAdapter() {

                    @Override
                    public int getCount() {
                        return selectMonthBean.dayList.size();
                    }

                    @Override
                    public Object getItem(int position) {
                        return selectMonthBean.dayList.get(position);
                    }

                    @Override
                    public long getItemId(int position) {
                        return position;
                    }

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {

                        List<Integer> dayList = selectMonthBean.dayList;
                        int intDay = dayList.get(position);
                        TextView textView = new TextView(context);
                        textView.setTextSize(16);
                        textView.setGravity(Gravity.CENTER);
                        textView.setHeight(SelectDateUtils.dp2px(context, 49.5f));
                        if (intDay == -1) {
                            textView.setText(" ");
                        } else if (intDay == 1) {
                            textView.setTextColor(vh.colorSelectBack);
                            if (selectMonthBean.mYear != todayIntDateBean.tYear) {
                                textView.setTextSize(16);
                                String showMouthString = selectMonthBean.mMonth + "月\n" + selectMonthBean.mYear + "年";
                                SpannableStringBuilder spannable = new SpannableStringBuilder(showMouthString);
                                spannable.setSpan(new RelativeSizeSpan(0.7f), showMouthString.length() - 5, showMouthString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                textView.setText(spannable);
                            } else {
                                textView.setTextSize(17);
                                textView.setText(selectMonthBean.mMonth + "月");
                            }
                        } else {
                            if (null != startIntDateBean && (startIntDateBean.tYear > selectMonthBean.mYear ||
                                    (startIntDateBean.tYear == selectMonthBean.mYear && startIntDateBean.tMonth > selectMonthBean.mMonth) ||
                                    (startIntDateBean.tYear == selectMonthBean.mYear && startIntDateBean.tMonth == selectMonthBean.mMonth && startIntDateBean.tDay > intDay))) {
                                textView.setTextColor(vh.colorHintText);
                            } else {
                                textView.setTextColor(vh.colorBlackText);

                            }
                            textView.setText("" + intDay);
                        }

                        if (defaultIntDateBean.tYear == selectMonthBean.mYear && defaultIntDateBean.tMonth == selectMonthBean.mMonth && defaultIntDateBean.tDay == intDay) {
                            textView.setText("" + intDay);
                            textView.setTextSize(17);
                            textView.setBackgroundResource(R.drawable.select_date_day_back_on);
                            textView.setTextColor(vh.colorSelectText);
                        } else if (todayIntDateBean.tYear == selectMonthBean.mYear && todayIntDateBean.tMonth == selectMonthBean.mMonth && todayIntDateBean.tDay == intDay) {
                            textView.setText("" + intDay);
                            textView.setTextColor(vh.colorSelectBack);
                            textView.setBackgroundResource(R.drawable.select_date_day_back_on_today);
                        } else {
                            textView.setBackgroundResource(R.drawable.select_date_day_back);
                        }
                        return textView;
                    }
                };
                noScrollGridView3.setAdapter(baseAdapter);
                noScrollGridView3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (isCanClick) {
                            isCanClick = false;
                            SelectMonthBean selectMonthBeanTag = (SelectMonthBean) noScrollGridView3.getTag();
                            int clickDay = selectMonthBeanTag.dayList.get(position);
                            if (clickDay != -1 && (startIntDateBean == null ||
                                    (startIntDateBean.tYear < selectMonthBeanTag.mYear ||
                                            (startIntDateBean.tYear == selectMonthBeanTag.mYear && startIntDateBean.tMonth < selectMonthBeanTag.mMonth) ||
                                            (startIntDateBean.tYear == selectMonthBeanTag.mYear && startIntDateBean.tMonth == selectMonthBeanTag.mMonth && startIntDateBean.tDay <= clickDay)))) {
                                defaultIntDateBean.tYear = selectMonthBeanTag.mYear;
                                defaultIntDateBean.tMonth = selectMonthBeanTag.mMonth;
                                defaultIntDateBean.tDay = clickDay;
                                vh.tv_date.setText(defaultIntDateBean.yearMonthDayToString());
                                pagerAdapter.notifyDataSetChanged();
                            }
                            isCanClick = true;
                        }
                    }
                });
                container.addView(noScrollGridView3);
                return noScrollGridView3;
            }

            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        };
        vh.verticalviewpager.setAdapter(pagerAdapter);
        int currentItem = selectDateUtils.getLastMonthCount(defaultSelectMonthBean, new SelectMonthBean(0, startIntDateBean.tYear, startIntDateBean.tMonth));
        vh.verticalviewpager.setCurrentItem(currentItem);
        currentSelectMonthBean = new SelectMonthBean(currentItem, defaultIntDateBean.tYear, defaultIntDateBean.tMonth);
        pagerAdapter.notifyDataSetChanged();
        vh.verticalviewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                int size;
                int flag = currentSelectMonthBean.id - position;
                switch (flag) {
                    case -1:
                        size = currentSelectMonthBean.getNextMonthBean().dayList.size();
                        break;
                    case 1:
                        size = currentSelectMonthBean.getPreviousMonthBean().dayList.size();
                        break;
                    default:
                        size = currentSelectMonthBean.dayList.size();
                        break;
                }
                setDateHight(size);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    //初始化布局
    private void initView() {
        //初始化ViewHolder
        vh = new SelectDateViewHolder(context, R.layout.layout_select_date);
        //判断 默认时间不为空
        if (TextUtils.isEmpty(defaultTime)) {
            defaultTime = selectDateUtils.getToday();
        }
        //初始化  默认对象
        defaultIntDateBean = selectDateUtils.setIntTimeBean(defaultTime);
        //初始 展示的位置
        defaultSelectMonthBean = new SelectMonthBean(0, defaultIntDateBean.tYear, defaultIntDateBean.tMonth);
        currentSelectMonthBean = new SelectMonthBean(0, defaultIntDateBean.tYear, defaultIntDateBean.tMonth);
        //今天
        todayIntDateBean = selectDateUtils.setIntTimeBean(selectDateUtils.getToday());
        //开始时间
        if (!TextUtils.isEmpty(startTime)) {
            startIntDateBean = selectDateUtils.setIntTimeBean(startTime);
        }
        //保存默认分
        vh.tv_date.setText(defaultIntDateBean.tYear + "年" + defaultIntDateBean.tMonth + "月" + defaultIntDateBean.tDay + "日");
        vh.tv_time.setText(defaultTime.split(" ")[1]);
        vh.ctp_time.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
        vh.ctp_time.setIs24HourView(true);
        vh.ctp_time.setTextColor(vh.colorSelectBack);
        vh.ctp_time.setMinuts(new String[]{"00", "30"});
        vh.ctp_time.setCurrentHour(defaultIntDateBean.tHour);
        vh.ctp_time.setCurrentMinute(defaultIntDateBean.tMinute);
    }

    /**
     * 内部方法 设置 控件的高度
     *
     * @param size 如果 超过 35天 就 高度提升   否则不变
     */
    private void setDateHight(int size) {
        if (lastMouthDaySize != size) {
            if (size > 35) {
                verticalMoveAnima(vh.ll_all_content, vh.dayViewHeight, 0);
                //verticalviewpager.getLayoutParams().height = SelectDateUtils.dp2px(context, 300);
                //pagerAdapter.notifyDataSetChanged();
            } else {
                verticalMoveAnima(vh.ll_all_content, 0, vh.dayViewHeight);
                //verticalviewpager.getLayoutParams().height = SelectDateUtils.dp2px(context, 250);
            }
            lastMouthDaySize = size;
        }
    }


    /**
     * 获取自定义popwindow
     *
     * @return
     */
    public PopupWindow getPopwindow() {
        return vh.popwindow;
    }

    /**
     * 显示弹窗
     */
    public void show() {
        vh.popwindow.showAtLocation(mRootView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        //初始化事件控件的高度
        if (null != currentSelectMonthBean.dayList && currentSelectMonthBean.dayList.size() > 0) {
            lastMouthDaySize = currentSelectMonthBean.dayList.size();
            if (lastMouthDaySize > 35) {
                // verticalviewpager.getLayoutParams().height = SelectDateUtils.dp2px(context, 250);
                ObjectAnimator.ofFloat(vh.ll_all_content, "translationY", vh.viewHeight, 0).setDuration(SelectDateViewHolder.ANIMA_DURATION).start();
            } else {
                ObjectAnimator.ofFloat(vh.ll_all_content, "translationY", vh.viewHeight, vh.dayViewHeight).setDuration(SelectDateViewHolder.ANIMA_DURATION).start();
            }
        }
    }

    /**
     * 上下移动使用的动画
     *
     * @param view  需要的移动的视图
     * @param fromY 从哪移动
     * @param toY   移动到哪
     */
    private void verticalMoveAnima(Object view, float fromY, float toY) {
        ObjectAnimator.ofFloat(view, "translationY", fromY, toY).setDuration(SelectDateViewHolder.ANIMA_DURATION).start();
    }

    /**
     * 回调接口
     */
    public interface SelectDateListener {
        /**
         * @param selectDate 时间格式:2000-01-01 08:00
         */
        void OnOk(String selectDate);
    }
}
