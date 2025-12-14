package com.example.makefoods.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.makefoods.R;
import com.example.makefoods.model.Ingredient;
import com.example.makefoods.ui.fridge.FridgeViewModel;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 이미지 인식 결과를 보여주고 수정할 수 있는 Fragment
 * - 촬영한 이미지 미리보기
 * - OCR/Vision 결과 표시 및 수정
 * - 냉장고에 추가 (DB 저장)
 * - 중복 재료는 수량 증가
 */
public class ImageResultFragment extends Fragment {

    // UI 요소
    private ImageView ivPreview;
    private EditText etResult;
    private Button btnRetake;
    private Button btnAddToFridge;

    // 데이터
    private Bitmap capturedImage;  // 촬영한 이미지
    private String recognizedText;  // 인식된 텍스트
    private boolean isReceipt;  // 영수증인지 식재료인지 구분

    // ViewModel
    private FridgeViewModel fridgeViewModel;

    /**
     * Fragment 생성 시 데이터 전달을 위한 팩토리 메서드
     *
     * @param image 촬영한 이미지
     * @param text 인식된 텍스트
     * @param isReceipt 영수증 여부
     */
    public static ImageResultFragment newInstance(Bitmap image, String text, boolean isReceipt) {
        ImageResultFragment fragment = new ImageResultFragment();
        Bundle args = new Bundle();
        // Bitmap은 Bundle에 직접 넣을 수 없으므로 Fragment 내부 변수로 전달
        fragment.capturedImage = image;
        args.putString("text", text);
        args.putBoolean("isReceipt", isReceipt);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Arguments에서 데이터 가져오기
        if (getArguments() != null) {
            recognizedText = getArguments().getString("text", "");
            isReceipt = getArguments().getBoolean("isReceipt", false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI 요소 초기화
        initViews(view);

        // ViewModel 초기화
        fridgeViewModel = new ViewModelProvider(this).get(FridgeViewModel.class);

        // 데이터 표시
        displayData();

        // 버튼 클릭 리스너 설정
        setupListeners();
    }

    /**
     * UI 요소 초기화
     */
    private void initViews(View view) {
        ivPreview = view.findViewById(R.id.ivPreview);
        etResult = view.findViewById(R.id.etResult);
        btnRetake = view.findViewById(R.id.btnRetake);
        btnAddToFridge = view.findViewById(R.id.btnAddToFridge);
    }

    /**
     * 데이터 표시
     */
    private void displayData() {
        // 이미지 미리보기
        if (capturedImage != null) {
            ivPreview.setImageBitmap(capturedImage);
        }

        // 인식된 텍스트 표시
        etResult.setText(recognizedText);
    }

    /**
     * 버튼 클릭 리스너 설정
     */
    private void setupListeners() {
        // 다시 촬영 버튼
        btnRetake.setOnClickListener(v -> {
            // CameraFragment로 돌아가기
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // 냉장고에 추가 버튼
        btnAddToFridge.setOnClickListener(v -> {
            addToFridge();
        });

        // 인식 결과 EditText 클릭 시 바텀시트 띄우기
        etResult.setOnClickListener(v -> {
            showEditBottomSheet();
        });

        // EditText가 직접 편집되지 않도록 설정
        etResult.setFocusable(false);
        etResult.setClickable(true);
    }

    /**
     * 냉장고에 추가
     *
     * 인식된 텍스트를 파싱하여 재료 목록으로 변환 후 DB에 저장
     * 이미 있는 재료면 수량만 증가
     */
    private void addToFridge() {
        String finalText = etResult.getText().toString().trim();

        if (finalText.isEmpty()) {
            showCustomToast("식재료 목록이 비어있습니다");
            return;
        }

        List<Ingredient> newIngredients = parseTextToIngredients(finalText);

        if (newIngredients.isEmpty()) {
            showCustomToast("인식된 식재료가 없습니다");
            return;
        }

        // 백그라운드 스레드에서 처리
        new Thread(() -> {
            try {
                List<Ingredient> existingIngredients = fridgeViewModel.getAllIngredientsSync();

                // final로 선언하기 - 배열 사용
                final int[] newCount = {0};
                final int[] updatedCount = {0};

                for (Ingredient newIngredient : newIngredients) {
                    boolean found = false;

                    for (Ingredient existing : existingIngredients) {
                        if (existing.getName().equalsIgnoreCase(newIngredient.getName())) {
                            int updatedQuantity = existing.getQuantity() + newIngredient.getQuantity();
                            existing.setQuantity(updatedQuantity);

                            fridgeViewModel.update(existing);
                            updatedCount[0]++;
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        fridgeViewModel.insert(newIngredient);
                        newCount[0]++;
                    }
                }

                // UI 스레드에서 메시지 표시
                requireActivity().runOnUiThread(() -> {
                    String message;
                    if (newCount[0] > 0 && updatedCount[0] > 0) {
                        message = newCount[0] + "개 추가, " + updatedCount[0] + "개 수량 증가";
                    } else if (newCount[0] > 0) {
                        message = newCount[0] + "개 재료 추가 완료";
                    } else if (updatedCount[0] > 0) {
                        message = updatedCount[0] + "개 재료 수량 증가";
                    } else {
                        message = "처리 완료";
                    }

                    showCustomToast(message);
                    requireActivity().getSupportFragmentManager().popBackStack();
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    showCustomToast("오류가 발생했습니다");
                });
            }
        }).start();
    }

    /**
     * 텍스트를 재료 리스트로 파싱
     *
     * 파싱 규칙
     * - 빈 줄은 무시
     * - 각 줄은 하나의 재료
     * - 수량은 기본값 1
     * - 소비기한은 7일 후
     */
    private List<Ingredient> parseTextToIngredients(String text) {
        List<Ingredient> ingredients = new ArrayList<>();

        // 현재 시간 (등록일)
        long currentTime = System.currentTimeMillis();

        // 소비기한 (7일 후)
        long expiryTime = currentTime + (7 * 24 * 60 * 60 * 1000L);

        // 줄바꿈으로 분리
        String[] lines = text.split("\n");

        for (String line : lines) {
            // 앞뒤 공백 제거
            String ingredientName = line.trim();

            // 빈 줄 무시
            if (ingredientName.isEmpty()) {
                continue;
            }

            // 숫자로 시작하는 경우 제거 (예: "1. 계란" → "계란")
            ingredientName = ingredientName.replaceAll("^\\d+\\.\\s*", "");

            // 특수문자 제거 (-, *, 등)
            ingredientName = ingredientName.replaceAll("^[-*]\\s*", "");

            // 재료 객체 생성
            Ingredient ingredient = new Ingredient(
                    ingredientName,  // 재료 이름
                    1,              // 수량 (기본값 1)
                    currentTime,    // 등록일
                    expiryTime      // 소비기한 (7일 후)
            );

            ingredients.add(ingredient);
        }

        return ingredients;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Bitmap 메모리 해제
        if (capturedImage != null && !capturedImage.isRecycled()) {
            capturedImage.recycle();
            capturedImage = null;
        }
    }

    /**
     * 식재료 목록 수정 바텀시트 표시
     */
    private void showEditBottomSheet() {
        // BottomSheetDialog 생성
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());

        // 레이아웃 inflate
        View sheetView = getLayoutInflater().inflate(
                R.layout.bottom_sheet_edit_result, null);

        // 바텀시트 내부 UI 요소 가져오기
        EditText etEditResult = sheetView.findViewById(R.id.etEditResult);
        Button btnComplete = sheetView.findViewById(R.id.btnComplete);

        // 현재 텍스트를 바텀시트 EditText에 설정
        etEditResult.setText(etResult.getText().toString());

        // 완료 버튼 클릭 리스너
        btnComplete.setOnClickListener(v -> {
            // 수정된 텍스트를 메인 EditText에 반영
            String editedText = etEditResult.getText().toString();
            etResult.setText(editedText);

            // 바텀시트 닫기
            bottomSheetDialog.dismiss();
        });

        // 바텀시트에 뷰 설정 및 표시
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }
}