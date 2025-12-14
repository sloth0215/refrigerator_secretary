package com.example.makefoods.ui.chat;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.makefoods.R;
import com.example.makefoods.model.Message;
import com.example.makefoods.model.Recipe;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;
    private static final int VIEW_TYPE_RECIPE_CARD = 3;

    private final List<Message> items = new ArrayList<>();
    private OnRecipeClickListener recipeClickListener;

    public interface OnRecipeClickListener {
        void onRecipeClick(String recipeName);
    }

    public void setOnRecipeClickListener(OnRecipeClickListener listener) {
        this.recipeClickListener = listener;
    }

    public void submitList(List<Message> newList) {
        items.clear();
        if (newList != null) items.addAll(newList);
        notifyDataSetChanged();
    }

    public void add(Message msg) {
        items.add(msg);
        notifyItemInserted(items.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        Message m = items.get(position);

        if (m.getType() == Message.Type.RECIPE_CARD) {
            return VIEW_TYPE_RECIPE_CARD;
        }

        return (m.getSender() == Message.Sender.USER) ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_USER) {
            View v = inflater.inflate(R.layout.item_message_user, parent, false);
            return new UserMessageVH(v);
        } else if (viewType == VIEW_TYPE_RECIPE_CARD) {
            View v = inflater.inflate(R.layout.item_message_recipe_card, parent, false);
            return new RecipeCardVH(v);
        } else {
            View v = inflater.inflate(R.layout.item_message_bot, parent, false);
            return new BotMessageVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message m = items.get(position);

        if (holder instanceof UserMessageVH) {
            ((UserMessageVH) holder).bind(m);
        } else if (holder instanceof BotMessageVH) {
            ((BotMessageVH) holder).bind(m, recipeClickListener);
        } else if (holder instanceof RecipeCardVH) {
            ((RecipeCardVH) holder).bind(m);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }



    static class UserMessageVH extends RecyclerView.ViewHolder {
        private final TextView tvMessage;

        UserMessageVH(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }

        void bind(Message m) {
            tvMessage.setText(m.getText());
        }
    }

    static class BotMessageVH extends RecyclerView.ViewHolder {
        private final TextView tvMessage;
        private final LinearLayout recipeButtonsContainer;

        BotMessageVH(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            recipeButtonsContainer = itemView.findViewById(R.id.recipeButtonsContainer);
        }

        void bind(Message m, OnRecipeClickListener listener) {
            tvMessage.setText(m.getText());

            recipeButtonsContainer.removeAllViews();

            if (m.getType() == Message.Type.RECIPE_LIST &&
                    m.getRecipeOptions() != null &&
                    !m.getRecipeOptions().isEmpty()) {

                recipeButtonsContainer.setVisibility(View.VISIBLE);

                for (String recipeName : m.getRecipeOptions()) {
                    Button recipeButton = new Button(itemView.getContext());
                    recipeButton.setText(recipeName);
                    recipeButton.setTextSize(14);
                    recipeButton.setBackgroundResource(R.drawable.rounded_button_bg);
                    recipeButton.setTextColor(itemView.getContext().getResources()
                            .getColor(android.R.color.black, null));

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 10, 0, 10);
                    recipeButton.setLayoutParams(params);

                    recipeButton.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onRecipeClick(recipeName);
                        }
                    });

                    recipeButtonsContainer.addView(recipeButton);
                }
            } else {
                recipeButtonsContainer.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 레시피 카드 뷰홀더
     * ViewPager2로 레시피들을 옆으로 스와이핑하면서 볼 수 있음
     */
    static class RecipeCardVH extends RecyclerView.ViewHolder {
        private final ViewPager2 recipeCardPager;
        private final TextView tvPageIndicator;
        private final RecipeCardAdapter adapter;

        RecipeCardVH(@NonNull View itemView) {
            super(itemView);
            recipeCardPager = itemView.findViewById(R.id.recipeCardPager);
            tvPageIndicator = itemView.findViewById(R.id.tvPageIndicator);

            // ===== ViewPager2용 어댑터 설정 =====
            adapter = new RecipeCardAdapter(
                    new ArrayList<>(),
                    new RecipeCardAdapter.OnRecipeClickListener() {
                        @Override
                        public void onRecipeClick(Recipe recipe) {
                            // 레시피 클릭 시 상세 정보 다이얼로그 표시
                            showRecipeDetailDialog(itemView, recipe);
                        }
                    }
            );
            recipeCardPager.setAdapter(adapter);

            // 페이지 변경 리스너 (인디케이터 업데이트)
            recipeCardPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    updatePageIndicator(position);
                }
            });
        }

        void bind(Message m) {
            if (m.getRecipes() != null && !m.getRecipes().isEmpty()) {
                adapter.updateRecipes(m.getRecipes());
                updatePageIndicator(0);
                recipeCardPager.setCurrentItem(0, false);
            }
        }

        private void updatePageIndicator(int currentPage) {
            int totalPages = adapter.getItemCount();
            tvPageIndicator.setText((currentPage + 1) + "/" + totalPages);
        }


         // 레시피 상세 정보 다이얼로그 표시

        private void showRecipeDetailDialog(View itemView, Recipe recipe) {
            if (recipe == null) {
                return;
            }

            // ===== 다이얼로그 레이아웃 만들기 =====
            LinearLayout dialogView = new LinearLayout(itemView.getContext());
            dialogView.setOrientation(LinearLayout.VERTICAL);
            dialogView.setPadding(20, 20, 20, 20);

            // 레시피 이름
            TextView tvName = new TextView(itemView.getContext());
            tvName.setText("🍳 " + (recipe.getName() != null ? recipe.getName() : "요리명 없음"));
            tvName.setTextSize(18);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            tvName.setTextColor(itemView.getContext().getResources()
                    .getColor(android.R.color.black, null));
            tvName.setPadding(0, 0, 0, 16);
            dialogView.addView(tvName);

            // 조리 시간
            TextView tvTime = new TextView(itemView.getContext());
            tvTime.setText("⏱️ 조리시간: " +
                    (recipe.getCookingTime() != null ? recipe.getCookingTime() : "미정"));
            tvTime.setTextSize(14);
            tvTime.setTextColor(itemView.getContext().getResources()
                    .getColor(android.R.color.black, null));
            tvTime.setPadding(0, 0, 0, 8);
            dialogView.addView(tvTime);

            // 난이도
            TextView tvDifficulty = new TextView(itemView.getContext());
            tvDifficulty.setText("📊 난이도: " +
                    (recipe.getDifficulty() != null ? recipe.getDifficulty() : "미정"));
            tvDifficulty.setTextSize(14);
            tvDifficulty.setTextColor(itemView.getContext().getResources()
                    .getColor(android.R.color.black, null));
            tvDifficulty.setPadding(0, 0, 0, 16);
            dialogView.addView(tvDifficulty);

            // 재료
            TextView tvIngredientsLabel = new TextView(itemView.getContext());
            tvIngredientsLabel.setText("📝 재료:");
            tvIngredientsLabel.setTextSize(14);
            tvIngredientsLabel.setTypeface(null, Typeface.BOLD);
            tvIngredientsLabel.setTextColor(itemView.getContext().getResources()
                    .getColor(android.R.color.black, null));
            tvIngredientsLabel.setPadding(0, 0, 0, 8);
            dialogView.addView(tvIngredientsLabel);

            TextView tvIngredients = new TextView(itemView.getContext());
            String ingredientsText = recipe.getIngredients() != null ?
                    recipe.getIngredients().replace("|", "\n") : "재료 정보 없음";
            tvIngredients.setText(ingredientsText);
            tvIngredients.setTextSize(12);
            tvIngredients.setTextColor(itemView.getContext().getResources()
                    .getColor(android.R.color.black, null));
            tvIngredients.setPadding(16, 0, 0, 16);
            dialogView.addView(tvIngredients);

            //  조리 방법
            TextView tvStepsLabel = new TextView(itemView.getContext());
            tvStepsLabel.setText("👨‍🍳 조리 방법:");
            tvStepsLabel.setTextSize(14);
            tvIngredientsLabel.setTypeface(null, Typeface.BOLD);
            tvStepsLabel.setTextColor(itemView.getContext().getResources()
                    .getColor(android.R.color.black, null));
            tvStepsLabel.setPadding(0, 0, 0, 8);
            dialogView.addView(tvStepsLabel);

            TextView tvSteps = new TextView(itemView.getContext());
            String stepsText = recipe.getCookingSteps() != null ?
                    recipe.getCookingSteps() : "조리 방법 정보 없음";
            tvSteps.setText(stepsText);
            tvSteps.setTextSize(12);
            tvSteps.setTextColor(itemView.getContext().getResources()
                    .getColor(android.R.color.black, null));
            tvSteps.setPadding(16, 0, 0, 16);
            dialogView.addView(tvSteps);

            // ===== 다이얼로그 생성 =====
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setView(dialogView)
                    .setPositiveButton("닫기", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        }
    }
}