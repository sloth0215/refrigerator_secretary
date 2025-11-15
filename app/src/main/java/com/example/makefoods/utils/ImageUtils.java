package com.example.makefoods.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 이미지 처리 유틸리티 클래스
 * - Uri → Bitmap 변환
 * - Bitmap 회전 처리
 * - Bitmap 크기 조정
 * - Bitmap → Base64 변환 (Gemini API용)
 */
public class ImageUtils {

    /**
     * Uri에서 Bitmap 가져오기
     * 이미지 회전 정보(EXIF)도 자동으로 처리
     */
    public static Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        if (inputStream != null) {
            inputStream.close();
        }

        // EXIF 정보로 회전 처리
        return rotateBitmapIfNeeded(context, bitmap, uri);
    }

    /**
     * EXIF 정보를 읽어서 이미지 회전
     * (카메라로 찍은 사진이 옆으로 돌아가는 문제 해결)
     */
    private static Bitmap rotateBitmapIfNeeded(Context context, Bitmap bitmap, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            ExifInterface exif = new ExifInterface(inputStream);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
            );

            int rotationAngle = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotationAngle = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotationAngle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotationAngle = 270;
                    break;
            }

            if (rotationAngle != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotationAngle);
                bitmap = Bitmap.createBitmap(
                        bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(),
                        matrix, true
                );
            }

            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * Bitmap 크기 조정
     * API에 보낼 때 너무 큰 이미지는 용량 낭비이므로 적당한 크기로 조정
     *
     * @param maxWidth 최대 너비
     * @param maxHeight 최대 높이
     */
    public static Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // 이미 작으면 그대로 반환
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }

        // 비율 계산
        float ratio = Math.min(
                (float) maxWidth / width,
                (float) maxHeight / height
        );

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    /**
     * Bitmap을 Base64 문자열로 변환
     * Gemini API에 이미지를 보낼 때 사용
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // JPEG로 압축 (품질 80%)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);

        byte[] byteArray = outputStream.toByteArray();

        // Base64로 인코딩
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    /**
     * Bitmap을 임시 파일로 저장
     * ML Kit에서 사용할 때 필요
     */
    public static File saveBitmapToTempFile(Context context, Bitmap bitmap) throws IOException {
        // 임시 파일 생성
        File tempFile = File.createTempFile("temp_image", ".jpg", context.getCacheDir());

        // Bitmap을 파일로 저장
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        outputStream.flush();
        outputStream.close();

        return tempFile;
    }

    /**
     * 임시 파일 삭제
     */
    public static void deleteTempFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}