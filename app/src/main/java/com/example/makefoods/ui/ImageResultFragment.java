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

import com.example.makefoods.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;  // 추가!

/**
 * 이미지 인식 결과를 보여주고 수정할 수 있는 Fragment
 * - 촬영한 이미지 미리보기
 * - OCR/Vision 결과 표시 (읽기 전용)
 * - EditText 탭하면 BottomSheet로 수정 가능
 * - 냉장고에 추가 or 다시 촬영
 */
public class ImageResultFragment extends Fragment {

    // UI 요소
    private ImageView ivPreview;
    private EditText etResult;  // 읽기 전용으로 사용
    private Button btnRetake;
    private Button btnAddToFridge;

    // 데이터
    private Bitmap capturedImage;
    private String recognizedText;
    private boolean isReceipt;

    /**
     * Fragment 생성 시 데이터 전달을 위한 팩토리 메서드
     */
    public static ImageResultFragment newInstance(Bitmap image, String text, boolean isReceipt) {
        ImageResultFragment fragment = new ImageResultFragment();
        Bundle args = new Bundle();
        fragment.capturedImage = image;
        args.putString("text", text);
        args.putBoolean("isReceipt", isReceipt);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        // EditText를 읽기 전용처럼 보이게 (터치하면 BottomSheet 띄움)
        etResult.setFocusable(false);
        etResult.setClickable(true);
    }

    /**
     * 버튼 클릭 리스너 설정
     */
    private void setupListeners() {
        // EditText 클릭 시 BottomSheet 띄우기
        etResult.setOnClickListener(v -> {
            showEditBottomSheet();
        });

        // 다시 촬영 버튼
        btnRetake.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // 냉장고에 추가 버튼
        btnAddToFridge.setOnClickListener(v -> {
            addToFridge();
        });
    }

    /**
     * BottomSheet를 띄워서 텍스트 수정
     */
    private void showEditBottomSheet() {
        // BottomSheetDialog 생성
        BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext());

        // 레이아웃 inflate
        View sheetView = getLayoutInflater().inflate(
                R.layout.bottom_sheet_edit_result,
                null
        );

        // BottomSheet 내부의 UI 요소 찾기
        EditText etEdit = sheetView.findViewById(R.id.etEditResult);
        Button btnComplete = sheetView.findViewById(R.id.btnComplete);

        // 현재 텍스트를 BottomSheet의 EditText에 설정
        etEdit.setText(recognizedText);

        // 완료 버튼 클릭 리스너
        btnComplete.setOnClickListener(v -> {
            // 수정된 텍스트 가져오기
            String editedText = etEdit.getText().toString().trim();

            // 원본 데이터 업데이트
            recognizedText = editedText;

            // 화면에 표시된 텍스트도 업데이트
            etResult.setText(recognizedText);

            // BottomSheet 닫기
            bottomSheet.dismiss();

            Toast.makeText(requireContext(), "수정 완료!", Toast.LENGTH_SHORT).show();
        });

        // BottomSheet에 레이아웃 설정 및 표시
        bottomSheet.setContentView(sheetView);
        bottomSheet.show();
    }

    /**
     * 냉장고에 추가
     */
    private void addToFridge() {
        // 현재 텍스트 (수정된 내용) 가져오기
        String finalText = recognizedText.trim();

        if (finalText.isEmpty()) {
            Toast.makeText(requireContext(), "식재료 목록이 비어있습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: FridgeFragment에 데이터 전달
        Toast.makeText(requireContext(), "냉장고에 추가됨!\n" + finalText, Toast.LENGTH_SHORT).show();

        // CameraFragment로 돌아가기
        requireActivity().getSupportFragmentManager().popBackStack();
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
}