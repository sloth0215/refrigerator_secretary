package com.example.makefoods.ui.fridge;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.makefoods.R;
import com.example.makefoods.model.Ingredient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * 냉장고 화면 Fragment
 *
 * 기능:
 * - 재료 목록 표시 (RecyclerView)
 * - 재료 추가 (FloatingActionButton)
 * - 재료 삭제 (삭제 모드)
 * - 재료 상세 보기
 */
public class FridgeFragment extends Fragment {

    // ViewModel
    private FridgeViewModel viewModel;

    // UI 요소
    private RecyclerView recyclerView;
    private IngredientAdapter adapter;
    private TextView tvEmptyState;
    private FloatingActionButton fabAdd;
    private ImageButton btnDelete;
    private View deleteButtonsContainer;
    private Button btnCancelDelete;
    private Button btnConfirmDelete;

    // 삭제 모드 여부
    private boolean isDeleteMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fridge, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI 초기화
        initViews(view);

        // RecyclerView 설정
        setupRecyclerView();

        // ViewModel 설정
        setupViewModel();

        // 버튼 리스너 설정
        setupListeners();
    }

    /**
     * UI 요소 초기화
     */
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewIngredients);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        fabAdd = view.findViewById(R.id.fabAddIngredient);
        btnDelete = view.findViewById(R.id.btnDelete);
        deleteButtonsContainer = view.findViewById(R.id.deleteButtonsContainer);
        btnCancelDelete = view.findViewById(R.id.btnCancelDelete);
        btnConfirmDelete = view.findViewById(R.id.btnConfirmDelete);
    }

    /**
     * RecyclerView 설정
     */
    private void setupRecyclerView() {
        // 2열
        androidx.recyclerview.widget.GridLayoutManager gridLayoutManager =
                new androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Adapter 생성 및 설정
        adapter = new IngredientAdapter();
        recyclerView.setAdapter(adapter);

        // Adapter 클릭 리스너 설정
        adapter.setOnItemClickListener(new IngredientAdapter.OnItemClickListener() {
            @Override
            public void onDetailClick(Ingredient ingredient) {
                // 상세 화면으로 이동
                showIngredientDetail(ingredient);
            }
        });
    }

    /**
     * ViewModel 설정
     */
    private void setupViewModel() {
        // ViewModel 가져오기
        viewModel = new ViewModelProvider(this).get(FridgeViewModel.class);

        // 재료 목록 관찰
        // 데이터가 변경되면 자동으로 UI 업데이트
        viewModel.getAllIngredients().observe(getViewLifecycleOwner(), ingredients -> {
            // Adapter에 데이터 전달
            adapter.submitList(ingredients);

            // 빈 상태 처리
            if (ingredients == null || ingredients.isEmpty()) {
                // 재료가 없으면 안내 메시지 표시
                tvEmptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                // 재료가 있으면 리스트 표시
                tvEmptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * 버튼 리스너 설정
     */
    private void setupListeners() {
        // 재료 추가 버튼 (FloatingActionButton)
        fabAdd.setOnClickListener(v -> {
            showAddIngredientDialog();
        });

        // 삭제 모드 전환 버튼
        btnDelete.setOnClickListener(v -> {
            toggleDeleteMode();
        });

        // 삭제 취소 버튼
        btnCancelDelete.setOnClickListener(v -> {
            toggleDeleteMode();
        });

        // 삭제 확인 버튼
        btnConfirmDelete.setOnClickListener(v -> {
            deleteSelectedIngredients();
        });
    }

    /**
     * 삭제 모드 전환
     */
    private void toggleDeleteMode() {
        isDeleteMode = !isDeleteMode;

        // Adapter에 삭제 모드 전달
        adapter.setDeleteMode(isDeleteMode);

        if (isDeleteMode) {
            // 삭제 모드 활성화
            deleteButtonsContainer.setVisibility(View.VISIBLE);
            fabAdd.hide();  // FloatingActionButton 숨김

            //  배경을 빨간색 그라데이션으로 변경
            View root = requireView();
            root.setBackgroundResource(R.drawable.bg_fridge_gradient_delete);

        } else {
            // 삭제 모드 비활성화
            deleteButtonsContainer.setVisibility(View.GONE);
            fabAdd.show();  // FloatingActionButton 표시

            //  배경을 원래 파란색 그라데이션으로 복원
            View root = requireView();
            root.setBackgroundResource(R.drawable.bg_fridge_gradient);
        }
    }


    private void deleteSelectedIngredients() {
        List<Ingredient> selectedIngredients = adapter.getSelectedIngredients();

        if (selectedIngredients.isEmpty()) {
            // 커스텀 Toast로 변경
            showCustomToast("삭제할 재료를 선택하세요");
            return;
        }

        // ViewModel을 통해 삭제
        viewModel.deleteAll(selectedIngredients);

        // 삭제 모드 해제
        toggleDeleteMode();

        // 커스텀 Toast로 변경
        showCustomToast(selectedIngredients.size() + "개 재료 삭제 완료");
    }


    private void showAddIngredientDialog() {
        // AlertDialog 생성
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());


        LinearLayout dialogLayout = new LinearLayout(requireContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(60, 60, 60, 60);


        TextView titleView = new TextView(requireContext());
        titleView.setText("재료 추가");
        titleView.setTextSize(20);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setTextColor(getResources().getColor(android.R.color.black, null));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, 0, 0, 60);
        titleView.setLayoutParams(titleParams);
        dialogLayout.addView(titleView);


        TextView labelName = new TextView(requireContext());
        labelName.setText("재료 이름");
        labelName.setTextSize(14);
        labelName.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        LinearLayout.LayoutParams labelNameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        labelNameParams.setMargins(0, 0, 0, 20);
        labelName.setLayoutParams(labelNameParams);
        dialogLayout.addView(labelName);

        final EditText etIngredientName = new EditText(requireContext());
        etIngredientName.setHint("예: 계란, 우유, 감자...");
        etIngredientName.setTextSize(16);
        etIngredientName.setPadding(30, 30, 30, 30);
        etIngredientName.setBackgroundResource(R.drawable.rounded_button_bg);
        LinearLayout.LayoutParams etNameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        etNameParams.setMargins(0, 0, 0, 60);
        etIngredientName.setLayoutParams(etNameParams);
        dialogLayout.addView(etIngredientName);


        TextView labelQuantity = new TextView(requireContext());
        labelQuantity.setText("수량");
        labelQuantity.setTextSize(14);
        labelQuantity.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        LinearLayout.LayoutParams labelQuantityParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        labelQuantityParams.setMargins(0, 0, 0, 20);
        labelQuantity.setLayoutParams(labelQuantityParams);
        dialogLayout.addView(labelQuantity);

        // 수량 조절 레이아웃
        LinearLayout quantityLayout = new LinearLayout(requireContext());
        quantityLayout.setOrientation(LinearLayout.HORIZONTAL);
        quantityLayout.setBackgroundResource(R.drawable.rounded_button_bg);
        quantityLayout.setPadding(30, 30, 30, 30);
        quantityLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams quantityLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        quantityLayoutParams.setMargins(0, 0, 0, 60);
        quantityLayout.setLayoutParams(quantityLayoutParams);

        // 감소 버튼
        ImageButton btnDecrease = new ImageButton(requireContext());
        btnDecrease.setImageResource(R.drawable.ic_left);
        btnDecrease.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
        LinearLayout.LayoutParams btnDecreaseParams = new LinearLayout.LayoutParams(100, 100);
        btnDecrease.setLayoutParams(btnDecreaseParams);
        quantityLayout.addView(btnDecrease);

        // 수량 표시
        final TextView tvQuantity = new TextView(requireContext());
        tvQuantity.setText("1");
        tvQuantity.setTextSize(18);
        tvQuantity.setTypeface(null, android.graphics.Typeface.BOLD);
        tvQuantity.setTextColor(getResources().getColor(android.R.color.black, null));
        tvQuantity.setGravity(android.view.Gravity.CENTER);
        tvQuantity.setMinWidth(120);
        LinearLayout.LayoutParams tvQuantityParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        tvQuantityParams.setMargins(50, 0, 50, 0);
        tvQuantity.setLayoutParams(tvQuantityParams);
        quantityLayout.addView(tvQuantity);

        // 증가 버튼
        ImageButton btnIncrease = new ImageButton(requireContext());
        btnIncrease.setImageResource(R.drawable.ic_right);
        btnIncrease.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
        LinearLayout.LayoutParams btnIncreaseParams = new LinearLayout.LayoutParams(100, 100);
        btnIncrease.setLayoutParams(btnIncreaseParams);
        quantityLayout.addView(btnIncrease);

        dialogLayout.addView(quantityLayout);

        // 수량 데이터 변수 (배열로 감싸서 내부 클래스에서 수정 가능하게)
        final int[] quantity = {1};

        // 감소 버튼 리스너
        btnDecrease.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                tvQuantity.setText(String.valueOf(quantity[0]));
            } else {
                // 커스텀 Toast로 변경
                showCustomToast("최소 1개 이상 필요합니다");
            }
        });

        // 증가 버튼 리스너
        btnIncrease.setOnClickListener(v -> {
            quantity[0]++;
            tvQuantity.setText(String.valueOf(quantity[0]));
        });

        // ========== 소비기한 설정 ==========
        TextView labelExpiry = new TextView(requireContext());
        labelExpiry.setText("소비기한");
        labelExpiry.setTextSize(14);
        labelExpiry.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        LinearLayout.LayoutParams labelExpiryParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        labelExpiryParams.setMargins(0, 0, 0, 20);
        labelExpiry.setLayoutParams(labelExpiryParams);
        dialogLayout.addView(labelExpiry);

        // 소비기한 표시 레이아웃
        LinearLayout expiryLayout = new LinearLayout(requireContext());
        expiryLayout.setOrientation(LinearLayout.HORIZONTAL);
        expiryLayout.setBackgroundResource(R.drawable.rounded_button_bg);
        expiryLayout.setPadding(30, 30, 30, 30);
        expiryLayout.setClickable(true);
        expiryLayout.setFocusable(true);
        LinearLayout.LayoutParams expiryLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        expiryLayoutParams.setMargins(0, 0, 0, 80);
        expiryLayout.setLayoutParams(expiryLayoutParams);

        final TextView tvExpiryDate = new TextView(requireContext());
        tvExpiryDate.setText("날짜 선택");
        tvExpiryDate.setTextSize(16);
        tvExpiryDate.setTextColor(getResources().getColor(android.R.color.black, null));
        expiryLayout.addView(tvExpiryDate);

        dialogLayout.addView(expiryLayout);

        // 소비기한 데이터 (기본값: 7일 후)
        final long[] expiryTimestamp = {System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000)};
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA);
        tvExpiryDate.setText(dateFormat.format(new Date(expiryTimestamp[0])));

        // 소비기한 클릭 리스너 - DatePicker 표시
        expiryLayout.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(expiryTimestamp[0]);

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(selectedYear, selectedMonth, selectedDay);

                        expiryTimestamp[0] = selectedDate.getTimeInMillis();
                        tvExpiryDate.setText(dateFormat.format(new Date(expiryTimestamp[0])));
                    },
                    year,
                    month,
                    day
            );

            datePickerDialog.show();
        });

        // 다이얼로그 설정
        builder.setView(dialogLayout);

        // 취소 버튼
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        // 확인 버튼
        builder.setPositiveButton("추가", (dialog, which) -> {
            String ingredientName = etIngredientName.getText().toString().trim();

            // 유효성 검사
            if (ingredientName.isEmpty()) {
                // 커스텀 Toast로 변경
                showCustomToast("재료 이름을 입력하세요");
                return;
            }

            // 재료 객체 생성
            long currentTime = System.currentTimeMillis();
            Ingredient newIngredient = new Ingredient(
                    ingredientName,
                    quantity[0],
                    currentTime,  // 등록일 = 현재 시간
                    expiryTimestamp[0]  // 소비기한 = 사용자가 선택한 날짜
            );

            // DB에 저장
            viewModel.insert(newIngredient);

            // 커스텀 Toast로 변경
            showCustomToast(ingredientName + " 추가 완료");
        });

        // 다이얼로그 표시
        builder.show();
    }

    /**
     * 깔끔한 커스텀 Toast 표시
     */
    private void showCustomToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView textView = layout.findViewById(R.id.toast_text);
        textView.setText(message);

        Toast toast = new Toast(requireContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    /**
     * 재료 상세 화면 표시
     *
     * @param ingredient 선택된 재료
     */
    private void showIngredientDetail(Ingredient ingredient) {
        // IngredientDetailFragment 생성
        IngredientDetailFragment detailFragment =
                IngredientDetailFragment.newInstance(ingredient.getId());

        // Fragment 전환
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)  // 뒤로가기 버튼으로 돌아올 수 있게
                .commit();
    }
}