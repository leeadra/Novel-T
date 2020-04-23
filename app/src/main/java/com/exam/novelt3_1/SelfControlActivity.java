package com.exam.novelt3_1;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.UartConfig;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class SelfControlActivity extends Activity implements ConnectionObserver, HeartRateObserver {
    TextView text_time, text_id, text_patch;
    TextView text_elapsedTime, text_hr, text_distance, text_calorie;
    ImageView indicator_connection, indicator_battery, img_ready_to_start, img_ready_to_stop;
    Button btn_speed_up, btn_speed_down, btn_incline_up, btn_incline_down;
    TextView text_speed, text_incline;
    TextView text_notice_band;
    Button btn_out;
    RelativeLayout layout;
    Custom_HRZoneGraph HRChart;

    static boolean isStarted = false;
    boolean isConnected, isFirst = true, isCancelled = false;
    int count = 0, batteryRatio, crt_hr,  max_hr = 0, avg_hr = 0, sum_hr = 0, cnt = 0;
    String patchName;
    Publisher publisher;
    HRPublisher publisher_hr;

    long startTime;
    float distance;
    float speed = 4;
    float max_speed = 4, avg_speed = 0, sum_speed = 5, cnt_speed = 1, percentage;
    int elapsed_time = 0;
    Animation anim, anim1, anim2;
    boolean isNoticed = false, isTouched = false, isReadyToStop = false;
    double calorie = 0;

    Physicaloid mSerial;
    private int mBaudrate           = 115200;
    private int mDataBits           = UartConfig.DATA_BITS8;
    private int mParity             = UartConfig.PARITY_NONE;
    private int mStopBits           = UartConfig.STOP_BITS1;
    private int mFlowControl        = UartConfig.FLOW_CONTROL_OFF;
    /*byte[] treadmill_start = {(byte)0xA0, 0x00};
    byte[] treadmill_stop = {(byte)0xA1, 0x00};
    byte[] treadmill_speed = {(byte)0xD0, (byte)((int)(Float.parseFloat(String.format("%.1f", speed)) * 10))};*/
    byte[] treadmill_start = {(byte)0xA0};
    byte[] treadmill_stop = {(byte)0xA1};
    byte[] treadmill_speed_up = {(byte)0xD0};
    byte[] treadmill_speed_down = {(byte)0xD1};
    byte[] treadmill_incline_up = {(byte)0xD2};
    byte[] treadmill_incline_down = {(byte)0xD3};

    ArrayList<Integer> HRs = new ArrayList<>();
    ArrayList<Long> times = new ArrayList<>();

    Timer timer_crtTime, timer_elapsed;
    TimerTask task_crtTime, task_elapsed;

    String name, height, weight, gender, max_bp, min_bp, bmi, hp, start_date, start_time, local, place;
    int age;

    String user_vo2max;

    float crt_speed = 4;
    int crt_incline = 0, max_incline = 0;

    LongPressRepeatListner repeatListner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_control);

        text_time = (TextView)findViewById(R.id.text_time_self_control_test);
        task_crtTime = new TimerTask() {
            @Override
            public void run() {
                calc_time();
            }
        };
        timer_crtTime = new Timer();
        timer_crtTime.schedule(task_crtTime, 0, 1000);

        text_id = (TextView)findViewById(R.id.text_userid_self_control_test);
        text_patch = (TextView)findViewById(R.id.text_patch_self_control_test);
        text_elapsedTime = (TextView)findViewById(R.id.text_elapsed_time_self_control_test);
        text_hr = (TextView)findViewById(R.id.text_heart_rate_self_control_test);
        text_distance = (TextView)findViewById(R.id.text_distance_self_control_test);
        text_calorie = (TextView)findViewById(R.id.text_calorie_self_control_test);
        indicator_connection = (ImageView)findViewById(R.id.indicator_connection_self_control_test);
        indicator_battery = (ImageView)findViewById(R.id.indicator_battery_self_control_test);
        img_ready_to_start = (ImageView)findViewById(R.id.img_ready_to_start_self_control_test);
        img_ready_to_stop = (ImageView)findViewById(R.id.img_ready_to_stop_self_control_test);
        btn_out = (Button)findViewById(R.id.btn_out_self_control_test);
        layout = (RelativeLayout)findViewById(R.id.layout_self_control_test);
        btn_speed_up = (Button)findViewById(R.id.btn_speed_up_self_control);
        btn_speed_down = (Button)findViewById(R.id.btn_speed_down_self_control);
        btn_incline_up = (Button)findViewById(R.id.btn_incline_up_self_control);
        btn_incline_down = (Button)findViewById(R.id.btn_incline_down_self_control);
        text_speed = (TextView)findViewById(R.id.text_speed_self_control);
        text_speed.setText(String.format("%.1f", crt_speed));
        text_incline = (TextView)findViewById(R.id.text_incline_self_control);
        text_notice_band = (TextView)findViewById(R.id.text_notice_band);

        Intent intent = getIntent();
        name = intent.getExtras().getString("NAME");
        height = intent.getExtras().getString("HEIGHT");
        weight = intent.getExtras().getString("WEIGHT");
        age = intent.getExtras().getInt("AGE");
        gender = intent.getExtras().getString("GENDER");
        max_bp = intent.getExtras().getString("MAX_BP");
        min_bp = intent.getExtras().getString("MIN_BP");
        bmi = intent.getExtras().getString("BMI");
        hp = intent.getExtras().getString("HP");
        local = intent.getExtras().getString("LOCAL");
        place = intent.getExtras().getString("PLACE");

        text_id.setText(name);

        anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(2000);
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);

        anim1 = new AlphaAnimation(0.0f, 1.0f);
        anim1.setDuration(2000);
        anim1.setStartOffset(20);
        anim1.setRepeatMode(Animation.REVERSE);
        anim1.setRepeatCount(Animation.INFINITE);

        anim2 = new AlphaAnimation(0.0f, 1.0f);
        anim2.setDuration(2000);
        anim2.setStartOffset(20);
        anim2.setRepeatMode(Animation.REVERSE);
        anim2.setRepeatCount(Animation.INFINITE);

        task_elapsed = task_ElapsedTime_creation();
        timer_elapsed = new Timer();

        timer_start_speed = new Timer();

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                img_ready_to_start.setVisibility(View.INVISIBLE);
                if(!isStarted) {
                    isStarted = true;
                    startTime = System.currentTimeMillis();
                    Date date = new Date(startTime);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    start_date = sdf.format(date);
                    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
                    start_time = sdf1.format(startTime);
                    timer_elapsed.schedule(task_elapsed, 0, 1000);
                    writeDataToSerial(1);
                } else {
                    if(isNoticed) {
                        img_ready_to_start.clearAnimation();
                        img_ready_to_start.setVisibility(View.INVISIBLE);
                    }
                    isReadyToStop = true;
                    if(isReadyToStop) {
                        count += 1;
                    }
                    if(count == 3) {
                        isCancelled = true;
                        img_ready_to_stop.clearAnimation();
                        img_ready_to_stop.setVisibility(View.GONE);
                        img_ready_to_start.clearAnimation();
                        img_ready_to_start.setVisibility(View.GONE);
                        writeDataToSerial(2);
                        onFinished();
                        finish();
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            count = 0;
                            isReadyToStop = false;
                        }
                    }, 2000);
                }
            }
        });

        publisher = MainActivity.publisher;
        publisher.addObserver(this);
        publisher_hr = MainActivity.publisher_hr;
        publisher_hr.addObserver(this);

        btn_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStarted = false;
                writeDataToSerial(2);
                //mSerial.close();
                finish();
            }
        });

        HRChart = (Custom_HRZoneGraph)findViewById(R.id.HRGraph_self_control_test);
        DPIHelper helper = new DPIHelper(getApplicationContext(), getWindowManager().getDefaultDisplay());
        HRChart.setTextSize((int)helper.getXDPI() / 16);

        mSerial = new Physicaloid(getApplicationContext());
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbRecevier, filter);

        //openUsbSerial();

        onLoad_UserVO2max();
        onLoad_UserRestHR();

        repeatListner = new LongPressRepeatListner(200, 100, listener);
        btn_speed_up.setOnTouchListener(repeatListner);
        btn_speed_down.setOnTouchListener(repeatListner);
        btn_incline_up.setOnTouchListener(repeatListner);
        btn_incline_down.setOnTouchListener(repeatListner);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    public TimerTask task_ElapsedTime_creation() {
        TimerTask tmp_task = new TimerTask() {
            @Override
            public void run() {
                onCalculate_elapsedTime();
            }
        };
        return tmp_task;
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

    BroadcastReceiver mUsbRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                if(!mSerial.isOpened()) {
                    openUsbSerial();
                }
            } else if(action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                Toast.makeText(getApplicationContext(), "트레드밀과 연결이 끊어졌습니다. 다시 실행해 주세요.", Toast.LENGTH_SHORT).show();
                mSerial.close();
                finish();
            }
        }
    };

    boolean isConnectedOTG = false;
    public void openUsbSerial() {
        if(mSerial == null) {
            Toast.makeText(getApplicationContext(), "트레드밀과 연결할 수 없습니다. 다시 실행해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!mSerial.isOpened()) {
            if(!mSerial.open()) {
                Toast.makeText(getApplicationContext(), "트레드밀과의 연결 상태를 확인하세요.", Toast.LENGTH_SHORT).show();
                return;
            } else {
                boolean dtrOn=false;
                boolean rtsOn=false;
                if(mFlowControl == UartConfig.FLOW_CONTROL_ON) {
                    dtrOn = true;
                    rtsOn = true;
                }
                mSerial.setConfig(new UartConfig(mBaudrate, mDataBits, mStopBits, mParity, dtrOn, rtsOn));
                if(!isConnectedOTG) {
                    Toast.makeText(this, "트레드밀과 연결되었습니다.", Toast.LENGTH_SHORT).show();
                    isConnectedOTG = true;
                }
            }
        }
    }

    int count_speed_up_start = 0;
    TimerTask task_start_speed = new TimerTask() {
        @Override
        public void run() {
            //mSerial.write(treadmill_speed_up);
            count_speed_up_start += 1;
            if(count_speed_up_start == 16) {
                if(task_start_speed != null) {
                    task_start_speed.cancel();
                }
                if(timer_start_speed != null) {
                    timer_start_speed.cancel();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn_out.setVisibility(View.VISIBLE);
                        img_ready_to_stop.startAnimation(anim);
                        img_ready_to_stop.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    };
    Timer timer_start_speed;

    private void writeDataToSerial(int id) {
        switch(id) {
            case 1:
                //mSerial.write(treadmill_start);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(timer_start_speed != null)
                            timer_start_speed.schedule(task_start_speed, 0, 100);
                    }
                }, 3500);
                break;
            case 2:
                //mSerial.write(treadmill_stop);
                break;
            case 3:
                //mSerial.write(treadmill_speed_up);
                break;
            case 4:
                //mSerial.write(treadmill_speed_down);
                break;
            case 5:
                //mSerial.write(treadmill_incline_up);
                break;
            case 6:
                //mSerial.write(treadmill_incline_down);
                break;
        }
    }

    @Override
    public void update(String name, boolean isConnect, int battery) {
        patchName = name;
        isConnected = isConnect;
        batteryRatio = battery;

        if(patchName != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text_patch.setVisibility(View.VISIBLE);
                    text_patch.setText(patchName);
                    indicator_connection.setVisibility(View.VISIBLE);
                    onUpdateBatteryIndicator();
                    text_notice_band.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text_patch.setVisibility(View.INVISIBLE);
                    text_patch.setText(" ");
                    indicator_connection.setVisibility(View.INVISIBLE);
                    indicator_battery.setVisibility(View.INVISIBLE);
                    text_notice_band.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public void onUpdateBatteryIndicator() {
        if(batteryRatio > 75) {
            indicator_battery.setImageResource(R.drawable.battery_indicator_full);
        } else if(batteryRatio > 50) {
            indicator_battery.setImageResource(R.drawable.battery_indicator_75p);
        } else if(batteryRatio > 25) {
            indicator_battery.setImageResource(R.drawable.battery_indicator_50p);
        } else if(batteryRatio > 10) {
            indicator_battery.setImageResource(R.drawable.battery_indicator_25p);
        } else {
            indicator_battery.setImageResource(R.drawable.battery_indicator_low);
        }
        indicator_battery.setVisibility(View.VISIBLE);
    }

    ArrayList<Integer> array_hr_10seconds = new ArrayList<>();
    long now;
    //WriteData writeData;
    @Override
    public void update(int heart_rate) {
        if(isStarted && !patchName.equals("") && patchName != null) {
            crt_hr = heart_rate;
            HRs.add(heart_rate);
            now = System.currentTimeMillis();
            times.add(now);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text_hr.setText(crt_hr + " bpm");
                    synchronized (HRChart) {
                        HRChart.add(crt_hr);
                    }
                }
            });

            if(max_hr < crt_hr) max_hr = crt_hr;

            sum_hr += crt_hr;
            cnt++;
        }
    }

    int rest_hr = 80;

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.btn_speed_up_self_control:
                    btn_speed_up.setBackgroundResource(R.drawable.btn_increase_signup_pressed);
                    writeDataToSerial(3);
                    if(crt_speed < 15.8) crt_speed += 0.2;
                    text_speed.setText(String.format("%.1f", crt_speed));
                    if(max_speed < crt_speed) max_speed = crt_speed;
                    break;
                case R.id.btn_speed_down_self_control:
                    btn_speed_down.setBackgroundResource(R.drawable.btn_decrease_signup_pressed);
                    writeDataToSerial(4);
                    if(crt_speed > 0.2) crt_speed -= 0.2;
                    text_speed.setText(String.format("%.1f", crt_speed));
                    break;
                case R.id.btn_incline_up_self_control:
                    btn_incline_up.setBackgroundResource(R.drawable.btn_increase_signup_pressed);
                    writeDataToSerial(5);
                    if(crt_incline < 16) crt_incline += 1;
                    text_incline.setText(String.valueOf(crt_incline));
                    if(max_incline < crt_incline) max_incline = crt_incline;
                    break;
                case R.id.btn_incline_down_self_control:
                    writeDataToSerial(6);
                    btn_incline_down.setBackgroundResource(R.drawable.btn_decrease_signup_pressed);
                    if(crt_incline > 0) crt_incline -= 1;
                    text_incline.setText(String.valueOf(crt_incline));
                    break;
            }
        }
    };

    public void onFinished() {
        elapsed_time = 0;
        mSerial.close();

        long[] tmp = new long[0];
        if(isConnected) {
            tmp = new long[times.size()];
            for(int i = 0; i < times.size(); i++) {
                tmp[i] = times.get(i);
            }
        }

        Intent intent = new Intent(getApplicationContext(), SelfControlResultActivity.class);
        intent.putExtra("NAME", name);
        intent.putExtra("HEIGHT", height);
        intent.putExtra("WEIGHT", weight);
        intent.putExtra("AGE", age);
        intent.putExtra("GENDER", gender);
        intent.putExtra("HP", hp);
        intent.putExtra("LOCAL", local);
        intent.putExtra("PLACE", place);

        intent.putExtra("DISTANCE", distance);
        if(isConnected) intent.putExtra("MAX_HR", max_hr);
        else intent.putExtra("MAX_HR", 0);
        intent.putExtra("MAX_SPEED", max_speed);
        intent.putExtra("ELAPSED_TIME", text_elapsedTime.getText());
        intent.putExtra("CALORIE", calorie);
        intent.putExtra("MAX_INCLINE", max_incline);
        Bundle bundle = new Bundle();
        intent.putExtra("bundle", bundle);
        if(isConnected) {
            intent.putExtra("HRs", HRs);
            intent.putExtra("times", tmp);
        }
        else {
            intent.putExtra("HRs", 0);
            intent.putExtra("times", 0);
        }
        startActivity(intent);
    }

    String str_min, str_second;
    public void onCalculate_elapsedTime() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                elapsed_time += 1;
                int min = elapsed_time / 60;
                int second = elapsed_time % 60;

                if(min < 10) str_min = "0" + min;
                else str_min = min + "";
                if(second < 10) str_second = "0" + second;
                else str_second = second + "";
                text_elapsedTime.setText(str_min + " : " + str_second);

                distance += crt_speed / 3600;
                text_distance.setText(String.format("%.2f", distance) + " km");

                if(elapsed_time % 10 != 0) {
                    if(crt_hr != 0) array_hr_10seconds.add(crt_hr);
                } else if(elapsed_time % 10 == 0) {
                    onCalculateCalorie();
                    array_hr_10seconds.clear();
                }
            }
        });
    }

    private void onCalculateCalorie() {
        double coef = 0;
        float weight_ = Float.parseFloat(weight);

        if(crt_speed < 6.5) coef = 0.001;
        else if(crt_speed >= 6.5 && crt_speed < 9) coef = 0.002;
        else if(crt_speed >= 9 && crt_speed < 12) coef = 0.003;
        else if(crt_speed >= 12 && crt_speed < 16) coef = 0.004;
        else if(crt_speed >= 16 && crt_speed < 19) coef = 0.005;
        else if(crt_speed >= 19 && crt_speed < 21) coef = 0.006;

        calorie += coef * weight_;

        text_calorie.setText((int)calorie + " kcal");
    }

    boolean isLoad_vo2max = false, isLoad_restHR = false;
    public void onLoad_UserVO2max() {
        isLoad_vo2max = true;
        ContentValues values = new ContentValues();
        values.put("HP", hp);
        values.put("LOCAL", local);
        values.put("PLACE", place);

        HttpPostDataClass_onLoadVO2max post = new HttpPostDataClass_onLoadVO2max("http://125.130.221.35:8001/PROC/AjaxForGetVo2Max.asp", values);
        post.execute();
    }

    public void onLoad_UserRestHR() {
        isLoad_restHR = true;
        ContentValues values = new ContentValues();
        values.put("HP", hp);
        values.put("LOCAL", local);
        values.put("PLACE", place);

        HttpPostDataClass_onLoadRestHR post = new HttpPostDataClass_onLoadRestHR("http://125.130.221.35:8001/PROC/AjaxForGetRESTHR.asp", values);
        post.execute();
    }

    class HttpPostDataClass_onLoadVO2max extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;

        public HttpPostDataClass_onLoadVO2max(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = null;
            RequestHttpURLConnection connection = new RequestHttpURLConnection();
            result = connection.request(url, values);
            if(result != null && isLoad_vo2max) {
                user_vo2max = result;
                isLoad_vo2max = false;
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(user_vo2max == null && user_vo2max.equals("")) {
                Toast.makeText(SelfControlActivity.this, "1.6km zone test를 먼저 진행해 주세요.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    class HttpPostDataClass_onLoadRestHR extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;

        public HttpPostDataClass_onLoadRestHR(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = null;
            RequestHttpURLConnection connection = new RequestHttpURLConnection();
            result = connection.request(url, values);
            if (result != null && isLoad_restHR && !result.equals("")) {
                rest_hr = Integer.parseInt(result);
                isLoad_restHR = false;
            }

            return result;
        }
    }

    /*class WriteData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            String[] time = start_time.split("_");
            onWriteDataToText_HR(Environment.getExternalStorageDirectory() + "/NovelT/Custom/", name + "_" + time[0] + time[1] + time[2] + "_" + time[3] + time[4] + ".txt", now, crt_hr);
            return null;
        }
    }

    private void onWriteDataToText_HR(final String folder, final String file, final long time, final int hr) {
        try {
            File dir = new File(folder);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/NovelT/Custom/" + file, true);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(time + "\t" + hr + "\t" + String.format("%.1f", speed));
            writer.newLine();
            writer.flush();
            writer.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onWriteDataToText_zone(final String folder, final String file) {
        try {
            File dir = new File(folder);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/NovelT/Custom/" + file, true);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(std_fourty + "/" + std_fifty + "/" + std_sixty + "/" + std_seventy + "/" + std_eighty + "/" + std_ninety + "/" + std_hundred);
            writer.newLine();
            writer.flush();
            writer.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    class LongPressRepeatListner implements View.OnTouchListener {
        Handler handler = new Handler();
        int initial_interval, interval;
        View.OnClickListener listener;
        View downView;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, interval);
                listener.onClick(downView);
            }
        };

        public LongPressRepeatListner(int initial_interval, int interval, View.OnClickListener listener) {
            if(listener == null) throw new IllegalArgumentException("listener is null");
            if(initial_interval < 0 || interval < 0) throw new IllegalArgumentException("interval is under zero");

            this.initial_interval = initial_interval;
            this.interval = interval;
            this.listener = listener;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, initial_interval);
                    downView = v;
                    listener.onClick(v);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    switch(v.getId()) {
                        case R.id.btn_speed_up_self_control:
                            btn_speed_up.setBackgroundResource(R.drawable.btn_increase_signup);
                            break;
                        case R.id.btn_speed_down_self_control:
                            btn_speed_down.setBackgroundResource(R.drawable.btn_decrease_signup);
                            break;
                        case R.id.btn_incline_up_self_control:
                            btn_incline_up.setBackgroundResource(R.drawable.btn_increase_signup);
                            break;
                        case R.id.btn_incline_down_self_control:
                            btn_incline_down.setBackgroundResource(R.drawable.btn_decrease_signup);
                            break;
                    }
                    handler.removeCallbacks(runnable);
                    downView = null;
                    return true;
            }
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        task_crtTime.cancel();
        timer_crtTime.cancel();
        task_start_speed.cancel();
        timer_start_speed.cancel();

        unregisterReceiver(mUsbRecevier);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() { }
}