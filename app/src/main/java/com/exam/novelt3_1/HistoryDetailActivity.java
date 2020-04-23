package com.exam.novelt3_1;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class HistoryDetailActivity extends Activity {
    TextView text_crt_time, text_start_time, text_protocol_name, text_hr_max, text_speed_max, text_calorie,
            text_hr_avg, text_speed_avg, text_distance;
    Button btn_goOut;
    Custom_HRZoneGraph HRGraph;
    ProgressBar loading_hrGraph;

    TimerTask task;
    Timer timer;

    String hp, name, height, weight, gender, start_time, protocol_name, max_hr, avg_hr, max_speed, avg_speed, calorie, distance;
    int age;
    int std_fourty, std_fifty, std_sixty, std_seventy, std_eighty, std_ninety, std_hundred;
    float zone_factor = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        text_crt_time = (TextView)findViewById(R.id.text_time_history_detail);
        task = new TimerTask() {
            @Override
            public void run() {
                calc_time();
            }
        };
        timer = new Timer();
        timer.schedule(task, 0, 1000);

        text_start_time = (TextView)findViewById(R.id.text_start_time_history_detail);
        text_protocol_name = (TextView)findViewById(R.id.text_protocol_name_history_detail);
        text_hr_max = (TextView)findViewById(R.id.text_hr_max_history_detail);
        text_speed_max = (TextView)findViewById(R.id.text_max_speed_history_detail);
        text_calorie = (TextView)findViewById(R.id.text_calorie_history_detail);
        text_hr_avg = (TextView)findViewById(R.id.text_hr_avg_history_detail);
        text_speed_avg = (TextView)findViewById(R.id.text_avg_speed_history_detail);
        text_distance = (TextView)findViewById(R.id.text_distance_history_detail);
        btn_goOut = (Button)findViewById(R.id.btn_goOut_history_detail);
        HRGraph = (Custom_HRZoneGraph)findViewById(R.id.HRGraph_history_detail);
        loading_hrGraph = (ProgressBar)findViewById(R.id.progress_hrgraph_history_detail);

        btn_goOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        hp = intent.getExtras().getString("HP");
        name = intent.getExtras().getString("NAME");
        height = intent.getExtras().getString("HEIGHT");
        weight = intent.getExtras().getString("WEIGHT");
        age = intent.getExtras().getInt("AGE");
        gender = intent.getExtras().getString("GENDER");
        start_time = intent.getExtras().getString("START_TIME");
        protocol_name = intent.getExtras().getString("PROTOCOL_NAME");
        max_hr = intent.getExtras().getString("MAX_HR");
        avg_hr = intent.getExtras().getString("AVG_HR");
        max_speed = intent.getExtras().getString("MAX_SPEED");
        avg_speed = intent.getExtras().getString("AVG_SPEED");
        calorie = intent.getExtras().getString("CALORIE");
        distance = intent.getExtras().getString("DISTANCE");
        zone_factor = intent.getExtras().getFloat("ZONE_FACTOR");

        String[] tmp = start_time.split("_");
        text_start_time.setText(tmp[0] + "년 " + tmp[1] + "월 " + tmp[2] + "일 " + tmp[3] + ": " + tmp[4]);
        text_protocol_name.setText(protocol_name);
        text_hr_max.setText(max_hr + " bpm");
        text_speed_max.setText(max_speed + " km/h");
        text_hr_avg.setText(avg_hr + " bpm");
        text_speed_avg.setText(avg_speed + " km/h");
        text_calorie.setText(calorie + " kcal");
        text_distance.setText(distance + " km");

        DPIHelper helper = new DPIHelper(getApplicationContext(), getWindowManager().getDefaultDisplay());
        HRGraph.setTextSize((int)helper.getXDPI() / 16);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        FTPHandler ftp = new FTPHandler();
        ftp.execute();
    }

    public void calc_time() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM. dd. HH:mm:ss");
                Date date = new Date(now);
                String time = sdf.format(date);
                text_crt_time.setText(time);
            }
        });
    }

    String protocol;
    int protocol_version;
    class FTPHandler extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            getFileFromFTP();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final String[] protocol_full = protocol_name.split("Ver.");

                    protocol_version = Integer.parseInt(protocol_full[1]);
                    if(protocol_full[0].equals("매우낮은강도")) protocol = "lowest";
                    else if(protocol_full[0].equals("낮은강도")) protocol = "low";
                    else if(protocol_full[0].equals("중간강도")) protocol = "medium";
                    else if(protocol_full[0].equals("높은강도")) protocol = "high";
                    else if(protocol_full[0].equals("매우높은강도")) protocol = "highest";
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            loading_hrGraph.setVisibility(View.INVISIBLE);
            onReadPrograms(R.raw.class.getFields());
        }
    }

    private void getFileFromFTP() {
        String URL = "125.130.221.35", id = "administrator", pw = "$11!!aa$";
        String download_path = Environment.getExternalStorageDirectory() + "/NovelT/Custom/";

        int reply = 0;
        boolean isLogin = false;

        FTPClient client = new FTPClient();
        client.setControlEncoding("euc-kr");
        try {
            client.connect(URL);
            reply = client.getReplyCode();
            if(!FTPReply.isPositiveCompletion(reply)) {
                client.disconnect();
            }
            isLogin = client.login(id, pw);
            Log.e("getFileFTP", "Login : " + isLogin);
            if(!isLogin) {
                client.logout();
            }
            client.setFileType(FTP.BINARY_FILE_TYPE);
            client.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
            client.enterLocalActiveMode();
            client.changeWorkingDirectory("/HRZONE");

            String[] ftpFiles = client.listNames();
            File downloadFile;
            FileOutputStream fos;
            ArrayList<String> list_ftpFiles = new ArrayList<>(Arrays.asList(ftpFiles));

            String[] tmp = start_time.split("_");
            if(list_ftpFiles.contains(name + "_" + tmp[0] + tmp[1] + tmp[2] + "_" + tmp[3] + tmp[4] + ".txt")) {
                downloadFile = new File(download_path + name + "_" + tmp[0] + tmp[1] + tmp[2] + "_" + tmp[3] + tmp[4] + ".txt");
                fos = new FileOutputStream(downloadFile);
                client.retrieveFile("/HRZONE/" + name + "_" + tmp[0] + tmp[1] + tmp[2] + "_" + tmp[3] + tmp[4] + ".txt", fos);
                fos.close();
            }

            client.logout();
            client.disconnect();

            onReadFile_HR();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ArrayList<String> data;
    ArrayList<Integer> hrs;
    long[] times;
    private void onReadFile_HR() {
        String[] tmp = start_time.split("_");
        String line;

        File dir = new File(Environment.getExternalStorageDirectory() + "/NovelT/Custom/");
        if(!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(Environment.getExternalStorageDirectory() + "/NovelT/Custom/" + name + "_" + tmp[0] + tmp[1] + tmp[2] + "_" + tmp[3] + tmp[4] + ".txt");
        if(file.exists()) {
            try {
                BufferedReader buffer = new BufferedReader(new FileReader(file));
                data = new ArrayList<>();
                while((line = buffer.readLine()) != null) {
                    data.add(line);
                }
                buffer.close();

                onParsingData();

                File[] tmp1 = dir.listFiles();
                for(File child : tmp1) {
                    child.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void onParsingData() {
        if(data != null) {
            hrs = new ArrayList<>();
            times = new long[data.size()-2];

            String[] tmp = data.get(0).split("/");
            std_fourty = Integer.parseInt(tmp[0]);
            std_fifty = Integer.parseInt(tmp[1]);
            std_sixty = Integer.parseInt(tmp[2]);
            std_seventy = Integer.parseInt(tmp[3]);
            std_eighty = Integer.parseInt(tmp[4]);
            std_ninety = Integer.parseInt(tmp[5]);
            std_hundred = Integer.parseInt(tmp[6]);

            for(int i = 2; i < data.size(); i++) {
                String[] tmp1 = data.get(i).split("\t");
                times[i-2] = (Long.parseLong(tmp1[0]));
                hrs.add(Integer.parseInt(tmp1[1]));
            }
        }
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

    String[][] params;
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
        HRGraph.onDrawZone(params, zone_factor);
        HRGraph.addAll(hrs, times);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() { }
}
