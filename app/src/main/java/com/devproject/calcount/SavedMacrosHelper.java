package com.devproject.calcount;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SavedMacrosHelper {

    private static final String PREF_NAME = "MacrosPrefs";
    private static final String KEY_TARGET_CALORIES = "target_calories";

    // Existing keys
    private static final String KEY_CALORIES = "calories";
    private static final String KEY_PROTEIN = "protein";
    private static final String KEY_CARBS = "carbs";
    private static final String KEY_FATS = "fats";
    private static final String KEY_LAST_RESET_DATE = "last_reset_date";


    private static final String KEY_MEALS_LOGGED = "meals_logged";
    private static final String KEY_LAST_MEAL_TIME = "last_meal_time";
    private static final String KEY_REMAINING_CAL = "remaining_calories";

    private final SharedPreferences prefs;

    public SavedMacrosHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        checkForMidnightReset();
    }

    // Save macros
    public void saveMacros(int calories, int protein, int carbs, int fats) {
        prefs.edit()
                .putInt(KEY_CALORIES, calories)
                .putInt(KEY_PROTEIN, protein)
                .putInt(KEY_CARBS, carbs)
                .putInt(KEY_FATS, fats)
                .apply();
    }
    // Save target calories
    public void saveTargetCalories(int target) {
        prefs.edit().putInt(KEY_TARGET_CALORIES, target).apply();
    }

    // Load target calories
    public int loadTargetCalories() {
        return prefs.getInt(KEY_TARGET_CALORIES, 2000); // default 2000
    }

    //Load macros
    public int[] loadMacros() {
        return new int[]{
                prefs.getInt(KEY_CALORIES, 0),
                prefs.getInt(KEY_PROTEIN, 0),
                prefs.getInt(KEY_CARBS, 0),
                prefs.getInt(KEY_FATS, 0)
        };
    }

    //Save daily summary info
    public void saveSummary(int mealsLogged, String lastMealTime, int remainingCalories) {
        prefs.edit()
                .putInt(KEY_MEALS_LOGGED, mealsLogged)
                .putString(KEY_LAST_MEAL_TIME, lastMealTime)
                .putInt(KEY_REMAINING_CAL, remainingCalories)
                .apply();
    }

    //Load daily summary
    public SummaryData loadSummary() {
        int meals = prefs.getInt(KEY_MEALS_LOGGED, 0);
        String lastTime = prefs.getString(KEY_LAST_MEAL_TIME, "—");
        int remain = prefs.getInt(KEY_REMAINING_CAL, 0);
        return new SummaryData(meals, lastTime, remain);
    }

    //Reset everything at night
    public void resetMacros() {
        saveMacros(0, 0, 0, 0);
        saveSummary(0, "—", 0);
        saveTodayAsLastReset();
    }

    //Save today’s date as last reset
    private void saveTodayAsLastReset() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        prefs.edit().putString(KEY_LAST_RESET_DATE, today).apply();
    }

    //Check if a new day started  reset automatically
    private void checkForMidnightReset() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastReset = prefs.getString(KEY_LAST_RESET_DATE, "");
        if (!today.equals(lastReset)) resetMacros();
    }

    //Helper inner class for summary
    public static class SummaryData {
        public int mealsLogged;
        public String lastMealTime;
        public int remainingCalories;

        public SummaryData(int mealsLogged, String lastMealTime, int remainingCalories) {
            this.mealsLogged = mealsLogged;
            this.lastMealTime = lastMealTime;
            this.remainingCalories = remainingCalories;
        }
    }
}
