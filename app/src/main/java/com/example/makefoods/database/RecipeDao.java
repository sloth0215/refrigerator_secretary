package com.example.makefoods.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.makefoods.model.Recipe;
import java.util.List;


@Dao
public interface RecipeDao {

    // ===== INSERT (삽입) =====


    @Insert
    void insertRecipe(Recipe recipe);


    @Insert
    void insertRecipes(List<Recipe> recipes);



    @Query("SELECT * FROM recipes")
    List<Recipe> getAllRecipes();



    @Query("SELECT * FROM recipes WHERE recipeId = :recipeId LIMIT 1")
    Recipe getRecipeById(int recipeId);



    @Query("SELECT * FROM recipes WHERE name LIKE '%' || :keyword || '%'")
    List<Recipe> searchByName(String keyword);



    @Query("SELECT * FROM recipes WHERE ingredients LIKE '%' || :ingredient || '%'")
    List<Recipe> searchByIngredient(String ingredient);



    @Query("SELECT * FROM recipes WHERE ingredients LIKE '%' || :ingredient1 || '%' " +
            "OR ingredients LIKE '%' || :ingredient2 || '%'")
    List<Recipe> searchByIngredients(String ingredient1, String ingredient2);



    @Query("DELETE FROM recipes")
    void deleteAllRecipes();



    @Query("SELECT COUNT(*) FROM recipes")
    int getRecipeCount();
}