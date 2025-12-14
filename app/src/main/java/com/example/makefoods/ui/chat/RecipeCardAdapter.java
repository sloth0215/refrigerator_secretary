package com.example.makefoods.ui.chat;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.makefoods.R;
import com.example.makefoods.model.Recipe;

import java.util.List;

/**
 * RecipeCardAdapter
 *
 * 검색된 레시피를 카드 형태로 표시하는 어댑터
 * 각 카드는 레시피 이름, 재료, 조리 시간, 난이도 등을 표시
 */
public class RecipeCardAdapter extends RecyclerView.Adapter<RecipeCardAdapter.RecipeCardViewHolder> {

    private List<Recipe> recipes;
    private OnRecipeClickListener listener;


    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeCardAdapter(List<Recipe> recipes, OnRecipeClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }


    @NonNull
    @Override
    public RecipeCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewGroup itemView = (ViewGroup) inflater.inflate(R.layout.recipe_card_item, parent, false);
        return new RecipeCardViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull RecipeCardViewHolder holder, int position) {
        if (recipes != null && position < recipes.size()) {
            holder.bind(recipes.get(position));
        }
    }


    @Override
    public int getItemCount() {
        return recipes != null ? recipes.size() : 0;
    }


    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }


    public class RecipeCardViewHolder extends RecyclerView.ViewHolder {

        private TextView recipeNameTextView;
        private TextView cookingTimeTextView;
        private TextView difficultyTextView;
        private TextView ingredientsTextView;

        public RecipeCardViewHolder(@NonNull ViewGroup itemView) {
            super(itemView);

            recipeNameTextView = itemView.findViewById(R.id.recipe_name_text_view);
            cookingTimeTextView = itemView.findViewById(R.id.cooking_time_text_view);
            difficultyTextView = itemView.findViewById(R.id.difficulty_text_view);
            ingredientsTextView = itemView.findViewById(R.id.ingredients_text_view);


            if (ingredientsTextView != null) {
                ingredientsTextView.setMovementMethod(new ScrollingMovementMethod());
                ingredientsTextView.setVerticalScrollBarEnabled(true);
            }
        }

        public void bind(Recipe recipe) {
            if (recipe == null) return;

            // 1) 레시피 이름
            if (recipeNameTextView != null) {
                recipeNameTextView.setText(
                        recipe.getName() != null ? recipe.getName() : "이름 없음"
                );
            }

            // 2) 조리 시간
            if (cookingTimeTextView != null) {
                cookingTimeTextView.setText(
                        recipe.getCookingTime() != null ? recipe.getCookingTime() : "시간 미정"
                );
            }

            // 3) 난이도
            if (difficultyTextView != null) {
                difficultyTextView.setText(
                        recipe.getDifficulty() != null ? recipe.getDifficulty() : "난이도 미정"
                );
            }

            // 4) 재료 표시 (파이프 "|"를 줄바꿈으로)
            if (ingredientsTextView != null) {
                String ingredientsStr = recipe.getIngredients();
                if (ingredientsStr != null && !ingredientsStr.isEmpty()) {
                    String displayIngredients = ingredientsStr.replace("|", "\n");
                    ingredientsTextView.setText(displayIngredients);
                } else {
                    ingredientsTextView.setText("재료 정보 없음");
                }

                // 스크롤을 항상 맨 위로 초기화 (재활용 때문에 필요)
                ingredientsTextView.scrollTo(0, 0);
            }

            // 카드 클릭
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecipeClick(recipe);
                }
            });
        }
    }
}
