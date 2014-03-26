package ca.mimic.apphangar;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.util.Collections;
import java.util.List;


/**
 * Implementation of App Widget functionality.
 */
public class StatsWidget extends AppWidgetProvider {

    private static TasksDataSource db;
    private static Context mContext;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        mContext = context;
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public static int dpToPx(int dp) {
        Resources r = mContext.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.statswidget_layout);
        PackageManager pkgm = context.getPackageManager();

        if (db == null) {
            db = new TasksDataSource(context);
            db.open();
        }
        int highestSeconds = db.getHighestSeconds();
        List<TasksModel> tasks = db.getAllTasks();
        Collections.sort(tasks, new SettingsActivity().new TasksComparator("seconds"));

        int count = 0;
        for (TasksModel task : tasks) {
            int iconID = context.getResources().getIdentifier("iconCont" + (count + 1), "id",
                    context.getPackageName());
            int labelID = context.getResources().getIdentifier("appName" + (count + 1), "id",
                    context.getPackageName());
            int barID = context.getResources().getIdentifier("barCont" + (count + 1), "id",
                    context.getPackageName());
            int statsID = context.getResources().getIdentifier("statsCont" + (count + 1), "id",
                    context.getPackageName());
            if (count == 10) {
                break;
            }

            Drawable taskIcon;
            try {
                ApplicationInfo appInfo = pkgm.getApplicationInfo(task.getPackageName(), 0);
                taskIcon = appInfo.loadIcon(pkgm);
            } catch (Exception e) {
                continue;
            }

            count++;

            Bitmap bmpIcon = ((BitmapDrawable) taskIcon).getBitmap();
            views.setImageViewBitmap(iconID, bmpIcon);
            views.setTextViewText(labelID, task.getName());

            int maxWidth = dpToPx(250) - dpToPx(32+14+14); // ImageView + Margin? + Stats text?
            float secondsRatio = (float) task.getSeconds() / highestSeconds;
            int barColor;
            int secondsColor = (Math.round(secondsRatio * 100));
            if (secondsColor >= 80 ) {
                barColor = 0xFF34B5E2;
            } else if (secondsColor >= 60) {
                barColor = 0xFFAA66CC;
            } else if (secondsColor >= 40) {
                barColor = 0xFF74C353;
            } else if (secondsColor >= 20) {
                barColor = 0xFFFFBB33;
            } else {
                barColor = 0xFFFF4444;
            }
            float adjustedWidth = maxWidth * secondsRatio;
            // views.setInt(iconID, "width", Math.round(adjustedWidth));
            int[] statsTime = new SettingsActivity().splitToComponentTimes(task.getSeconds());
            String statsString = ((statsTime[0] > 0) ? statsTime[0] + "h " : "") + ((statsTime[1] > 0) ? statsTime[1] + "m " : "") + ((statsTime[2] > 0) ? statsTime[2] + "s " : "");
            views.setTextViewText(statsID, statsString);
            views.setInt(barID, "setBackgroundColor", barColor);
        }
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

