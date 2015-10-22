package barqsoft.footballscores.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import barqsoft.footballscores.db.DbUtilities;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;
import barqsoft.footballscores.service.MyFetchService;

/**
 * Implementation of App Widget functionality.
 */
public class ScoresWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(createUpdateScoreWidgetIntent(context));
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (MyFetchService.ACTION_SCORE_UPDATED.equals(intent.getAction())) {
            context.startService(createUpdateScoreWidgetIntent(context));
        } else {
            super.onReceive(context, intent);
        }
    }

    public Intent createUpdateScoreWidgetIntent(Context context) {
        return new Intent(context, UpdateScoreWidgetService.class);
    }

    public static class UpdateScoreWidgetService extends IntentService {

        public UpdateScoreWidgetService() {
            super("UpdateScoreWidgetService");
        }

        @Override
        public void onHandleIntent(Intent intent) {
            ComponentName me = new ComponentName(this, ScoresWidget.class);
            AppWidgetManager mgr = AppWidgetManager.getInstance(this);

            Intent i = new Intent(this, ScoresWidget.class);
            mgr.updateAppWidget(me, setupUpdateScoreWidgetView(this, i));
        }

        protected RemoteViews setupUpdateScoreWidgetView(Context context, Intent updateIntent) {
            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.scores_widget);
            Cursor cursor = DbUtilities.getTodaysScore(context);
            if (DbUtilities.getCursorCount(cursor) == 0) {
                views.setViewVisibility(R.id.score_board_container, View.GONE);
                views.setViewVisibility(R.id.widget_score_empty_text, View.VISIBLE);
            } else {
                int limitItems = 2;
                while (cursor.moveToNext() && limitItems != 0) {
                    limitItems--;
                    views.setViewVisibility(R.id.score_board_container, View.VISIBLE);
                    views.setViewVisibility(R.id.widget_score_empty_text, View.GONE);
                    final String homeTeamName = cursor.getString(DbUtilities.COL_HOME);
                    final String awayTeamName = cursor.getString(DbUtilities.COL_AWAY);
                    final String matchTime = cursor.getString(DbUtilities.COL_MATCHTIME);
                    final String score = Utilities.getScores(context,
                            cursor.getInt(DbUtilities.COL_HOME_GOALS),
                            cursor.getInt(DbUtilities.COL_AWAY_GOALS));

                    views.setImageViewResource(R.id.score_widget_home_logo,
                            Utilities.getTeamCrestByTeamName(context, homeTeamName));

                    views.setTextViewText(R.id.widget_home_name, homeTeamName);

                    views.setImageViewResource(R.id.score_widget_away_logo,
                            Utilities.getTeamCrestByTeamName(context, awayTeamName));

                    views.setTextViewText(R.id.widget_away_name, awayTeamName);
                    views.setTextViewText(R.id.score_widget_match_time, matchTime);
                    views.setTextViewText(R.id.widget_score, score);
                }
            }

            // Close cursor
            DbUtilities.closeCursor(cursor);

            // Launch main activity when widget is tapped
            try {
                Intent intent = new Intent(context, MainActivity.class);
                intent.addCategory("android.intent.category.LAUNCHER");
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context.getApplicationContext(),
                        "Problem loading Footbal Scores",
                        Toast.LENGTH_SHORT).show();
            }

            //updateIntent.setAction(MyFetchService.ACTION_SCORE_UPDATED);
            //PendingIntent pi = PendingIntent.getBroadcast(context, 0, updateIntent, 0);
            //views.setOnClickPendingIntent(R.id.widget_title_container, pi);

            return views;
        }
    }
}

