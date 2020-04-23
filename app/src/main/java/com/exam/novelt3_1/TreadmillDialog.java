package com.exam.novelt3_1;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.samsung.slsi.HPatch;
import com.samsung.slsi.HPatchDetectObserver;
import com.samsung.slsi.HPatchException;
import com.samsung.slsi.HPatchManager;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class TreadmillDialog extends Dialog implements HPatchDetectObserver {
    TextView text_connected_treadmill;
    Button btn_disconnect, btn_re_search, btn_ok, btn_cancel, btn_connect;
    ListView list_devices;
    ProgressBar scan_progressbar;

    HPatchManager deviceManager;
    ArrayList<DeviceListItem> items = new ArrayList<>();
    ArrayList<HPatch> detectedTreadmills = new ArrayList<>();
    HPatchListAdapter adapter;
    HPatch hTreadmill;

    Timer timer_stop;
    TimerTask task_stop;

    String connected_treadmill_id;
    Context context;

    public HPatch gethTreadmill() {
        return hTreadmill;
    }

    public String getConnected_treadmill_id() {
        return connected_treadmill_id;
    }

    public TreadmillDialog(@NonNull Context context, HPatchManager manager) {
        super(context);
        this.context = context;
        deviceManager = manager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_treadmill);

        text_connected_treadmill = (TextView)findViewById(R.id.text_connected_treadmill);
        btn_disconnect = (Button)findViewById(R.id.btn_disconnect_treadmill);
        btn_re_search = (Button)findViewById(R.id.btn_re_search_treadmill);
        btn_ok = (Button)findViewById(R.id.btn_ok_treadmill);
        btn_cancel = (Button)findViewById(R.id.btn_cancel_treadmill);
        scan_progressbar = (ProgressBar)findViewById(R.id.progressbar_scan_treadmill);

        list_devices = (ListView)findViewById(R.id.list_treadmills);
        adapter = new HPatchListAdapter();
        list_devices.setAdapter(adapter);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        btn_re_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                items.clear();
                detectedTreadmills.clear();
                adapter.notifyDataSetChanged();
                timer_stop.cancel();
                onStartScanning();
            }
        });

        deviceManager.addHPatchDeviceObserver(this);
        onStartScanning();

        if(MainActivity.connected_treadmill_id != null) {
            text_connected_treadmill.setText(MainActivity.connected_treadmill_id);
        }

        btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.app != null) {
                    if(MainActivity.hTreadmill_connected != null) {
                        MainActivity.app.clearHPatch(MainActivity.hTreadmill_connected);
                        text_connected_treadmill.setText("");
                        dismiss();
                    }
                }
            }
        });
    }

    public void onStartScanning() {
        timer_stop = new Timer();
        task_stop = new TimerTask() {
            @Override
            public void run() {
                deviceManager.stopScanning();
                scan_progressbar.setVisibility(View.INVISIBLE);
            }
        };

        try {
            deviceManager.startScanning();
            scan_progressbar.setVisibility(View.VISIBLE);
        } catch (HPatchException e) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            }, 500);
        }
        timer_stop.schedule(task_stop, 5000);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        deviceManager.stopScanning();
        deviceManager.removeHPatchDeviceObserver(this);

    }

    @Override
    public void onHPatchDetected(HPatch hPatch) {
        if(hPatch.getBLEInfo().name.contains("Novel-T")) {
            for(HPatch p : detectedTreadmills) {
                if(p == hPatch) return;
            }
            detectedTreadmills.add(hPatch);
            String name = String.valueOf(hPatch.getId());
            DeviceListItem item = new DeviceListItem(name);
            items.add(item);
            adapter.notifyDataSetChanged();
        }
    }

    class HPatchListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final Context context = parent.getContext();
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.treadmill_list_item, parent, false);
            }

            TextView text_deviceName = (TextView)convertView.findViewById(R.id.text_treadmillName);
            btn_connect = (Button)convertView.findViewById(R.id.btn_connect_treadmill);

            final DeviceListItem listItem = items.get(position);
            text_deviceName.setText(listItem.device_name);

            btn_connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hTreadmill = detectedTreadmills.get(position);
                    connected_treadmill_id = String.valueOf(hTreadmill.getId());
                    dismiss();
                }
            });

            return convertView;
        }
    }

    class DeviceListItem {
        String device_name;

        DeviceListItem(String name) {
            device_name = name;
        }
    }
}
