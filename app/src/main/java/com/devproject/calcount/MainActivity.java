package com.devproject.calcount;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    CustomArcProgressView arc;
    Spinner spin;
    EditText foodInputBox, targetEditText;
    TextView tvNutrition;
    TextView tvMealsLogged, tvLastMeal, tvRemainingCalories;
    ImageButton send;
    int targetCalories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        foodInputBox = findViewById(R.id.foodInput);
        send = findViewById(R.id.buttonSend);
        tvNutrition = findViewById(R.id.nutritions);

        tvMealsLogged = findViewById(R.id.tvMealsLogged);
        tvLastMeal = findViewById(R.id.tvLastMeal);
        tvRemainingCalories = findViewById(R.id.tvRemainingCalories);

        TextView tvGainedCalories = findViewById(R.id.gainedCalText);
        TextView tvProtein = findViewById(R.id.protienVal);
        TextView tvCarbs = findViewById(R.id.carbsVal);
        TextView tvFats = findViewById(R.id.fatsVal);

        NutritionixHelper nutritionixHelper = new NutritionixHelper();
        SavedMacrosHelper macrosHelper = new SavedMacrosHelper(this);

        // Load saved macros and summary
        int[] savedMacros = macrosHelper.loadMacros();
        tvGainedCalories.setText(String.valueOf(savedMacros[0]));



        SavedMacrosHelper.SummaryData summary = macrosHelper.loadSummary();

        tvGainedCalories.setText(String.valueOf(savedMacros[0]));
        tvProtein.setText(String.valueOf(savedMacros[1]));
        tvCarbs.setText(String.valueOf(savedMacros[2]));
        tvFats.setText(String.valueOf(savedMacros[3]));

        // Restore summary card data
        tvMealsLogged.setText("Meals Logged: " + summary.mealsLogged);
        tvLastMeal.setText("Last Meal: " + summary.lastMealTime);
        tvRemainingCalories.setText("Remaining: " + summary.remainingCalories);

        // Setup arc
        arc = findViewById(R.id.CustomArcProgressView);
        targetEditText = findViewById(R.id.targetCal);

        // Default or saved target
        // Load saved target calories
        targetCalories = macrosHelper.loadTargetCalories();
        targetEditText.setText(String.valueOf(targetCalories));
        arc.setMax(targetCalories);


        arc.setProgress(savedMacros[0]);

        // Update arc max dynamically if target changes
        targetEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    try {
                        targetCalories = Integer.parseInt(s.toString().trim());
                        arc.setMax(targetCalories);
                        macrosHelper.saveTargetCalories(targetCalories);

                        int currentCalories = Integer.parseInt(tvGainedCalories.getText().toString());
                        if (currentCalories > targetCalories) currentCalories = targetCalories;
                        arc.setProgressWithAnimation(currentCalories);

                        // Also update remaining calories dynamically
                        SavedMacrosHelper.SummaryData summaryNow = macrosHelper.loadSummary();
                        int remaining = Math.max(targetCalories - currentCalories, 0);
                        macrosHelper.saveSummary(summaryNow.mealsLogged, summaryNow.lastMealTime, remaining);
                        tvRemainingCalories.setText("Remaining: " + remaining);

                    } catch (NumberFormatException e) {
                        targetCalories = 2000;
                        arc.setMax(targetCalories);
                    }
                }
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // When Send button is clicked (log meal)
        send.setOnClickListener(v -> {
            String foodQuery = foodInputBox.getText().toString().trim();
            if (foodQuery.isEmpty()) {
                Toast.makeText(this, "Please enter food name", Toast.LENGTH_SHORT).show();
                return;
            }

            tvNutrition.setVisibility(View.VISIBLE);
            tvNutrition.setText("Fetching...");

            nutritionixHelper.getNutrition(foodQuery, new NutritionixHelper.NutritionCallback() {
                @Override
                public void onResult(Nutrition nutrition) {
                    int[] oldMacros = macrosHelper.loadMacros();

                    int newCalories = oldMacros[0] + (int) nutrition.calories;
                    int newProtein = oldMacros[1] + (int) nutrition.protein;
                    int newCarbs = oldMacros[2] + (int) nutrition.carbs;
                    int newFats = oldMacros[3] + (int) nutrition.fat;

                    macrosHelper.saveMacros(newCalories, newProtein, newCarbs, newFats);

                    // Update nutrition display
                    String summaryText = "Calorie = " + (int) nutrition.calories + "\n" +
                            "Protein = " + (int) nutrition.protein + " g\n" +
                            "Carbs = " + (int) nutrition.carbs + " g\n" +
                            "Fat = " + (int) nutrition.fat + " g";

                    tvNutrition.setText(summaryText);
                    tvNutrition.setVisibility(View.VISIBLE);
                    tvNutrition.setOnClickListener(v1 -> tvNutrition.setVisibility(View.GONE));

                    tvGainedCalories.setText(String.valueOf(newCalories));
                    tvProtein.setText(String.valueOf(newProtein));
                    tvCarbs.setText(String.valueOf(newCarbs));
                    tvFats.setText(String.valueOf(newFats));

                    // Update arc progress
                    arc.setMax(targetCalories);
                    arc.setProgressWithAnimation(newCalories);

                    // Check if goal completed
                    if (newCalories >= targetCalories) {
                        Toast.makeText(MainActivity.this, "Today's Calorie goal is completed!", Toast.LENGTH_LONG).show();
                    }

                    // Update daily summary
                    SavedMacrosHelper.SummaryData summary = macrosHelper.loadSummary();
                    int meals = summary.mealsLogged + 1;
                    String lastMealTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
                    int remainingCalories = Math.max(targetCalories - newCalories, 0);

                    macrosHelper.saveSummary(meals, lastMealTime, remainingCalories);

                    // Update UI
                    tvMealsLogged.setText("Meals Logged: " + meals);
                    tvLastMeal.setText("Last Meal: " + lastMealTime);
                    tvRemainingCalories.setText("Remaining: " + remainingCalories);
                }

                @Override
                public void onError(String error) {
                    tvNutrition.setText(error);
                }
            });
        });
    }
}
