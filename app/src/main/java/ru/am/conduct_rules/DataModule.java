package ru.am.conduct_rules;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.DisplayMetrics;

public class DataModule {

    public static SQLiteDatabase dbReader;
    public static SQLiteDatabase dbWriter;
    public static boolean isFirstStart;

    public static void initDBHelper(Context c) {
        DBHelper dbHelper = new DBHelper(c);
        dbWriter = dbHelper.getWritableDatabase();
        dbReader = dbHelper.getReadableDatabase();
    }

    public static int convertPixelsToDp(float px, Context context) {
        return Math.round(px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int convertDpToPixel(float dp, Context context) {
        return Math.round(dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

}
