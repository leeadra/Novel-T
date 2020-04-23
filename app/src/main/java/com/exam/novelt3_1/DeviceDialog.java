package com.exam.novelt3_1;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

public class DeviceDialog extends Dialog implements HPatchDetectObserver {
    TextView text_connected_patch;
    Button btn_disconnect, btn_re_search, btn_ok, btn_cancel, btn_connect;
    ListView list_devices;
    ProgressBar scan_progressbar;

    HPatchManager deviceManager;
    ArrayList<DeviceListItem> items = new ArrayList<>();
    ArrayList<HPatch> detectedPatches = new ArrayList<>();
    HPatchListAdapter adapter;
    HPatch hPatch;

    Timer timer_stop;
    TimerTask task_stop;

    String connected_patch_id;
    Context context;

    public HPatch gethPatch() {
        return hPatch;
    }

    public String getConnected_patch_id() {
        return connected_patch_id;
    }

    public DeviceDialog(@NonNull Context context, HPatchManager manager) {
        super(context);
        this.context = context;
        deviceManager = manager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_devices);

        text_connected_patch = (TextView)findViewById(R.id.text_connected_patch);
        btn_disconnect = (Button)findViewById(R.id.btn_disconnect);
        btn_re_search = (Button)findViewById(R.id.btn_re_search);
        btn_ok = (Button)findViewById(R.id.btn_ok);
        btn_cancel = (Button)findViewById(R.id.btn_cancel);
        scan_progressbar = (ProgressBar)findViewById(R.id.progressbar_scan);

        list_devices = (ListView)findViewById(R.id.list_devices);
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
                detectedPatches.clear();
                adapter.notifyDataSetChanged();
                timer_stop.cancel();
                onStartScanning();
            }
        });

        deviceManager.addHPatchDeviceObserver(this);
        onStartScanning();

        if(MainActivity.connected_patch_id != null) {
            text_connected_patch.setText(MainActivity.connected_patch_id);
        }

        btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.app != null) {
                    if(MainActivity.hPatch_connected != null) {
                        MainActivity.app.clearHPatch(MainActivity.hPatch_connected);
                        text_connected_patch.setText("");
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
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        scan_progressbar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        };

        try {
            deviceManager.startScanning();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    scan_progressbar.setVisibility(View.VISIBLE);
                }
            });
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
        if(!hPatch.getBLEInfo().name.contains("Novel-T")) {
            for(HPatch p : detectedPatches) {
                if(p == hPatch) return;
            }
            detectedPatches.add(hPatch);
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
                convertView = inflater.inflate(R.layout.list_item, parent, false);
            }

            TextView text_deviceName = (TextView)convertView.findViewById(R.id.text_deviceName);
            btn_connect = (Button)convertView.findViewById(R.id.btn_connect);

            final DeviceListItem listItem = items.get(position);
            text_deviceName.setText(listItem.device_name);

            btn_connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hPatch = detectedPatches.get(position);
                    connected_patch_id = String.valueOf(hPatch.getId());
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

    @Override
    public void onBackPressed() { }
}
