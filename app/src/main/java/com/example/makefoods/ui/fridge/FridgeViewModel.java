package com.example.makefoods.ui.fridge;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.makefoods.database.AppDatabase;
import com.example.makefoods.database.IngredientDao;
import com.example.makefoods.model.Ingredient;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 냉장고 화면용 ViewModel
 *
 * 역할:
 * - 데이터베이스와 UI 사이의 중간 다리
 * - 재료 데이터 관리 (추가, 삭제, 조회)
 * - LiveData로 데이터 변경 시 자동으로 UI 업데이트
 *
 * AndroidViewModel 사용 이유:
 * - Application Context가 필요해서 (Database 초기화용)
 */
public class FridgeViewModel extends AndroidViewModel {

    // 데이터베이스 DAO (데이터 접근 객체)
    private IngredientDao ingredientDao;

    // 모든 재료 목록 (LiveData로 자동 업데이트)
    private LiveData<List<Ingredient>> allIngredients;

    // 백그라운드 작업용 Executor
    // 데이터베이스 작업은 메인 스레드에서 하면 안 되므로
    private ExecutorService executorService;

    /**
     * 생성자
     * @param application Application Context
     */
    public FridgeViewModel(@NonNull Application application) {
        super(application);

        // 데이터베이스 인스턴스 가져오기
        AppDatabase database = AppDatabase.getInstance(application);
        ingredientDao = database.ingredientDao();

        // 모든 재료 목록 가져오기 (LiveData)
        allIngredients = ingredientDao.getAllIngredients();

        // Executor 초기화 (백그라운드 작업용)
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * 모든 재료 목록 가져오기 (LiveData)
     * @return LiveData로 감싸진 재료 리스트
     */
    public LiveData<List<Ingredient>> getAllIngredients() {
        return allIngredients;
    }

    /**
     * 모든 재료 목록 가져오기 (동기식)
     * 백그라운드 스레드에서 사용 가능
     *
     * @return 재료 리스트 (동기식)
     */
    public List<Ingredient> getAllIngredientsSync() {
        return ingredientDao.getAllIngredientsSync();
    }

    /**
     * 재료 추가
     * 백그라운드 스레드에서 실행됨
     *
     * @param ingredient 추가할 재료
     */
    public void insert(Ingredient ingredient) {
        executorService.execute(() -> {
            ingredientDao.insert(ingredient);
        });
    }

    /**
     * 여러 재료 한 번에 추가
     * ImageResultFragment에서 여러 재료를 한 번에 추가할 때 사용
     *
     * @param ingredients 추가할 재료 리스트
     */
    public void insertAll(List<Ingredient> ingredients) {
        executorService.execute(() -> {
            ingredientDao.insertAll(ingredients);
        });
    }

    /**
     * 재료 정보 수정
     *
     * @param ingredient 수정할 재료 (ID 포함)
     */
    public void update(Ingredient ingredient) {
        executorService.execute(() -> {
            ingredientDao.update(ingredient);
        });
    }

    /**
     * 재료 삭제
     *
     * @param ingredient 삭제할 재료
     */
    public void delete(Ingredient ingredient) {
        executorService.execute(() -> {
            ingredientDao.delete(ingredient);
        });
    }

    /**
     * 여러 재료 한 번에 삭제
     * 삭제 모드에서 선택된 항목들을 한 번에 삭제할 때 사용
     *
     * @param ingredients 삭제할 재료 리스트
     */
    public void deleteAll(List<Ingredient> ingredients) {
        executorService.execute(() -> {
            ingredientDao.deleteAll(ingredients);
        });
    }

    /**
     * ID로 재료 조회 (동기 방식)
     * 상세 화면에서 사용
     *
     * @param id 재료 ID
     * @return 해당 재료 객체
     */
    public Ingredient getIngredientById(int id) {
        return ingredientDao.getIngredientById(id);
    }

    /**
     * ViewModel이 제거될 때 호출
     * Executor 정리
     */
    @Override
    protected void onCleared() {
        super.onCleared();

        // Executor 종료 (메모리 누수 방지)
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}