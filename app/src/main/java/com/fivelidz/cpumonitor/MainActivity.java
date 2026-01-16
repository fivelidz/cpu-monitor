package com.fivelidz.cpumonitor;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private ListView processListView;
    private TextView cpuStatusText;
    private TextView memStatusText;
    private Button refreshButton;
    private Button killAllButton;
    private ArrayAdapter<String> adapter;
    private List<String> processNames;
    private List<Integer> processPids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        processListView = findViewById(R.id.process_list);
        cpuStatusText = findViewById(R.id.cpu_status);
        memStatusText = findViewById(R.id.mem_status);
        refreshButton = findViewById(R.id.refresh_button);
        killAllButton = findViewById(R.id.kill_all_button);

        processNames = new ArrayList<>();
        processPids = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, processNames);
        processListView.setAdapter(adapter);

        startService(new Intent(this, CpuMonitorService.class));

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshProcessList();
            }
        });

        killAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                killAllSuspicious();
            }
        });

        processListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                killProcess(position);
            }
        });

        refreshProcessList();
    }

    private void refreshProcessList() {
        processNames.clear();
        processPids.clear();

        cpuStatusText.setText(String.format("CPU Usage: %.1f%%", CpuMonitorService.cpuUsage));
        memStatusText.setText(String.format("Memory Usage: %d%%", CpuMonitorService.memoryUsage));

        for (CpuMonitorService.ProcessInfo info : CpuMonitorService.suspiciousProcesses) {
            String display = String.format("%s (PID: %d, CPU: %d)",
                info.name, info.pid, info.cpuTime);
            processNames.add(display);
            processPids.add(info.pid);
        }

        if (processNames.isEmpty()) {
            processNames.add("No suspicious processes detected");
            processPids.add(-1);
        }

        adapter.notifyDataSetChanged();
    }

    private void killProcess(int position) {
        if (position < processPids.size() && processPids.get(position) > 0) {
            int pid = processPids.get(position);
            String processName = CpuMonitorService.suspiciousProcesses.get(position).name;

            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            am.killBackgroundProcesses(processName);

            Toast.makeText(this, "Killed process: " + processName, Toast.LENGTH_SHORT).show();
            refreshProcessList();
        }
    }

    private void killAllSuspicious() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        int count = 0;

        for (CpuMonitorService.ProcessInfo info : CpuMonitorService.suspiciousProcesses) {
            am.killBackgroundProcesses(info.name);
            count++;
        }

        if (count > 0) {
            Toast.makeText(this, "Killed " + count + " processes", Toast.LENGTH_SHORT).show();
            refreshProcessList();
        } else {
            Toast.makeText(this, "No processes to kill", Toast.LENGTH_SHORT).show();
        }
    }
}