package com.exam.novelt3_1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

public class HistoryCalendarActivity extends Activity {
    TextView text_time, text_username;
    Button btn_goOut;
    MaterialCalendarView calendarView;
    OneDayDecorator oneDayDecorator = new OneDayDecorator();

    TimerTask task;
    Timer timer;

    String name, hp, height, weight, gender, local, place;
    int age, Year, Month, Day;
    String[] history_dates;

    ListView list_history;
    ArrayList<HistoryListItem> list_items = new ArrayList<>();
    historyListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_calendar);

        text_time = (TextView)findViewById(R.id.text_time_history_calendar);
        task = new TimerTask() {
            @Override
            public void run() {
                calc_time();
            }
        };
        timer = new Timer();
        timer.schedule(task, 0, 1000);

        btn_goOut = (Button)findViewById(R.id.btn_goOut_history_calendar);
        btn_goOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        text_username = (TextView)findViewById(R.id.text_user_name_history_calendar);
        Intent intent = getIntent();
        name = intent.getExtras().getString("NAME");
        hp = intent.getExtras().getString("HP");
        height = intent.getExtras().getString("HEIGHT");
        weight = intent.getExtras().getString("WEIGHT");
        age = intent.getExtras().getInt("AGE");
        gender = intent.getExtras().getString("GENDER");
        text_username.setText(name.substring(0,1) + " " + name.substring(1,2) + " " + name.substring(2,3));
        local = intent.getExtras().getString("LOCAL");
        place = intent.getExtras().getString("PLACE");

        Calendar calendar = Calendar.getInstance();
        Year = calendar.get(calendar.YEAR);
        Month = calendar.get(calendar.MONTH) + 1;
        //Day = calendar.get(Calendar.DAY_OF_MONTH);
        onLoad_calendar_dates();

        calendarView = (MaterialCalendarView)findViewById(R.id.history_calendarView);
        calendarView.state().edit().setFirstDayOfWeek(Calendar.SUNDAY)
                .setMinimumDate(CalendarDay.from(Year, 0, 1))
                .setMaximumDate(CalendarDay.from(Year, 12, 31))
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();

        calendarView.setShowOtherDates(MaterialCalendarView.SHOW_OUT_OF_RANGE);
        calendarView.setDynamicHeightEnabled(true);
        calendarView.setWeekDayTextAppearance(R.style.HistoryCalanderAppearance);
        calendarView.setHeaderTextAppearance(R.style.HistoryCalanderAppearance);
        calendarView.setDateTextAppearance(R.style.HistoryCalanderAppearance);
        calendarView.setArrowColor(Color.WHITE);

        calendarView.addDecorators(new SundayDecorator(), new SaturdayDecorator(), oneDayDecorator);

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {     // 특정 날짜 클릭 이벤트 - 해당 날짜에 대한 운동 내역 조회 후, 리스트뷰에 내용 출력
                Year = date.getYear();
                Month = date.getMonth() + 1;
                Day = date.getDay();
                init();
            }
        });

        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                Year = date.getYear();
                Month = date.getMonth() + 1;
                exerciseData.clear();
                list_items.clear();
                adapter.notifyDataSetChanged();
                onLoad_calendar_dates();
            }
        });

        list_history = (ListView)findViewById(R.id.history_list);
        adapter = new historyListAdapter();
        list_history.setAdapter(adapter);
        init();
        adapter.notifyDataSetChanged();
        list_history.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(HistoryCalendarActivity.this, HistoryDetailActivity.class);
                intent.putExtra("HP", hp);
                intent.putExtra("NAME", name);
                intent.putExtra("HEIGHT", height);
                intent.putExtra("WEIGHT", weight);
                intent.putExtra("AGE", age);
                intent.putExtra("GENDER", gender);
                intent.putExtra("START_TIME", exerciseData.get(position).start_time);
                intent.putExtra("PROTOCOL_NAME", exerciseData.get(position).protocol_name);
                intent.putExtra("MAX_HR", exerciseData.get(position).max_hr);
                intent.putExtra("AVG_HR", exerciseData.get(position).avg_hr);
                intent.putExtra("MAX_SPEED", exerciseData.get(position).max_speed);
                intent.putExtra("AVG_SPEED", exerciseData.get(position).avg_speed);
                intent.putExtra("CALORIE", exerciseData.get(position).calorie);
                intent.putExtra("DISTANCE", exerciseData.get(position).distance);
                intent.putExtra("ZONE_FACTOR", exerciseData.get(position).zone_factor);
                intent.putExtra("LOCAL", local);
                intent.putExtra("PLACE", place);
                startActivity(intent);
            }
        });
    }

    public void calc_time() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM. dd. HH:mm:ss");
                Date date = new Date(now);
                String time = sdf.format(date);
                text_time.setText(time);
            }
        });
    }

    boolean isLoad_dates = false;
    private void onLoad_calendar_dates() {
        String tmp = String.valueOf(Month);
        isLoad_dates = true;
        ContentValues values = new ContentValues();
        values.put("YEAR", Year);
        if(Month < 10) tmp = "0" + Month;
        values.put("MONTH", tmp);
        values.put("HP", hp);
        values.put("LOCAL", local);
        values.put("PLACE", place);

        HttpPostDataClass post = new HttpPostDataClass("http://125.130.221.35:8001/PROC/AjaxForGetHR_Exercise_Date.asp", values);
        post.execute();
        new CalendarHistoryPointer(history_dates).executeOnExecutor(Executors.newSingleThreadExecutor());
    }

    boolean isLoad_exercises = false;
    private void onLoad_day_exercises(int year, int month, int day) {
        String tmp_month = String.valueOf(month);
        if(month < 10) tmp_month = "0" + month;
        String tmp_day = String.valueOf(day);
        if(day < 10) tmp_day = "0" + day;

        String date = year + "-" + tmp_month + "-" + tmp_day;

        isLoad_exercises = true;
        ContentValues values = new ContentValues();
        values.put("HP", hp);
        values.put("THIS_DATE", date);
        values.put("LOCAL", local);
        values.put("PLACE", place);

        HttpPostDataClass post = new HttpPostDataClass("http://125.130.221.35:8001/PROC/AjaxForGetHR_Exercise_List.asp", values);
        post.execute();
    }

    private void init() {
        list_items.clear();
        onLoad_day_exercises(Year, Month, Day);
        exerciseData.clear();
    }

    ArrayList<ExerciseData> exerciseData = new ArrayList<>();
    class HttpPostDataClass extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;

        public HttpPostDataClass(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = null;
            RequestHttpURLConnection connection = new RequestHttpURLConnection();
            result = connection.request(url, values);

            if(result != null) {
                if(isLoad_dates) {
                    if(!result.equals("")) {
                        String[] tmp = result.split("<|>");
                        history_dates = tmp;
                    } else {
                        history_dates = null;
                    }
                    isLoad_dates = false;
                } else if(isLoad_exercises) {
                    if(!result.equals("")) {
                        String[] tmp = result.split("<\\|>");
                        for(int i = 0; i < tmp.length; i++) {
                            String[] tmp1 = tmp[i].split("<#>");
                            ExerciseData data = null;
                            if(tmp1.length == 12) {
                                data  = new ExerciseData(tmp1[0], tmp1[1], tmp1[2], tmp1[3], tmp1[4], tmp1[5], tmp1[6], tmp1[7], tmp1[8], tmp1[9], "", "", Float.parseFloat(tmp1[10]));
                            } else if(tmp1.length == 13) {
                                data  = new ExerciseData(tmp1[0], tmp1[1], tmp1[2], tmp1[3], tmp1[4], tmp1[5], tmp1[6], tmp1[7], tmp1[8], tmp1[9], tmp1[10], "", Float.parseFloat(tmp1[11]));
                            } else if(tmp1.length == 14) {
                                data  = new ExerciseData(tmp1[0], tmp1[1], tmp1[2], tmp1[3], tmp1[4], tmp1[5], tmp1[6], tmp1[7], tmp1[8], tmp1[9], tmp1[10], tmp1[11], Float.parseFloat(tmp1[12]));
                            }
                            exerciseData.add(data);
                        }
                    }
                    isLoad_exercises = false;
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            for(int i = 0; i < exerciseData.size(); i++) {
                String[] tmp_ = exerciseData.get(i).start_time.split("_");
                String tmp = tmp_[0] + "-" + tmp_[1] + "-" + tmp_[2] + " " + tmp_[3] + ":" + tmp_[4];

                String tmp_survey_speed, tmp_survey_incline, tmp_survey_zone;
                if(!exerciseData.get(i).survey_speed.equals("")) tmp_survey_speed = exerciseData.get(i).survey_speed;
                else tmp_survey_speed = "";

                if(!exerciseData.get(i).survey_incline.equals("")) tmp_survey_incline = exerciseData.get(i).survey_incline;
                else tmp_survey_incline = "";

                if(!exerciseData.get(i).survey_zone.equals("")) tmp_survey_zone = exerciseData.get(i).survey_zone;
                else tmp_survey_zone = "";

                String number = String.valueOf(i+1);
                if(i < 10) number = "0" + (i+1);
                final HistoryListItem listItem = new HistoryListItem(number, tmp, exerciseData.get(i).protocol_name, exerciseData.get(i).rate + "%", tmp_survey_speed, tmp_survey_incline, tmp_survey_zone);
                list_items.add(listItem);
            }
            adapter.notifyDataSetChanged();
        }
    }

    class ExerciseData {
        String protocol_name;
        String start_time;
        String rate;
        String distance;
        String max_hr;
        String avg_hr;
        String max_speed;
        String avg_speed;
        String calorie;
        String survey_speed;
        String survey_incline;
        String survey_zone;
        float zone_factor;

        public ExerciseData(String protocolName, String startTime, String Rate, String Distance, String maxHR, String avgHR, String maxSpeed, String avgSpeed, String Calorie, String surveySpeed, String surveyIncline, String surveyZone, float factor) {
            this.protocol_name = protocolName; this.start_time = startTime; this.rate = Rate; this.distance = Distance; this.max_hr = maxHR;
            this.avg_hr = avgHR; this.max_speed = maxSpeed; this.avg_speed = avgSpeed; this.calorie = Calorie;
            this.survey_speed = surveySpeed; this.survey_incline = surveyIncline; this.survey_zone = surveyZone; this.zone_factor = factor;
        }
    }

    private class CalendarHistoryPointer extends AsyncTask<Void, Void, List<CalendarDay>> {
        String[] Time_Result;

        CalendarHistoryPointer(String[] Time_Result){
            this.Time_Result = Time_Result;
        }

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ArrayList<CalendarDay> dates = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            if(history_dates != null) {
                for(int i = 0 ; i < history_dates.length; i += 2) {
                    String[] tmp = history_dates[i].split("-");
                    calendar.set(Calendar.YEAR, Integer.valueOf(tmp[0]));
                    calendar.set(Calendar.MONTH, Integer.valueOf(tmp[1]) - 1);
                    calendar.set(Calendar.DATE, Integer.valueOf(tmp[2]));
                    CalendarDay day = CalendarDay.from(calendar);
                    dates.add(day);
                }
            }

            return dates;
        }

        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);
            if (isFinishing()) {
                return;
            }
            calendarView.addDecorator(new EventDecorator(Color.RED, calendarDays, HistoryCalendarActivity.this));
        }
    }

    @Override
    protected void onDestroy() {
        task = null;
        timer = null;
        super.onDestroy();
    }

    class HistoryListItem {
        String list_number;
        String protocol_time, protocol_name, protocol_progress, survey_speed, survey_incline, survey_zone;
        int survey_speed_index = 1, survey_incline_index = 1, survey_zone_index = 1;            // 0 : blue, 1 : green, 2 : red, 3 : gray

        HistoryListItem(String listNumber, String protocolTime, String protocolName, String protocolProgress, String surveySpeed, String surveyIncline, String surveyZone) {
            this.list_number = listNumber;
            this.protocol_time = protocolTime;
            this.protocol_name = protocolName;
            this.protocol_progress = protocolProgress;
            this.survey_speed = surveySpeed;
            this.survey_incline = surveyIncline;
            this.survey_zone = surveyZone;

            if(!survey_speed.equals("")) {
                String[] tmp_speed = survey_speed.split("_");
                if(tmp_speed[0].equals("속도")) {
                    if(tmp_speed[1].equals("-5")) survey_speed_index = 3;
                } else if(tmp_speed[0].equals("HRzone")) {
                    if(tmp_speed[1].equals("-5")) survey_speed_index = 2;
                    else if(tmp_speed[1].equals("+5")) survey_speed_index = 0;
                    else survey_speed_index = 1;
                }
            } else {
                survey_speed_index = 4;     // 이미지 null
            }

            if(!survey_incline.equals("")) {
                String[] tmp_incline = survey_incline.split("_");
                if(tmp_incline[0].equals("경사")) {
                    if(tmp_incline[1].equals("-2")) survey_incline_index = 2;
                    else if(tmp_incline[1].equals("+2")) survey_incline_index = 0;
                    else survey_incline_index = 1;
                }
            } else {
                survey_incline_index = 3;
            }

            if(!survey_zone.equals("")) {
                String[] tmp_zone = survey_zone.split("_");
                if(tmp_zone[0].equals("운동시간")) {
                    if(tmp_zone[1].equals("+10")) survey_zone_index = 0;
                    else if(tmp_zone[1].equals("-10")) survey_zone_index = 2;
                    else survey_zone_index = 1;
                } else if(tmp_zone[0].equals("버전")) {
                    survey_zone_index = 2;
                }
            } else {
                survey_zone_index = 3;
            }
        }
    }

    class historyListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list_items.size();
        }

        @Override
        public Object getItem(int position) {
            return list_items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Context context = parent.getContext();
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.history_list_item, parent, false);
            }

            TextView text_list_number, text_protocol_time, text_protocol_name, text_progress;
            ImageView img_survey_speed, img_survey_incline, img_survey_zone;

            text_list_number = (TextView)convertView.findViewById(R.id.text_list_number);
            text_protocol_time = (TextView)convertView.findViewById(R.id.text_protocol_start_time);
            text_protocol_name = (TextView)convertView.findViewById(R.id.text_protocol_name_history_list);
            text_progress = (TextView)convertView.findViewById(R.id.text_progress_history_list);
            img_survey_speed = (ImageView)convertView.findViewById(R.id.img_survey_speed);
            img_survey_incline = (ImageView)convertView.findViewById(R.id.img_survey_incline);
            img_survey_zone = (ImageView)convertView.findViewById(R.id.img_survey_zone);

            // 리스트 아이템을 서버로부터 조회하는 부분 필요

            HistoryListItem item = list_items.get(position);
            text_list_number.setText(item.list_number + "");
            text_protocol_time.setText(item.protocol_time);
            text_protocol_name.setText(item.protocol_name);
            text_progress.setText(item.protocol_progress);

            // 설문 속도, 경사, zone에 대한 내용에 따라 이미지 변경
            if(item.survey_speed_index == 0) {
                img_survey_speed.setImageResource(R.drawable.img_survey_circle_blue);
            } else if(item.survey_speed_index == 1) {
                img_survey_speed.setImageResource(R.drawable.img_survey_circle_green);
            } else if(item.survey_speed_index == 2) {
                img_survey_speed.setImageResource(R.drawable.img_survey_circle_red);
            } else if(item.survey_speed_index == 3) {
                img_survey_speed.setImageResource(R.drawable.img_survey_circle_gray);
            } else img_survey_speed.setImageResource(0);

            if(item.survey_incline_index == 0) {
                img_survey_incline.setImageResource(R.drawable.img_survey_circle_blue);
            } else if(item.survey_incline_index == 1) {
                img_survey_incline.setImageResource(R.drawable.img_survey_circle_green);
            } else if(item.survey_incline_index == 2) {
                img_survey_incline.setImageResource(R.drawable.img_survey_circle_red);
            } else if(item.survey_incline_index == 3) {
                img_survey_incline.setImageResource(0);
            }

            if(item.survey_zone_index == 0) {
                img_survey_zone.setImageResource(R.drawable.img_survey_circle_blue);
            } else if(item.survey_zone_index == 1) {
                img_survey_zone.setImageResource(R.drawable.img_survey_circle_green);
            } else if(item.survey_zone_index == 2) {
                img_survey_zone.setImageResource(R.drawable.img_survey_circle_red);
            } else if(item.survey_zone_index == 3)img_survey_zone.setImageResource(0);

            return convertView;
        }
    }

    @Override
    public void onBackPressed() { }
}
