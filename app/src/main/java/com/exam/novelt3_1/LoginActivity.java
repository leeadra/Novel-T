package com.exam.novelt3_1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends Activity {
    TextView text_time;
    EditText edit_id, edit_pw;
    Button btn_registration, btn_login;

    Timer timer;
    TimerTask task;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        text_time = (TextView)findViewById(R.id.text_time_login);
        edit_id = (EditText)findViewById(R.id.edit_id);
        edit_pw = (EditText)findViewById(R.id.edit_pw);
        btn_registration = (Button)findViewById(R.id.btn_registration);
        btn_login = (Button)findViewById(R.id.btn_login);

        final InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

        btn_registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!edit_id.getText().equals("") && edit_id.getText().length() > 0) {
                    onCheckID();
                } else {
                    Toast.makeText(getApplicationContext(), "아이디를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        edit_id.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_NEXT) {
                    edit_pw.requestFocus();
                }
                return true;
            }
        });

        edit_pw.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int i, KeyEvent event) {
                if(i == EditorInfo.IME_ACTION_DONE) {
                    imm.hideSoftInputFromWindow(edit_pw.getWindowToken(), 0);
                    btn_login.performClick();
                }
                return true;
            }
        });

        edit_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_id.setText(null);
            }
        });
        edit_pw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_pw.setText(null);
            }
        });

        task = new TimerTask() {
            @Override
            public void run() {
                calc_time();
            }
        };
        timer = new Timer();
        timer.schedule(task, 0, 1000);
    }

    private void onCheckID() {
        ContentValues values = new ContentValues();
        values.put("HP", edit_id.getText().toString());
        values.put("PW", edit_pw.getText().toString());
        HttpPostDataClass post = new HttpPostDataClass("http://125.130.221.35:8001/PROC/AjaxForLoginProc.asp", values);
        post.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && requestCode == 1) {
            edit_id.setText("");
            Toast.makeText(getApplicationContext(), "회원등록이 완료되었습니다. 로그인 해주세요", Toast.LENGTH_SHORT).show();
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

    class HttpPostDataClass extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;

        ProgressBar login_progress;

        public HttpPostDataClass(String url, ContentValues values) {
            this.url = url;
            this.values = values;
            login_progress = (ProgressBar)findViewById(R.id.progress_login);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            login_progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = null;
            RequestHttpURLConnection connection = new RequestHttpURLConnection();
            if(values != null)
                result = connection.request(url, values);
            if(result != null) {
                final String[] tmp = result.split("</>");
                if(tmp[0].equals("OK")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    login_progress.setVisibility(View.INVISIBLE);
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.putExtra("HP", tmp[1]);
                                    intent.putExtra("NAME", tmp[2]);
                                    intent.putExtra("GENDER", tmp[3]);
                                    intent.putExtra("DATE_OF_BIRTH", tmp[4]);
                                    intent.putExtra("HEIGHT", tmp[5]);
                                    intent.putExtra("WEIGHT", tmp[6]);
                                    intent.putExtra("MAX_BP", tmp[7]);
                                    intent.putExtra("MIN_BP", tmp[8]);
                                    intent.putExtra("FAT_PERCENT", tmp[9]);
                                    intent.putExtra("LOCAL", tmp[10]);
                                    intent.putExtra("PLACE", tmp[11]);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 500);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            login_progress.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "핸드폰번호가 일치하는 사용자가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            return result;
        }
    }
}
