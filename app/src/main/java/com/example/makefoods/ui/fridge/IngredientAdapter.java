package com.example.makefoods.ui.fridge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.makefoods.R;
import com.example.makefoods.model.Ingredient;

import java.util.ArrayList;
import java.util.List;

/**
 * 냉장고 재료 목록 RecyclerView Adapter
 *
 * ListAdapter를 사용하여 DiffUtil로 효율적인 업데이트
 * 삭제 모드 지원 (체크박스 표시/숨김)
 */
public class IngredientAdapter extends ListAdapter<Ingredient, IngredientAdapter.IngredientViewHolder> {

    // 삭제 모드 여부
    private boolean isDeleteMode = false;

    // 선택된 재료들 (삭제용)
    private List<Ingredient> selectedIngredients = new ArrayList<>();

    // 클릭 리스너 인터페이스
    private OnItemClickListener listener;

    /**
     * 재료 항목 클릭 리스너 인터페이스
     */
    public interface OnItemClickListener {
        void onDetailClick(Ingredient ingredient);  // 상세보기 버튼 클릭
    }

    /**
     * 생성자
     */
    public IngredientAdapter() {
        super(DIFF_CALLBACK);
    }

    /**
     * DiffUtil Callback
     * 리스트 변경 시 효율적으로 업데이트하기 위한 콜백
     */
    private static final DiffUtil.ItemCallback<Ingredient> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Ingredient>() {

                @Override
                public boolean areItemsTheSame(@NonNull Ingredient oldItem, @NonNull Ingredient newItem) {
                    // ID로 같은 아이템인지 비교
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Ingredient oldItem, @NonNull Ingredient newItem) {
                    // 내용이 같은지 비교
                    return oldItem.getName().equals(newItem.getName()) &&
                            oldItem.getQuantity() == newItem.getQuantity() &&
                            oldItem.getExpiryDate() == newItem.getExpiryDate();
                }
            };

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 레이아웃 inflate
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        // 해당 위치의 재료 데이터 가져오기
        Ingredient ingredient = getItem(position);
        holder.bind(ingredient);
    }

    /**
     * 클릭 리스너 설정
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 삭제 모드 설정
     * @param deleteMode true면 삭제 모드, false면 일반 모드
     */
    public void setDeleteMode(boolean deleteMode) {
        this.isDeleteMode = deleteMode;

        // 삭제 모드 해제 시 선택 목록 초기화
        if (!deleteMode) {
            selectedIngredients.clear();
        }

        // 전체 리스트 업데이트
        notifyDataSetChanged();
    }

    /**
     * 선택된 재료 목록 가져오기
     * @return 선택된 재료 리스트
     */
    public List<Ingredient> getSelectedIngredients() {
        return new ArrayList<>(selectedIngredients);
    }

    /**
     * ViewHolder 클래스
     */
    class IngredientViewHolder extends RecyclerView.ViewHolder {

        private CheckBox checkBox;
        private TextView tvName;
        private ImageButton btnDetail;
        private TextView tvQuantity;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);

            // UI 요소 초기화
            checkBox = itemView.findViewById(R.id.checkboxIngredient);
            tvName = itemView.findViewById(R.id.tvIngredientName);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            tvQuantity = itemView.findViewById(R.id.tvIngredientQuantity);
        }


        public void bind(Ingredient ingredient) {
            // 재료 이름 표시
            tvName.setText(ingredient.getName());

            // 수량 표시
            tvQuantity.setText("수량 : " + ingredient.getQuantity());

            // 삭제 모드에 따라 체크박스 표시/숨김
            if (isDeleteMode) {
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setChecked(selectedIngredients.contains(ingredient));

                // 체크박스 클릭 리스너
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        // 선택 목록에 추가
                        if (!selectedIngredients.contains(ingredient)) {
                            selectedIngredients.add(ingredient);
                        }
                    } else {
                        // 선택 목록에서 제거
                        selectedIngredients.remove(ingredient);
                    }
                });
            } else {
                checkBox.setVisibility(View.GONE);
            }

            // 상세보기 버튼 클릭 리스너
            btnDetail.setOnClickListener(v -> {
                if (listener != null && !isDeleteMode) {
                    listener.onDetailClick(ingredient);
                }
            });
        }
    }
}