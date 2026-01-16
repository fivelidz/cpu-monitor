package com.fivelidz.cpumonitor;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.app.ActivityManager;
import android.content.Context;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class CpuMonitorService extends Service {
    private Handler handler = new Handler();
    private Runnable updateTask;
    public static float cpuUsage = 0f;
    public static int memoryUsage = 0;
    public static List<ProcessInfo> suspiciousProcesses = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        updateTask = new Runnable() {
            @Override
            public void run() {
                updateCpuUsage();
                updateMemoryUsage();
                detectSuspiciousProcesses();

                Intent intent = new Intent("com.fivelidz.cpumonitor.UPDATE");
                sendBroadcast(intent);

                handler.postDelayed(this, 2000);
            }
        };
        handler.post(updateTask);
    }

    private void updateCpuUsage() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/stat"));
            String line = reader.readLine();
            reader.close();

            if (line != null && line.startsWith("cpu ")) {
                String[] parts = line.split(" +");
                long idle = Long.parseLong(parts[4]);
                long total = 0;
                for (int i = 1; i < parts.length; i++) {
                    total += Long.parseLong(parts[i]);
                }

                cpuUsage = 100f * (1f - (float)idle / total);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateMemoryUsage() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memInfo);

        long totalMem = memInfo.totalMem;
        long availMem = memInfo.availMem;
        memoryUsage = (int)(100 * (1 - (float)availMem / totalMem));
    }

    private void detectSuspiciousProcesses() {
        suspiciousProcesses.clear();
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo process : processes) {
            try {
                BufferedReader reader = new BufferedReader(
                    new FileReader("/proc/" + process.pid + "/stat"));
                String line = reader.readLine();
                reader.close();

                if (line != null) {
                    String[] parts = line.split(" ");
                    if (parts.length > 14) {
                        long cpuTime = Long.parseLong(parts[13]) + Long.parseLong(parts[14]);

                        if (cpuTime > 10000 &&
                            process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                            ProcessInfo info = new ProcessInfo();
                            info.name = process.processName;
                            info.pid = process.pid;
                            info.cpuTime = cpuTime;
                            suspiciousProcesses.add(info);
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTask);
    }

    public static class ProcessInfo {
        public String name;
        public int pid;
        public long cpuTime;
    }
}