package barqsoft.footballscores.db;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.R;

public final class DbUtilities {

    public static final int COL_HOME = 0;
    public static final int COL_AWAY = 1;
    public static final int COL_HOME_GOALS = 2;
    public static final int COL_AWAY_GOALS = 3;
    public static final int COL_MATCHTIME = 4;
    public static final int COL_MATCHID = 5;

    private static final String[] COLUMNS = {
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.MATCH_ID
    };

    private DbUtilities() {
        // No instance
    }

    public static Cursor getTodaysScore(Context context) {
        Uri uri = DatabaseContract.scores_table.buildScoreWithDate();
        String formatString = context.getString(R.string.date_format_yyymmdd);
        SimpleDateFormat format = new SimpleDateFormat(formatString);
        String todayDate = format.format(new Date());

        return context.getContentResolver()
                .query(uri, COLUMNS, null, new String[]{todayDate}, null);
    }

    public static void closeCursor(@Nullable Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    public static int getCursorCount(@Nullable Cursor cursor) {
        if (cursor != null) {
            return cursor.getCount();
        }
        return 0;
    }
}
