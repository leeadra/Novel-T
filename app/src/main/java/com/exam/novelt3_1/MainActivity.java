package com.exam.novelt3_1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.slsi.HPatch;
import com.samsung.slsi.HPatchAlgorithmResultManager;
import com.samsung.slsi.HPatchECGDataManager;
import com.samsung.slsi.HPatchError;
import com.samsung.slsi.HPatchHealthDataObserver;
import com.samsung.slsi.HPatchStatusObserver;
import com.samsung.slsi.HPatchValue;
import com.samsung.slsi.HPatchValueContainer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements TargetDeviceManager, Publisher, HRPublisher {
    TextView text_time;
    TextView text_id;
    static TextView text_patch;
    TextView text_comments;
    static ImageView indicator_connection, indicator_battery;
    Button btn_connection, btn_history, btn_setting, btn_logout;
    LinearLayout layout_1mile_test, layout_hr_zone_protocol, layout_free_running, layout_game_zone;

    Timer timer;
    TimerTask task;

    static Application app;
    Activity activity;

    HPatchController supporter, supporter_treadmill;
    static HPatch hPatch_connected, hTreadmill_connected;

    List<TargetDeviceObserver> targetDeviceObservers = new ArrayList<>();
    static String connected_patch_id, connected_treadmill_id;

    ArrayList<ConnectionObserver> observers_connection;
    boolean isConnected = false;
    int ratio_battery;
    static Publisher publisher;

    ArrayList<HeartRateObserver> observers_hr;
    static HRPublisher publisher_hr;

    int crt_hr;

    String hp, name, height, weight, age, gender, max_bp, min_bp, fat_percent, local, place;
    int usr_age;
    long mlastClickTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        task = new TimerTask() {
            @Override
            public void run() {
                calc_time();
            }
        };
        timer = new Timer();
        timer.schedule(task, 0, 1000);

        text_time = (TextView)findViewById(R.id.text_time_main);

        text_id = (TextView)findViewById(R.id.text_userid);
        text_patch = (TextView)findViewById(R.id.text_patch);
        text_comments = (TextView)findViewById(R.id.text_comments);

        indicator_connection = (ImageView)findViewById(R.id.indicator_connection);
        indicator_battery = (ImageView)findViewById(R.id.indicator_battery);

        btn_connection = (Button)findViewById(R.id.btn_connection);
        btn_history = (Button)findViewById(R.id.btn_history);
        btn_setting = (Button)findViewById(R.id.btn_setting);
        btn_logout = (Button)findViewById(R.id.btn_logout);

        layout_1mile_test = (LinearLayout)findViewById(R.id.layout_1mile_test);
        layout_hr_zone_protocol = (LinearLayout)findViewById(R.id.layout_hr_zone_protocol);
        layout_free_running = (LinearLayout)findViewById(R.id.layout_free_running);
        layout_game_zone = (LinearLayout)findViewById(R.id.layout_game_zone);

        Intent intent = getIntent();
        name = intent.getExtras().getString("NAME");
        height = intent.getExtras().getString("HEIGHT");
        weight = intent.getExtras().getString("WEIGHT");
        age = intent.getExtras().getString("DATE_OF_BIRTH");
        String usr_year = age.substring(0, 4);
        String usr_month = age.substring(4, 6);
        String usr_day = age.substring(6, 8);
        Calendar current = Calendar.getInstance();
        int crt_year = current.get(Calendar.YEAR);
        int crt_month = current.get(Calendar.MONTH);
        int crt_day = current.get(Calendar.DAY_OF_MONTH);
        usr_age = (crt_year - Integer.parseInt(usr_year));
        if((Integer.parseInt(usr_month) * 100 + Integer.parseInt(usr_day)) > (crt_month * 100 + crt_day)) usr_age -= 1;

        gender = intent.getExtras().getString("GENDER");
        max_bp = intent.getExtras().getString("MAX_BP");
        min_bp = intent.getExtras().getString("MIN_BP");
        fat_percent = intent.getExtras().getString("FAT_PERCENT");
        hp = intent.getExtras().getString("HP");
        local = intent.getExtras().getString("LOCAL");
        place = intent.getExtras().getString("PLACE");

        text_id.setText(name);

        btn_connection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SystemClock.elapsedRealtime() - mlastClickTime < 1000) {
                    return;
                }
                mlastClickTime = SystemClock.elapsedRealtime();
                onShow(view);
            }
        });

        btn_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SystemClock.elapsedRealtime() - mlastClickTime < 1000) {
                    return;
                }
                mlastClickTime = SystemClock.elapsedRealtime();

                Intent intent = new Intent(getApplicationContext(), HistoryCalendarActivity.class);
                intent.putExtra("HP", hp);
                intent.putExtra("NAME", name);
                intent.putExtra("HEIGHT", height);
                intent.putExtra("WEIGHT", weight);
                intent.putExtra("AGE", usr_age);
                intent.putExtra("GENDER", gender);
                intent.putExtra("LOCAL", local);
                intent.putExtra("PLACE", place);
                startActivity(intent);
            }
        });

        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {    // 서버에서 회원정보 받아서 회원등록 화면에 보여주기
                if(SystemClock.elapsedRealtime() - mlastClickTime < 1000) {
                    return;
                }
                mlastClickTime = SystemClock.elapsedRealtime();

                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                intent.putExtra("HP", hp);
                intent.putExtra("NAME", name);
                intent.putExtra("HEIGHT", height);
                intent.putExtra("WEIGHT", weight);
                intent.putExtra("BIRTHDAY", age);
                intent.putExtra("GENDER", gender);
                intent.putExtra("MAX_BP", max_bp);
                intent.putExtra("MIN_BP", min_bp);
                intent.putExtra("FATNESS", fat_percent);
                intent.putExtra("LOCAL", local);
                intent.putExtra("PLACE", place);
                startActivityForResult(intent, 2);
            }
        });

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SystemClock.elapsedRealtime() - mlastClickTime < 1000) {
                    return;
                }
                mlastClickTime = SystemClock.elapsedRealtime();

                if(hPatch_connected != null) {
                    dialog = new ProgressDialog(MainActivity.this);
                    new LogoutProgressDialog().execute("Logout");
                } else {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        layout_1mile_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SystemClock.elapsedRealtime() - mlastClickTime < 1000) {
                    return;
                }
                mlastClickTime = SystemClock.elapsedRealtime();

                if(hPatch_connected != null) {
                    Intent intent = new Intent(getApplicationContext(), OneMileTestActivity.class);
                    intent.putExtra("HP", hp);
                    intent.putExtra("NAME", name);
                    intent.putExtra("HEIGHT", height);
                    intent.putExtra("WEIGHT", weight);
                    intent.putExtra("AGE", usr_age);
                    intent.putExtra("GENDER", gender);
                    intent.putExtra("MAX_BP", max_bp);
                    intent.putExtra("MIN_BP", min_bp);
                    intent.putExtra("BMI", fat_percent);
                    intent.putExtra("HPATCH", hPatch_connected.getId());
                    intent.putExtra("LOCAL", local);
                    intent.putExtra("PLACE", place);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "센서 연결을 해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        layout_hr_zone_protocol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SystemClock.elapsedRealtime() - mlastClickTime < 1000) {
                    return;
                }
                mlastClickTime = SystemClock.elapsedRealtime();

                if(hPatch_connected != null) {
                    Intent intent = new Intent(getApplicationContext(), HRZoneProtocolActivity.class);
                    intent.putExtra("HP", hp);
                    intent.putExtra("NAME", name);
                    intent.putExtra("HEIGHT", height);
                    intent.putExtra("WEIGHT", weight);
                    intent.putExtra("AGE", usr_age);
                    intent.putExtra("GENDER", gender);
                    intent.putExtra("MAX_BP", max_bp);
                    intent.putExtra("MIN_BP", min_bp);
                    intent.putExtra("BMI", fat_percent);
                    intent.putExtra("LOCAL", local);
                    intent.putExtra("PLACE", place);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "센서 연결을 해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        layout_free_running.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SelfControlActivity.class);
                intent.putExtra("HP", hp);
                intent.putExtra("NAME", name);
                intent.putExtra("HEIGHT", height);
                intent.putExtra("WEIGHT", weight);
                intent.putExtra("AGE", usr_age);
                intent.putExtra("GENDER", gender);
                intent.putExtra("MAX_BP", max_bp);
                intent.putExtra("MIN_BP", min_bp);
                intent.putExtra("BMI", fat_percent);
                intent.putExtra("LOCAL", local);
                intent.putExtra("PLACE", place);
                startActivity(intent);
            }
        });

        layout_game_zone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "서비스 준비중입니다.", Toast.LENGTH_SHORT).show();
            }
        });

        app = Application.getInstance();
        observers_connection = new ArrayList<>();
        publisher = this;

        observers_hr = new ArrayList<>();
        publisher_hr = this;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        activity = this;
        try {
            app.initialize(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onShow(View view) {
        final DeviceDialog dialog = new DeviceDialog(view.getContext(), app.gethPatchManager());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                hPatch_connected = dialog.gethPatch();
                if(hPatch_connected != null) {
                    try {
                        supporter = new HPatchController(hPatch_connected, healthDataObserver);
                        hPatch_connected.addStatusObserver(statusObserver);
                        app.setupHPatch(hPatch_connected, supporter);
                        broadcastTargetDeviceChanged(hPatch_connected);
                        connected_patch_id = dialog.getConnected_patch_id();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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

    @Override
    public void addTargetDeviceObserver(TargetDeviceObserver observer) {
        synchronized(targetDeviceObservers) {
            targetDeviceObservers.add(observer);
        }
    }

    @Override
    public void removeTargetDeviceObserver(TargetDeviceObserver observer) {
        synchronized(targetDeviceObservers) {
            targetDeviceObservers.remove(observer);
        }
    }

    private void broadcastTargetDeviceChanged(final HPatch hPatch) {
        synchronized (targetDeviceObservers) {
            for (TargetDeviceObserver observer : targetDeviceObservers) {
                try {
                    observer.onTargetDeviceChanged(hPatch);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void addObserver(ConnectionObserver observer) {
        observers_connection.add(observer);
    }

    @Override
    public void removeObserver(ConnectionObserver observer) {
        int index = observers_connection.indexOf(observer);
        observers_connection.remove(index);
    }

    @Override
    public void notifyObserver() {
        for(ConnectionObserver ob : observers_connection) {
            ob.update(connected_patch_id, isConnected, ratio_battery);
        }
    }

    @Override
    public void addObserver(HeartRateObserver observer) {
        observers_hr.add(observer);
    }

    @Override
    public void removeObserver(HeartRateObserver observer) {
        int index = observers_hr.indexOf(observer);
        observers_hr.remove(index);
    }

    @Override
    public void notifyObserver_HR() {
        for(HeartRateObserver ob : observers_hr) {
            ob.update(crt_hr);
        }
    }

    public HPatchStatusObserver statusObserver = new HPatchStatusObserver() {
        @Override
        public void updateSPatchDeviceInformation(HPatch hPatch) {

        }

        @Override
        public void updateBatteryRatio(HPatch hPatch, final int batteryRatio) {
            ratio_battery = batteryRatio;
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    indicator_battery.setVisibility(View.VISIBLE);
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
                    notifyObserver();
                }
            });
        }

        @Override
        public void updateBLEConnectionStatus(final HPatch hPatch, final boolean isBLEConnected) {
            isConnected = isBLEConnected;
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(isBLEConnected) {
                        hPatch_connected = hPatch;
                        connected_patch_id = String.valueOf(hPatch.getId());
                        text_patch.setVisibility(View.VISIBLE);
                        text_patch.setText(connected_patch_id);
                        indicator_connection.setVisibility(View.VISIBLE);
                    } else {
                        hPatch_connected = null;
                        text_patch.setText("");
                        indicator_connection.setVisibility(View.INVISIBLE);
                        indicator_battery.setVisibility(View.INVISIBLE);
                        connected_patch_id = "";
                    }
                    notifyObserver();
                }
            });
        }

        @Override
        public void updateLeadContactStatus(HPatch hPatch, int leadStatus) { }
        @Override
        public void onError(HPatch hPatch, HPatchError id, String message) { }
    };

    public HPatchHealthDataObserver healthDataObserver = new HPatchHealthDataObserver() {
        @Override
        public void onHPatchRRIDataUpdated(int[] rriData) { }
        @Override
        public void onHPatchECGDataUpdated(HPatchECGDataManager ecgDataManager, HPatch hPatch, int sequence, int[] ecgSignalData) { }
        @Override
        public void onHPatchAlgorithmResultUpdated(HPatchAlgorithmResultManager hPatchAlgorithmResultManager, HPatchValueContainer result) {
            if(result != null) {
                final HPatchValue value = result.getValue("HeartRate");
                if(value != null) {
                    crt_hr = value.getValueAsInteger();
                    notifyObserver_HR();
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && requestCode == 2) {
            Toast.makeText(MainActivity.this, "회원정보가 수정되었습니다. 다시 로그인 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() { }

    @Override
    protected void onDestroy() {
        if(dialog != null) dialog.dismiss();
        publisher = null;
        publisher_hr = null;
        super.onDestroy();
    }

    ProgressDialog dialog;
    class LogoutProgressDialog extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("로그아웃 중입니다...");

            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            app.clearHPatch(hPatch_connected);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
