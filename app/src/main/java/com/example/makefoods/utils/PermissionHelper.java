package com.example.makefoods.utils;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**
 * 권한 관리 헬퍼 클래스
 * - 카메라 권한
 * - 갤러리(저장소) 권한
 */
public class PermissionHelper {

    // 필요한 권한 목록
    private static final String[] CAMERA_PERMISSION = {
            Manifest.permission.CAMERA
    };

    private static final String[] GALLERY_PERMISSION_ANDROID_13_PLUS = {
            Manifest.permission.READ_MEDIA_IMAGES
    };

    private static final String[] GALLERY_PERMISSION_BELOW_ANDROID_13 = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    /**
     * 카메라 권한이 있는지 확인
     */
    public static boolean hasCameraPermission(Fragment fragment) {
        return ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 갤러리 권한이 있는지 확인
     * Android 버전에 따라 다른 권한 체크
     */
    public static boolean hasGalleryPermission(Fragment fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 이상
            return ContextCompat.checkSelfPermission(
                    fragment.requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 12 이하
            return ContextCompat.checkSelfPermission(
                    fragment.requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * 카메라 권한 요청
     */
    public static void requestCameraPermission(
            ActivityResultLauncher<String[]> launcher
    ) {
        launcher.launch(CAMERA_PERMISSION);
    }

    /**
     * 갤러리 권한 요청
     * Android 버전에 따라 다른 권한 요청
     */
    public static void requestGalleryPermission(
            ActivityResultLauncher<String[]> launcher
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 이상
            launcher.launch(GALLERY_PERMISSION_ANDROID_13_PLUS);
        } else {
            // Android 12 이하
            launcher.launch(GALLERY_PERMISSION_BELOW_ANDROID_13);
        }
    }
}