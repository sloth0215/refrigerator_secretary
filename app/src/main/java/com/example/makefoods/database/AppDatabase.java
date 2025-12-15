package com.example.makefoods.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.makefoods.model.Ingredient;
import com.example.makefoods.model.Recipe;

/**
 * AppDatabase
 *
 * SQLite 데이터베이스의 중앙 관리 클래스
 * Singleton 패턴으로 구현되어 앱 전체에서 하나의 DB 인스턴스만 사용
 *
 * 데이터베이스 구성:
 * - Ingredient: 냉장고에 저장된 재료들
 * - Recipe: CSV에서 로드한 레시피 데이터 (2000개)
 *
 */
@Database(
        entities = {Ingredient.class, Recipe.class},
        version = 2
)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;
    private static boolean isLoadingRecipes = false;


    public abstract IngredientDao ingredientDao();

    public abstract RecipeDao recipeDao();



    /**
     * AppDatabase 싱글톤 인스턴스 획득
     *
     * 첫 호출시 DB 초기화, 이후 호출시 기존 인스턴스 반환
     * 동시에 CSV 임포트 필요 여부 자동 판단
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            // ===== DB 파일 생성 =====
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "makefoods.db"  // 데이터베이스 파일명
                    )
                    .allowMainThreadQueries()  // 메인 스레드에서 DB 접근 허용
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(androidx.sqlite.db.SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            android.util.Log.d("AppDatabase", "DB 생성됨");
                        }

                        @Override
                        public void onOpen(androidx.sqlite.db.SupportSQLiteDatabase db) {
                            super.onOpen(db);
                            android.util.Log.d("AppDatabase", "DB 오픈됨");
                            // 별도 스레드에서 CSV 로드
                            new Thread(() -> checkAndLoadRecipesIfNeeded(context)).start();
                        }
                    })
                    .build();
        }
        return instance;
    }



    private static void checkAndLoadRecipesIfNeeded(Context context) {
        // ===== 중복 로딩 방지 =====
        synchronized (AppDatabase.class) {

            if (isLoadingRecipes) {
                android.util.Log.d("AppDatabase", "CSV 로딩이 이미 진행 중입니다");
                return;
            }

            isLoadingRecipes = true;
        }

        try {
            if (instance == null) {
                android.util.Log.w("AppDatabase", "인스턴스가 아직 생성되지 않음");
                return;
            }

            RecipeDao recipeDao = instance.recipeDao();

            // 레시피 테이블의 레코드 수 확인
            int recipeCount = recipeDao.getRecipeCount();

            if (recipeCount == 0) {

                android.util.Log.d("AppDatabase", "레시피 테이블이 비어있음. CSV에서 로드 중...");

                // CSV 파일 로드
                java.util.List<Recipe> recipes = RecipeCsvLoader.loadRecipesFromCsv(context);

                // 로드된 레시피를 DB에 저장
                if (!recipes.isEmpty()) {
                    recipeDao.insertRecipes(recipes);
                    android.util.Log.d("AppDatabase", "CSV 임포트 완료 총 " + recipes.size() + "개 레시피 저장됨");
                } else {
                    android.util.Log.w("AppDatabase", "CSV 파일에서 레시피를 찾지 못함");
                }
            } else {
                android.util.Log.d("AppDatabase", "레시피 테이블에 이미 " + recipeCount + "개 데이터 있음");
            }
        } catch (Exception e) {
            android.util.Log.e("AppDatabase", "레시피 임포트 실패: " + e.getMessage(), e);
        } finally {
            // 로딩 완료 표시
            synchronized (AppDatabase.class) {
                isLoadingRecipes = false;
            }
        }
    }
}