package com.example.makefoods.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 레시피 데이터 모델
 * CSV 파일에서 로드한 레시피를 SQLite DB에 저장하는 클래스
 * Room 라이브러리를 사용해 자동으로 DB 테이블로 변환
 */
@Entity(tableName = "recipes")
public class Recipe {
    // ===== DB 컬럼 =====
    @PrimaryKey
    public int recipeId;              // 레시피 고유 ID (RCP_SNO)

    public String name;               // 요리 이름 (RCP_TTL)
    public String ingredients;        // 재료 목록 (문자열, CKG_MTRL_CN)
    public String cookingSteps;       // 조리 단계 (COOKING_STEPS)
    public String cookingTime;        // 조리 시간 (CKG_TIME_NM)
    public String difficulty;         // 난이도 (CKG_DODF_NM)
    public String imageUrl;           // 이미지 URL (RCP_IMG_URL)
    public String description;        // 설명 (CKG_IPDC)

    // ===== 생성자 =====

    public Recipe() {
    }


    public Recipe(int recipeId, String name, String ingredients, String cookingSteps,
                  String cookingTime, String difficulty, String imageUrl, String description) {
        this.recipeId = recipeId;
        this.name = name;
        this.ingredients = ingredients;
        this.cookingSteps = cookingSteps;
        this.cookingTime = cookingTime;
        this.difficulty = difficulty;
        this.imageUrl = imageUrl;
        this.description = description;
    }


    public int getRecipeId() { return recipeId; }
    public String getName() { return name; }
    public String getIngredients() { return ingredients; }
    public String getCookingSteps() { return cookingSteps; }
    public String getCookingTime() { return cookingTime; }
    public String getDifficulty() { return difficulty; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }


    public void setRecipeId(int recipeId) { this.recipeId = recipeId; }
    public void setName(String name) { this.name = name; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public void setCookingSteps(String cookingSteps) { this.cookingSteps = cookingSteps; }
    public void setCookingTime(String cookingTime) { this.cookingTime = cookingTime; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setDescription(String description) { this.description = description; }
}