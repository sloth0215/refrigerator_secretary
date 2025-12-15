package com.example.makefoods.ui.fridge;


import android.app.DatePickerDialog;
import java.util.Calendar;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.makefoods.R;
import com.example.makefoods.model.Ingredient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 재료 상세 정보 화면
 *
 * 기능:
 * - 재료 정보 표시 (이름, 수량, 등록일, 소비기한)
 * - 재료 수정
 * - 재료 삭제
 */
public class IngredientDetailFragment extends Fragment {

    // Arguments Key
    private static final String ARG_INGREDIENT_ID = "ingredient_id";

    // UI 요소
    private ImageButton btnBack;
    private TextView tvIngredientName;
    private TextView tvQuantity;
    private TextView tvRegisteredDate;
    private TextView tvExpiryDate;
    private ImageButton btnDecreaseQuantity;
    private ImageButton btnIncreaseQuantity;
    private Button btnDelete;

    // ViewModel
    private FridgeViewModel viewModel;

    // 데이터
    private int ingredientId;
    private Ingredient currentIngredient;

    // 날짜 포맷
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA);

    /**
     * Fragment 생성 팩토리 메서드
     *
     * @param ingredientId 재료 ID
     * @return IngredientDetailFragment 인스턴스
     */
    public static IngredientDetailFragment newInstance(int ingredientId) {
        IngredientDetailFragment fragment = new IngredientDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_INGREDIENT_ID, ingredientId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Arguments에서 재료 ID 가져오기
        if (getArguments() != null) {
            ingredientId = getArguments().getInt(ARG_INGREDIENT_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ingredient_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI 초기화
        initViews(view);

        // ViewModel 설정
        setupViewModel();

        // 버튼 리스너 설정
        setupListeners();

        // 재료 데이터 로드
        loadIngredientData();
    }

    /**
     * UI 요소 초기화
     */
    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        tvIngredientName = view.findViewById(R.id.tvIngredientName);
        tvQuantity = view.findViewById(R.id.tvQuantity);
        tvRegisteredDate = view.findViewById(R.id.tvRegisteredDate);
        tvExpiryDate = view.findViewById(R.id.tvExpiryDate);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnDecreaseQuantity = view.findViewById(R.id.btnDecreaseQuantity);
        btnIncreaseQuantity = view.findViewById(R.id.btnIncreaseQuantity);
    }

    /**
     * ViewModel 설정
     */
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(FridgeViewModel.class);
    }

    /**
     * 버튼 리스너 설정
     */
    private void setupListeners() {
        // 뒤로가기 버튼
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // 삭제 버튼
        btnDelete.setOnClickListener(v -> {
            deleteIngredient();
        });
    }

    /**
     * 재료 데이터 로드
     *
     * 백그라운드 스레드에서 DB 조회 후 UI 업데이트
     */
    private void loadIngredientData() {
        // 백그라운드 스레드에서 DB 조회
        new Thread(() -> {
            // DAO를 통해 재료 조회
            currentIngredient = viewModel.getIngredientById(ingredientId);

            // UI 스레드에서 화면 업데이트
            requireActivity().runOnUiThread(() -> {
                if (currentIngredient != null) {
                    displayIngredientData();
                } else {
                    // 커스텀 Toast로 변경
                    showCustomToast("재료를 찾을 수 없습니다");
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }).start();
    }

    /**
     * 재료 데이터 화면에 표시
     */
    private void displayIngredientData() {
        // 재료 이름
        tvIngredientName.setText(currentIngredient.getName());

        // 수량
        tvQuantity.setText(String.valueOf(currentIngredient.getQuantity()));


        String registeredDate = dateFormat.format(
                new Date(currentIngredient.getRegisteredDate())
        );
        tvRegisteredDate.setText(registeredDate);


        String expiryDate = dateFormat.format(
                new Date(currentIngredient.getExpiryDate())
        );
        tvExpiryDate.setText(expiryDate);


        tvIngredientName.setOnClickListener(v -> {
            showEditNameDialog();
        });


        btnDecreaseQuantity.setOnClickListener(v -> {
            int currentQuantity = currentIngredient.getQuantity();
            if (currentQuantity > 1) {  // 최소 1개
                currentIngredient.setQuantity(currentQuantity - 1);
                tvQuantity.setText(String.valueOf(currentIngredient.getQuantity()));
                viewModel.update(currentIngredient);  // DB 업데이트
            } else {
                // 커스텀 Toast로 변경
                showCustomToast("최소 1개 이상 필요합니다");
            }
        });


        btnIncreaseQuantity.setOnClickListener(v -> {
            int currentQuantity = currentIngredient.getQuantity();
            currentIngredient.setQuantity(currentQuantity + 1);
            tvQuantity.setText(String.valueOf(currentIngredient.getQuantity()));
            viewModel.update(currentIngredient);  // DB 업데이트
        });

        // 등록일 클릭 리스너 - DatePicker 띄우기
        tvRegisteredDate.setOnClickListener(v -> {
            showDatePickerForRegisteredDate();
        });

        // 소비기한 클릭 리스너 - DatePicker 띄우기
        tvExpiryDate.setOnClickListener(v -> {
            showDatePickerForExpiryDate();
        });
    }

    /**
     * 재료 삭제
     */
    private void deleteIngredient() {
        if (currentIngredient == null) {
            return;
        }

        // ViewModel을 통해 삭제
        viewModel.delete(currentIngredient);

        // 커스텀 Toast로 변경
        showCustomToast(currentIngredient.getName() + " 삭제 완료");

        // 이전 화면으로 돌아가기
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    /**
     * 등록일 변경을 위한 DatePicker 표시
     */
    private void showDatePickerForRegisteredDate() {
        // 현재 등록일을 Calendar로 변환
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentIngredient.getRegisteredDate());

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // DatePickerDialog 생성 및 표시
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // 선택한 날짜를 Calendar로 변환
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);

                    // timestamp로 변환하여 저장
                    long newTimestamp = selectedDate.getTimeInMillis();
                    currentIngredient.setRegisteredDate(newTimestamp);

                    // 화면 업데이트
                    String dateString = dateFormat.format(new Date(newTimestamp));
                    tvRegisteredDate.setText(dateString);

                    // DB 업데이트
                    viewModel.update(currentIngredient);

                    showCustomToast("등록일 변경 완료");
                },
                year,
                month,
                day
        );

        datePickerDialog.show();
    }

    /**
     * 소비기한 변경을 위한 DatePicker 표시
     */
    private void showDatePickerForExpiryDate() {
        // 현재 소비기한을 Calendar로 변환
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentIngredient.getExpiryDate());

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // DatePickerDialog 생성 및 표시
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // 선택한 날짜를 Calendar로 변환
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);

                    // timestamp로 변환하여 저장
                    long newTimestamp = selectedDate.getTimeInMillis();
                    currentIngredient.setExpiryDate(newTimestamp);

                    // 화면 업데이트
                    String dateString = dateFormat.format(new Date(newTimestamp));
                    tvExpiryDate.setText(dateString);

                    // DB 업데이트
                    viewModel.update(currentIngredient);

                    showCustomToast("소비기한 변경 완료");
                },
                year,
                month,
                day
        );

        datePickerDialog.show();
    }

    /**
     * 재료 이름 수정 Dialog 표시
     */
    private void showEditNameDialog() {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("재료 이름 수정");


        final android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setText(currentIngredient.getName());
        input.setSelection(currentIngredient.getName().length());  // 커서를 맨 끝으로
        builder.setView(input);

        // 버튼 설정
        builder.setPositiveButton("확인", (dialog, which) -> {
            String newName = input.getText().toString().trim();

            if (newName.isEmpty()) {
                showCustomToast("재료 이름을 입력하세요");
                return;
            }


            currentIngredient.setName(newName);
            tvIngredientName.setText(newName);


            viewModel.update(currentIngredient);


            showCustomToast("재료 이름 변경 완료");
        });

        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }


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
}