package com.example.makefoods.database;

import android.content.Context;
import android.util.Log;
import com.example.makefoods.model.Recipe;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class RecipeCsvLoader {

    private static final String TAG = "RecipeCsvLoader";


    public static List<Recipe> loadRecipesFromCsv(Context context) {
        List<Recipe> recipes = new ArrayList<>();

        try {
            //CSV 파일 오픈

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("recipes.csv"), "UTF-8")
            );

            String line;
            int lineNumber = 0;


            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // 첫 줄 헤더 스킵
                if (lineNumber == 1) {
                    Log.d(TAG, "CSV 헤더 스킵");
                    continue;
                }

                try {


                    List<String> values = parseCSVLine(line);


                    if (values.size() < 20) {
                        Log.w(TAG, "Line " + lineNumber + ": 필드 개수 부족 (" + values.size() + "/20)");
                        continue;
                    }


                    try {
                        int recipeId = Integer.parseInt(values.get(0).trim());    // RCP_SNO
                        String name = values.get(1).trim();                       // RCP_TTL
                        String description = values.get(12).trim();               // CKG_IPDC
                        String ingredients = values.get(13).trim();               // CKG_MTRL_CN
                        String difficulty = values.get(15).trim();                // CKG_DODF_NM
                        String cookingTime = values.get(16).trim();               // CKG_TIME_NM
                        String imageUrl = values.get(18).trim();                  // RCP_IMG_URL
                        String cookingSteps = values.get(19).trim();              // COOKING_STEPS


                        if (name.isEmpty()) {
                            Log.w(TAG, "Line " + lineNumber + ": 음식 이름이 비어있음");
                            continue;
                        }

                        // ===== Recipe 객체 생성 =====
                        Recipe recipe = new Recipe(
                                recipeId,
                                name,
                                ingredients,
                                cookingSteps,
                                cookingTime,
                                difficulty,
                                imageUrl,
                                description
                        );

                        recipes.add(recipe);


                        if (lineNumber % 100 == 0) {
                            Log.d(TAG, "CSV 로드 중... " + lineNumber + "개 처리됨");
                        }

                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Line " + lineNumber + ": 레시피 ID 파싱 실패: " + values.get(0));
                    }

                } catch (Exception e) {
                    // 특정 행 파싱 실패시 로그만 출력하고 계속 진행
                    Log.e(TAG, "Line " + lineNumber + " 파싱 실패: " + e.getMessage());
                }
            }

            reader.close();
            Log.d(TAG, "CSV 로드 완료! 총 " + recipes.size() + "개 레시피");

        } catch (Exception e) {
            Log.e(TAG, "CSV 파일 읽기 실패: " + e.getMessage(), e);
        }

        return recipes;
    }



    private static List<String> parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            char nextChar = (i + 1 < line.length()) ? line.charAt(i + 1) : '\0';


            if (c == '"') {
                if (insideQuotes && nextChar == '"') {

                    currentField.append('"');
                    i++;
                } else {

                    insideQuotes = !insideQuotes;
                }
            }

            else if (c == ',' && !insideQuotes) {

                fields.add(currentField.toString());
                currentField = new StringBuilder();
            }

            else {
                currentField.append(c);
            }
        }


        fields.add(currentField.toString());

        return fields;
    }
}