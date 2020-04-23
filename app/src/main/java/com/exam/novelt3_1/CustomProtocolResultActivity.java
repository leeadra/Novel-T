package com.exam.novelt3_1;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.Toast;

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

public class CustomProtocolResultActivity extends AppCompatActivity implements ConnectionObserver {
    TextView text_time, text_id, text_patch, text_protocol;
    TextView text_elapsedTime, text_distance, text_calorie, text_tab_hr_graph;
    TextView text_max_hr, text_avg_hr, text_max_speed, text_avg_speed;
    ImageView indicator_connection, indicator_battery;
    Button btn_out;
    ProgressBar progress_time;
    RelativeLayout barChart_hr, barChart_speed;
    TextView text_username_rank_gold, text_userlocation_rank_gold, text_userdistance_rank_gold;
    TextView text_username_rank_silver, text_userlocation_rank_silver, text_userdistance_rank_silver;
    TextView text_username_rank_cooper, text_userlocation_rank_cooper, text_userdistance_rank_cooper;
    TextView text_user_rank;

    String patchName, elapsed_time;
    float distance, max_speed, avg_speed, percentage;
    double calorie;
    int max_hr, avg_hr, batteryRatio;
    boolean isConnected, isCancelled;

    ArrayList<Integer> HRs = new ArrayList<>();
    long[] times = new long[] {};

    Custom_HRZoneGraph HRChart;

    Timer timer_crtTime;
    TimerTask task_crtTime;

    String name, height, weight, gender, hp, max_bp, min_bp, bmi, start_date, start_time, local, place;
    static String protocol_name;
    int age;
    String survey_incline, survey_duration_short, survey_zone_bad;
    int survey_speed = 0, coef, incline;
    float zone_factor;

