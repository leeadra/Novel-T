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
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.UartConfig;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class CustomProtocolActivity extends Activity implements ConnectionObserver, HeartRateObserver {
    TextView text_time, text_id, text_patch, text_protocol;
    TextView text_elapsedTime, text_hr, text_distance, text_speed, text_calorie, text_crt_state;
    ImageView indicator_connection, indicator_battery, img_ready_to_start, img_ready_to_stop;
    Button btn_out;
    RelativeLayout layout;
    Custom_HRZoneGraph HRChart;
    ProgressBar progress_time;

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
    Animation anim1, anim2;
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

    static int std_fourty, std_fifty, std_sixty, std_seventy, std_eighty, std_ninety, std_hundred;
    ArrayList<Integer> HRs = new ArrayList<>();
    ArrayList<Long> times = new ArrayList<>();

    Timer timer, timer_crtTime, timer_elapsed;
    TimerTask task, task_crtTime, task_elapsed;

    String name, height, weight, gender, max_bp, min_bp, bmi, hp, start_date, start_time, local, place;
    int age;

    String user_vo2max, user_survey, user_restHR;
    static float zone_factor = 1;
    int coef = 1, incline = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_protocol);

        text_time = (TextView)findViewById(R.id.text_time_customprotocol);
        task_crtTime = new TimerTask() {
            @Override
            public void run() {
                calc_time();
            }
        };
        timer_crtTime = new Timer();
        timer_crtTime.schedule(task_crtTime, 0, 1000);

        text_id = (TextView)findViewById(R.id.text_userid_customprotocol);
        text_patch = (TextView)findViewById(R.id.text_patch_customprotocol);
        text_protocol = (TextView)findViewById(R.id.text_protocol_name_customprotocol);
        text_elapsedTime = (TextView)findViewById(R.id.text_progress_customprotocol);
        text_hr = (TextView)findViewById(R.id.text_heart_rate_customprotocol);
        text_distance = (TextView)findViewById(R.id.text_distance_customprotocol);
        text_speed = (TextView)findViewById(R.id.text_speed_customprotocol);
        text_calorie = (TextView)findViewById(R.id.text_calorie_customprotocol);
        text_crt_state = (TextView)findViewById(R.id.text_crt_state_custom);
        indicator_connection = (ImageView)findViewById(R.id.indicator_connection_customprotocol);
        indicator_battery = (ImageView)findViewById(R.id.indicator_battery_customprotocol);
        img_ready_to_start = (ImageView)findViewById(R.id.img_ready_to_start_customprotocol);
        img_ready_to_stop = (ImageView)findViewById(R.id.img_ready_to_stop_customprotocol);
        btn_out = (Button)findViewById(R.id.btn_out_customprotocol);
        layout = (RelativeLayout)findViewById(R.id.layout_customprotocol);
        progress_time = (ProgressBar)findViewById(R.id.custom_protocol_test_progressbar);

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

        final Animation anim = new AlphaAnimation(0.0f, 1.0f);
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

        task = new TimerTask() {
            @Override
            public void run() {
                onControlSpeed(crt_hr);
                sum_speed += speed;
                cnt_speed++;
                if(max_speed < speed) max_speed = speed;
            }
        };
        timer = new Timer();

        task_elapsed = new TimerTask() {
            @Override
            public void run() {
                onCalculate_elapsedTime();
            }
        };
        timer_elapsed = new Timer();

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
                    onCalculateZones();
                    onReadPrograms(R.raw.class.getFields());
                    timer_elapsed.schedule(task_elapsed, 0, 1000);
                    text_speed.setText(String.format("%.1f", speed) + " km/h");
                    writeDataToSerial(1);
                    timer.schedule(task, 1000 * 60 * 3, 4000);

                    text_crt_state.setVisibility(View.VISIBLE);
                    text_crt_state.startAnimation(anim2);
                } else {
                    if(isNoticed) {
                        img_ready_to_start.clearAnimation();
                        img_ready_to_start.setVisibility(View.INVISIBLE);
                        isTouched = true;
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

        if(MainActivity.connected_patch_id != null && !MainActivity.connected_patch_id.equals(" ")) {
            patchName = MainActivity.connected_patch_id;
            text_patch.setVisibility(View.VISIBLE);
            text_patch.setText(patchName);
            if(!patchName.equals("")) {
                indicator_connection.setVisibility(View.VISIBLE);
                indicator_battery.setVisibility(View.VISIBLE);
            }
        }

        btn_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeDataToSerial(2);
                //mSerial.close();
                finish();
            }
        });

        HRChart = (Custom_HRZoneGraph)findViewById(R.id.HRGraph_hr_zone_protocol_test);
        DPIHelper helper = new DPIHelper(getApplicationContext(), getWindowManager().getDefaultDisplay());
        HRChart.setTextSize((int)helper.getXDPI() / 16);
        // 사용자 운동 강도 및 버전 구분하여 HRChart에 데이터 넘기고 HRChart에서 강도, 버전에 따라서 zone을 표시

        mSerial = new Physicaloid(getApplicationContext());
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbRecevier, filter);

        //openUsbSerial();

        onLoad_UserVO2max();
        onLoad_UserRestHR();
        onLoad_UserCrtProtocol();
        onLoad_UserSurvey();
        onLoad_UserZoneFactor();
        onLoad_SurveySpeedSum();

        timer_start_speed = new Timer();
        timer_warmingup_speed = new Timer();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(usr_protocol != null) {
                    if(usr_protocol.length == 0) {
                        text_protocol.setText(protocol_level + " Ver." + protocol_version);
                    } else {
                        if(!usr_protocol[0].equals(protocol_level)) text_protocol.setText(protocol_level + " Ver." + protocol_version);
                        else {
                            protocol_version = Integer.parseInt(usr_protocol[1]);
                            if(survey_protocol_version != null) {
                                if(!survey_protocol_version.equals("")) {
                                    if(survey_protocol_version.equals("1")) {
                                        if(protocol_version != 7) protocol_version += 1;
                                    } else {
                                        if(protocol_version != 1) protocol_version -= 1;
                                    }
                                }
                            }
                            text_protocol.setText(protocol_level + " Ver." + protocol_version);
                        }
                    }
                }
            }
        }, 800);
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
                        img_ready_to_stop.setVisibility(View.VISIBLE);
                        img_ready_to_stop.startAnimation(anim1);
                    }
                });
            }
        }
    };
    Timer timer_start_speed;

    TimerTask task_warmingup_speed = new TimerTask() {
        @Override
        public void run() {
            //mSerial.write(treadmill_speed_down);
            if(speed == 4) {
                if(task_warmingup_speed != null) task_warmingup_speed.cancel();
                if(timer_warmingup_speed != null) timer_warmingup_speed.cancel();
            }
        }
    };
    Timer timer_warmingup_speed;

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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text_speed.setText(String.format("%.1f", speed) + " km/h");
                    }
                });
                break;
            case 2:
                //mSerial.write(treadmill_stop);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text_speed.setText(String.format("%.1f", speed) + " km/h");
                    }
                });
                break;
            case 3:
                //mSerial.write(treadmill_speed_up);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text_speed.setText(String.format("%.1f", speed) + " km/h");
                    }
                });
                break;
            case 4:
                //mSerial.write(treadmill_speed_down);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text_speed.setText(String.format("%.1f", speed) + " km/h");
                    }
                });
                break;
            case 5:
                //mSerial.write(treadmill_incline_up);
                incline += 1;
                break;
            case 6:
                //mSerial.write(treadmill_incline_down);
                incline -= 1;
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
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text_patch.setVisibility(View.INVISIBLE);
                    text_patch.setText("");
                    indicator_connection.setVisibility(View.INVISIBLE);
                    indicator_battery.setVisibility(View.INVISIBLE);
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
    WriteData writeData;
    @Override
    public void update(int heart_rate) {
        if(isStarted) {
            crt_hr = heart_rate;
            HRs.add(heart_rate);
            now = System.currentTimeMillis();
            times.add(now);

            if(writeData == null) {
                writeData = new WriteData();
                writeData.execute();
                writeData = null;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text_hr.setText(crt_hr + " bpm");
                    HRChart.add(crt_hr);
                }
            });

            if(max_hr < crt_hr) max_hr = crt_hr;

            sum_hr += crt_hr;
            cnt++;
        }
    }

    int speed_count_up = 0, speed_count_down = 0, speed_count_rest = 0;

    private void onSpeed_4050() {
        if(crt_hr < std_fourty) {
            if(speed < 15.8 && speed_count_up != 5) {
                speed += 0.2; speed_count_up += 1; speed_count_down = 0; speed_count_rest = 0;
                writeDataToSerial(3);
            } else if(speed_count_up == 5) {
                speed_count_rest += 1;
                if(speed_count_rest == 2) {
                    speed_count_up = 0;
                }
            }
        } else if(crt_hr > std_fifty) {
            if(speed > 1 && speed_count_down != 5) {
                speed -= 0.2; speed_count_down += 1; speed_count_up = 0; speed_count_rest = 0;
                writeDataToSerial(4);
            } else if(speed_count_down == 5) {
                speed_count_rest += 1;
                if(speed_count_rest == 2) {
                    speed_count_down = 0;
                }
            }
        }
    }

    private void onSpeed_5060() {
        if(crt_hr < std_fifty) {
            if(speed < 15.8 && speed_count_up != 5) {
                speed += 0.2; speed_count_up += 1; speed_count_down = 0; speed_count_rest = 0;
                writeDataToSerial(3);
            } else if(speed_count_up == 5) {
                speed_count_rest += 1;
                if(speed_count_rest == 2) {
                    speed_count_up = 0;
                }
            }
        } else if(crt_hr > std_sixty) {
            if(speed > 1 && speed_count_down != 5) {
                speed -= 0.2; speed_count_down += 1; speed_count_up = 0; speed_count_rest = 0;
                writeDataToSerial(4);
            } else if(speed_count_down == 5) {
                speed_count_rest += 1;
                if(speed_count_rest == 2) {
                    speed_count_down = 0;
                }
            }
        }
    }

    private void onSpeed_6070() {
        if(crt_hr < std_sixty) {
            if(speed < 15.8 && speed_count_up != 5) {
                speed += 0.2; speed_count_up += 1; speed_count_down = 0; speed_count_rest = 0;
                writeDataToSerial(3);
            } else if(speed_count_up == 5) {
                speed_count_rest += 1;
                if(speed_count_rest == 2) {
                    speed_count_up = 0;
                }
            }
        } else if(crt_hr > std_seventy) {
            if(speed > 1 && speed_count_down != 5) {
                speed -= 0.2; speed_count_down += 1; speed_count_up = 0; speed_count_rest = 0;
                writeDataToSerial(4);
            } else if(speed_count_down == 5) {
                speed_count_rest += 1;
                if(speed_count_rest == 2) {
                    speed_count_down = 0;
                }
            }
        }
    }

    private void onSpeed_7080() {
        if(crt_hr < std_seventy) {
            if(speed < 15.8 && speed_count_up != 5) {
                speed += 0.2; speed_count_up += 1; speed_count_down = 0; speed_count_rest = 0;
                writeDataToSerial(3);
            } else if(speed_count_up == 5) {
                speed_count_rest += 1;
                if(speed_count_rest == 2) {
                    speed_count_up = 0;
                }
            }
        } else if(crt_hr > std_eighty) {
            if(speed > 1 && speed_count_down != 5) {
                speed -= 0.2; speed_count_down += 1; speed_count_up = 0; speed_count_rest = 0;
                writeDataToSerial(4);
            } else if(speed_count_down == 5) {
                speed_count_rest += 1;
                if(speed_count_rest == 2) {
                    speed_count_down = 0;
                }
            }
        }
    }

    private void onSpeed_8090() {
        if(crt_hr < std_eighty) {
            if(speed < 15.8 && speed_count_up != 5) {
                speed += 0.2; speed_count_up += 1; speed_count_down = 0; speed_count_rest = 0;
                writeDataToSerial(3);
            } else if(speed_count_up == 5) {
                speed_count_rest += 1;
                if(speed_count_rest == 2) {
                    speed_count_up = 0;
                }
            }
        } else if(crt_hr > std_ninety) {
            if(speed > 1 && speed_count_down != 5) {
                speed -= 0.2; speed_count_down += 1; speed_count_up = 0; speed_count_rest = 0;
                writeDataToSerial(4);
            } else if(speed_count_down == 5) {
                speed_count_rest += 1;
                if(speed_count_rest == 2) {
                    speed_count_down = 0;
                }
            }
        }
    }

    private void onSpeed_90100() {
        if(crt_hr < std_ninety) {
            if(speed < 15.8 && speed_count_up != 5) {
                speed += 0.2; speed_count_up += 1; speed_count_down = 0; speed_count_rest = 0;
                writeDataToSerial(3);
            } else if(speed_count_up == 5) {
                speed_count_rest += 1;
                if(speed_count_rest == 2) {
                    speed_count_up = 0;
                }
            }
        } else if(crt_hr > std_hundred) {
            if(speed > 1 && speed_count_down != 5) {
                speed -= 0.2; speed_count_down += 1; speed_count_up = 0; speed_count_rest = 0;
                writeDataToSerial(4);
            } else if(speed_count_down == 5) {
                speed_count_rest += 1;
                if(speed_count_rest == 2) {
                    speed_count_down = 0;
                }
            }
        }
    }

    boolean isCoolDown = false;
    public void onControlSpeed(int crt_hr) {                        // 기울기 0.6 이상 계산 보류
        this.crt_hr = crt_hr;
        if(protocol_level.equals("매우낮은강도")) {
            switch(protocol_version) {
                case 1:
                    if(elapsed_time >= 180 && elapsed_time < ((1440 * zone_factor) + 180)) {        // 3분 ~ 27분 * zone factor
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {    // 27분 ~ 33분 * zone factor
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {        // 33분 ~ 36분 * zone factor
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 2:
                    if(elapsed_time >= 180 && elapsed_time < ((720 * zone_factor) + 180)) {                 // 3분 ~ 15분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp= (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((720 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {         // 15분 ~ 21분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 21분 ~ 33분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 361)) {        // 33분 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 3:
                    if(elapsed_time >= 180 && elapsed_time < ((720 * zone_factor) + 180)) {                 // 3 ~ 15분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((720 * zone_factor) + 180) && elapsed_time < ((900 * zone_factor) + 180)) {         // 15 ~ 18분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((900 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {        // 18 ~ 30분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 30 ~ 33분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {        // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 4:
                    if(elapsed_time >= 180 && elapsed_time < ((420 * zone_factor) + 180)) {                  // 3 ~ 10분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((420 * zone_factor) + 180) && elapsed_time < ((600 * zone_factor) + 180)) {          // 10 ~ 13분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((600 * zone_factor) + 180) && elapsed_time < ((1020 * zone_factor) + 180)) {         // 13 ~ 20분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1020 * zone_factor) + 180) && elapsed_time < ((1200 * zone_factor) + 180)) {        // 20 ~ 23분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1200 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 23 ~ 33분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {        // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 5:
                    if(elapsed_time >= 180 && elapsed_time < ((480 * zone_factor) + 180)) {                 // 3 ~ 11분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((600 * zone_factor) + 180)) {          // 11 ~ 13분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((600 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {         // 13 ~ 21분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1200 * zone_factor) + 180)) {        // 21 ~ 23분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1200 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {        // 23 ~ 31분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 31 ~ 33분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {        // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 6:
                    if(elapsed_time >= 180 && elapsed_time < ((270 * zone_factor) + 180)) {                 // 3 ~ 7.5분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((270 * zone_factor) + 180) && elapsed_time < ((300 * zone_factor) + 180)) {          // 7.5 ~ 8분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((300 * zone_factor) + 180) && elapsed_time < ((570 * zone_factor) + 180)) {          // 8 ~ 12.5분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((570 * zone_factor) + 180) && elapsed_time < ((600 * zone_factor) + 180)) {          // 12.5 ~ 13분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((600 * zone_factor) + 180) && elapsed_time < ((870 * zone_factor) + 180)) {         // 13 ~ 17.5분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((870 * zone_factor) + 180) && elapsed_time < ((900 * zone_factor) + 180)) {        // 17.5 ~ 18분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((900 * zone_factor) + 180) && elapsed_time < ((1170 * zone_factor) + 180)) {        // 18 ~ 22.5분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1170 * zone_factor) + 180) && elapsed_time < ((1200 * zone_factor) + 180)) {        // 22.5 ~ 23분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1200 * zone_factor) + 180) && elapsed_time < ((1470 * zone_factor) + 180)) {        // 23 ~ 27.5분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1470 * zone_factor) + 180) && elapsed_time < ((1500 * zone_factor) + 180)) {        // 27.5 ~ 28분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1500 * zone_factor) + 180) && elapsed_time < ((1770 * zone_factor) + 180)) {        // 28 ~ 32.5분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1770 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 32.5 ~ 33분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {        // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 7:
                    if(elapsed_time >= 180 && elapsed_time < ((210 * zone_factor) + 180)) {                 // 3 ~ 6.5분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((210 * zone_factor) + 180) && elapsed_time < ((240 * zone_factor) + 180)) {          // 6.5 ~ 7분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((240 * zone_factor) + 180) && elapsed_time < ((450 * zone_factor) + 180)) {          // 7 ~ 10.5분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((450 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {          // 10.5 ~ 11분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((810 * zone_factor) + 180)) {          // 11 ~ 16.5분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((810 * zone_factor) + 180) && elapsed_time < ((840 * zone_factor) + 180)) {         // 16.5 ~ 17분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((840 * zone_factor) + 180) && elapsed_time < ((1050 * zone_factor) + 180)) {        // 17 ~ 20.5분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1050 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {        // 20.5 ~ 21분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1410 * zone_factor) + 180)) {        // 21 ~ 26.5분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1410 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {        // 26.5 ~ 27분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1650 * zone_factor) + 180)) {        // 27 ~ 30.5분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1650 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {        // 30.5 ~ 31분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 31 ~ 33분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {        // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
            }
        } else if(protocol_level.equals("낮은강도")) {
            switch(protocol_version) {
                case 1:
                    if(elapsed_time >= 180 && elapsed_time < ((1080 * zone_factor) + 180)) {                // 3 ~ 21분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {        // 21 ~ 27분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 27 ~ 33분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {        // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 2:
                    if(elapsed_time >= 180 && elapsed_time < ((420 * zone_factor) + 180)) {                 // 3 ~ 10분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((420 * zone_factor) + 180) && elapsed_time < ((780 * zone_factor) + 180)) {          // 10 ~ 16분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((780 * zone_factor) + 180) && elapsed_time < ((1020 * zone_factor) + 180)) {         // 16 ~ 20분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1020 * zone_factor) + 180) && elapsed_time < ((1380 * zone_factor) + 180)) {        // 20 ~ 26분         // 시작 속도 5.4 시작
                        if(speed < 5.4) {
                            float tmp = (float)(5.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1200 * zone_factor) + 9)) {
                            onSpeed_6070();
                        }
                    } else if(elapsed_time >= ((1380 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 26 ~ 33분         // 시작 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((1380 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1380 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {        // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 3:
                    if(elapsed_time >= 180 && elapsed_time < ((540 * zone_factor) + 180)) {                 // 3 ~ 12분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((540 * zone_factor) + 180) && elapsed_time < ((720 * zone_factor) + 180)) {          // 12 ~ 15분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((720 * zone_factor) + 180) && elapsed_time < ((900 * zone_factor) + 180)) {         // 15 ~ 18분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((900 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {        // 18 ~ 27분         // 시작 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((900 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((900 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1620 * zone_factor) + 180)) {        // 27 ~ 30분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1620 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 30 ~ 33분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {        // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 4:
                    if(elapsed_time >= 180 && elapsed_time < ((240 * zone_factor) + 180)) {                     // 3 ~ 7분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((240 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {              // 7 ~ 11분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((600 * zone_factor) + 180)) {              // 11 ~ 13분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((600 * zone_factor) + 180) && elapsed_time < ((840 * zone_factor) + 180)) {             // 13 ~ 17분     // 속도 4.4로 시작, 10초 후부터 속도 제어
                        if(speed > 4.4 && elapsed_time < ((600 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((600 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((840 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {            // 17 ~ 21분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1200 * zone_factor) + 180)) {            // 21 ~ 23분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1200 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {            // 23 ~ 27분         // 속도 4.4로 시작, 10초 후부터 속도 제어
                        if(speed > 4.4 && elapsed_time < ((1200 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1200 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {            // 27 ~ 31분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {            // 31 ~ 33분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {            // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 5:
                    if(elapsed_time >= 180 && elapsed_time < ((120 * zone_factor) + 180)) {                     // 3 ~ 5분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num+1; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((120 * zone_factor) + 180) && elapsed_time < ((240 * zone_factor) + 180)) {              // 5 ~ 7분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((240 * zone_factor) + 180) && elapsed_time < ((360 * zone_factor) + 180)) {              // 7 ~ 9분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((360 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {             // 9 ~ 11분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((720 * zone_factor) + 180)) {            // 11 ~ 15분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((720 * zone_factor) + 180) && elapsed_time < ((840 * zone_factor) + 180)) {            // 15 ~ 17분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((840 * zone_factor) + 180) && elapsed_time < ((960 * zone_factor) + 180)) {            // 17 ~ 19분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((960 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {            // 19 ~ 21분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1320 * zone_factor) + 180)) {            // 21 ~ 25분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1320 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {            // 25 ~ 27분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1560 * zone_factor) + 180)) {            // 27 ~ 29분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1560 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {            // 29 ~ 31분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {            // 31 ~ 33분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {            // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 6:
                    if(elapsed_time >= 180 && elapsed_time < ((120 * zone_factor) + 180)) {                     // 3 ~ 5분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((120 * zone_factor) + 180) && elapsed_time < ((240 * zone_factor) + 180)) {              // 5 ~ 7분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((240 * zone_factor) + 180) && elapsed_time < ((360 * zone_factor) + 180)) {              // 7 ~ 9분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((360 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {             // 9 ~ 11분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((600 * zone_factor) + 180)) {            // 11 ~ 13분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((600 * zone_factor) + 180) && elapsed_time < ((720 * zone_factor) + 180)) {            // 13 ~ 15분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((720 * zone_factor) + 180) && elapsed_time < ((840 * zone_factor) + 180)) {            // 15 ~ 17분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((840 * zone_factor) + 180) && elapsed_time < ((960 * zone_factor) + 180)) {            // 17 ~ 19분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((960 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {            // 19 ~ 21분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1200 * zone_factor) + 180)) {            // 21 ~ 23분         // 속도 5.4 시작, 10초 후 속도 제어
                        if(speed < 5.4 && elapsed_time < ((1080 * zone_factor) + 181)) {
                            float tmp = (float)(5.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1080 * zone_factor) + 189)) {
                            onSpeed_6070();
                        }
                    } else if(elapsed_time >= ((1200 * zone_factor) + 180) && elapsed_time < ((1320 * zone_factor) + 180)) {            // 23 ~ 25분         // 속도 4.4 시작, 10초 후 속도 제어
                        if(speed > 4.4 && elapsed_time < ((1200 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1200 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1320 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {            // 25 ~ 27분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1560 * zone_factor) + 180)) {            // 27 ~ 29분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1560 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {            // 29 ~ 31분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {            // 31 ~ 33분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {            // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 7:
                    if(elapsed_time >= 180 && elapsed_time < ((60 * zone_factor) + 180)) {                     // 3 ~ 4분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((60 * zone_factor) + 180) && elapsed_time < ((120 * zone_factor) + 180)) {              // 4 ~ 5분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((120 * zone_factor) + 180) && elapsed_time < ((180 * zone_factor) + 180)) {              // 5 ~ 6분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((180 * zone_factor) + 180) && elapsed_time < ((240 * zone_factor) + 180)) {             // 6 ~ 7분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((240 * zone_factor) + 180)  && elapsed_time < ((360 * zone_factor) + 180)) {            // 7 ~ 9분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((360 * zone_factor) + 180) && elapsed_time < ((420 * zone_factor) + 180)) {            // 9 ~ 10분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((420 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {            // 10 ~ 11분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((540 * zone_factor) + 180)) {            // 11 ~ 12분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((540 * zone_factor) + 180) && elapsed_time < ((660 * zone_factor) + 180)) {            // 12 ~ 14분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((660 * zone_factor) + 180) && elapsed_time < ((720 * zone_factor) + 180)) {            // 14 ~ 15분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((720 * zone_factor) + 180) && elapsed_time < ((780 * zone_factor) + 180)) {            // 15 ~ 16분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((780 * zone_factor) + 180) && elapsed_time < ((840 * zone_factor) + 180)) {            // 16 ~ 17분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((840 * zone_factor) + 180) && elapsed_time < ((960 * zone_factor) + 180)) {            // 17 ~ 19분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((960 * zone_factor) + 180) && elapsed_time < ((1020 * zone_factor) + 180)) {            // 19 ~ 20분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1020 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {            // 20 ~ 21분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1140 * zone_factor) + 180)) {            // 21 ~ 22분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1140 * zone_factor) + 180) && elapsed_time < ((1260 * zone_factor) + 180)) {            // 22 ~ 24분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1260 * zone_factor) + 180) && elapsed_time < ((1320 * zone_factor) + 180)) {            // 24 ~ 25분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1320 * zone_factor) + 180) && elapsed_time < ((1380 * zone_factor) + 180)) {            // 25 ~ 26분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1380 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {            // 26 ~ 27분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1560 * zone_factor) + 180)) {            // 27 ~ 29분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1560 * zone_factor) + 180) && elapsed_time < ((1620 * zone_factor) + 180)) {            // 29 ~ 30분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1620 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {            // 30 ~ 31분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1740 * zone_factor) + 180)) {            // 31 ~ 32분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1740 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {            // 32 ~ 33분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {            // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
            }
        } else if(protocol_level.equals("중간강도")) {
            switch(protocol_version) {
                case 1:
                    if(elapsed_time >= 180 && elapsed_time < ((480 * zone_factor) + 180)) {                // 3 ~ 11분           // 속도 5로 시작, 10초 후 속도 제어 시작
                        if(speed < 5 && elapsed_time < 181) {
                            float tmp = (float)(5 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189)
                            onSpeed_5060();
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((960 * zone_factor) + 180)) {        // 11 ~ 19분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((960 * zone_factor) + 180) && elapsed_time < ((1320 * zone_factor) + 180)) {        // 19 ~ 25분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1320 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 25 ~ 33분         // 속도 4.4로 시작, 10초 후 속도 제어 시작
                        if(speed > 4.4 && elapsed_time < ((1320 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1320 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {        // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 2:
                    if(elapsed_time >= 180 && elapsed_time < ((180 * zone_factor) + 180)) {                     // 3 ~ 6분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((180 * zone_factor) + 180) && elapsed_time < ((360 * zone_factor) + 180)) {              // 6 ~ 9분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((360 * zone_factor) + 180) && elapsed_time < ((540 * zone_factor) + 180)) {              // 9 ~ 12분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((540 * zone_factor) + 180) && elapsed_time < ((720 * zone_factor) + 180)) {             // 12 ~ 15분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((720 * zone_factor) + 180) && elapsed_time < ((900 * zone_factor) + 180)) {            // 15 ~ 18분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((900 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {            // 18 ~ 21분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1260 * zone_factor) + 180)) {            // 21 ~ 24분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1260 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {            // 24 ~ 27분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1620 * zone_factor) + 180)) {            // 27 ~ 30분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1620 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {            // 30 ~ 33분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {            // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 3:
                    if(elapsed_time >= 180 && elapsed_time < ((180 * zone_factor) + 180)) {                     // 3 ~ 6분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((180 * zone_factor) + 180) && elapsed_time < ((360 * zone_factor) + 180)) {              // 6 ~ 9분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((360 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {              // 9 ~ 11분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((660 * zone_factor) + 180)) {             // 11 ~ 13분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((660 * zone_factor) + 180) && elapsed_time < ((780 * zone_factor) + 180)) {            // 13 ~ 16분           // 속도 4.4 시작, 10초 후 속도 제어
                        if(speed > 4.4 && elapsed_time < ((660 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((660 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((780 * zone_factor) + 180) && elapsed_time < ((960 * zone_factor) + 180)) {            // 16 ~ 19분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((960 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {            // 19 ~ 21분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1200 * zone_factor) + 180)) {            // 21 ~ 23분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1200 * zone_factor) + 180) && elapsed_time < ((1380 * zone_factor) + 180)) {            // 23 ~ 26분             // 속도 4.4 시작, 10초 후 속도 제어
                        if(speed > 4.4 && elapsed_time < ((1200 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1200 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1380 * zone_factor) + 180) && elapsed_time < ((1560 * zone_factor) + 180)) {            // 26 ~ 29분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1560 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {            // 29 ~ 31분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {            // 31 ~ 33분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {            // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 4:
                    if(elapsed_time >= 180 && elapsed_time < ((120 * zone_factor) + 180)) {                     // 3 ~ 5분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((120 * zone_factor) + 180) && elapsed_time < ((240 * zone_factor) + 180)) {              // 5 ~ 7분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((240 * zone_factor) + 180) && elapsed_time < ((360 * zone_factor) + 180)) {              // 7 ~ 9분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((360 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {             // 9 ~ 11분           // 속도 4.4 시작, 10초 후 속도 제어
                        if(speed > 4.4 && elapsed_time < ((360 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((360 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((600 * zone_factor) + 180)) {            // 11 ~ 13분           // 속도 6 시작, 10초 후 속도 제어
                        if(speed < 6 && elapsed_time < ((480 * zone_factor) + 181)) {
                            float tmp = (float)(6 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((480 * zone_factor) + 189)) {
                            onSpeed_7080();
                        }
                    } else if(elapsed_time >= ((600 * zone_factor) + 180) && elapsed_time < ((780 * zone_factor) + 180)) {            // 13 ~ 16분           // 속도 5 시작, 10초 후 속도 제어
                        if(speed > 5 && elapsed_time < ((600 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((600 * zone_factor) + 189)) {
                            onSpeed_5060();
                        }
                    } else if(elapsed_time >= ((780 * zone_factor) + 180) && elapsed_time < ((900 * zone_factor) + 180)) {            // 16 ~ 18분          // 속도 6 시작, 10초 후 속도 제어
                        if(speed < 6 && elapsed_time < ((780 * zone_factor) + 181)) {
                            float tmp = (float)(6 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((780 * zone_factor) + 189)) {
                            onSpeed_7080();
                        }
                    } else if(elapsed_time >= ((900 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {            // 18 ~ 21분         // 속도 5 시작, 10초 후 속도 제어
                        if(speed > 5 && elapsed_time < ((900 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((900 * zone_factor) + 189)) {
                            onSpeed_5060();
                        }
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1200 * zone_factor) + 180)) {            // 21 ~ 23분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1200 * zone_factor) + 180) && elapsed_time < ((1380 * zone_factor) + 180)) {            // 23 ~ 25분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1380 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {            // 25 ~ 27분             // 속도 4.4 시작, 10초 후 속도 제어
                        if(speed > 4.4 && elapsed_time < ((1380 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1380 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1560 * zone_factor) + 180)) {            // 27 ~ 29분         // 속도 5.4 시작, 10초 후 속도 제어
                        if(speed < 5.4 && elapsed_time < ((1440 * zone_factor) + 181)) {
                            float tmp = (float) (5.4 - speed);
                            int num = (int) ((tmp * 10) / 2);
                            for (int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1440 * zone_factor) + 189)) {
                            onSpeed_6070();
                        }
                    } else if(elapsed_time >= ((1560 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {            // 29 ~ 31분         // 속도 4.4 시작, 10초 후 속도 제어
                        if(speed > 4.4 && elapsed_time < ((1560 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1560 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {            // 31 ~ 33분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {            // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 5:
                    if(elapsed_time >= 180 && elapsed_time < ((60 * zone_factor) + 180)) {                     // 3 ~ 4분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((60 * zone_factor) + 180) && elapsed_time < ((180 * zone_factor) + 180)) {              // 4 ~ 6분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((180 * zone_factor) + 180) && elapsed_time < ((240 * zone_factor) + 180)) {              // 6 ~ 7분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((240 * zone_factor) + 180) && elapsed_time < ((360 * zone_factor) + 180)) {             // 7 ~ 9분        // 속도 5.4 시작
                        if(speed < 5.4 && elapsed_time < ((240 * zone_factor) + 181)) {
                            float tmp = (float)(5.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((240 * zone_factor) + 189)) {
                            onSpeed_6070();
                        }
                    } else if(elapsed_time >= ((360 * zone_factor) + 180) && elapsed_time < ((420 * zone_factor) + 180)) {            // 9 ~ 10분        // 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((360 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((360 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((420 * zone_factor) + 180) && elapsed_time < ((540 * zone_factor) + 180)) {            // 10 ~ 12분       // 속도 6 시작
                        if(speed < 6 && elapsed_time < ((420 * zone_factor) + 181)) {
                            float tmp = (float)(6 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((420 * zone_factor) + 189)) {
                            onSpeed_7080();
                        }
                    } else if(elapsed_time >= ((540 * zone_factor) + 180) && elapsed_time < ((660 * zone_factor) + 180)) {            // 12 ~ 14분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((660 * zone_factor) + 180) && elapsed_time < ((780 * zone_factor) + 180)) {            // 14 ~ 16분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((780 * zone_factor) + 180) && elapsed_time < ((840 * zone_factor) + 180)) {            // 16 ~ 17분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((840 * zone_factor) + 180) && elapsed_time < ((960 * zone_factor) + 180)) {            // 17 ~ 19분     // 속도 5.4 시작
                        if(speed < 5.4 && elapsed_time < ((840 * zone_factor) + 181)) {
                            float tmp = (float)(5.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((840 * zone_factor) + 189)) {
                            onSpeed_6070();
                        }
                    } else if(elapsed_time >= ((960 * zone_factor) + 180) && elapsed_time < ((1020 * zone_factor) + 180)) {            // 19 ~ 20분         // 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((960 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((960 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1020 * zone_factor) + 180) && elapsed_time < ((1140 * zone_factor) + 180)) {            // 20 ~ 22분     // 속도 6 시작
                        if(speed < 6 && elapsed_time < ((1020 * zone_factor) + 181)) {
                            float tmp = (float)(6 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1020 * zone_factor) + 189)) {
                            onSpeed_7080();
                        }
                    } else if(elapsed_time >= ((1140 * zone_factor) + 180) && elapsed_time < ((1260 * zone_factor) + 180)) {            // 22 ~ 24분     // 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((1140 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1140 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1260 * zone_factor) + 180) && elapsed_time < ((1380 * zone_factor) + 180)) {            // 24 ~ 26분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1380 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {            // 26 ~ 27분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1560 * zone_factor) + 180)) {            // 27 ~ 29분     // 속도 5.4 시작
                        if(speed < 5.4 && elapsed_time < ((1440 * zone_factor) + 181)) {
                            float tmp = (float)(5.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1440 * zone_factor) + 189)) {
                            onSpeed_6070();
                        }
                    } else if(elapsed_time >= ((1560 * zone_factor) + 180) && elapsed_time < ((1560 * zone_factor) + 180)) {        // 29 ~ 30분     // 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((1560 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }if(elapsed_time > ((1560 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1560 * zone_factor) + 180) && elapsed_time < ((1740 * zone_factor) + 180)) {        // 30 ~ 32분     // 속도 6 시작
                        if(speed < 6 && elapsed_time < ((1560 * zone_factor) + 181)) {
                            float tmp = (float)(6 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1560 * zone_factor) + 189)) {
                            onSpeed_7080();
                        }
                    } else if(elapsed_time >= ((1740 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 32 ~ 33분     // 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((1740 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1740 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {            // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 6:
                    if(elapsed_time >= 180 && elapsed_time < ((60 * zone_factor) + 180)) {                     // 3 ~ 4분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((60 * zone_factor) + 180) && elapsed_time < ((120 * zone_factor) + 180)) {              // 4 ~ 5분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((120 * zone_factor) + 180) && elapsed_time < ((180 * zone_factor) + 180)) {              // 5 ~ 6분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((180 * zone_factor) + 180) && elapsed_time < ((240 * zone_factor) + 180)) {             // 6 ~ 7분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((240 * zone_factor) + 180) && elapsed_time < ((300 * zone_factor) + 180)) {            // 7 ~ 8분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((300 * zone_factor) + 180) && elapsed_time < ((360 * zone_factor) + 180)) {            // 8 ~ 9분       // 속도 5.4 시작
                        if(speed < 5.4 && elapsed_time < ((300 * zone_factor) + 181)) {
                            float tmp = (float)(5.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((300 * zone_factor) + 189)) {
                            onSpeed_6070();
                        }
                    } else if(elapsed_time >= ((360 * zone_factor) + 180) && elapsed_time < ((420 * zone_factor) + 180)) {            // 9 ~ 10분        // 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((360 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((360 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((420 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {            // 10 ~ 11분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((540 * zone_factor) + 180)) {            // 11 ~ 12분           // 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((480 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((480 * zone_factor) + 89)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((540 * zone_factor) + 180) && elapsed_time < ((600 * zone_factor) + 180)) {            // 12 ~ 13분     // 속도 6 시작
                        if(speed < 6 && elapsed_time < ((540 * zone_factor) + 181)) {
                            float tmp = (float)(6 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((540 * zone_factor) + 189)) {
                            onSpeed_7080();
                        }
                    } else if(elapsed_time >= ((600 * zone_factor) + 180) && elapsed_time < ((660 * zone_factor) + 180)) {            // 13 ~ 14분         // 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((600 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((600 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((660 * zone_factor) + 180) && elapsed_time < ((720 * zone_factor) + 180)) {            // 14 ~ 15분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((720 * zone_factor) + 180) && elapsed_time < ((780 * zone_factor) + 180)) {            // 15 ~ 16분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((780 * zone_factor) + 180) && elapsed_time < ((840 * zone_factor) + 180)) {            // 16 ~ 17분          // 속도 5.4 시작
                        if(speed < 5.4 && elapsed_time < ((780 * zone_factor) + 181)) {
                            float tmp = (float)(5.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((780 * zone_factor) + 189)) {
                            onSpeed_6070();
                        }
                    } else if(elapsed_time >= ((840 * zone_factor) + 180) && elapsed_time < ((900 * zone_factor) + 180)) {            // 17 ~ 18분         // 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((840 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((840 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((900 * zone_factor) + 180) && elapsed_time < ((960 * zone_factor) + 180)) {            // 18 ~ 19분     // 속도 5.4 시작
                        if(speed < 5.4 && elapsed_time < ((900 * zone_factor) + 181)) {
                            float tmp = (float)(5.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time >((900 * zone_factor) + 189)) {
                            onSpeed_6070();
                        }
                    } else if(elapsed_time >= ((960 * zone_factor) + 180) && elapsed_time < ((1020 * zone_factor) + 180)) {        // 19 ~ 20분     // 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((960 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((960 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1020 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {        // 20 ~ 21분     // 속도 6 시작
                        if(speed < 6 && elapsed_time < ((1020 * zone_factor) + 181)) {
                            float tmp = (float)(6 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time >((1020 * zone_factor) + 189)) {
                            onSpeed_7080();
                        }
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1140 * zone_factor) + 180)) {        // 21 ~ 22분     // 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((1080 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1080 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1140 * zone_factor) + 180) && elapsed_time < ((1200 * zone_factor) + 180)) {        // 22 ~ 23분     // 속도 6 시작
                        if(speed < 6 && elapsed_time < ((1140 * zone_factor) + 181)) {
                            float tmp = (float)(6 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time >((1140 * zone_factor) + 189)) {
                            onSpeed_7080();
                        }
                    } else if(elapsed_time >= ((1200 * zone_factor) + 180) && elapsed_time < ((1320 * zone_factor) + 180)) {        // 23 ~ 25분     // 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((1200 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1200 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1320 * zone_factor) + 180) && elapsed_time < ((1380 * zone_factor) + 180)) {        // 25 ~ 26분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1380 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {        // 26 ~ 27분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1500 * zone_factor) + 180)) {        // 27 ~ 28분     // 속도 5.4 시작
                        if(speed < 5.4 && elapsed_time < ((1440 * zone_factor) + 181)) {
                            float tmp = (float)(5.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time >((1440 * zone_factor) + 189)) {
                            onSpeed_6070();
                        }
                    } else if(elapsed_time >= ((1500 * zone_factor) + 180) && elapsed_time < ((1560 * zone_factor) + 180)) {        // 28 ~ 29분     // 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((1500 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1500 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1560 * zone_factor) + 180) && elapsed_time < ((1620 * zone_factor) + 180)) {        // ` 29 ~ 30분       // 속도 6 시작
                        if(speed < 6 && elapsed_time < ((1560 * zone_factor) + 181)) {
                            float tmp = (float)(6 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time >((1560 * zone_factor) + 189)) {
                            onSpeed_7080();
                        }
                    } else if(elapsed_time >= ((1620 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {        // 30 ~ 31분     // 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((1620 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1620 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1740 * zone_factor) + 180)) {        // 31 ~ 32분     // 속도 6 시작
                        if(speed < 6 && elapsed_time < ((1680 * zone_factor) + 181)) {
                            float tmp = (float)(6 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time >((1680 * zone_factor) + 189)) {
                            onSpeed_7080();
                        }
                    } else if(elapsed_time >= ((1740 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 32 ~ 33분     // 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((1740 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1740 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {            // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 7:
                    if(elapsed_time >= 180 && elapsed_time < ((60 * zone_factor) + 180)) {                 // 3 ~ 4분       // 시작 속도 6 시작
                        if(speed < 6 && elapsed_time < 181) {
                            float tmp = (float)(6 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_7080();
                        }
                    } else if(elapsed_time >= ((60 * zone_factor) + 180) && elapsed_time < ((120 * zone_factor + 180))) {          // 4 ~ 5분
                        onSpeed_6070();
                    } else if(elapsed_time >= (120 * zone_factor + 180) && elapsed_time < ((180 * zone_factor) + 180)) {          // 5 ~ 6분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((180 * zone_factor) + 180) && elapsed_time < ((240 * zone_factor) + 180)) {          // 6 ~ 7분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((240 * zone_factor) + 180) && elapsed_time < ((300 * zone_factor) + 180)) {          // 7 ~ 8분       // 시작 속도 5.4 시작
                        if(speed < 5.4 && elapsed_time < ((240 * zone_factor) + 181)) {
                            float tmp = (float)(5.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((240 * zone_factor) + 189)) {
                            onSpeed_6070();
                        }
                    } else if(elapsed_time >= ((300 * zone_factor) + 180) && elapsed_time < ((360 * zone_factor) + 180)) {          // 8 ~ 9분       // 시작 속도 6 시작
                        if(speed < 6 && elapsed_time < ((300 * zone_factor) + 181)) {
                            float tmp = (float)(6 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((300 * zone_factor) + 189)) {
                            onSpeed_7080();
                        }
                    } else if(elapsed_time >= ((360 * zone_factor) + 180) && elapsed_time < ((420 * zone_factor) + 180)) {          // 9 ~ 10분      // 시작 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((360 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((360 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((420 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {          // 10 ~ 11분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((540 * zone_factor) + 180)) {          // 11 ~ 12분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((540 * zone_factor) + 180) && elapsed_time < ((600 * zone_factor) + 180)) {          // 12 ~ 13분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((600 * zone_factor) + 180) && elapsed_time < ((720 * zone_factor) + 180)) {          // 13 ~ 15분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((720 * zone_factor) + 180) && elapsed_time < ((780 * zone_factor) + 180)) {          // 15 ~ 16분     // 시작 속도 6 시작
                        if(speed < 6 && elapsed_time < ((720 * zone_factor) + 181)) {
                            float tmp = (float)(6 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((720 * zone_factor) + 189)) {
                            onSpeed_7080();
                        }
                    } else if(elapsed_time >= ((780 * zone_factor) + 180) && elapsed_time < ((840 * zone_factor) + 180)) {         // 16 ~ 17분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((840 * zone_factor) + 180) && elapsed_time < ((900 * zone_factor) + 180)) {        // 17 ~ 18분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((900 * zone_factor) + 180) && elapsed_time < ((960 * zone_factor) + 180)) {        // 18 ~ 19분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((960 * zone_factor) + 180) && elapsed_time < ((1020 * zone_factor) + 180)) {        // 19 ~ 20분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1020 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {        // 20 ~ 21분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1140 * zone_factor) + 180)) {        // 21 ~ 22분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1140 * zone_factor) + 180) && elapsed_time < ((1200 * zone_factor) + 180)) {        // 22 ~ 23분     // 시작 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((1140 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1140 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1200 * zone_factor) + 180) && elapsed_time < ((1260 * zone_factor) + 180)) {        // 23 ~ 24분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1260 * zone_factor) + 180) && elapsed_time < ((1320 * zone_factor) + 180)) {        // 24 ~ 25분     // 시작 속도 6 시작
                        if(speed < 6 && elapsed_time < ((1260 * zone_factor) + 181)) {
                            float tmp = (float)(6 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1260 * zone_factor) + 189)) {
                            onSpeed_7080();
                        }
                    } else if(elapsed_time >= ((1320 * zone_factor) + 180) && elapsed_time < ((1380 * zone_factor) + 180)) {        // 25 ~ 26분     // 시작 속도 5 시작
                        if(speed > 5 && elapsed_time < ((1320 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1320 * zone_factor) + 189)) {
                            onSpeed_5060();
                        }
                    } else if(elapsed_time >= ((1380 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {        // 26 ~ 27분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1500 * zone_factor) + 180)) {        // 27 ~ 28분     // 시작 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((1440 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1440 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1500 * zone_factor) + 180) && elapsed_time < ((1560 * zone_factor) + 180)) {        // 28 ~ 29분     // 시작 속도 5.4 시작
                        if(speed < 5.4 && elapsed_time < ((1500 * zone_factor) + 181)) {
                            float tmp = (float)(5.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1500 * zone_factor) + 189)) {
                            onSpeed_6070();
                        }
                    } else if(elapsed_time >= ((1560 * zone_factor) + 180) && elapsed_time < ((1620 * zone_factor) + 180)) {        // 29 ~ 30분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1620 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {        // 30 ~ 31분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1740 * zone_factor) + 180)) {        // 31 ~ 32분     // 시작 속도 5.4 시작
                        if(speed < 5.4 && elapsed_time < ((1680 * zone_factor) + 181)) {
                            float tmp = (float)(5.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1680 * zone_factor) + 189)) {
                            onSpeed_6070();
                        }
                    } else if(elapsed_time >= ((1740 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 32 ~ 33분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {        // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
            }
        } else if(protocol_level.equals("높은강도")) {
            switch(protocol_version) {
                case 1:
                    if(elapsed_time >= 180 && elapsed_time < ((240 * zone_factor) + 180)) {                 // 3 ~ 7분
                        if(speed < 5 && elapsed_time < 181) {
                            float tmp = (float)(5 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_5060();
                        }
                    } else if(elapsed_time >= ((240 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {          // 7 ~ 11분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((600 * zone_factor) + 180)) {          // 11 ~ 13분     // 시작 속도 6.4 시작
                        if(speed < 6.4 && elapsed_time < ((480 * zone_factor) + 181)) {
                            float tmp = (float)(6.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((480 * zone_factor) + 189)) {
                            onSpeed_8090();
                        }
                    } else if(elapsed_time >= ((600 * zone_factor) + 180) && elapsed_time < ((840 * zone_factor) + 180)) {         // 13 ~ 17분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((840 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {        // 17 ~ 21분     // 시작 속도 5 시작
                        if(speed > 5 && elapsed_time < ((840 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((840 * zone_factor) + 189)) {
                            onSpeed_5060();
                        }
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1200 * zone_factor) + 180)) {        // 21 ~ 23분     // 시작 속도 6.4 시작
                        if(speed < 6.4 && elapsed_time < ((1080 * zone_factor) + 181)) {
                            float tmp = (float)(6.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1080 * zone_factor) + 189)) {
                            onSpeed_8090();
                        }
                    } else if(elapsed_time >= ((1200 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {        // 23 ~ 27분     // 시작 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((1200 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1200 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {        // 27 ~ 31분     // 시작 속도 5.4 시작
                        if(speed < 5.4 && elapsed_time < ((1440 * zone_factor) + 181)) {
                            float tmp = (float)(5.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1440 * zone_factor) + 189)) {
                            onSpeed_6070();
                        }
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 31 ~ 33분     // 시작 속도 6.4 시작
                        if(speed < 6.4 && elapsed_time < ((1680 * zone_factor) + 181)) {
                            float tmp = (float)(6.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1680 * zone_factor) + 189)) {
                            onSpeed_8090();
                        }
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {        // 33 ~ 36분     // 시작 속도 4 시작
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 2:
                    if(elapsed_time >= 180 && elapsed_time < ((120 * zone_factor) + 180)) {                 // 3 ~ 5분       // 시작 속도 6.4 시작
                        if(speed < 6.4 && elapsed_time < 181) {
                            float tmp = (float)(6.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_8090();
                        }
                    } else if(elapsed_time >= ((120 * zone_factor) + 180) && elapsed_time < ((300 * zone_factor) + 180)) {          // 5 ~ 8분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((300 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {          // 8 ~ 11분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((660 * zone_factor) + 180)) {          // 11 ~ 14분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((660 * zone_factor) + 180) && elapsed_time < ((840 * zone_factor) + 180)) {         // 14 ~ 17분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((840 * zone_factor) + 180) && elapsed_time < ((960 * zone_factor) + 180)) {        // 17 ~ 19분     // 시작 속도 6.4 시작
                        if(speed < 6.4 && elapsed_time < ((840 * zone_factor) + 181)) {
                            float tmp = (float)(6.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((840 * zone_factor) + 189)) {
                            onSpeed_8090();
                        }
                    } else if(elapsed_time >= ((960 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {        // 19 ~ 21분     // 시작 속도 5 시작
                        if(speed > 5 && elapsed_time < ((960 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((960 * zone_factor) + 189)) {
                            onSpeed_5060();
                        }
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1200 * zone_factor) + 180)) {        // 21 ~ 23분     // 시작 속도 6.4 시작
                        if(speed < 6.4 && elapsed_time < ((1080 * zone_factor) + 181)) {
                            float tmp = (float)(6.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1080 * zone_factor) + 189)) {
                            onSpeed_8090();
                        }
                    } else if(elapsed_time >= ((1200 * zone_factor) + 180) && elapsed_time < ((1380 * zone_factor) + 180)) {        // 23 ~ 26분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1380 * zone_factor) + 180) && elapsed_time < ((1560 * zone_factor) + 180)) {        // 26 ~ 29분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1560 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 29 ~ 33분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {        // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 3:
                    if(elapsed_time >= 180 && elapsed_time < ((120 * zone_factor) + 180)) {                     // 3 ~ 5분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((120 * zone_factor) + 180) && elapsed_time < ((240 * zone_factor) + 180)) {              // 5 ~ 7분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((240 * zone_factor) + 180) && elapsed_time < ((360 * zone_factor) + 180)) {              // 7 ~ 9분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((360 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {              // 9 ~ 11분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((600 * zone_factor) + 180)) {              // 11 ~ 13분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((600 * zone_factor) + 180) && elapsed_time < ((720 * zone_factor) + 180)) {              // 13 ~ 15분         // 시작 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((600 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((600 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((720 * zone_factor) + 180) && elapsed_time < ((840 * zone_factor) + 180)) {             // 15 ~ 17분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((840 * zone_factor) + 180) && elapsed_time < ((960 * zone_factor) + 180)) {            // 17 ~ 19분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((960 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {            // 19 ~ 21분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1200 * zone_factor) + 180)) {            // 21 ~ 23분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((1200 * zone_factor) + 180) && elapsed_time < ((1320 * zone_factor) + 180)) {            // 23 ~ 25분         // 시작 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((1200 * zone_factor) + 181)) {
                            float tmp = (float) (speed - 4.4);
                            int num = (int) ((tmp * 10) / 2);
                            for (int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1200 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1320 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {            // 25 ~ 27분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1560 * zone_factor) + 180)) {            // 27 ~ 29분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1560 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {            // 29 ~ 31분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {            // 31 ~ 33분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {        // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 4:
                    if(elapsed_time >= 180 && elapsed_time < ((120 * zone_factor) + 180)) {                 // 3 ~ 5분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((120 * zone_factor) + 180) && elapsed_time < ((240 * zone_factor) + 180)) {          // 5 ~ 7분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((240 * zone_factor) + 180) && elapsed_time < ((360 * zone_factor) + 180)) {          // 7 ~ 9분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((360 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {          // 9 ~ 11분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((600 * zone_factor) + 180)) {          // 11 ~ 13분         // 시작 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((480 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((480 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((600 * zone_factor) + 180) && elapsed_time < ((720 * zone_factor) + 180)) {          // 13 ~ 15분         // 시작 속도 6.4 시작
                        if(speed < 6.4 && elapsed_time < ((600 * zone_factor) + 181)) {
                            float tmp = (float)(6.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((600 * zone_factor) + 189)) {
                            onSpeed_8090();
                        }
                    } else if(elapsed_time >= ((720 * zone_factor) + 180) && elapsed_time < ((900 * zone_factor) + 180)) {         // 15 ~ 18분         // 시작 속도 5 시작
                        if(speed > 5 && elapsed_time < ((720 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((720 * zone_factor) + 189)) {
                            onSpeed_5060();
                        }
                    } else if(elapsed_time >= ((900 * zone_factor) + 180) && elapsed_time < ((1020 * zone_factor) + 180)) {        // 18 ~ 20분         // 시작 속도 6.4 시작
                        if(speed < 6.4 && elapsed_time < ((900 * zone_factor) + 181)) {
                            float tmp = (float)(6.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((900 * zone_factor) + 189)) {
                            onSpeed_8090();
                        }
                    } else if(elapsed_time >= ((1020 * zone_factor) + 180) && elapsed_time < ((1200 * zone_factor) + 180)) {        // 20 ~ 23분         // 시작 속도 5 시작
                        if(speed > 5 && elapsed_time < ((1020 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1020 * zone_factor) + 189)) {
                            onSpeed_5060();
                        }
                    } else if(elapsed_time >= ((1200 * zone_factor) + 180) && elapsed_time < ((1320 * zone_factor) + 180)) {        // 23 ~ 25분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1320 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {        // 25 ~ 27분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1560 * zone_factor) + 180)) {        // 27 ~ 29분         // 시작 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((1440 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1440 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1560 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {        // 29 ~ 31분         // 시작 속도 6.4 시작
                        if(speed < 6.4 && elapsed_time < ((1560 * zone_factor) + 181)) {
                            float tmp = (float)(6.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1560 * zone_factor) + 189)) {
                            onSpeed_8090();
                        }
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 31 ~ 33분         // 시작 속도 5 시작
                        if(speed > 5 && elapsed_time < ((1680 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1680 * zone_factor) + 189)) {
                            onSpeed_5060();
                        }
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {        // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 5:
                    if(elapsed_time >= 180 && elapsed_time < ((60 * zone_factor) + 180)) {                 // 3 ~ 4분
                        if(speed < 6.4 && elapsed_time < 181) {
                            float tmp = (float)(6.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_8090();
                        }
                    } else if(elapsed_time >= ((60 * zone_factor) + 180) && elapsed_time < ((120 * zone_factor) + 180)) {          // 4 ~ 5분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((120 * zone_factor) + 180) && elapsed_time < ((180 * zone_factor) + 180)) {          // 5 ~ 6분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((180 * zone_factor) + 180) && elapsed_time < ((240 * zone_factor) + 180)) {          // 6 ~ 7분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((240 * zone_factor) + 180) && elapsed_time < ((360 * zone_factor) + 180)) {          // 7 ~ 9분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((360 * zone_factor) + 180) && elapsed_time < ((420 * zone_factor) + 180)) {          // 9 ~ 10분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((420 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {          // 10 ~ 11분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((540 * zone_factor) + 180)) {          // 11 ~ 12분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((540 * zone_factor) + 180) && elapsed_time < ((660 * zone_factor) + 180)) {          // 12 ~ 14분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((660 * zone_factor) + 180) && elapsed_time < ((720 * zone_factor) + 180)) {          // 14 ~ 15분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((720 * zone_factor) + 180) && elapsed_time < ((780 * zone_factor) + 180)) {          // 15 ~ 16분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((780 * zone_factor) + 180) && elapsed_time < ((840 * zone_factor) + 180)) {         // 16 ~ 17분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((840 * zone_factor) + 180) && elapsed_time < ((960 * zone_factor) + 180)) {        // 17 ~ 19분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((960 * zone_factor) + 180) && elapsed_time < ((1020 * zone_factor) + 180)) {        // 19 ~ 20분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1020 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {        // 20 ~ 21분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1140 * zone_factor) + 180)) {        // 21 ~ 22분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1140 * zone_factor) + 180) && elapsed_time < ((1260 * zone_factor) + 180)) {        // 22 ~ 24분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((1260 * zone_factor) + 180) && elapsed_time < ((1320 * zone_factor) + 180)) {        // 24 ~ 25분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1320 * zone_factor) + 180) && elapsed_time < ((1380 * zone_factor) + 180)) {        // 25 ~ 26분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1380 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {        // 26 ~ 27분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1560 * zone_factor) + 180)) {        // 27 ~ 29분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1560 * zone_factor) + 180) && elapsed_time < ((1620 * zone_factor) + 180)) {        // 29 ~ 30분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1620 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {        // 30 ~ 31분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1740 * zone_factor) + 180)) {        // 31 ~ 32분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1740 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 32 ~ 33분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {        // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 6:
                    if(elapsed_time >= 180 && elapsed_time < ((120 * zone_factor) + 180)) {                 // 3 ~ 5분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((120 * zone_factor) + 180) && elapsed_time < ((240 * zone_factor) + 180)) {          // 5 ~ 7분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((240 * zone_factor) + 180) && elapsed_time < ((300 * zone_factor) + 180)) {          // 7 ~ 8분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((300 * zone_factor) + 180) && elapsed_time < ((360 * zone_factor) + 180)) {          // 8 ~ 9분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((360 * zone_factor) + 180) && elapsed_time < ((420 * zone_factor) + 180)) {          // 9 ~ 10분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((420 * zone_factor) + 180) && elapsed_time < ((540 * zone_factor) + 180)) {          // 10 ~ 12분     // 시작 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((420 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((420 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((540 * zone_factor) + 180) && elapsed_time < ((600 * zone_factor) + 180)) {          // 12 ~ 13분     // 시작 속도 6.4
                        if(speed < 6.4 && elapsed_time < ((540 * zone_factor) + 181)) {
                            float tmp = (float)(6.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((540 * zone_factor) + 189)) {
                            onSpeed_8090();
                        }
                    } else if(elapsed_time >= ((600 * zone_factor) + 180) && elapsed_time < ((720 * zone_factor) + 180)) {          // 13 ~ 15분     // 시작 속도 4.4
                        if(speed > 4.4 && elapsed_time < ((600 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((600 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((720 * zone_factor) + 180) && elapsed_time < ((840 * zone_factor) + 180)) {         // 15 ~ 17분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((840 * zone_factor) + 180) && elapsed_time < ((900 * zone_factor) + 180)) {        // 17 ~ 18분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((900 * zone_factor) + 180) && elapsed_time < ((960 * zone_factor) + 180)) {        // 18 ~ 19분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((960 * zone_factor) + 180) && elapsed_time < ((1020 * zone_factor) + 180)) {        // 19 ~ 20분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((1020 * zone_factor) + 180) && elapsed_time < ((1140 * zone_factor) + 180)) {        // 20 ~ 22분     // 시작 속도 4.4
                        if(speed > 4.4 && elapsed_time < ((1020 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1020 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1140 * zone_factor) + 180) && elapsed_time < ((1200 * zone_factor) + 180)) {        // 22 ~ 23분     // 시작 속도 6.4
                        if(speed < 6.4 && elapsed_time < ((1140 * zone_factor) + 181)) {
                            float tmp = (float)(6.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1140 * zone_factor) + 189)) {
                            onSpeed_8090();
                        }
                    } else if(elapsed_time >= ((1200 * zone_factor) + 180) && elapsed_time < ((1320 * zone_factor) + 180)) {        // 23 ~ 25분     // 시작 속도 4.4
                        if(speed > 4.4 && elapsed_time < ((1200 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1200 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1320 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {        // 25 ~ 27분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1500 * zone_factor) + 180)) {        // 27 ~ 28분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1500 * zone_factor) + 180) && elapsed_time < ((1560 * zone_factor) + 180)) {        // 28 ~ 29분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1560 * zone_factor) + 180) && elapsed_time < ((1620 * zone_factor) + 180)) {        // 29 ~ 30분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((1620 * zone_factor) + 180) && elapsed_time < ((1740 * zone_factor) + 180)) {        // 30 ~ 32분     // 시작 속도 4.4
                        if(speed > 4.4 && elapsed_time < ((1620 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1620 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1740 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 32 ~ 33분     // 시작 속도 6.4
                        if(speed < 6.4 && elapsed_time < ((1740 * zone_factor) + 181)) {
                            float tmp = (float)(6.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1740 * zone_factor) + 189)) {
                            onSpeed_8090();
                        }
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {        // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 7:
                    if(elapsed_time >= 180 && elapsed_time < ((120 * zone_factor) + 180)) {                 // 3 ~ 5분       // 시작 속도 6.4
                        if(speed < 6.4 && elapsed_time < 181) {
                            float tmp = (float)(6.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_8090();
                        }
                    } else if(elapsed_time >= ((120 * zone_factor) + 180) && elapsed_time < ((180 * zone_factor) + 180)) {          // 5 ~ 6분       // 시작 속도 5 시작
                        if(speed > 5 && elapsed_time < ((120 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((120 * zone_factor) + 189)) {
                            onSpeed_5060();
                        }
                    } else if(elapsed_time >= ((180 * zone_factor) + 180) && elapsed_time < ((240 * zone_factor) + 180)) {          // 6 ~ 7분       // 시작 속도 6 시작
                        if(speed < 6 && elapsed_time < ((180 * zone_factor) + 181)) {
                            float tmp = (float)(6 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((180 * zone_factor) + 189)) {
                            onSpeed_7080();
                        }
                    } else if(elapsed_time >= ((240 * zone_factor) + 180) && elapsed_time < ((300 * zone_factor) + 180)) {          // 7 ~ 8분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((300 * zone_factor) + 180) && elapsed_time < ((420 * zone_factor) + 180)) {          // 8 ~ 10분      // 시작 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((300 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((300 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((420 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {          // 10 ~ 11분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((600 * zone_factor) + 180)) {          // 11 ~ 13분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((600 * zone_factor) + 180) && elapsed_time < ((720 * zone_factor) + 180)) {          // 13 ~ 15분     // 시작 속도 4.4 시작
                        if(speed > 4.4 && elapsed_time < ((600 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((600 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((720 * zone_factor) + 180) && elapsed_time < ((840 * zone_factor) + 180)) {         // 15 ~ 17분     // 시작 속도 6.4 시작
                        if(speed < 6.4 && elapsed_time < ((720 * zone_factor) + 181)) {
                            float tmp = (float)(6.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((720 * zone_factor) + 189)) {
                            onSpeed_8090();
                        }
                    } else if(elapsed_time >= ((840 * zone_factor) + 180) && elapsed_time < ((960 * zone_factor) + 180)) {        // 17 ~ 19분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((960 * zone_factor) + 180) && elapsed_time < ((1020 * zone_factor) + 180)) {        // 19 ~ 20분     // 시작 속도 5 시작
                        if(speed > 5 && elapsed_time < ((960 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((960 * zone_factor) + 189)) {
                            onSpeed_5060();
                        }
                    } else if(elapsed_time >= ((1020 * zone_factor) + 180) && elapsed_time < ((1140 * zone_factor) + 180)) {        // 20 ~ 22분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1140 * zone_factor) + 180) && elapsed_time < ((1200 * zone_factor) + 180)) {        // 22 ~ 23분     // 시작 속도 6.4
                        if(speed < 6.4 && elapsed_time < ((1140 * zone_factor) + 181)) {
                            float tmp = (float)(6.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1140 * zone_factor) + 189)) {
                            onSpeed_8090();
                        }
                    } else if(elapsed_time >= ((1200 * zone_factor) + 180) && elapsed_time < ((1260 * zone_factor) + 180)) {        // 23 ~ 24분     // 시작 속도 5.4
                        if(speed > 5.4 && elapsed_time < ((1200 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1200 * zone_factor) + 189)) {
                            onSpeed_6070();
                        }
                    } else if(elapsed_time >= ((1260 * zone_factor) + 180) && elapsed_time < ((1320 * zone_factor) + 180)) {        // 24 ~ 25분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1320 * zone_factor) + 180) && elapsed_time < ((1380 * zone_factor) + 180)) {        // 25 ~ 26분     // 시작 속도 5 시작
                        if(speed > 5 && elapsed_time < ((1320 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1320 * zone_factor) + 189)) {
                            onSpeed_5060();
                        }
                    } else if(elapsed_time >= ((1380 * zone_factor) + 180) && elapsed_time < ((1500 * zone_factor) + 180)) {        // 26 ~ 28분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1500 * zone_factor) + 180) && elapsed_time < ((1560 * zone_factor) + 180)) {        // 28 ~ 29분     // 시작 속도 6.4 시작
                        if(speed < 6.4 && elapsed_time < ((1500 * zone_factor) + 181)) {
                            float tmp = (float)(6.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1500 * zone_factor) + 189)) {
                            onSpeed_8090();
                        }
                    } else if(elapsed_time >= ((1560 * zone_factor) + 180) && elapsed_time < ((1620 * zone_factor) + 180)) {        // 29 ~ 30분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1620 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {        // 30 ~ 31분     // 시작 속도 5
                        if(speed > 5 && elapsed_time < ((1620 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1620 * zone_factor) + 189)) {
                            onSpeed_5060();
                        }
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1740 * zone_factor) + 180)) {        // 31 ~ 32분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1740 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 32 ~ 33분     // 시작 속도 4.4
                        if(speed > 4.4 && elapsed_time < ((1740 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1740 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    }
                    else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {        // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
            }
        } else if(protocol_level.equals("매우높은강도")) {
            switch(protocol_version) {
                case 1:
                    if(elapsed_time >= 180 && elapsed_time < ((240 * zone_factor) + 180)) {                 // 3 ~ 7분       // 시작 속도 5
                        if(speed < 5 && elapsed_time < 181) {
                            float tmp = (float)(5 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_5060();
                        }
                    } else if(elapsed_time >= ((240 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {          // 7 ~ 11분      // 시작 속도 6
                        if(speed < 6 && elapsed_time < ((240 * zone_factor) + 181)) {
                            float tmp = (float)(6 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((240 * zone_factor) + 189)) {
                            onSpeed_7080();
                        }
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((600 * zone_factor) + 180)) {          // 11 ~ 13분     // 시작 속도 7
                        if(speed < 7 && elapsed_time < ((480 * zone_factor) + 181)) {
                            float tmp = (float)(7 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((480 * zone_factor) + 189)) {
                            onSpeed_90100();
                        }
                    } else if(elapsed_time >= ((600 * zone_factor) + 180) && elapsed_time < ((840 * zone_factor) + 180)) {         // 13 ~ 17분     // 시작 속도 5.4
                        if(speed > 5.4 && elapsed_time < ((600 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((600 * zone_factor) + 189)) {
                            onSpeed_6070();
                        }
                    } else if(elapsed_time >= ((840 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {        // 17 ~ 21분     // 시작 속도 4.4
                        if(speed > 4.4 && elapsed_time < ((840 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((840 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1200 * zone_factor) + 180)) {        // 21 ~ 23분     // 시작 속도 7
                        if(speed < 7 && elapsed_time < ((1080 * zone_factor) + 181)) {
                            float tmp = (float)(7 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1080 * zone_factor) + 189)) {
                            onSpeed_90100();
                        }
                    } else if(elapsed_time >= ((1200 * zone_factor) + 180) && elapsed_time < ((1320 * zone_factor) + 180)) {        // 23 ~ 25분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((1320 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {        // 25 ~ 27분     // 시작 속도 5
                        if(speed > 5 && elapsed_time < ((1320 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1320 * zone_factor) + 189)) {
                            onSpeed_5060();
                        }
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {        // 27 ~ 31분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 31 ~ 33분     // 시작 속도 7
                        if(speed < 7 && elapsed_time < ((1680 * zone_factor) + 181)) {
                            float tmp = (float)(7 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1680 * zone_factor) + 189)) {
                            onSpeed_90100();
                        }
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {        // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 2:
                    if(elapsed_time >= 180 && elapsed_time < ((120 * zone_factor) + 180)) {                 // 3 ~ 5분           // 시작 속도 7
                        if(speed < 7 && elapsed_time < 181) {
                            float tmp = (float)(7 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_90100();
                        }
                    } else if(elapsed_time >= ((120 * zone_factor) + 180) && elapsed_time < ((300 * zone_factor) + 180)) {          // 5 ~ 8분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((300 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {          // 8 ~ 11분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((660 * zone_factor) + 180)) {          // 11 ~ 14분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((660 * zone_factor) + 180) && elapsed_time < ((840 * zone_factor) + 180)) {         // 14 ~ 17분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((840 * zone_factor) + 180) && elapsed_time < ((960 * zone_factor) + 180)) {        // 17 ~ 19분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((960 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {        // 19 ~ 21분         // 시작 속도 7
                        if(speed < 7 && elapsed_time < ((960 * zone_factor) + 181)) {
                            float tmp = (float)(7 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((960 * zone_factor) + 189)) {
                            onSpeed_90100();
                        }
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1200 * zone_factor) + 180)) {        // 21 ~ 23분         // 시작 속도 4.4
                        if(speed > 4.4 && elapsed_time < ((1080 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1080 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1200 * zone_factor) + 180) && elapsed_time < ((1320 * zone_factor) + 180)) {        // 23 ~ 25분         // 시작 속도 7
                        if(speed < 7 && elapsed_time < ((1200 * zone_factor) + 181)) {
                            float tmp = (float)(7 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1200 * zone_factor) + 189)) {
                            onSpeed_90100();
                        }
                    } else if(elapsed_time >= ((1320 * zone_factor) + 180) && elapsed_time < ((1500 * zone_factor) + 180)) {        // 25 ~ 28분         // 시작 속도 5.4
                        if(speed > 5.4 && elapsed_time < ((1320 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1320 * zone_factor) + 189)) {
                            onSpeed_6070();
                        }
                    } else if(elapsed_time >= ((1500 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {        // 28 ~ 31분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {        // 31 ~ 33분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {        // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 3:
                    if(elapsed_time >= 180 && elapsed_time < ((120 * zone_factor) + 180)) {                     // 3 ~ 5분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((120 * zone_factor) + 180) && elapsed_time < ((240 * zone_factor) + 180)) {              // 5 ~ 7분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((240 * zone_factor) + 180) && elapsed_time < ((360 * zone_factor) + 180)) {              // 7 ~ 9분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((360 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {              // 9 ~ 11분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((600 * zone_factor) + 180)) {              // 11 ~ 13분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((600 * zone_factor) + 180) && elapsed_time < ((720 * zone_factor) + 180)) {              // 13 ~ 15분
                        onSpeed_90100();
                    } else if(elapsed_time >= ((720 * zone_factor) + 180) && elapsed_time < ((900 * zone_factor) + 180)) {             // 15 ~ 18분         // 시작 속도 4.4
                        if(speed > 4.4 && elapsed_time < ((720 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((720 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((900 * zone_factor) + 180) && elapsed_time < ((1020 * zone_factor) + 180)) {            // 18 ~ 20분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1020 * zone_factor) + 180) && elapsed_time < ((1140 * zone_factor) + 180)) {            // 20 ~ 22분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1140 * zone_factor) + 180) && elapsed_time < ((1260 * zone_factor) + 180)) {            // 22 ~ 24분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1260 * zone_factor) + 180) && elapsed_time < ((1380 * zone_factor) + 180)) {            // 24 ~ 26분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((1380 * zone_factor) + 180) && elapsed_time < ((1500 * zone_factor) + 180)) {            // 26 ~ 28분
                        onSpeed_90100();
                    } else if(elapsed_time >= ((1500 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {            // 28 ~ 31분         // 시작 속도 4.5
                        if(speed > 4.4 && elapsed_time < ((1500 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1500 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {            // 31 ~ 33분         // 시작 속도 7
                        if(speed < 7 && elapsed_time < ((1680 * zone_factor) + 181)) {
                            float tmp = (float)(7 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1680 * zone_factor) + 189)) {
                            onSpeed_90100();
                        }
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {            // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 4:
                    if(elapsed_time >= 180 && elapsed_time < ((120 * zone_factor) + 180)) {                     // 3 ~ 5분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((120 * zone_factor) + 180) && elapsed_time < ((240 * zone_factor) + 180)) {              // 5 ~ 7분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((240 * zone_factor) + 180) && elapsed_time < ((360 * zone_factor) + 180)) {              // 7 ~ 9분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((360 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {              // 9 ~ 11분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((600 * zone_factor) + 180)) {              // 11 ~ 13분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((600 * zone_factor) + 180) && elapsed_time < ((720 * zone_factor) + 180)) {              // 13 ~ 15분         // 시작 속도 4.4
                        if(speed > 4.4 && elapsed_time < ((600 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((600 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((720 * zone_factor) + 180) && elapsed_time < ((840 * zone_factor) + 180)) {              // 15 ~ 17분        // 시작 속도 7
                        if(speed < 7 && elapsed_time < ((720 * zone_factor) + 181)) {
                            float tmp = (float) (7 - speed);
                            int num = (int) ((tmp * 10) / 2);
                            for (int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((720 * zone_factor) + 189)) {
                            onSpeed_90100();
                        }
                    } else if(elapsed_time >= ((840 * zone_factor) + 180) && elapsed_time < ((960 * zone_factor) + 180)) {              // 17 ~ 19분       // 시작 속도 5
                        if(speed > 5 && elapsed_time < ((840 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((840 * zone_factor) + 189)) {
                            onSpeed_5060();
                        }
                    } else if(elapsed_time >= ((960 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {              // 19 ~ 21분       // 시작 속도 7
                        if(speed < 7 && elapsed_time < ((960 * zone_factor) + 181)) {
                            float tmp = (float)(7 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((960 * zone_factor) + 189)) {
                            onSpeed_90100();
                        }
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1200 * zone_factor) + 180)) {              // 21 ~ 23분       // 시작 속도 5
                        if(speed > 5 && elapsed_time < ((1080 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1080 * zone_factor) + 189)) {
                            onSpeed_5060();
                        }
                    } else if(elapsed_time >= ((1200 * zone_factor) + 180) && elapsed_time < ((1320 * zone_factor) + 180)) {              // 23 ~ 25분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1320 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {              // 25 ~ 27분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1560 * zone_factor) + 180)) {              // 27 ~ 29분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((1560 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {              // 29 ~ 31분       // 시작 속도 4.4
                        if(speed > 4.4 && elapsed_time < ((1560 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1560 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {              // 31 ~ 33분        // 시작 속도 7
                        if(speed < 7 && elapsed_time < ((1680 * zone_factor) + 181)) {
                            float tmp = (float)(7 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1680 * zone_factor) + 289)) {
                            onSpeed_90100();
                        }
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {            // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 5:
                    if(elapsed_time >= 180 && elapsed_time < ((120 * zone_factor) + 180)) {                     // 3 ~ 5분
                        if(speed < 4.4 && elapsed_time < 181) {
                            float tmp = (float)(4.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i< num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_4050();
                        }
                    }else if(elapsed_time >= ((120 * zone_factor) + 180) && elapsed_time < ((180 * zone_factor) + 180)) {              // 5 ~ 6분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((180 * zone_factor) + 180) && elapsed_time < ((240 * zone_factor) + 180)) {             // 6 ~ 7분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((240 * zone_factor) + 180) && elapsed_time < ((300 * zone_factor) + 180)) {             // 7 ~ 8분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((300 * zone_factor) + 180) && elapsed_time < ((360 * zone_factor) + 180)) {             // 8 ~ 9분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((360 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {             // 9 ~ 11분
                        onSpeed_90100();
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((600 * zone_factor) + 180)) {             // 11 ~ 13분          // 시작 속도 4.4
                        if(speed > 4.4 && elapsed_time < ((480 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((480 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((600 * zone_factor) + 180) && elapsed_time < ((660 * zone_factor) + 180)) {             // 13 ~ 14분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((660 * zone_factor) + 180) && elapsed_time < ((720 * zone_factor) + 180)) {             // 14 ~ 15분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((720 * zone_factor) + 180) && elapsed_time < ((780 * zone_factor) + 180)) {             // 15 ~ 16분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((780 * zone_factor) + 180) && elapsed_time < ((840 * zone_factor) + 180)) {             // 16 ~ 17분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((840 * zone_factor) + 180) && elapsed_time < ((900 * zone_factor) + 180)) {             // 17 ~ 18분
                        onSpeed_90100();
                    } else if(elapsed_time >= ((900 * zone_factor) + 180) && elapsed_time < ((1020 * zone_factor) + 180)) {             // 18 ~ 20분        // 시작 속도 4.4
                        if(speed > 4.4 && elapsed_time < ((900 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((900 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1020 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {             // 20 ~ 21분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1140 * zone_factor) + 180)) {             // 21 ~ 22분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1140 * zone_factor) + 180) && elapsed_time < ((1200 * zone_factor) + 180)) {             // 22 ~ 23분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1200 * zone_factor) + 180) && elapsed_time < ((1260 * zone_factor) + 180)) {             // 23 ~ 24분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((1260 * zone_factor) + 180) && elapsed_time < ((1320 * zone_factor) + 180)) {             // 24 ~ 25분
                        onSpeed_90100();
                    } else if(elapsed_time >= ((1320 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {             // 25 ~ 27분        // 시작 속도 4.4
                        if(speed > 4.4 && elapsed_time < ((1320 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1320 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1500 * zone_factor) + 180)) {             // 27 ~ 28분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1500 * zone_factor) + 180) && elapsed_time < ((1560 * zone_factor) + 180)) {             // 28 ~ 29분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1560 * zone_factor) + 180) && elapsed_time < ((1620 * zone_factor) + 180)) {             // 29 ~ 30분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1620 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {             // 30 ~ 31분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {             // 31 ~ 33분
                        onSpeed_90100();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {            // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 6:
                    if(elapsed_time >= 180 && elapsed_time < ((60 * zone_factor) + 180)) {                     // 3 ~ 4분           // 시작 속도 7
                        if(speed < 7 && elapsed_time < 181) {
                            float tmp = (float)(7 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_90100();
                        }
                    } else if(elapsed_time >= ((60 * zone_factor) + 180) && elapsed_time < ((90 * zone_factor) + 180)) {             // 4 ~ 4.5분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((90 * zone_factor) + 180) && elapsed_time < ((120 * zone_factor) + 180)) {             // 4.5 ~ 5분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((120 * zone_factor) + 180) && elapsed_time < ((150 * zone_factor) + 180)) {             // 5 ~ 5.5분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((150 * zone_factor) + 180) && elapsed_time < ((180 * zone_factor) + 180)) {             // 5.5 ~ 6분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((180 * zone_factor) + 180) && elapsed_time < ((300 * zone_factor) + 180)) {             // 6 ~ 8분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((300 * zone_factor) + 180) && elapsed_time < ((360 * zone_factor) + 180)) {             // 8 ~ 9분            // 시작 속도 7
                        if(speed < 7 && elapsed_time < ((300 * zone_factor) + 181)) {
                            float tmp = (float)(7 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((300 * zone_factor) + 189)) {
                            onSpeed_90100();
                        }
                    } else if(elapsed_time >= ((360 * zone_factor) + 180) && elapsed_time < ((390 * zone_factor) + 180)) {             // 9 ~ 9.5분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((390 * zone_factor) + 180) && elapsed_time < ((420 * zone_factor) + 180)) {             // 9.5 ~ 10분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((420 * zone_factor) + 180) && elapsed_time < ((450 * zone_factor) + 180)) {             // 10 ~ 10.5분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((450 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {             // 10.5 ~ 11분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((600 * zone_factor) + 180)) {             // 11 ~ 13분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((600 * zone_factor) + 180) && elapsed_time < ((660 * zone_factor) + 180)) {             // 13 ~ 14분          // 시작 속도 7
                        if(speed < 7 && elapsed_time < ((600 * zone_factor) + 181)) {
                            float tmp = (float)(7 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((600 * zone_factor) + 189)) {
                            onSpeed_90100();
                        }
                    } else if(elapsed_time >= ((660 * zone_factor) + 180) && elapsed_time < ((690 * zone_factor) + 180)) {             // 14 ~ 14.5분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((690 * zone_factor) + 180) && elapsed_time < ((720 * zone_factor) + 180)) {             // 14.5 ~ 15분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((720 * zone_factor) + 180) && elapsed_time < ((750 * zone_factor) + 180)) {             // 15 ~ 15.5분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((750 * zone_factor) + 180) && elapsed_time < ((780 * zone_factor) + 180)) {             // 15.5 ~ 16분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((780 * zone_factor) + 180) && elapsed_time < ((900 * zone_factor) + 180)) {             // 16 ~ 18분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((900 * zone_factor) + 180) && elapsed_time < ((960 * zone_factor) + 180)) {             // 18 ~ 19분        // 시작 속도 7
                        if(speed < 7 && elapsed_time < ((900 * zone_factor) + 181)) {
                            float tmp = (float)(7 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((900 * zone_factor) + 189)) {
                            onSpeed_90100();
                        }
                    } else if(elapsed_time >= ((960 * zone_factor) + 180) && elapsed_time < ((990 * zone_factor) + 180)) {             // 19 ~ 19.5분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((990 * zone_factor) + 180) && elapsed_time < ((1020 * zone_factor) + 180)) {             // 19.5 ~ 20분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1020 * zone_factor) + 180) && elapsed_time < ((1050 * zone_factor) + 180)) {             // 20 ~ 20.5분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1050 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {             // 20.5 ~ 21분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1200 * zone_factor) + 180)) {             // 21 ~ 23분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1200 * zone_factor) + 180) && elapsed_time < ((1260 * zone_factor) + 180)) {             // 23 ~ 24분        // 시작 속도 7
                        if(speed < 7 && elapsed_time < ((1200 * zone_factor) + 181)) {
                            float tmp = (float)(7 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1200 * zone_factor) + 189)) {
                            onSpeed_90100();
                        }
                    } else if(elapsed_time >= ((1260 * zone_factor) + 180) && elapsed_time < ((1290 * zone_factor) + 180)) {             // 24 ~ 24.5분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((1290 * zone_factor) + 180) && elapsed_time < ((1320 * zone_factor) + 180)) {             // 24.5 ~ 25분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1320 * zone_factor) + 180) && elapsed_time < ((1350 * zone_factor) + 180)) {             // 25 ~ 25.5분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1350 * zone_factor) + 180) && elapsed_time < ((1380 * zone_factor) + 180)) {             // 25.5 ~ 26분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1380 * zone_factor) + 180) && elapsed_time < ((1500 * zone_factor) + 180)) {             // 26 ~ 28분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1500 * zone_factor) + 180) && elapsed_time < ((1560 * zone_factor) + 180)) {             // 28 ~ 29분        // 시작 속도 7
                        if(speed < 7 && elapsed_time < ((1500 * zone_factor) + 181)) {
                            float tmp = (float)(7 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1500 * zone_factor) + 189)) {
                            onSpeed_90100();
                        }
                    }  else if(elapsed_time >= ((1560 * zone_factor) + 180) && elapsed_time < ((1590 * zone_factor) + 180)) {            // 29 ~ 29.5분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((1590 * zone_factor) + 180) && elapsed_time < ((1620 * zone_factor) + 180)) {             // 29.5 ~ 30분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((1620 * zone_factor) + 180) && elapsed_time < ((1650 * zone_factor) + 180)) {             // 30 ~ 30.5분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1650 * zone_factor) + 180) && elapsed_time < ((1680 * zone_factor) + 180)) {             // 30.5 ~ 31분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1680 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {             // 31 ~ 33분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {            // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
                case 7:
                    if(elapsed_time >= 180 && elapsed_time < ((120 * zone_factor) + 180)) {                     // 3 ~ 5분           //  시작 속도 7
                        if(speed < 7 && elapsed_time < 181) {
                            float tmp = (float)(7 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > 189) {
                            onSpeed_90100();
                        }
                    } else if(elapsed_time >= ((120 * zone_factor) + 180) && elapsed_time < ((180 * zone_factor) + 180)) {              // 5 ~ 6분           // 시작 속도 5
                        if(speed > 5 && elapsed_time < ((120 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((120 * zone_factor) + 189)) {
                            onSpeed_5060();
                        }
                    } else if(elapsed_time >= ((180 * zone_factor) + 180) && elapsed_time < ((240 * zone_factor) + 180)) {              // 6 ~ 7분           // 시작 속도 6.4
                        if(speed < 6.4 && elapsed_time < ((180 * zone_factor) + 181)) {
                            float tmp = (float)(6.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((180 * zone_factor) + 189)) {
                            onSpeed_8090();
                        }
                    } else if(elapsed_time >= ((240 * zone_factor) + 180) && elapsed_time < ((300 * zone_factor) + 180)) {              // 7 ~ 8분           // 시작 속도 5.4
                        if(speed > 5.4 && elapsed_time < ((240 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((240 * zone_factor) + 189)) {
                            onSpeed_6070();
                        }
                    } else if(elapsed_time >= ((300 * zone_factor) + 180) && elapsed_time < ((360 * zone_factor) + 180)) {              // 8 ~ 9분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((360 * zone_factor) + 180) && elapsed_time < ((480 * zone_factor) + 180)) {              // 9 ~ 11분          // 시작 속도 4.4
                        if(speed > 4.4 && elapsed_time < ((360 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((360 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if(elapsed_time >= ((480 * zone_factor) + 180) && elapsed_time < ((540 * zone_factor) + 180)) {              // 11 ~ 12분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((540 * zone_factor) + 180) && elapsed_time < ((600 * zone_factor) + 180)) {              // 12 ~ 13분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((600 * zone_factor) + 180) && elapsed_time < ((660 * zone_factor) + 180)) {              // 13 ~ 14분         // 시작 속도 7
                        if(speed < 7 && elapsed_time < ((600 * zone_factor) + 181)) {
                            float tmp = (float)(7 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((600 * zone_factor) + 189)) {
                            onSpeed_90100();
                        }
                    } else if(elapsed_time >= ((660 * zone_factor) + 180) && elapsed_time < ((720 * zone_factor) + 180)) {              // 14 ~ 15분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((720 * zone_factor) + 180) && elapsed_time < ((780 * zone_factor) + 180)) {              // 15 ~ 16분
                        onSpeed_7080();
                    } else if(elapsed_time >= ((780 * zone_factor) + 180) && elapsed_time < ((900 * zone_factor) + 180)) {              // 16 ~ 18분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((900 * zone_factor) + 180) && elapsed_time < ((960 * zone_factor) + 180)) {              // 18 ~ 19분       // 시작 속도 6.4
                        if(speed < 6.4 && elapsed_time < ((900 * zone_factor) + 181)) {
                            float tmp = (float)(6.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((900 * zone_factor) + 189)) {
                            onSpeed_8090();
                        }
                    } else if(elapsed_time >= ((960 * zone_factor) + 180) && elapsed_time < ((1020 * zone_factor) + 180)) {              // 19 ~ 20분
                        onSpeed_90100();
                    } else if(elapsed_time >= ((1020 * zone_factor) + 180) && elapsed_time < ((1080 * zone_factor) + 180)) {              // 20 ~ 21분       // 시작 속도 6
                        if(speed > 6 && elapsed_time < ((1020 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 6);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1020 * zone_factor) + 189)) {
                            onSpeed_7080();
                        }
                    } else if(elapsed_time >= ((1080 * zone_factor) + 180) && elapsed_time < ((1140 * zone_factor) + 180)) {              // 21 ~ 22분
                        onSpeed_6070();
                    } else if(elapsed_time >= ((1140 * zone_factor) + 180) && elapsed_time < ((1260 * zone_factor) + 180)) {              // 22 ~ 23분
                        onSpeed_5060();
                    } else if(elapsed_time >= ((1260 * zone_factor) + 180) && elapsed_time < ((1320 * zone_factor) + 180)) {              // 23 ~ 25분
                        onSpeed_4050();
                    } else if(elapsed_time >= ((1320 * zone_factor) + 180) && elapsed_time < ((1380 * zone_factor) + 180)) {              // 25 ~ 26분       // 시작 속도 5.4
                        if(speed < 5.4 && elapsed_time < ((1320 * zone_factor) + 181)) {
                            float tmp = (float)(5.4 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1320 * zone_factor) + 189)) {
                            onSpeed_6070();
                        }
                    } else if(elapsed_time >= ((1380 * zone_factor) + 180) && elapsed_time < ((1440 * zone_factor) + 180)) {              // 26 ~ 27분       // 시작 속도 7
                        if(speed < 7 && elapsed_time < ((1380 * zone_factor) + 181)) {
                            float tmp = (float)(7 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1380 * zone_factor) + 189)) {
                            onSpeed_90100();
                        }
                    } else if(elapsed_time >= ((1440 * zone_factor) + 180) && elapsed_time < ((1500 * zone_factor) + 180)) {              // 27 ~ 28분       // 시작 속도 5
                        if(speed > 5 && elapsed_time < ((1440 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 5);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1440 * zone_factor) + 189)) {
                            onSpeed_5060();
                        }
                    } else if(elapsed_time >= ((1500 * zone_factor) + 180) && elapsed_time < ((1560 * zone_factor) + 180)) {              // 28 ~ 29분       // 시작 속도 6
                        if(speed < 6 && elapsed_time < ((1500 * zone_factor) + 181)) {
                            float tmp = (float)(6 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1500 * zone_factor) + 189)) {
                            onSpeed_7080();
                        }
                    } else if(elapsed_time >= ((1560 * zone_factor) + 180) && elapsed_time < ((1620 * zone_factor) + 180)) {              // 29 ~ 30분
                        onSpeed_8090();
                    } else if(elapsed_time >= ((1620 * zone_factor) + 180) && elapsed_time < ((1740 * zone_factor) + 180)) {              // 30 ~ 32분       // 시작 속도 4.4
                        if(speed > 4.4 && elapsed_time < ((1620 * zone_factor) + 181)) {
                            float tmp = (float)(speed - 4.4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                        }
                        if(elapsed_time > ((1620 * zone_factor) + 189)) {
                            onSpeed_4050();
                        }
                    } else if (elapsed_time >= ((1740 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 180)) {             // 32 ~ 33분       // 시작 속도 7
                        if(speed < 7 && elapsed_time < ((1740 * zone_factor) + 181)) {
                            float tmp = (float)(7 - speed);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num; i++) {
                                speed += 0.2;
                                writeDataToSerial(3);
                            }
                        }
                        if(elapsed_time > ((1740 * zone_factor) + 189)) {
                            onSpeed_90100();
                        }
                    } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 360)) {              // 33 ~ 36분
                        if(speed > 4 && !isCoolDown) {
                            float tmp = (float)(speed - 4);
                            int num = (int)((tmp * 10) / 2);
                            for(int i = 0; i < num+1; i++) {
                                speed -= 0.2;
                                writeDataToSerial(4);
                            }
                            isCoolDown = true;
                        }
                    }
                    break;
            }
        }
    }

    int rest_hr = 80;
    public void onCalculateZones() {
        int userage = age;
        if(user_restHR != null && !user_restHR.equals(""))
            rest_hr = Integer.parseInt(user_restHR);

        double tmp = 1 + (coef / 100.0);

        std_fourty = (int)(((220-userage-rest_hr) * 0.4 + rest_hr) * tmp);
        std_fifty = (int)(((220-userage-rest_hr) * 0.5 + rest_hr) * tmp);
        std_sixty = (int)(((220-userage-rest_hr) * 0.6 + rest_hr) * tmp);
        std_seventy = (int)(((220-userage-rest_hr) * 0.7 + rest_hr) * tmp);
        std_eighty = (int)(((220-userage-rest_hr) * 0.8 + rest_hr) * tmp);
        std_ninety = (int)(((220-userage-rest_hr) * 0.9 + rest_hr) * tmp);
        std_hundred = (int)(((220-userage-rest_hr) * 1.0 + rest_hr) * tmp);

        String[] time = start_time.split("_");
        onWriteDataToText_zone(Environment.getExternalStorageDirectory() + "/NovelT/Custom/", name + "_" + time[0] + time[1] + time[2] + "_" + time[3] + time[4] + ".txt");
    }

    public void onReadPrograms(Field[] files) {
        String data = null;
        InputStream is = null;
        String protocol_file_name = protocol + protocol_version;

        for(int i = 0; i < files.length; i++) {
            if(files[i].getName().equals(protocol_file_name)) {
                is = getResources().openRawResource(getResources().getIdentifier(protocol_file_name, "raw", getPackageName()));
            }
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        if(is != null) {
            int i;
            try {
                i = is.read();
                while (i != -1) {
                    bos.write(i);
                    i = is.read();
                }

                data = new String(bos.toByteArray(), "MS949");
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            onParsingExPrograms(data);
        }
    }

    static String[][] params;
    public void onParsingExPrograms(String data) {
        String[] tmp = data.split("\r\n");
        params = new String[tmp.length][5];
        for(int i = 0; i < tmp.length; i++) {
            String[] tmp_ = tmp[i].split("\t");
            for(int j = 0; j < tmp_.length; j++) {
                params[i][j] = tmp_[j];
                if(tmp_[j].contains("%")) {
                    if(tmp_[j].equals("40%")) {
                        params[i][3] = String.valueOf(std_fourty);
                        params[i][4] = String.valueOf(std_fifty);
                    } else if(tmp_[j].equals("50%")) {
                        params[i][3] = String.valueOf(std_fifty);
                        params[i][4] = String.valueOf(std_sixty);
                    } else if(tmp_[j].equals("60%")) {
                        params[i][3] = String.valueOf(std_sixty);
                        params[i][4] = String.valueOf(std_seventy);
                    } else if(tmp_[j].equals("70%")) {
                        params[i][3] = String.valueOf(std_seventy);
                        params[i][4] = String.valueOf(std_eighty);
                    } else if(tmp_[j].equals("80%")) {
                        params[i][3] = String.valueOf(std_eighty);
                        params[i][4] = String.valueOf(std_ninety);
                    } else if(tmp_[j].equals("90%")) {
                        params[i][3] = String.valueOf(std_ninety);
                        params[i][4] = String.valueOf(std_hundred);
                    }
                }
            }
        }
        HRChart.onDrawZone(params, zone_factor);
    }

    public void onFinished() {
        elapsed_time = 0;
        mSerial.close();

        avg_hr = sum_hr / cnt;
        avg_speed = sum_speed / cnt_speed;

        long[] tmp = new long[times.size()];
        for(int i = 0; i < times.size(); i++) {
            tmp[i] = times.get(i);
        }

        Intent intent = new Intent(getApplicationContext(), CustomProtocolResultActivity.class);
        intent.putExtra("NAME", name);
        intent.putExtra("HEIGHT", height);
        intent.putExtra("WEIGHT", weight);
        intent.putExtra("AGE", age);
        intent.putExtra("GENDER", gender);
        intent.putExtra("HP", hp);
        intent.putExtra("MAX_BP", max_bp);
        intent.putExtra("MIN_BP", min_bp);
        intent.putExtra("BMI", bmi);
        intent.putExtra("LOCAL", local);
        intent.putExtra("PLACE", place);

        intent.putExtra("START_DATE", start_date);
        intent.putExtra("START_TIME", start_time);
        intent.putExtra("distance", distance);
        intent.putExtra("percentage", percentage);
        intent.putExtra("avg_hr", avg_hr);
        intent.putExtra("max_hr", max_hr);
        intent.putExtra("max_speed", max_speed);
        intent.putExtra("avg_speed", avg_speed);
        intent.putExtra("incline", incline);
        intent.putExtra("elapsed_time", text_elapsedTime.getText());
        intent.putExtra("calorie", calorie);
        intent.putExtra("PROTOCOL_NAME", text_protocol.getText().toString());
        if(isCancelled) intent.putExtra("isCancelled", true);
        else intent.putExtra("isCancelled", false);
        intent.putExtra("ZONE_FACTOR", zone_factor);
        intent.putExtra("COEF", coef);
        Bundle bundle = new Bundle();
        intent.putExtra("bundle", bundle);
        intent.putExtra("HRs", HRs);
        intent.putExtra("times", tmp);
        startActivity(intent);
    }

    public void onCalculate_elapsedTime() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                elapsed_time += 1;
                long now = System.currentTimeMillis();
                int time = (int) ((now - startTime) / 1000);
                int second = time % 60;
                int minute = time / 60 % 60;
                String str_minute, str_second;
                if(minute < 10) {
                    str_minute = "0" + minute;
                } else str_minute = minute + "";
                if(second < 10) {
                    str_second = "0" + second;
                } else str_second = second + "";
                text_elapsedTime.setText(str_minute + " : " + str_second);
                percentage = (float)(elapsed_time / (((1800 * zone_factor) + 360) / 100));
                progress_time.setProgress((int)percentage);

                distance += speed / 3600;
                text_distance.setText(String.format("%.1f", distance) + " km");

                if(elapsed_time % 10 != 0) {
                    if(crt_hr != 0) array_hr_10seconds.add(crt_hr);
                } else if(elapsed_time % 10 == 0) {
                    onCalculateCalorie();
                    array_hr_10seconds.clear();
                }

                if(elapsed_time >= 180 && elapsed_time < (1980 * zone_factor)) {
                    text_crt_state.clearAnimation();
                    text_crt_state.setVisibility(View.INVISIBLE);
                } else if(elapsed_time >= ((1800 * zone_factor) + 180) && elapsed_time < ((1800 * zone_factor) + 181)) {
                    text_crt_state.setVisibility(View.VISIBLE);
                    text_crt_state.startAnimation(anim2);
                    text_crt_state.setText("운동 후 3분 쿨다운~");
                }

                if(elapsed_time >= ((1800 * zone_factor) + 361)) {                                       //36분
                    writeDataToSerial(2);
                    onFinished();
                    finish();
                }
            }
        });
    }

    private void onShow_SpeedLimitationNotice() {
        img_ready_to_start.setVisibility(View.VISIBLE);
        img_ready_to_start.setImageResource(R.drawable.img_notice_speed);
        img_ready_to_start.startAnimation(anim1);
        isNoticed = true;
    }

    private void onCalculateCalorie() {
        // vo2max = 168.499 - (6.232 * 성별) - (0.712 * 나이) - (0.701 * 시간백분율) - (0.326 * BMI) - (0.923 * HR max 백분율), 성별 - 1 : 남, 2 : 여
        // 칼로리 = ((몸무게 * VO2max)/1000) * 시간(분) * 5, 5 : 산소 1리터 흡입 시 소모되는 칼로리
        float sum = 0, avg = 0, vo2max = Float.valueOf(user_vo2max), weight_ = Float.valueOf(weight);

        if(array_hr_10seconds.size() > 0) {
            for(int i = 0; i < array_hr_10seconds.size(); i++) {
                sum += array_hr_10seconds.get(i);
            }
            avg = sum / array_hr_10seconds.size();
        }

        float hr_zone_percentage = (avg - rest_hr) / (max_hr - rest_hr);
        float level_vo2max = (float)(hr_zone_percentage * (vo2max - 3.5));
        float level_vo2max_liter = (level_vo2max * weight_) / 1000;

        double tmp = level_vo2max_liter * 0.167 * 5;
        if(tmp > 0) calorie += tmp;

        text_calorie.setText((int)calorie + " kcal");
    }

    boolean isLoad_vo2max = false, isLoad_survey = false, isLoad_restHR = false, isLoad_zoneFactor = false, isUpdated_zoneFactor = false, isLoad_usrCrtProtocol = false;
    public void onLoad_UserVO2max() {
        isLoad_vo2max = true;
        ContentValues values = new ContentValues();
        values.put("HP", hp);
        values.put("LOCAL", local);
        values.put("PLACE", place);

        HttpPostDataClass_onLoadVO2max post = new HttpPostDataClass_onLoadVO2max("http://125.130.221.35:8001/PROC/AjaxForGetVo2Max.asp", values);
        post.execute();
    }

    public void onLoad_UserCrtProtocol() {
        isLoad_usrCrtProtocol = true;
        ContentValues values = new ContentValues();
        values.put("HP", hp);
        values.put("LOCAL", local);
        values.put("PLACE", place);

        HttpPostDataClass_onLoadHRProtocol post = new HttpPostDataClass_onLoadHRProtocol("http://125.130.221.35:8001/PROC/AjaxForGetHR_Protocol.asp", values);
        post.execute();
    }

    public void onLoad_UserSurvey() {
        isLoad_survey = true;
        ContentValues values = new ContentValues();
        values.put("HP", hp);
        values.put("LOCAL", local);
        values.put("PLACE", place);

        HttpPostDataClass_onLoadSurvey post = new HttpPostDataClass_onLoadSurvey("http://125.130.221.35:8001/PROC/AjaxForGetSurvey.asp", values);
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

    public void onLoad_UserZoneFactor() {
        isLoad_zoneFactor = true;
        ContentValues values = new ContentValues();
        values.put("HP", hp);
        values.put("LOCAL", local);
        values.put("PLACE", place);

        HttpPostDataClass_onLoadZonefactor post = new HttpPostDataClass_onLoadZonefactor("http://125.130.221.35:8001/PROC/AjaxForGetHR_Zone_Factor.asp", values);
        post.execute();
    }

    public void onLoad_SurveySpeedSum() {
        ContentValues values = new ContentValues();
        values.put("HP", hp);
        values.put("LOCAL", local);
        values.put("PLACE", place);

        HttpPostDataClass_onLoadSurveySpeedSum post = new HttpPostDataClass_onLoadSurveySpeedSum("http://125.130.221.35:8001/PROC/AjaxForGetSurveySpeedSum.asp", values);
        post.execute();
    }

    String[] tmp;
    String survey_incline, survey_zone_range, survey_protocol_version, survey_zone_time;
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
            if(user_vo2max != null && !user_vo2max.equals("")) {
                if(gender.equals("남")) {
                    onSetProtocol_men();
                } else {
                    onSetProtocol_women();
                }
                text_protocol.setText(protocol_level + " Ver." + protocol_version);
            } else {
                Toast.makeText(CustomProtocolActivity.this, "1.6km zone test를 먼저 진행해 주세요.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    String[] usr_protocol;
    class HttpPostDataClass_onLoadHRProtocol extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;

        public HttpPostDataClass_onLoadHRProtocol(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = null;
            RequestHttpURLConnection connection = new RequestHttpURLConnection();
            result = connection.request(url, values);
            if (result != null && isLoad_usrCrtProtocol && !result.equals("")) {
                usr_protocol = result.split("Ver.");
                isLoad_usrCrtProtocol = false;
            }
            return result;
        }
    }

    class HttpPostDataClass_onLoadSurvey extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;

        public HttpPostDataClass_onLoadSurvey(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = null;
            RequestHttpURLConnection connection = new RequestHttpURLConnection();
            result = connection.request(url, values);
            if(isLoad_survey) {
                tmp = result.split("</>");
                if(tmp.length == 0) {
                    return null;
                } else {
                    if(tmp.length == 1) {
                        String[] tmp1 = tmp[0].split("_");
                        if(tmp1[0].equals("경사")) survey_incline = tmp1[1];
                        else if(tmp1[0].equals("HRzone")) survey_zone_range = tmp1[1];
                        else if(tmp1[0].equals("버전")) survey_protocol_version = tmp1[1];
                        else if(tmp1[0].equals("운동시간")) survey_zone_time = tmp1[1];
                    } else if(tmp.length == 2) {
                        String[] tmp1 = tmp[0].split("_");
                        if(tmp1[0].equals("경사")) survey_incline = tmp1[1];
                        else if(tmp1[0].equals("HRzone")) survey_zone_range = tmp1[1];
                        else if(tmp1[0].equals("버전")) survey_protocol_version = tmp1[1];
                        else if(tmp1[0].equals("운동시간")) survey_zone_time = tmp1[1];

                        String[] tmp2 = tmp[1].split("_");
                        if(tmp2[0].equals("경사")) survey_incline = tmp2[1];
                        else if(tmp2[0].equals("HRzone")) survey_zone_range = tmp2[1];
                        else if(tmp2[0].equals("버전")) survey_protocol_version = tmp2[1];
                        else if(tmp2[0].equals("운동시간")) survey_zone_time = tmp2[1];
                    } else if(tmp.length == 3) {
                        String[] tmp1 = tmp[0].split("_");
                        if(tmp1[0].equals("경사")) survey_incline = tmp1[1];
                        else if(tmp1[0].equals("HRzone")) survey_zone_range = tmp1[1];
                        else if(tmp1[0].equals("버전")) survey_protocol_version = tmp1[1];
                        else if(tmp1[0].equals("운동시간")) survey_zone_time = tmp1[1];

                        String[] tmp2 = tmp[1].split("_");
                        if(tmp2[0].equals("경사")) survey_incline = tmp2[1];
                        else if(tmp2[0].equals("HRzone")) survey_zone_range = tmp2[1];
                        else if(tmp2[0].equals("버전")) survey_protocol_version = tmp2[1];
                        else if(tmp2[0].equals("운동시간")) survey_zone_time = tmp2[1];

                        String[] tmp3 = tmp[2].split("_");
                        if(tmp3[0].equals("경사")) survey_incline = tmp3[1];
                        else if(tmp3[0].equals("HRzone")) survey_zone_range = tmp3[1];
                        else if(tmp3[0].equals("버전")) survey_protocol_version = tmp3[1];
                        else if(tmp3[0].equals("운동시간")) survey_zone_time = tmp3[1];
                    }
                    isLoad_survey = false;
                }
            }

            return result;
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
                user_restHR = result;
                isLoad_restHR = false;
            }

            return result;
        }
    }

    class HttpPostDataClass_onLoadZonefactor extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;

        public HttpPostDataClass_onLoadZonefactor(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = null;
            RequestHttpURLConnection connection = new RequestHttpURLConnection();
            result = connection.request(url, values);
            if(isLoad_zoneFactor) {
                if(result != null && !result.equals("")) {
                    zone_factor = Float.parseFloat(result);
                    isUpdated_zoneFactor = true;
                }
                isLoad_zoneFactor = false;
            }

            return result;
        }
    }

    class HttpPostDataClass_onLoadSurveySpeedSum extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;

        public HttpPostDataClass_onLoadSurveySpeedSum(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = null;
            RequestHttpURLConnection connection = new RequestHttpURLConnection();
            result = connection.request(url, values);
            if (result != null && !result.equals("")) {
                coef = Integer.parseInt(result);
                if(coef > 50) coef = 50;
                else if(coef < -50) coef = -50;
            }
            return result;
        }
    }

    static String protocol_level;
    static int protocol_version = 4;
    String protocol;
    public void onSetProtocol_women() {
        int user_age = Integer.valueOf(age);
        float vo2max = Float.valueOf(user_vo2max);
        if(user_age >= 13 && user_age <= 19) {
            if(vo2max <= 24.9f) protocol_level = "매우낮은강도";
            else if(vo2max >= 25.0f && vo2max <= 30.9f) protocol_level = "낮은강도";
            else if(vo2max >= 31.0f && vo2max <= 38.9f) protocol_level = "중간강도";
            else if(vo2max >= 39.0f && vo2max <= 41.9f) protocol_level = "높은강도";
            else if(vo2max >= 42.0f) protocol_level = "매우높은강도";

        } else if(user_age >= 20 && user_age <= 29) {
            if(vo2max <= 23.5f) protocol_level = "매우낮은강도";
            else if(vo2max >= 23.6f && vo2max <= 28.9f) protocol_level = "낮은강도";
            else if(vo2max >= 29.0f && vo2max <= 36.9f) protocol_level = "중간강도";
            else if(vo2max >= 37.0f && vo2max <= 41.0f) protocol_level = "높은강도";
            else if(vo2max >= 41.1f) protocol_level = "매우높은강도";

        } else if(user_age >= 30 && user_age <= 39) {
            if(vo2max <= 22.7f) protocol_level = "매우낮은강도";
            else if(vo2max >= 22.8f && vo2max <= 26.9f) protocol_level = "낮은강도";
            else if(vo2max >= 27.0f && vo2max <= 35.6f) protocol_level = "중간강도";
            else if(vo2max >= 35.7f && vo2max <= 40.0f) protocol_level = "높은강도";
            else if(vo2max >= 40.1f) protocol_level = "매우높은강도";

        } else if(user_age >= 40 && user_age <= 49) {
            if(vo2max <= 20.9f) protocol_level = "매우낮은강도";
            else if(vo2max >= 21.0f && vo2max <= 24.4f) protocol_level = "낮은강도";
            else if(vo2max >= 24.5f && vo2max <= 32.8f) protocol_level = "중간강도";
            else if(vo2max >= 32.9f && vo2max <= 36.9f) protocol_level = "높은강도";
            else if(vo2max >= 37.0f) protocol_level = "매우높은강도";

        } else if(user_age >= 50 && user_age <= 59) {
            if(vo2max <= 20.1f) protocol_level = "매우낮은강도";
            else if(vo2max >= 20.2f && vo2max <= 22.7f) protocol_level = "낮은강도";
            else if(vo2max >= 22.8f && vo2max <= 31.4f) protocol_level = "중간강도";
            else if(vo2max >= 31.5f && vo2max <= 35.7f) protocol_level = "높은강도";
            else if(vo2max >= 35.8f) protocol_level = "매우높은강도";

        } else if(user_age >= 60) {
            if(vo2max <= 17.4f) protocol_level = "매우낮은강도";
            else if(vo2max >= 17.5f && vo2max <= 20.1f) protocol_level = "낮은강도";
            else if(vo2max >= 20.2f && vo2max <= 30.2f) protocol_level = "중간강도";
            else if(vo2max >= 30.3f && vo2max <= 31.4f) protocol_level = "높은강도";
            else if(vo2max >= 31.5f) protocol_level = "매우높은강도";
        }

        if(protocol_level.equals("매우낮은강도")) protocol = "lowest";
        else if(protocol_level.equals("낮은강도")) protocol = "low";
        else if(protocol_level.equals("중간강도")) protocol = "medium";
        else if(protocol_level.equals("높은강도")) protocol = "high";
        else if(protocol_level.equals("매우높은강도")) protocol = "highest";
    }

    public void onSetProtocol_men() {
        int user_age = Integer.valueOf(age);
        float vo2max = Float.valueOf(user_vo2max);
        if(user_age >= 13 && user_age <= 19) {
            if(vo2max <= 34.9f) protocol_level = "매우낮은강도";
            else if(vo2max >= 35.0f && vo2max <= 38.3f) protocol_level = "낮은강도";
            else if(vo2max >= 38.4f && vo2max <= 50.9f) protocol_level = "중간강도";
            else if(vo2max >= 51.0f && vo2max <= 55.9f) protocol_level = "높은강도";
            else if(vo2max >= 56.0f) protocol_level = "매우높은강도";

        } else if(user_age >= 20 && user_age <= 29) {
            if(vo2max <= 32.9f) protocol_level = "매우낮은강도";
            else if(vo2max >= 33.0f && vo2max <= 36.4f) protocol_level = "낮은강도";
            else if(vo2max >= 36.5f && vo2max <= 46.4f) protocol_level = "중간강도";
            else if(vo2max >= 46.5f && vo2max <= 52.4f) protocol_level = "높은강도";
            else if(vo2max >= 52.5f) protocol_level = "매우높은강도";

        } else if(user_age >= 30 && user_age <= 39) {
            if(vo2max <= 31.4f) protocol_level = "매우낮은강도";
            else if(vo2max >= 31.5f && vo2max <= 35.4f) protocol_level = "낮은강도";
            else if(vo2max >= 35.5f && vo2max <= 44.9f) protocol_level = "중간강도";
            else if(vo2max >= 45.0f && vo2max <= 49.4f) protocol_level = "높은강도";
            else if(vo2max >= 49.5f) protocol_level = "매우높은강도";

        } else if(user_age >= 40 && user_age <= 49) {
            if(vo2max <= 30.1f) protocol_level = "매우낮은강도";
            else if(vo2max >= 30.2f && vo2max <= 33.5f) protocol_level = "낮은강도";
            else if(vo2max >= 33.6f && vo2max <= 43.7f) protocol_level = "중간강도";
            else if(vo2max >= 43.8f && vo2max <= 48.0f) protocol_level = "높은강도";
            else if(vo2max >= 48.1f) protocol_level = "매우높은강도";

        } else if(user_age >= 50 && user_age <= 59) {
            if(vo2max <= 26.0f) protocol_level = "매우낮은강도";
            else if(vo2max >= 26.1f && vo2max <= 30.9f) protocol_level = "낮은강도";
            else if(vo2max >= 31.0f && vo2max <= 40.9f) protocol_level = "중간강도";
            else if(vo2max >= 41.0f && vo2max <= 45.3f) protocol_level = "높은강도";
            else if(vo2max >= 45.4f) protocol_level = "매우높은강도";

        } else if(user_age >= 60) {
            if(vo2max <= 20.4f) protocol_level = "매우낮은강도";
            else if(vo2max >= 20.5f && vo2max <= 26.0f) protocol_level = "낮은강도";
            else if(vo2max >= 26.1f && vo2max <= 36.4f) protocol_level = "중간강도";
            else if(vo2max >= 36.5f && vo2max <= 44.2f) protocol_level = "높은강도";
            else if(vo2max >= 44.3f) protocol_level = "매우높은강도";
        }

        if(protocol_level.equals("매우낮은강도")) protocol = "lowest";
        else if(protocol_level.equals("낮은강도")) protocol = "low";
        else if(protocol_level.equals("중간강도")) protocol = "medium";
        else if(protocol_level.equals("높은강도")) protocol = "high";
        else if(protocol_level.equals("매우높은강도")) protocol = "highest";
    }

    class WriteData extends AsyncTask<Void, Void, Void> {
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
    }

    @Override
    protected void onDestroy() {
        task = null;
        timer = null;
        task_crtTime = null;
        timer_crtTime = null;
        task_start_speed = null;
        timer_start_speed = null;
        task_warmingup_speed = null;
        timer_warmingup_speed = null;
        task_elapsed = null;
        timer_elapsed = null;
        writeData = null;

        unregisterReceiver(mUsbRecevier);
        zone_factor = 1;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() { }
}
