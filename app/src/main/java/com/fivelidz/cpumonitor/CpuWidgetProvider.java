package com.fivelidz.cpumonitor;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.app.PendingIntent;
import android.graphics.Color;

public class CpuWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.cpu_widget);

            float cpuUsage = CpuMonitorService.cpuUsage;
            int memUsage = CpuMonitorService.memoryUsage;
            int suspiciousCount = CpuMonitorService.suspiciousProcesses.size();

            views.setTextViewText(R.id.cpu_text, String.format("CPU: %.1f%%", cpuUsage));
            views.setTextViewText(R.id.mem_text, String.format("MEM: %d%%", memUsage));

            if (cpuUsage > 80) {
                views.setTextColor(R.id.cpu_text, Color.RED);
            } else if (cpuUsage > 50) {
                views.setTextColor(R.id.cpu_text, Color.YELLOW);
            } else {
                views.setTextColor(R.id.cpu_text, Color.GREEN);
            }

            if (suspiciousCount > 0) {
                views.setTextViewText(R.id.alert_text,
                    String.format("%d suspicious process%s", suspiciousCount, suspiciousCount > 1 ? "es" : ""));
                views.setTextColor(R.id.alert_text, Color.RED);
            } else {
                views.setTextViewText(R.id.alert_text, "System Normal");
                views.setTextColor(R.id.alert_text, Color.GREEN);
            }

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

            appWidgetManager.updateAppWidget(widgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if ("com.fivelidz.cpumonitor.UPDATE".equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] ids = appWidgetManager.getAppWidgetIds(
                new android.content.ComponentName(context, CpuWidgetProvider.class));
            onUpdate(context, appWidgetManager, ids);
        }
    }
}