    Publisher publisher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_protocol_result);

        text_time = (TextView)findViewById(R.id.text_time_custom_protocol_result);
        task_crtTime = new TimerTask() {
            @Override
            public void run() {
                calc_time();
            }
        };
        timer_crtTime = new Timer();
        timer_crtTime.schedule(task_crtTime, 0, 1000);

        text_id = (TextView)findViewById(R.id.text_userid_custom_protocol_result);
        text_patch = (TextView)findViewById(R.id.text_patch_custom_protocol_result);
        text_protocol = (TextView)findViewById(R.id.text_protocol_custom_protocol_result);

        text_elapsedTime = (TextView)findViewById(R.id.text_progress_custom_protocol_result);
        text_distance = (TextView)findViewById(R.id.text_distance_custom_protocol_result);
        text_calorie = (TextView)findViewById(R.id.text_calorie_custom_protocol_result);
        text_tab_hr_graph = (TextView)findViewById(R.id.text_tab_hr_graph_custom_protocol_result);
        text_tab_hr_graph.setTextColor(Color.WHITE);
        //text_tab_normal_distribution = (TextView)findViewById(R.id.text_tab_normal_distribution_custom_protocol_result);
        text_max_hr = (TextView)findViewById(R.id.text_hr_max_custom_result);
        text_avg_hr = (TextView)findViewById(R.id.text_hr_avg_custom_result);
        text_max_speed = (TextView)findViewById(R.id.text_speed_max_custom_result);
        text_avg_speed = (TextView)findViewById(R.id.text_speed_avg_custom_result);
        indicator_connection = (ImageView)findViewById(R.id.indicator_connection_custom_protocol_result);
        indicator_battery = (ImageView)findViewById(R.id.indicator_battery_custom_protocol_result);

        btn_out = (Button)findViewById(R.id.btn_out_custom_protocol_result);
        progress_time = (ProgressBar)findViewById(R.id.custom_protocol_result_progressbar);
        barChart_hr = (RelativeLayout)findViewById(R.id.custom_protocol_result_HRBar);
        barChart_speed = (RelativeLayout)findViewById(R.id.custom_protocol_result_SpeedBar);
        HRChart = (Custom_HRZoneGraph)findViewById(R.id.HRGraph_custom_result);

        text_username_rank_gold = (TextView)findViewById(R.id.text_username_rank_gold);
        text_userlocation_rank_gold = (TextView)findViewById(R.id.text_userlocation_rank_gold);
        text_userdistance_rank_gold = (TextView)findViewById(R.id.text_userdistance_rank_gold);
        text_username_rank_silver = (TextView)findViewById(R.id.text_username_rank_silver);
        text_userlocation_rank_silver = (TextView)findViewById(R.id.text_userlocation_rank_silver);
        text_userdistance_rank_silver = (TextView)findViewById(R.id.text_userdistance_rank_silver);
        text_username_rank_cooper = (TextView)findViewById(R.id.text_username_rank_cooper);
        text_userlocation_rank_cooper = (TextView)findViewById(R.id.text_userlocation_rank_cooper);
        text_userdistance_rank_cooper = (TextView)findViewById(R.id.text_userdistance_rank_cooper);
        text_user_rank = (TextView)findViewById(R.id.text_user_rank_custom_result);

        btn_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        text_tab_hr_graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*text_tab_hr_graph.setTextColor(Color.WHITE);
                text_tab_normal_distribution.setTextColor(Color.rgb(175, 171, 171)); // #bebebe color
                onChangeFragment(1);*/
            }
        });

        /*text_tab_normal_distribution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                text_tab_hr_graph.setTextColor(Color.rgb(175, 171, 171));
                text_tab_normal_distribution.setTextColor(Color.WHITE);
                onChangeFragment(2);
            }
        });*/

        patchName = MainActivity.connected_patch_id;
        text_patch.setVisibility(View.VISIBLE);
        text_patch.setText(patchName);
        if(!patchName.equals("")) {
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
        start_date = intent.getExtras().getString("START_DATE");
        start_time = intent.getExtras().getString("START_TIME");
        local = intent.getExtras().getString("LOCAL");
        place = intent.getExtras().getString("PLACE");

        distance = intent.getExtras().getFloat("distance");
        percentage = intent.getExtras().getFloat("percentage");
        max_hr = intent.getExtras().getInt("max_hr");
        avg_hr = intent.getExtras().getInt("avg_hr");
        max_speed = intent.getExtras().getFloat("max_speed");
        avg_speed = intent.getExtras().getFloat("avg_speed");
        incline = intent.getExtras().getInt("incline");
        elapsed_time = intent.getExtras().getString("elapsed_time");
        calorie = intent.getExtras().getDouble("calorie");
        isCancelled = intent.getExtras().getBoolean("isCancelled");
        protocol_name = intent.getExtras().getString("PROTOCOL_NAME");
        zone_factor = intent.getExtras().getFloat("ZONE_FACTOR");
        coef = intent.getExtras().getInt("COEF");
        Bundle b = intent.getExtras();
        HRs = b.getIntegerArrayList("HRs");
        times = b.getLongArray("times");

        progress_time.setProgress((int)percentage);
        text_elapsedTime.setVisibility(View.VISIBLE);
        text_elapsedTime.setText(elapsed_time);
        text_distance.setText(String.format("%.1f", distance) + " km");
        text_calorie.setText(String.format("%.1f", calorie) + " kcal");

        text_id.setText(name);
        text_protocol.setText(protocol_name);

        int[] val_hr = new int[] {0, max_hr, avg_hr, 0};
        text_max_hr.setText(String.valueOf(max_hr));
        text_avg_hr.setText(String.valueOf(avg_hr));
        String[] yLabels = new String[] {" "};
        String[] xLabels = new String[] {" ", "Max", "Avg", " "};
        HR_Bar_Chart_GraphView barChart_HR = new HR_Bar_Chart_GraphView(getApplicationContext(), val_hr, " ", xLabels, yLabels, HR_Bar_Chart_GraphView.BAR);
        barChart_hr.addView(barChart_HR);
        text_max_hr.bringToFront();
        text_avg_hr.bringToFront();

        float[] val_speed = new float[] {0f, max_speed, avg_speed, 0f};
        text_max_speed.setText(String.format("%.1f", max_speed));
        text_avg_speed.setText(String.format("%.1f", avg_speed));
        Speed_Bar_Chart_GraphView barchart_speed = new Speed_Bar_Chart_GraphView(getApplicationContext(), val_speed, " ", xLabels, yLabels, Speed_Bar_Chart_GraphView.BAR);
        barChart_speed.addView(barchart_speed);
        text_max_speed.bringToFront();
        text_avg_speed.bringToFront();

        DPIHelper helper = new DPIHelper(getApplicationContext(), getWindowManager().getDefaultDisplay());
        HRChart.setTextSize((int)helper.getXDPI() / 16);
        HRChart.addAll(HRs, times);

        calc_steps();

        onLoadUserRank();

        publisher = MainActivity.publisher;
        publisher.addObserver(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(isCancelled) {
            onShowSurvey_cancelled();
        } else {
            onShowSurvey();
        }
    }

    public void onChangeFragment(int index) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch(index) {
            case 1:
                HRChart.setVisibility(View.VISIBLE);
                Fragment_TabHRGraph_custom_protocol fragment_tabHRGraph = new Fragment_TabHRGraph_custom_protocol();
                transaction.replace(R.id.tab_framelayout_custom_protocol_result, fragment_tabHRGraph);
                transaction.commit();
                break;

            case 2:
                HRChart.setVisibility(View.INVISIBLE);
                Fragment_TabNormalDistribution_custom_protocol fragment_tabNormalDistribution = new Fragment_TabNormalDistribution_custom_protocol();
                transaction.replace(R.id.tab_framelayout_custom_protocol_result, fragment_tabNormalDistribution);
                transaction.commit();
                break;
        }
    }

    public void onShowSurvey_cancelled() {
        final SurveyDialog_Cancelled dialog = new SurveyDialog_Cancelled(CustomProtocolResultActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dlg) {
                if(dialog.getSurvey_incline() != null) {
                    survey_incline = dialog.getSurvey_incline();
                    survey_speed = 0;
                    survey_zone_bad = null;
                }
                else if(dialog.getSurvey_speed() != 0) {
                    survey_speed = dialog.getSurvey_speed();
                    if(coef > 50 || coef < -50) {
                        Toast.makeText(getApplicationContext(), "최대 적용 횟수 10번을 초과하여 더이상 적용되지 않습니다.", Toast.LENGTH_LONG).show();
                    }
                    survey_incline = null;
                    survey_zone_bad = null;
                }
                else if(dialog.getSurvey_zone_period() != null) {
                    survey_zone_bad = dialog.getSurvey_zone_period();
                    zone_factor -= 0.1;
                    survey_incline = null;
                    survey_speed = 0;
                }
                else if(dialog.getSurvey_zone_dynamic() != null) {
                    survey_zone_bad = dialog.getSurvey_zone_dynamic();
                    survey_incline = null;
                    survey_speed = 0;
                }
                else if(dialog.getSurvey_zone_simple() != null) {
                    survey_zone_bad = dialog.getSurvey_zone_simple();
                    survey_incline = null;
                    survey_speed = 0;
                }

                onSendData();
            }
        });
    }

    public void onShowSurvey() {
        final SurveyDialog_Finished dialog = new SurveyDialog_Finished(CustomProtocolResultActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dlg) {
                if(dialog.getSurvey_speed() != 0) {
                    survey_speed = dialog.getSurvey_speed();
                    if(coef > 50 || coef < -50) {
                        Toast.makeText(getApplicationContext(), "최대 적용 횟수 10번을 초과하여 더이상 적용되지 않습니다.", Toast.LENGTH_LONG).show();
                    }
                } else survey_speed = 0;

                if(dialog.getSurvey_incline() != null) {
                    survey_incline = dialog.getSurvey_incline();
                } else survey_incline = null;

                if(dialog.getSurvey_zone_duration() != null) {
                    survey_duration_short = dialog.getSurvey_zone_duration();
                    zone_factor += 0.1;
                    survey_zone_bad = null;
                } else survey_duration_short = null;

                if(dialog.getSurvey_zone_bad() != null) {
                    survey_zone_bad = dialog.getSurvey_zone_bad();
                    if(survey_zone_bad.equals("운동시간_-10%")) zone_factor -= 0.1;
                    survey_duration_short = null;
                } else survey_zone_bad = null;

                onSendData();
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

    /*int BMR;
    public void calc_BMR() {
        if(gender.equals("남")) {
            BMR = (int)(66.47 + (13.75 * Float.parseFloat(weight)) + (5 * Float.parseFloat(height)) - (6.76 * age));
        } else {
            BMR = (int)(655.1 + (9.56 * Float.parseFloat(weight)) + (1.85 * Float.parseFloat(height)) - (4.68 * age));
        }
    }*/

    int steps;
    public void calc_steps() {
        float step_width = Float.parseFloat(height) - 100;
        steps = (int)((distance * 1000) / step_width);
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

    double custom_rank = 0;
    public void calc_custom_rank() {
        double tmp = 16.0 - avg_speed;
        double pre_score = tmp * 5;
        custom_rank = 100.0 - pre_score;
    }

    boolean onUpload = false;
    File directory;
    public void onSendData() {
        String path = Environment.getExternalStorageDirectory() + "/NovelT/Custom/";
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
                    }
                }
            }).start();
        }

        ContentValues values = new ContentValues();
        values.put("HP", hp);
        values.put("PROTOCOL_NAME", text_protocol.getText().toString());
        values.put("THIS_DATE", start_date);
        values.put("START_TIME", start_time);
        String tmp = String.format("%.1f", distance);
        values.put("DISTANCE", Float.valueOf(tmp));
        values.put("MAX_HR", max_hr);
        values.put("AVG_HR", avg_hr);
        tmp = String.format("%.1f", max_speed);
        values.put("MAX_SPEED", Float.valueOf(tmp));
        tmp = String.format("%.1f", avg_speed);
        values.put("AVG_SPEED", Float.valueOf(tmp));
        values.put("CALORIE", (int)calorie);
        if(percentage >= 100) percentage = 100;
        values.put("RATE", percentage);
        values.put("SENSOR", Integer.parseInt(patchName));
        values.put("STEPS", steps);
        values.put("ZONE_FACTOR", zone_factor);
        values.put("LOCAL", local);
        values.put("PLACE", place);

        boolean clear = false;
        if(percentage == 100) clear = true;
        values.put("CLEAR", clear);
        String tmp_incline = "", tmp_zone = "";
        values.put("SURVEY_SPEED", survey_speed);
        if(survey_incline != null) tmp_incline = survey_incline;
        values.put("SURVEY_INCLINE", tmp_incline);

        if(survey_duration_short != null) tmp_zone = survey_duration_short;
        else if(survey_zone_bad != null) tmp_zone = survey_zone_bad;
        values.put("SURVEY_ZONE", tmp_zone);

        values.put("ELAPSED_TIME", "00:" + text_elapsedTime.getText().toString());
        values.put("SLOPE", incline);
        values.put("TOTAL_RANK", 70);

        HttpPostDataClass post = new HttpPostDataClass("http://125.130.221.35:8001/PROC/HrZone_Proc.asp", values);
        post.execute();
    }

    public File[] searchByFileFilter(final File fileList) {
        File[] resultList = fileList.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File path, String name_) {
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
            client.changeWorkingDirectory("/HRZONE");

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

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            onLoadRanking(protocol_name);
        }
    }

    boolean isLoad_ranking = false;
    public void onLoadRanking(String protocol) {
        isLoad_ranking = true;
        ContentValues values = new ContentValues();
        values.put("PROTOCOL_NAME", protocol);

        HttpPostDataClass_onLoadRanking post = new HttpPostDataClass_onLoadRanking("http://125.130.221.35:8001/PROC/AjaxForGetHR_Protocol_Total_Rank.asp", values);
        post.execute();
    }

    String goldRank_name = "", goldRank_place = "", goldRank_distance = "";
    String silverRank_name = "", silverRank_place = "", silverRank_distance = "";
    String cooperRank_name = "", cooperRank_place = "", cooperRank_distance = "";
    class HttpPostDataClass_onLoadRanking extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;

        public HttpPostDataClass_onLoadRanking(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = null;
            RequestHttpURLConnection connection = new RequestHttpURLConnection();
            result = connection.request(url, values);
            if(isLoad_ranking) {
                if(result != null && !result.equals("")) {
                    String[] rank = result.split("<\\|>");                   // 순위 구분
                    if(rank.length == 1) {                      // 1등만 있는 경우
                        String[] rankData = rank[0].split("</>");
                        goldRank_name = rankData[4];
                        goldRank_place = rankData[2];
                        goldRank_distance = rankData[3];
                    } else if(rank.length == 2) {               // 2등까지만 있는 경우
                        String[] rankData1 = rank[0].split("</>");
                        if(rankData1[0].equals("1")) {
                            goldRank_name = rankData1[4];
                            goldRank_place = rankData1[2];
                            goldRank_distance = rankData1[3];
                        } else if(rankData1[0].equals("2")) {
                            silverRank_name = rankData1[4];
                            silverRank_place = rankData1[2];
                            silverRank_distance = rankData1[3];
                        }
                        String[] rankData2 = rank[1].split("</>");
                        if(rankData2[0].equals("1")) {
                            goldRank_name = rankData2[4];
                            goldRank_place = rankData2[2];
                            goldRank_distance = rankData2[3];
                        } else if(rankData2[0].equals("2")) {
                            silverRank_name = rankData2[4];
                            silverRank_place = rankData2[2];
                            silverRank_distance = rankData2[3];
                        }
                    } else if(rank.length == 3) {               // 3등까지 있는 경우
                        String[] rankData1 = rank[0].split("</>");
                        if(rankData1[0].equals("1")) {
                            goldRank_name = rankData1[4];
                            goldRank_place = rankData1[2];
                            goldRank_distance = rankData1[3];
                        } else if(rankData1[0].equals("2")) {
                            silverRank_name = rankData1[4];
                            silverRank_place = rankData1[2];
                            silverRank_distance = rankData1[3];
                        } else if(rankData1[0].equals("3")) {
                            cooperRank_name = rankData1[4];
                            cooperRank_place = rankData1[2];
                            cooperRank_distance = rankData1[3];
                        }
                        String[] rankData2 = rank[1].split("</>");
                        if(rankData2[0].equals("1")) {
                            goldRank_name = rankData2[4];
                            goldRank_place = rankData2[2];
                            goldRank_distance = rankData2[3];
                        } else if(rankData2[0].equals("2")) {
                            silverRank_name = rankData2[4];
                            silverRank_place = rankData2[2];
                            silverRank_distance = rankData2[3];
                        } else if(rankData2[0].equals("3")) {
                            cooperRank_name = rankData2[4];
                            cooperRank_place = rankData2[2];
                            cooperRank_distance = rankData2[3];
                        }
                        String[] rankData3 = rank[2].split("</>");
                        if(rankData3[0].equals("1")) {
                            goldRank_name = rankData3[4];
                            goldRank_place = rankData3[2];
                            goldRank_distance = rankData3[3];
                        } else if(rankData3[0].equals("2")) {
                            silverRank_name = rankData3[4];
                            silverRank_place = rankData3[2];
                            silverRank_distance = rankData3[3];
                        } else if(rankData3[0].equals("3")) {
                            cooperRank_name = rankData3[4];
                            cooperRank_place = rankData3[2];
                            cooperRank_distance = rankData3[3];
                        }
                    }
                }
                isLoad_ranking = false;
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            text_username_rank_gold.setText(goldRank_name);
            text_userlocation_rank_gold.setText(goldRank_place);
            if(!goldRank_distance.equals("")) text_userdistance_rank_gold.setText(goldRank_distance + " km");
            else text_userdistance_rank_gold.setText("");

            text_username_rank_silver.setText(silverRank_name);
            text_userlocation_rank_silver.setText(silverRank_place);
            if(!silverRank_distance.equals("")) text_userdistance_rank_silver.setText(silverRank_distance + " km");
            else text_userdistance_rank_silver.setText("");

            text_username_rank_cooper.setText(cooperRank_name);
            text_userlocation_rank_cooper.setText(cooperRank_place);
            if(!cooperRank_distance.equals("")) text_userdistance_rank_cooper.setText(cooperRank_distance + " km");
            else text_userdistance_rank_cooper.setText("");
        }
    }

    public void onLoadUserRank() {
        ContentValues values = new ContentValues();
        values.put("HP", hp);
        values.put("THIS_DATE", start_date);
        values.put("PROTOCOL_NAME", protocol_name);
        values.put("LOCAL", local);
        values.put("PLACE", place);

        HttpPostDataClass_onLoadUserRank post = new HttpPostDataClass_onLoadUserRank("http://125.130.221.35:8001/PROC/AjaxForGetHR_Zone_ProtocolRank.asp", values);
        post.execute();
    }

    class HttpPostDataClass_onLoadUserRank extends AsyncTask<Void, Void, String> {
        String url, usr_rank;
        ContentValues values;

        public HttpPostDataClass_onLoadUserRank(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = null;
            RequestHttpURLConnection connection = new RequestHttpURLConnection();
            result = connection.request(url, values);
            if(result != null && !result.equals("")) {
                String[] tmp = result.split("/");
                usr_rank = tmp[0];
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            text_user_rank.setText(usr_rank + " 위");
        }
    }

    @Override
    public void onBackPressed() { }

    @Override
    protected void onDestroy() {
        File[] tmp1 = directory.listFiles();
        for(File child : tmp1) {
            child.delete();
        }
        super.onDestroy();
        CustomProtocolActivity.isStarted = false;
        CustomProtocolActivity.params = null;
        CustomProtocolActivity.zone_factor = 0;
    }
}
