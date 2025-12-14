package com.example.makefoods.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.makefoods.model.Ingredient;

import java.util.List;

/**
 * Ingredient DAO (Data Access Object)
 * 데이터베이스 접근을 위한 인터페이스
 *
 * Room이 자동으로 구현체를 생성해줌
 * LiveData를 사용하여 데이터 변경 시 자동으로 UI 업데이트
 */
@Dao
public interface IngredientDao {

    /**
     * 모든 재료 조회
     * LiveData를 사용하여 데이터 변경 시 자동으로 관찰자에게 알림
     * @return 모든 재료 목록 (등록일 최신순으로 정렬)
     */
    @Query("SELECT * FROM ingredients ORDER BY registeredDate DESC")
    LiveData<List<Ingredient>> getAllIngredients();

    /**
     * 특정 재료 조회
     * @param id 재료 ID
     * @return 해당 ID의 재료
     */
    @Query("SELECT * FROM ingredients WHERE id = :id")
    Ingredient getIngredientById(int id);

    /**
     * 재료 추가
     * @param ingredient 추가할 재료
     * @return 추가된 재료의 ID (자동 생성된 값)
     */
    @Insert
    long insert(Ingredient ingredient);

    /**
     * 여러 재료 한 번에 추가
     * @param ingredients 추가할 재료 목록
     */
    @Insert
    void insertAll(List<Ingredient> ingredients);

    /**
     * 재료 정보 수정
     * ID를 기준으로 찾아서 업데이트
     * @param ingredient 수정할 재료 (ID 포함)
     */
    @Update
    void update(Ingredient ingredient);

    /**
     * 재료 삭제
     * @param ingredient 삭제할 재료
     */
    @Delete
    void delete(Ingredient ingredient);

    /**
     * 여러 재료 한 번에 삭제
     * @param ingredients 삭제할 재료 목록
     */
    @Delete
    void deleteAll(List<Ingredient> ingredients);

    /**
     * 특정 ID의 재료 삭제
     * @param id 삭제할 재료의 ID
     */
    @Query("DELETE FROM ingredients WHERE id = :id")
    void deleteById(int id);

    /**
     * 모든 재료 삭제
     */
    @Query("DELETE FROM ingredients")
    void deleteAllIngredients();

    /**
     * 모든 재료 조회 (동기 방식)
     * ChatGPT에게 재료 정보를 전달할 때 사용
     * 백그라운드 스레드에서만 호출해야 함
     * @return 모든 재료 목록 (등록일 최신순으로 정렬)
     */

    @Query("SELECT * FROM ingredients")
    List<Ingredient> getAllIngredientsSync();

}