package com.exam.novelt3_1;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class OneMileTestResultActivity extends AppCompatActivity implements ConnectionObserver {
    TextView text_time, text_id, text_patch;
    TextView text_progress, text_cfi, text_elapsedTime, text_calorie, tab_hr_graph;
    TextView text_hr_max, text_hr_avg, text_speed_max, text_speed_avg, text_vo2max_user, text_vo2max_others;
    ImageView indicator_connection, indicator_battery;
    Button btn_out;
    ProgressBar progress_distance;

    RelativeLayout chart_hr, chart_speed, chart_vo2max;

    String patch_name, elapsed_time, calorie;
    float distance, max_speed, avg_speed;
    float max_hr, percentage, batteryRatio;
    ArrayList<Integer> HRs = new ArrayList<>();
    float[] distances = new float[] {};
    boolean isConnected;

    OneMile_HRZoneGraph HRChart;

    Timer timer_crtTime;
    TimerTask task_crtTime;

    double vo2max, cfi, avg_vo2max = 30;
    int elapsedTime, restHR, restHR_1min, restHR_2min, restHR_3min, avg_sitting_time;
    boolean isSmoking = false, isSmoking_past = false;

    String name, height, weight, gender, max_bp, min_bp, bmi, hp, start_date, start_time, local, place;
    int age, avg_hr, patch_id, crt_zone;

    float avg_speed1, avg_speed2, avg_speed3, avg_speed4, avg_speed5;
    int avg_hr_zone1, avg_hr_zone2, avg_hr_zone3, avg_hr_zone4, avg_hr_zone5;
    int zone_start, zone_time1, zone_time2, zone_time3, zone_time4, zone_time5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_mile_result);

        text_time = (TextView)findViewById(R.id.text_time_mile_result);
        task_crtTime = new TimerTask() {
            @Override
            public void run() {
                calc_time();
            }
        };
        timer_crtTime = new Timer();
        timer_crtTime.schedule(task_crtTime, 0, 1000);

        text_id = (TextView)findViewById(R.id.text_userid_mile_result);
        text_patch = (TextView)findViewById(R.id.text_patch_mile_result);
        text_progress = (TextView)findViewById(R.id.text_progress_mile_result);
        text_cfi = (TextView)findViewById(R.id.text_cfi);
        text_elapsedTime = (TextView)findViewById(R.id.text_time_mileresult);
        text_calorie = (TextView)findViewById(R.id.text_calorie_mileresult);
        tab_hr_graph = (TextView)findViewById(R.id.text_tab_hr_graph);
        //tab_normal_distribution = (TextView)findViewById(R.id.text_tab_normal_distribution);
        tab_hr_graph.setTextColor(Color.WHITE);
        btn_out = (Button)findViewById(R.id.btn_out_mile_result);
        //btn_print = (Button)findViewById(R.id.btn_result_print);
        progress_distance = (ProgressBar)findViewById(R.id.mile_result_progressbar);
        indicator_connection = (ImageView)findViewById(R.id.indicator_connection_mile_result);
        indicator_battery = (ImageView)findViewById(R.id.indicator_battery_mile_result);
        text_hr_max = (TextView)findViewById(R.id.text_hr_max_mile_result);
        text_hr_avg = (TextView)findViewById(R.id.text_hr_avg_mile_result);
        text_speed_max = (TextView)findViewById(R.id.text_speed_max_mile_result);
        text_speed_avg = (TextView)findViewById(R.id.text_speed_avg_mile_result);
        text_vo2max_user = (TextView)findViewById(R.id.text_vo2max_user_mile_result);
        text_vo2max_others = (TextView)findViewById(R.id.text_vo2max_others_mile_result);

        chart_hr = (RelativeLayout)findViewById(R.id.chart_hr);
        chart_speed = (RelativeLayout)findViewById(R.id.chart_speed);
        chart_vo2max = (RelativeLayout)findViewById(R.id.chart_vo2max);

        btn_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tab_hr_graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*tab_hr_graph.setTextColor(Color.WHITE);
                tab_normal_distribution.setTextColor(Color.rgb(175, 171, 171)); // #bebebe color
                onChangeFragment(1);*/
            }
        });

        /*tab_normal_distribution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tab_hr_graph.setTextColor(Color.rgb(175, 171, 171));
                tab_normal_distribution.setTextColor(Color.WHITE);
                onChangeFragment(2);
            }
        });*/

        patch_name = MainActivity.connected_patch_id;
        text_patch.setVisibility(View.VISIBLE);
        text_patch.setText(patch_name);
        if(!patch_name.equals("")) {
            indicator_connection.setVisibility(View.VISIBLE);
            indicator_battery.setVisibility(View.VISIBLE);
        }

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

        distance = intent.getExtras().getFloat("distance");
        percentage = intent.getExtras().getInt("percentage");
        max_hr = intent.getExtras().getInt("max_hr");
        max_speed = intent.getExtras().getFloat("max_speed");
        elapsed_time = intent.getExtras().getString("elapsed_time");
        elapsedTime = intent.getExtras().getInt("elapsedTime");
        calorie = intent.getExtras().getString("calorie");
        isSmoking = intent.getExtras().getBoolean("isSmoking");
        isSmoking_past = intent.getExtras().getBoolean("isSmoking_past");
        avg_sitting_time = intent.getExtras().getInt("sittingTime");
        start_date = intent.getExtras().getString("THIS_DATE");
        start_time = intent.getExtras().getString("START_TIME");
        restHR = intent.getExtras().getInt("REST_HR");
        avg_hr = intent.getExtras().getInt("AVG_HR");
        avg_speed = intent.getExtras().getFloat("AVG_SPEED");

        zone_start = intent.getExtras().getInt("ZONE_START");
        zone_time1 = intent.getExtras().getInt("ZONE_TIME1");
        zone_time2 = intent.getExtras().getInt("ZONE_TIME2");
        zone_time3 = intent.getExtras().getInt("ZONE_TIME3");
        zone_time4 = intent.getExtras().getInt("ZONE_TIME4");
        zone_time5 = intent.getExtras().getInt("ZONE_TIME5");

        avg_speed1 = intent.getExtras().getFloat("AVG_SPEED1");
        avg_speed2 = intent.getExtras().getFloat("AVG_SPEED2");
        avg_speed3 = intent.getExtras().getFloat("AVG_SPEED3");
        avg_speed4 = intent.getExtras().getFloat("AVG_SPEED4");
        avg_speed5 = intent.getExtras().getFloat("AVG_SPEED5");

        avg_hr_zone1 = intent.getExtras().getInt("AVG_HR1");
        avg_hr_zone2 = intent.getExtras().getInt("AVG_HR2");
        avg_hr_zone3 = intent.getExtras().getInt("AVG_HR3");
        avg_hr_zone4 = intent.getExtras().getInt("AVG_HR4");
        avg_hr_zone5 = intent.getExtras().getInt("AVG_HR5");

        patch_id = intent.getExtras().getInt("HPATCH");
        crt_zone = intent.getExtras().getInt("CRT_ZONE");

        Bundle b = intent.getExtras();
        HRs = b.getIntegerArrayList("HRs");
        distances = b.getFloatArray("distances");

        progress_distance.setProgress((int)percentage);
        text_progress.setVisibility(View.VISIBLE);
        text_progress.setText(String.format("%.1f", distance) + " km");
        text_elapsedTime.setText(elapsed_time);
        double tmp = Double.parseDouble(calorie);
        text_calorie.setText(String.format("%.0f", tmp) + " kcal");

        HRChart = (OneMile_HRZoneGraph)findViewById(R.id.HRGraph_mile_result);
        DPIHelper helper = new DPIHelper(getApplicationContext(), getWindowManager().getDefaultDisplay());
        HRChart.setTextSize((int)helper.getXDPI() / 16);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        HRChart.addAll(HRs, distances);
        onRest();
    }

    private void onRest() {
        final RestDialog_stop dialog = new RestDialog_stop(OneMileTestResultActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dlg) {
                restHR_1min = dialog.restHR_1min;
                restHR_2min = dialog.restHR_2min;
                restHR_3min = dialog.restHR_3min;
                init();
            }
        });
    }

    public void init() {
        onLoad_avgVO2max();
        calc_vo2max();
        calc_cfi();
        calc_steps();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int[] val_hr = new int[] {0, (int)max_hr, (int)avg_hr, 0};
                text_hr_max.setText(String.format("%.0f", max_hr));
                text_hr_avg.setText(String.valueOf(avg_hr));
                String[] yLabels = new String[] {" "};
                String[] xLabels = new String[] {" ", "Max", "Avg", " "};
                HR_Bar_Chart_GraphView barChart_HR = new HR_Bar_Chart_GraphView(getApplicationContext(), val_hr, " ", xLabels, yLabels, HR_Bar_Chart_GraphView.BAR);
                chart_hr.addView(barChart_HR);
                text_hr_max.bringToFront();
                text_hr_avg.bringToFront();

                float[] val_speed = new float[] {0f, max_speed, avg_speed, 0f};
                text_speed_max.setText(String.format("%.1f", max_speed));
                text_speed_avg.setText(String.format("%.1f", avg_speed));
                Speed_Bar_Chart_GraphView barchart_speed = new Speed_Bar_Chart_GraphView(getApplicationContext(), val_speed, " ", xLabels, yLabels, Speed_Bar_Chart_GraphView.BAR);
                chart_speed.addView(barchart_speed);
                text_speed_max.bringToFront();
                text_speed_avg.bringToFront();

                float[] val_vo2max = new float[] {0f, (float)vo2max, (float)avg_vo2max, 0f};
                text_vo2max_user.setText(String.format("%.1f", vo2max));
                text_vo2max_others.setText(String.format("%.1f", avg_vo2max));
                String[] xLabels_vo2max = new String[] {" ", "회원", "연령 평균", " "};
                VO2max_Bar_Chart_GraphView barchart_vo2max = new VO2max_Bar_Chart_GraphView(getApplicationContext(), val_vo2max, " ", xLabels_vo2max, yLabels, VO2max_Bar_Chart_GraphView.BAR);
                chart_vo2max.addView(barchart_vo2max);
                text_vo2max_user.bringToFront();
                text_vo2max_others.bringToFront();

                text_cfi.setText((int)cfi + "");

                calc_BMR();

                onSendData();
            }
        }, 800);
    }

    boolean isLoad_avgVO2max = false;
    public void onLoad_avgVO2max() {
        isLoad_avgVO2max = true;
        ContentValues values = new ContentValues();
        values.put("HP", hp);
        values.put("LOCAL", local);
        values.put("PLACE", place);

        HttpPostDataClass post = new HttpPostDataClass("http://125.130.221.35:8001/PROC/AjaxForGetVo2Max_Avg.asp", values);
        post.execute();
    }

    public void onChangeFragment(int index) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch(index) {
            case 1:
                HRChart.setVisibility(View.VISIBLE);
                Fragment_TabHRGraph fragment_tabHRGraph = new Fragment_TabHRGraph();
                transaction.replace(R.id.tab_framelayout, fragment_tabHRGraph);
                transaction.commit();
                break;

            case 2:
                HRChart.setVisibility(View.INVISIBLE);
                Fragment_TabNormalDistribution fragment_tabNormalDistribution = new Fragment_TabNormalDistribution();
                transaction.replace(R.id.tab_framelayout, fragment_tabNormalDistribution);
                transaction.commit();
                break;
        }
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

    float BMI;
    public void calc_vo2max() {
        int coef_gender;
        if(gender.equals("남")) coef_gender = 1;
        else coef_gender = 2;

        float time_percentage = (elapsedTime / 60);
        BMI = Float.parseFloat(weight) / ((Float.parseFloat(height) / 100) * (Float.parseFloat(height) / 100));

        switch(crt_zone) {
            case 0:
            case 1:
                vo2max = 0;
                break;
            case 2:
                vo2max = 106.286 - (3.961 * coef_gender) - (0.660 * age) - (0.07 * zone_time1) - (0.446 * BMI) - (0.245 * (max_hr / (220 - age) * 100));
                break;
            case 3:
                vo2max = 134.074 - (4.399 * coef_gender) - (0.766 * age) - (0.018 * zone_time2) - (0.433 * BMI) - (0.378 * (max_hr / (220 - age) * 100));
                break;
            case 4:
                vo2max = 184.697 - (5.550 * coef_gender) - (0.933 * age) - (0.013 * zone_time3) - (0.475 * BMI) - (0.630 * (max_hr / (220 - age) * 100));
                break;
            case 5:
                if(avg_hr_zone5 == 0)
                    vo2max = 256.885 - (6.507 * coef_gender) - (1.205 * age) - (0.025 * zone_time4) - (0.399 * BMI) - (0.974 * (max_hr / (220 - age) * 100));
                else vo2max = 168.499 - (6.232 * coef_gender) - (0.712 * age) - (0.701 * time_percentage) - (0.326 * BMI) - (0.923 * (max_hr / (220 - age) * 100));
                break;
        }
        // vo2max = 168.499 - (6.232 * 성별) - (0.712 * 나이) - (0.701 * 시간백분율) - (0.326 * BMI) - (0.923 * HR max 백분율)
    }

    int coef_smoking;
    public void calc_cfi() {
        int coef_gender;
        if(gender.equals("남")) coef_gender = 1;
        else coef_gender = 2;

        if(isSmoking) coef_smoking = 4;
        else if(isSmoking_past) coef_smoking = 2;
        else coef_smoking = 0;

        cfi = 74.772 + (7.845 * coef_gender) - (0.247 * age) - (0.193 * Float.parseFloat(bmi)) + (0.008 * Integer.parseInt(max_bp)) + (0.387 * vo2max) - (4.262 * coef_smoking) - (0.738 * avg_sitting_time) - (0.087 * (max_hr - restHR_1min));
        // cfi = 74.772 + (7.845 * 성별) - (0.247 * 나이) - (0.193 * 체지방률) + (0.008 * 수축혈압) + (0.387 * vo2max) - (4.262 * 흡연) - (0.738 * 좌식시간) - (0.087 * 1분휴식HR)
        if(crt_zone <= 1) {
            cfi = 0;
        } else if(crt_zone > 1 && crt_zone < 5) {
            cfi -= 6;
        }
    }

    int BMR;
    public void calc_BMR() {
        if(gender.equals("남")) {
            BMR = (int)(66.47 + (13.75 * Float.parseFloat(weight)) + (5 * Float.parseFloat(height)) - (6.76 * age));
        } else {
            BMR = (int)(655.1 + (9.56 * Float.parseFloat(weight)) + (1.85 * Float.parseFloat(height)) - (4.68 * age));
        }
    }

    @Override
    public void update(String name, boolean isConnect, int battery) {
        patch_name = name;
        isConnected = isConnect;
        batteryRatio = battery;

        if(patch_name != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text_patch.setVisibility(View.VISIBLE);
                    text_patch.setText(patch_name);
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

    int steps;
    public void calc_steps() {
        float step_width = Float.parseFloat(height) - 100;
        steps = (int)((distance * 1000) / step_width);
    }

    boolean onUpload = false;
    File directory;
    public void onSendData() {
        String path = Environment.getExternalStorageDirectory() + "/NovelT/1mile/";
        directory = new File(path);
        final File[] files = searchByFileFilter(directory);
        for(int i = 0; i < files.length; i++) {
            final int index = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(!onUpload) {
                        onUploadFTP_HR(files[index]);
                        onUpload = true;
                        files[index].delete();
                    }
                }
            }).start();
        }

        ContentValues values = new ContentValues();
        values.put("HP", hp);
        values.put("PROTOCOL_NAME", "1.6km zone test");
        values.put("THIS_DATE", start_date);
        values.put("START_TIME", start_time);
        values.put("MAX_HR", max_hr);
        values.put("MAX_SPEED", max_speed);
        values.put("VO2MAX", vo2max);
        values.put("CFI", (int)cfi);
        values.put("ELAPSED_TIME", elapsed_time);
        values.put("CALORIE", String.valueOf(calorie));
        values.put("REST_HR", restHR);
        values.put("BMI", String.format("%.1f", BMI));
        values.put("LOCAL", local);
        values.put("PLACE", place);
        values.put("SMOKING", coef_smoking);
        values.put("SEDENTARY_TIME", avg_sitting_time);
        values.put("ZONE_START", zone_start);
        values.put("ZONE_TIME1", zone_time1);
        values.put("ZONE_TIME2", zone_time2);
        values.put("ZONE_TIME3", zone_time3);
        values.put("ZONE_TIME4", zone_time4);
        values.put("ZONE_TIME5", zone_time5);
        values.put("AVG_SPEED1", avg_speed1);
        values.put("AVG_SPEED2", avg_speed2);
        values.put("AVG_SPEED3", avg_speed3);
        values.put("AVG_SPEED4", avg_speed4);
        values.put("AVG_SPEED5", avg_speed5);
        values.put("AVG_HR1", avg_hr_zone1);
        values.put("AVG_HR2", avg_hr_zone2);
        values.put("AVG_HR3", avg_hr_zone3);
        values.put("AVG_HR4", avg_hr_zone4);
        values.put("AVG_HR5", avg_hr_zone5);
        values.put("RECOVERY_HR1", restHR_1min);
        values.put("RECOVERY_HR2", restHR_2min);
        values.put("RECOVERY_HR3", restHR_3min);
        if(distance >= 1.6) values.put("CLEAR", true);
        else values.put("CLEAR", false);
        values.put("SENSOR", patch_id);
        values.put("BMR", BMR);
        values.put("STEPS", steps);

        HttpPostDataClass post = new HttpPostDataClass("http://125.130.221.35:8001/PROC/TestZone_Proc.asp", values);
        post.execute();
    }

    boolean isThere = false;
    public File[] searchByFileFilter(final File fileList) {
        File[] resultList = fileList.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File path, String name_) {
                isThere = true;
                String[] time = start_time.split("_");
                return name_.endsWith(name + "_" + time[0] + time[1] + time[2] + "_" + time[3] + time[4] + ".txt");
            }
        });
        return resultList;
    }

    FTPClient client;
    FileInputStream fis;
    private void onUploadFTP_HR(File file) {
        String ftpId = "administrator";
        String ftpPW = "$11!!aa$";
        int reply = 0;
        boolean isLogin = false;

        try {
            client = new FTPClient();
            client.setControlEncoding("euc-kr");
            client.connect("125.130.221.35");
            reply = client.getReplyCode();
            if(!FTPReply.isPositiveCompletion(reply)) {
                client.disconnect();
            }
            isLogin = client.login(ftpId, ftpPW);
            if(!isLogin) {
                client.logout();
            }
            client.setFileType(FTP.BINARY_FILE_TYPE);
            client.enterLocalActiveMode();
            client.changeWorkingDirectory("/TESTZONE");

            fis = new FileInputStream(file);
            onUpload = client.storeFile(file.getName(), fis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(fis != null) {
                try {
                    fis.close();
                    client.logout();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

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
            if(isLoad_avgVO2max) {
                if(result != null) avg_vo2max = Double.valueOf(result);
                isLoad_avgVO2max = false;
            }

            return result;
        }
    }

    @Override
    protected void onDestroy() {
        timer_crtTime = null;
        task_crtTime = null;

        File[] tmp = directory.listFiles();
        for(File child : tmp) {
            child.delete();
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() { }
}
