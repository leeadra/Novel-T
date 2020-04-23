package com.exam.novelt3_1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class SignUpActivity extends Activity {
    TextView text_time;
    EditText edit_name, edit_phone, edit_password, edit_birthday;
    ImageView img_correctness;
    RadioGroup radioGroup_gender;
    RadioButton radio_male, radio_female;
    TextView text_height, text_weight, text_max_bp, text_min_bp, text_fatness;
    Spinner area, place;
    Button btn_increase_height, btn_decrease_height, btn_increase_weight, btn_decrease_weight,
            btn_increase_max_bp, btn_decrease_max_bp, btn_increase_min_bp, btn_decrease_min_bp,
            btn_increase_fatness, btn_decrease_fatness, btn_signup;
    CheckBox agreement;

    ArrayAdapter<String> adapter_Local, adapter_Place;
    ArrayList<String> user_local = new ArrayList<>();
    ArrayList<String> user_place = new ArrayList<>();
    int spinner_selectedItem_position_local = 0, spinner_selectedItem_position_place = 0;

    String sending_result, gender;
    String name, hp, pw, birthday;
    float BMI;
    float height = 170, weight = 60, fatness = 25;
    int max_bp = 120, min_bp = 80;
    boolean isChecked_agreement = false, isSetHeight = false, isSetWeight = false,
    isSetMaxBP = false, isSetMinBP = false, isSetFatness = false, isCorrectPW = false;

    Timer timer;
    TimerTask task;

    InputMethodManager imm;

    LongPressRepeatListner repeatListner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

        text_time = (TextView)findViewById(R.id.text_time_signup);
        edit_name = (EditText)findViewById(R.id.edit_user_name_signup);
        radioGroup_gender = (RadioGroup)findViewById(R.id.radioGroup_gender);
        radio_male = (RadioButton)findViewById(R.id.radio_male);
        radio_female = (RadioButton)findViewById(R.id.radio_female);
        edit_phone = (EditText)findViewById(R.id.edit_phone_number_signup);
        edit_password = (EditText)findViewById(R.id.edit_pw_signup);
        img_correctness = (ImageView)findViewById(R.id.img_pw_correctness_signup);
        edit_birthday = (EditText)findViewById(R.id.edit_birth_signup);
        text_height = (TextView)findViewById(R.id.edit_height_signup);
        btn_increase_height = (Button)findViewById(R.id.btn_increase_height_signup);
        btn_decrease_height = (Button)findViewById(R.id.btn_decrease_height_signup);
        text_weight = (TextView)findViewById(R.id.edit_weight_signup);
        btn_increase_weight = (Button)findViewById(R.id.btn_increase_weight_signup);
        btn_decrease_weight = (Button)findViewById(R.id.btn_decrease_weight_signup);
        text_max_bp = (TextView)findViewById(R.id.edit_max_bp_signup);
        btn_increase_max_bp = (Button)findViewById(R.id.btn_increase_max_bp_signup);
        btn_decrease_max_bp = (Button)findViewById(R.id.btn_decrease_max_bp_signup);
        text_min_bp = (TextView)findViewById(R.id.edit_min_bp_signup);
        btn_increase_min_bp = (Button)findViewById(R.id.btn_increase_min_bp_signup);
        btn_decrease_min_bp = (Button)findViewById(R.id.btn_decrease_min_bp_signup);
        text_fatness = (TextView)findViewById(R.id.edit_fatness_signup);
        btn_increase_fatness = (Button)findViewById(R.id.btn_increase_fatness_signup);
        btn_decrease_fatness = (Button)findViewById(R.id.btn_decrease_fatness_signup);

        area = (Spinner)findViewById(R.id.spinner_area_signup);
        place = (Spinner)findViewById(R.id.spinner_place_signup);
        btn_signup = (Button)findViewById(R.id.btn_signup);
        agreement = (CheckBox)findViewById(R.id.privacy_agreement);

        task = new TimerTask() {
            @Override
            public void run() {
                calc_time();
            }
        };
        timer = new Timer();
        timer.schedule(task, 0, 1000);

        radioGroup_gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                imm.hideSoftInputFromWindow(edit_name.getWindowToken(), 0);
                if(checkedId == R.id.radio_male) {
                    radio_male.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#ffff00")));
                    radio_female.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#807f7f")));
                    gender = "남";
                } else if(checkedId == R.id.radio_female) {
                    radio_male.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#807f7f")));
                    radio_female.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#ffff00")));
                    gender = "여";
                }
            }
        });

        edit_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                name = s.toString();
            }
        });

        edit_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_name.setText("");
                edit_name.setHint("");
            }
        });

        edit_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                hp = s.toString();
                if(edit_phone.getText().toString().length() == 11) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            imm.hideSoftInputFromWindow(edit_phone.getWindowToken(), 0);
                        }
                    }, 300);
                }
            }
        });

        edit_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_phone.setText("");
                edit_phone.setHint("");
            }
        });

        edit_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().length() == 6) {
                    img_correctness.setVisibility(View.VISIBLE);
                    img_correctness.setImageResource(R.drawable.img_correct);
                    isCorrectPW = true;
                } else {
                    img_correctness.setVisibility(View.VISIBLE);
                    isCorrectPW = false;
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                pw = s.toString();
                if(edit_password.getText().toString().length() == 6) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            imm.hideSoftInputFromWindow(edit_password.getWindowToken(), 0);
                        }
                    }, 300);
                }
            }
        });

        edit_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_password.setText("");
                edit_password.setHint("");
            }
        });

        edit_birthday.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                birthday = s.toString();
                if(edit_birthday.getText().toString().length() == 8) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            imm.hideSoftInputFromWindow(edit_birthday.getWindowToken(), 0);
                        }
                    }, 300);
                }
            }
        });

        edit_birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_birthday.setText("");
                edit_birthday.setHint("");
            }
        });

        text_max_bp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text_max_bp.setText("");
                text_max_bp.setHint("");
            }
        });

        text_min_bp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text_min_bp.setText("");
                text_min_bp.setHint("");
            }
        });

        text_fatness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text_fatness.setText("");
                text_fatness.setHint("");
            }
        });

        repeatListner = new LongPressRepeatListner(200, 100, listener);
        btn_increase_height.setOnTouchListener(repeatListner);
        btn_decrease_height.setOnTouchListener(repeatListner);
        btn_increase_weight.setOnTouchListener(repeatListner);
        btn_decrease_weight.setOnTouchListener(repeatListner);
        btn_increase_max_bp.setOnTouchListener(repeatListner);
        btn_decrease_max_bp.setOnTouchListener(repeatListner);
        btn_increase_min_bp.setOnTouchListener(repeatListner);
        btn_decrease_min_bp.setOnTouchListener(repeatListner);
        btn_increase_fatness.setOnTouchListener(repeatListner);
        btn_decrease_fatness.setOnTouchListener(repeatListner);

        agreement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agreement.setChecked(false);
                if(name == null) {
                    Toast.makeText(getApplicationContext(), "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    if(gender == null) {
                        Toast.makeText(getApplicationContext(), "성별을 선택해 주세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        if(hp == null) {
                            Toast.makeText(getApplicationContext(), "핸드폰 번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        } else {
                            if(hp.length() != 11) {
                                Toast.makeText(getApplicationContext(), "핸드폰 번호 11자리를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                            }else if(pw == null) {
                                Toast.makeText(getApplicationContext(), "비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                            } else {
                                if(!isCorrectPW) {
                                    Toast.makeText(getApplicationContext(), "비밀번호 6자리를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                                } else if(birthday == null) {
                                    Toast.makeText(getApplicationContext(), "생년월일을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                                } else {
                                    if(birthday.length() != 8) {
                                        Toast.makeText(getApplicationContext(), "생년월일 8자리를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                                    } else if(!isSetHeight) {
                                        Toast.makeText(getApplicationContext(), "키를 설정해 주세요.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if(!isSetWeight) {
                                            Toast.makeText(getApplicationContext(), "몸무게를 설정해 주세요.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            final AgreementDialog dialog = new AgreementDialog(SignUpActivity.this, isChecked_agreement);
                                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                            dialog.show();
                                            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                @Override
                                                public void onDismiss(DialogInterface dlg) {
                                                    if(dialog.radio_agree.isChecked()) {
                                                        agreement.setChecked(true);
                                                        isChecked_agreement = true;
                                                    } else if(dialog.radio_disagree.isChecked()) {
                                                        agreement.setChecked(false);
                                                        isChecked_agreement = false;
                                                        Toast.makeText(getApplicationContext(), "개인정보 수집 및 이용 동의를 해주세요", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(spinner_selectedItem_position_local == 0) {
                    Toast.makeText(getApplicationContext(), "지역을 선택해 주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    if(spinner_selectedItem_position_place == 0) {
                        Toast.makeText(getApplicationContext(), "지점을 선택해 주세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        onCheck();
                    }
                }
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        onLoadAreas();
        user_local.add("선택");
        user_place.add("선택");

        adapter_Local = new ArrayAdapter<String>(this, R.layout.spinner_item, user_local);  // 서버에서 지역 리스트 받기
        adapter_Local.setDropDownViewResource(R.layout.spinner_item);
        area.setAdapter(adapter_Local);
        area.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                spinner_selectedItem_position_local = position;
                onLoadPlaces();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        adapter_Place = new ArrayAdapter<String>(this, R.layout.spinner_item, user_place);  // 지역 선택되면 지점 리스트 받기
        adapter_Place.setDropDownViewResource(R.layout.spinner_item);
        place.setAdapter(adapter_Place);
        place.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                spinner_selectedItem_position_place = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.btn_increase_height_signup:
                    isSetHeight = true;
                    btn_increase_height.setBackgroundResource(R.drawable.btn_increase_signup_pressed);
                    if(height < 300) height += 0.1;
                    text_height.setText(String.format("%.1f", height));
                    break;
                case R.id.btn_decrease_height_signup:
                    isSetHeight = true;
                    btn_decrease_height.setBackgroundResource(R.drawable.btn_decrease_signup_pressed);
                    if(height > 100) height -= 0.1;
                    text_height.setText(String.format("%.1f", height));
                    break;
                case R.id.btn_increase_weight_signup:
                    isSetWeight = true;
                    btn_increase_weight.setBackgroundResource(R.drawable.btn_increase_signup_pressed);
                    if(weight < 200) weight += 0.1;
                    text_weight.setText(String.format("%.1f", weight));
                    break;
                case R.id.btn_decrease_weight_signup:
                    isSetWeight = true;
                    btn_decrease_weight.setBackgroundResource(R.drawable.btn_decrease_signup_pressed);
                    if(weight > 30) weight -= 0.1;
                    text_weight.setText(String.format("%.1f", weight));
                    break;
                case R.id.btn_increase_max_bp_signup:
                    isSetMaxBP = true;
                    btn_increase_max_bp.setBackgroundResource(R.drawable.btn_increase_signup_pressed);
                    if(max_bp < 200) max_bp += 1;
                    text_max_bp.setText(String.valueOf(max_bp));
                    break;
                case R.id.btn_decrease_max_bp_signup:
                    isSetMaxBP = true;
                    btn_decrease_max_bp.setBackgroundResource(R.drawable.btn_decrease_signup_pressed);
                    if(max_bp > 40) max_bp -= 1;
                    text_max_bp.setText(String.valueOf(max_bp));
                    break;
                case R.id.btn_increase_fatness_signup:
                    isSetFatness = true;
                    btn_increase_fatness.setBackgroundResource(R.drawable.btn_increase_signup_pressed);
                    if(fatness < 100) fatness += 0.1;
                    text_fatness.setText(String.format("%.1f", fatness));
                    break;
                case R.id.btn_decrease_fatness_signup:
                    isSetFatness = true;
                    btn_decrease_fatness.setBackgroundResource(R.drawable.btn_decrease_signup_pressed);
                    if(fatness > 0) fatness -= 0.1;
                    text_fatness.setText(String.format("%.1f", fatness));
                    break;
                case R.id.btn_increase_min_bp_signup:
                    isSetMinBP = true;
                    btn_increase_min_bp.setBackgroundResource(R.drawable.btn_increase_signup_pressed);
                    if(min_bp < 150) min_bp += 1;
                    text_min_bp.setText(String.valueOf(min_bp));
                    break;
                case R.id.btn_decrease_min_bp_signup:
                    isSetMinBP = true;
                    btn_decrease_min_bp.setBackgroundResource(R.drawable.btn_decrease_signup_pressed);
                    if(min_bp > 40) min_bp -= 1;
                    text_min_bp.setText(String.valueOf(min_bp));
                    break;
            }
        }
    };

    private void onCheck() {
        ContentValues values = new ContentValues();
        values.put("HP", edit_phone.getText().toString());
        values.put("LOCAL", user_local.get(spinner_selectedItem_position_local));
        values.put("PLACE", user_place.get(spinner_selectedItem_position_place));
        HttpPostDataClass post = new HttpPostDataClass("http://125.130.221.35:8001/PROC/AjaxForUserVaridation.asp", values, true);
        post.execute();
    }

    public void onLoadAreas() {
        HttpPostDataClass_onLoadAreas post = new HttpPostDataClass_onLoadAreas("http://125.130.221.35:8001/PROC/AjaxForGetLocal.asp");
        post.execute();
    }

    public void onSendData() {
        ContentValues values = new ContentValues();
        values.put("FLAG", "Reg");
        values.put("NAME", edit_name.getText().toString());
        values.put("HP", edit_phone.getText().toString());
        values.put("HEIGHT", text_height.getText().toString());
        values.put("WEIGHT", text_weight.getText().toString());
        values.put("GENDER", gender);
        values.put("DATE_OF_BIRTH", edit_birthday.getText().toString());
        values.put("PW", edit_password.getText().toString());
        values.put("LOCAL", user_local.get(spinner_selectedItem_position_local));
        values.put("PLACE", user_place.get(spinner_selectedItem_position_place));
        BMI = Float.parseFloat(text_weight.getText().toString()) / ((Float.parseFloat(text_height.getText().toString()) / 100) * (Float.parseFloat(text_height.getText().toString()) / 100));
        values.put("BMI", BMI);
        if(!text_max_bp.getText().toString().equals("")) values.put("MAX_BP", max_bp);
        else values.put("MAX_BP", 120);
        if(!text_min_bp.getText().toString().equals("")) values.put("MIN_BP", min_bp);
        else values.put("MIN_BP", 80);
        if(!text_fatness.getText().toString().equals("")) values.put("FAT_PERCENT", fatness);
        else values.put("FAT_PERCENT", 25);

        HttpPostDataClass post = new HttpPostDataClass("http://125.130.221.35:8001/PROC/User_Mst_Proc.asp", values, false);
        post.execute();
    }

    String checkHP_result;
    class HttpPostDataClass extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;
        boolean isCheckHP;

        public HttpPostDataClass(String url, ContentValues values, boolean isCheckHP) {
            this.url = url;
            this.values = values;
            this.isCheckHP = isCheckHP;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = null;
            RequestHttpURLConnection connection = new RequestHttpURLConnection();
            if(!isCheckHP) {
                if(values != null)
                    result = connection.request(url, values);
                Log.w("result", "result : " + result);
                sending_result = result;
            } else {
                result = connection.request(url, values);
                checkHP_result = result;
                if(checkHP_result != null) {
                    if(checkHP_result.equals("N")) {
                        new Handler(getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "동일한 핸드폰 번호가 있습니다. 다시 한번 확인해 주세요.", Toast.LENGTH_SHORT).show();
                                edit_phone.setText(null);
                                edit_phone.setHint("01012345678");
                                edit_phone.setFocusableInTouchMode(true);
                                edit_phone.requestFocus();
                                imm.showSoftInput(edit_phone, 0);
                            }
                        }, 500);
                    } else {
                        if(isChecked_agreement && spinner_selectedItem_position_local != 0 && spinner_selectedItem_position_place != 0) {
                            onSendData();
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                }
            }
            return result;
        }
    }

    class HttpPostDataClass_onLoadAreas extends AsyncTask<Void, Void, String> {
        String url;

        public HttpPostDataClass_onLoadAreas(String url) {
            this.url = url;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = null;
            RequestHttpURLConnection connection = new RequestHttpURLConnection();
            result = connection.request(url, null);
            if (result != null && !result.equals("")) {
                String[] tmp = result.split("<\\|>");
                user_local.addAll(Arrays.asList(tmp));
            }
            return result;
        }
    }

    public void onLoadPlaces() {
        user_place.clear();
        user_place.add("선택");
        place.setSelection(0);
        ContentValues values = new ContentValues();
        values.put("LOCAL", user_local.get(spinner_selectedItem_position_local));
        HttpPostDataClass_onLoadPlaces post = new HttpPostDataClass_onLoadPlaces("http://125.130.221.35:8001/PROC/AjaxForGetPlace.asp", values);
        post.execute();
    }

    class HttpPostDataClass_onLoadPlaces extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;

        public HttpPostDataClass_onLoadPlaces(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = null;
            RequestHttpURLConnection connection = new RequestHttpURLConnection();
            result = connection.request(url, values);
            if (result != null && !result.equals("")) {
                String[] tmp = result.split("<\\|>");
                user_place.addAll(Arrays.asList(tmp));
            }
            return result;
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
                        case R.id.btn_increase_height_signup:
                            btn_increase_height.setBackgroundResource(R.drawable.btn_increase_signup);
                            break;
                        case R.id.btn_decrease_height_signup:
                            btn_decrease_height.setBackgroundResource(R.drawable.btn_decrease_signup);
                            break;
                        case R.id.btn_increase_weight_signup:
                            btn_increase_weight.setBackgroundResource(R.drawable.btn_increase_signup);
                            break;
                        case R.id.btn_decrease_weight_signup:
                            btn_decrease_weight.setBackgroundResource(R.drawable.btn_decrease_signup);
                            break;
                        case R.id.btn_increase_max_bp_signup:
                            btn_increase_max_bp.setBackgroundResource(R.drawable.btn_increase_signup);
                            break;
                        case R.id.btn_decrease_max_bp_signup:
                            btn_decrease_max_bp.setBackgroundResource(R.drawable.btn_decrease_signup);
                            break;
                        case R.id.btn_increase_min_bp_signup:
                            btn_increase_min_bp.setBackgroundResource(R.drawable.btn_increase_signup);
                            break;
                        case R.id.btn_decrease_min_bp_signup:
                            btn_decrease_min_bp.setBackgroundResource(R.drawable.btn_decrease_signup);
                            break;
                        case R.id.btn_increase_fatness_signup:
                            btn_increase_fatness.setBackgroundResource(R.drawable.btn_increase_signup);
                            break;
                        case R.id.btn_decrease_fatness_signup:
                            btn_decrease_fatness.setBackgroundResource(R.drawable.btn_decrease_signup);
                            break;
                    }
                    handler.removeCallbacks(runnable);
                    downView = null;
                    return true;
            }
            return false;
        }
    }
}
