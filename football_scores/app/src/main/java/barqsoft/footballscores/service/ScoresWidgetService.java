package barqsoft.footballscores.service;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.db.DbUtilities;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;

/**
 * Widget service
 */
@TargetApi(11)
public class ScoresWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ScoresWidgetRemoveViewFactory(this.getApplicationContext(), intent);
    }

    private static class ScoresWidgetRemoveViewFactory
            implements RemoteViewsService.RemoteViewsFactory {

        private Cursor cursor = null;

        private Context context;

        private Intent intent;

        public ScoresWidgetRemoveViewFactory(Context context, Intent intent) {
            this.context = context;
            this.intent = intent;
        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {
            /*
              Clear the identity of the remote process, so that permission enforcement is checked
              against the app instead of against the remote caller. This also prevent us from
              from exporting the ScoreProvider

              See: http://stackoverflow.com/a/20645908/235930
             */
            final long token = Binder.clearCallingIdentity();
            try {
                cursor = DbUtilities.getTodaysScore(context);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        @Override
        public void onDestroy() {
            closeCursor();
            cursor = null;
        }

        @Override
        public int getCount() {
            return DbUtilities.getCursorCount(cursor);
        }

        @Override
        public RemoteViews getViewAt(int i) {
            if (i == AdapterView.INVALID_POSITION || cursor == null || !cursor.moveToPosition(i)) {
                return null;
            }
            final RemoteViews views = getLoadingView();

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
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(context.getPackageName(), R.layout.scores_widget_list_item);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            if (cursor != null) {
                cursor.moveToPosition(i);
                cursor.getLong(DbUtilities.COL_MATCHID);
            }
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        private void closeCursor() {
            DbUtilities.closeCursor(cursor);
        }
    }
}
