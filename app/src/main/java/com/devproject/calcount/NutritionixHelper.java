package com.devproject.calcount;

import android.os.Handler;
import android.os.Looper;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class NutritionixHelper {

    private static final String APP_ID = "773e44d8";
    private static final String API_KEY = "bcdc63d210f5cd820cd908280e67e57f";
    private static final String URL = "https://trackapi.nutritionix.com/v2/natural/nutrients";

    private OkHttpClient client;

    public interface NutritionCallback {
        void onResult(Nutrition nutrition);
        void onError(String error);
    }

    public NutritionixHelper() {
        client = new OkHttpClient();
    }

    public void getNutrition(String query, NutritionCallback callback) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("query", query);

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(URL)
                    .addHeader("x-app-id", APP_ID)
                    .addHeader("x-app-key", API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    runOnMainThread(() -> callback.onError("API call failed: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String resStr = response.body().string();
                        parseNutritionResponse(resStr, callback);
                    } else {
                        runOnMainThread(() -> callback.onError("Error: " + response.message()));
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            callback.onError("Error: " + e.getMessage());
        }
    }

    private void parseNutritionResponse(String response, NutritionCallback callback) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray foods = jsonObject.getJSONArray("foods");

            JSONObject food = foods.getJSONObject(0); // just take the first food
            double calories = food.getDouble("nf_calories");
            double protein = food.getDouble("nf_protein");
            double carbs = food.getDouble("nf_total_carbohydrate");
            double fat = food.getDouble("nf_total_fat");

            Nutrition nutrition = new Nutrition(calories, protein, carbs, fat);

            runOnMainThread(() -> callback.onResult(nutrition));

        } catch (Exception e) {
            e.printStackTrace();
            runOnMainThread(() -> callback.onError("Parsing error"));
        }
    }

    private void runOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
