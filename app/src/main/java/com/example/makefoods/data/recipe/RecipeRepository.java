package com.example.makefoods.data.recipe;

import android.content.Context;
import android.util.Log;
import com.example.makefoods.database.AppDatabase;
import com.example.makefoods.database.RecipeDao;
import com.example.makefoods.model.Recipe;
import java.util.ArrayList;
import java.util.List;

/**
 * RecipeRepository
 *
 * 레시피 검색 비즈니스 로직을 담당하는 클래스
 * DAO와 UI 사이의 중간 계층 역할
 *
 * 책임:
 * 1. 재료 목록에서 레시피 검색
 * 2. 검색 결과 정렬 및 필터링
 * 3. 데이터 변환 (필요시)
 *
 * 모듈화 포인트: 검색 로직을 한 곳에 집중시킴 (유지보수 용이)
 */
public class RecipeRepository {

    private static final String TAG = "RecipeRepository";
    private final RecipeDao recipeDao;

    /**
     * RecipeRepository 생성자
     *
     * @param context 안드로이드 컨텍스트
     */
    public RecipeRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.recipeDao = database.recipeDao();
    }


    /**
     * Gemini가 인식한 재료들을 기반으로 레시피 추천
     *
     * 동작:
     * 1. 각 재료마다 일치하는 레시피 검색
     * 2. 중복 제거
     * 3. 결과 정렬
     *
     * 예: 사용자가 "소고기, 계란, 파"를 인식
     *     → 이들 중 하나라도 포함된 모든 레시피 반환
     *
     * 모듈화 포인트: Gemini 인식 결과를 직접 처리하는 핵심 메서드
     *
     * @param ingredients 재료 목록 (Gemini가 인식한 재료들)
     * @return 매칭되는 레시피 목록 (중복 없음)
     */
    public List<Recipe> searchRecipesByIngredients(List<String> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            Log.w(TAG, "재료 목록이 비어있음");
            return new ArrayList<>();
        }

        // ===== 중복 제거를 위해 Set 사용 =====
        // Recipe를 직접 Set에 넣으면 equals() 구현 필요하므로
        // 레시피 ID로 중복 체크
        java.util.Set<Integer> foundRecipeIds = new java.util.HashSet<>();
        List<Recipe> allResults = new ArrayList<>();

        // ===== 각 재료별로 검색 =====
        for (String ingredient : ingredients) {
            if (ingredient == null || ingredient.trim().isEmpty()) {
                continue;
            }

            Log.d(TAG, "재료로 검색: " + ingredient);

            // DB에서 해당 재료를 포함하는 레시피 검색
            List<Recipe> results = recipeDao.searchByIngredient(ingredient.trim());

            // 중복이 아닌 것만 추가
            for (Recipe recipe : results) {
                if (!foundRecipeIds.contains(recipe.getRecipeId())) {
                    foundRecipeIds.add(recipe.getRecipeId());
                    allResults.add(recipe);
                }
            }
        }

        Log.d(TAG, "검색 완료: " + ingredients.size() + "개 재료로 " +
                allResults.size() + "개 레시피 찾음");

        return allResults;
    }


    /**
     * 단일 재료로 레시피 검색
     *
     * @param ingredient 검색할 재료명
     * @return 매칭되는 레시피 목록
     */
    public List<Recipe> searchRecipeByIngredient(String ingredient) {
        if (ingredient == null || ingredient.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return recipeDao.searchByIngredient(ingredient.trim());
    }


    /**
     * 음식 이름으로 검색
     *
     * 사용자가 직접 음식 이름을 입력했을 때 사용
     *
     * @param keyword 검색 키워드 (음식 이름)
     * @return 매칭되는 레시피 목록
     */
    public List<Recipe> searchRecipeByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return recipeDao.searchByName(keyword.trim());
    }


    /**
     * 특정 ID의 레시피 조회
     *
     * 상세 화면에서 전체 정보를 보여줄 때 사용
     *
     * @param recipeId 레시피 ID
     * @return 해당 ID의 레시피, 없으면 null
     */
    public Recipe getRecipeById(int recipeId) {
        return recipeDao.getRecipeById(recipeId);
    }


    /**
     * 전체 레시피 수 조회
     *
     * 디버깅이나 UI 정보 표시용
     *
     * @return 데이터베이스에 저장된 레시피 총 개수
     */
    public int getRecipeCount() {
        return recipeDao.getRecipeCount();
    }
}