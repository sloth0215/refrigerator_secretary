package com.example.makefoods.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.makefoods.R;
import com.example.makefoods.network.GeminiService;
import com.example.makefoods.utils.ImageUtils;
import com.example.makefoods.utils.PermissionHelper;

import java.io.IOException;

/**
 * 카메라 촬영 Fragment
 * - 영수증 촬영 (ML Kit OCR)
 * - 식재료 촬영 (Gemini Vision)
 */
public class CameraFragment extends Fragment {

    // UI 요소
    private LinearLayout btnReceipt;
    private LinearLayout btnIngredient;

    // API 서비스

    private GeminiService geminiService;

    // 권한 요청 런처
    private ActivityResultLauncher<String[]> cameraPermissionLauncher;
    private ActivityResultLauncher<String[]> galleryPermissionLauncher;

    // 카메라/갤러리 런처
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    // 현재 선택된 모드 (영수증 vs 식재료)
    private boolean isReceiptMode = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // API 서비스 초기화

        geminiService = new GeminiService();

        // 권한 요청 런처 초기화
        initPermissionLaunchers();

        // 카메라/갤러리 런처 초기화
        initImageLaunchers();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI 요소 초기화
        initViews(view);

        // 버튼 클릭 리스너 설정
        setupListeners();
    }

    /**
     * UI 요소 초기화
     */
    private void initViews(View view) {
        btnReceipt = view.findViewById(R.id.btnReceipt);
        btnIngredient = view.findViewById(R.id.btnIngredient);
    }

    /**
     * 버튼 클릭 리스너 설정
     */
    private void setupListeners() {
        // 영수증 촬영 버튼
        btnReceipt.setOnClickListener(v -> {
            isReceiptMode = true;
            showImageSourceDialog();
        });

        // 식재료 촬영 버튼
        btnIngredient.setOnClickListener(v -> {
            isReceiptMode = false;
            showImageSourceDialog();
        });
    }

    /**
     * 권한 요청 런처 초기화
     */
    private void initPermissionLaunchers() {
        // 카메라 권한 요청 결과
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean cameraGranted = result.get(android.Manifest.permission.CAMERA);
                    if (cameraGranted != null && cameraGranted) {
                        // 권한 허용됨 → 카메라 실행
                        openCamera();
                    } else {
                        // 권한 거부됨
                        Toast.makeText(requireContext(), "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // 갤러리 권한 요청 결과
        galleryPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    // 갤러리는 권한 체크가 복잡하므로 일단 실행
                    openGallery();
                }
        );
    }

    /**
     * 카메라/갤러리 런처 초기화
     */
    private void initImageLaunchers() {
        // 카메라 촬영 결과
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        // 촬영된 이미지 가져오기
                        Bundle extras = result.getData().getExtras();
                        Bitmap photo = (Bitmap) extras.get("data");

                        if (photo != null) {
                            // 이미지 처리
                            processImage(photo);
                        }
                    }
                }
        );

        // 갤러리 선택 결과
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();

                        if (imageUri != null) {
                            try {
                                // Uri → Bitmap 변환
                                Bitmap photo = ImageUtils.getBitmapFromUri(requireContext(), imageUri);

                                // 이미지 처리
                                processImage(photo);
                            } catch (IOException e) {
                                Toast.makeText(requireContext(), "이미지 로드 실패", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    /**
     * 이미지 소스 선택 다이얼로그 (카메라 vs 갤러리)
     */
    private void showImageSourceDialog() {
        String[] options = {"카메라로 촬영", "갤러리에서 선택"};

        new AlertDialog.Builder(requireContext())
                .setTitle("사진 선택")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // 카메라 선택
                        checkCameraPermissionAndOpen();
                    } else {
                        // 갤러리 선택
                        checkGalleryPermissionAndOpen();
                    }
                })
                .show();
    }

    /**
     * 카메라 권한 확인 후 카메라 열기
     */
    private void checkCameraPermissionAndOpen() {
        if (PermissionHelper.hasCameraPermission(this)) {
            // 권한 있음 → 바로 카메라 열기
            openCamera();
        } else {
            // 권한 없음 → 권한 요청
            PermissionHelper.requestCameraPermission(cameraPermissionLauncher);
        }
    }

    /**
     * 갤러리 권한 확인 후 갤러리 열기
     */
    private void checkGalleryPermissionAndOpen() {
        if (PermissionHelper.hasGalleryPermission(this)) {
            // 권한 있음 → 바로 갤러리 열기
            openGallery();
        } else {
            // 권한 없음 → 권한 요청
            PermissionHelper.requestGalleryPermission(galleryPermissionLauncher);
        }
    }

    /**
     * 카메라 열기
     */
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }

    /**
     * 갤러리 열기
     */
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        galleryLauncher.launch(galleryIntent);
    }

    /**
     * 이미지 처리 (OCR or Gemini)
     */
    private void processImage(Bitmap bitmap) {
        // 이미지 크기 조정 (API 효율을 위해)
        Bitmap resizedBitmap = ImageUtils.resizeBitmap(bitmap, 1024, 1024);

        // 로딩 표시
        Toast.makeText(requireContext(), "처리 중...", Toast.LENGTH_SHORT).show();

        if (isReceiptMode) {
            // 영수증 모드 → Gemini로 처리
            processReceiptWithGemini(resizedBitmap);
        } else {
            // 식재료 모드 → Gemini Vision
            processIngredientWithGemini(resizedBitmap);
        }
    }

    /**
     * 영수증 처리 (Gemini)
     */

    private void processReceiptWithGemini(Bitmap bitmap) {
        // Gemini에게 영수증 분석 요청
        geminiService.recognizeReceipt(bitmap, new GeminiService.GeminiCallback() {
            @Override
            public void onSuccess(String result) {
                // UI 스레드에서 실행
                requireActivity().runOnUiThread(() -> {
                    navigateToResultFragment(bitmap, result, true);
                });
            }

            @Override
            public void onError(String error) {
                // UI 스레드에서 실행
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "영수증 인식 실패: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }


    /**
     * 식재료 처리 (Gemini Vision)
     */
    private void processIngredientWithGemini(Bitmap bitmap) {
        geminiService.recognizeIngredients(bitmap, new GeminiService.GeminiCallback() {
            @Override
            public void onSuccess(String result) {
                // UI 스레드에서 실행
                requireActivity().runOnUiThread(() -> {
                    navigateToResultFragment(bitmap, result, false);
                });
            }

            @Override
            public void onError(String error) {
                // UI 스레드에서 실행
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "인식 실패: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * 결과 피그먼트로 이동
     */
    private void navigateToResultFragment(Bitmap image, String text, boolean isReceipt) {
        ImageResultFragment resultFragment = ImageResultFragment.newInstance(image, text, isReceipt);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, resultFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}