package com.example.makefoods.network;

import android.graphics.Bitmap;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.type.GenerativeBackend;;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Gemini Vision API 서비스 클래스 (Firebase AI Logic 최신 버전)
 * - 영수증/식재료 사진을 Gemini에게 보내서 인식 결과를 받아옴
 */
public class GeminiService {

    private final GenerativeModelFutures model;
    private final Executor executor;


    public GeminiService() {
        // Firebase AI Logic - Gemini Developer API 초기화
        GenerativeModel ai = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-2.5-flash");

        // Java 호환 레이어로 변환 (ListenableFuture 지원)
        this.model = GenerativeModelFutures.from(ai);

        // 백그라운드 작업용 Executor
        this.executor = Executors.newSingleThreadExecutor();
    }


    public void recognizeReceipt(Bitmap bitmap, GeminiCallback callback) {
        // Gemini에게 보낼 프롬프트 (영수증 전용)
        String prompt = "이 영수증 이미지에서 구매한 식재료 품목들을 추출해줘. " +
                "특이사항이 있어도 오직 재료만 추출해줘. " +
                "각 식재료는 한 줄에 하나씩, 식재료 이름만 간단하게 작성해줘. " +
                "가격, 수량, 날짜 등은 제외하고 식재료 이름만 추출해줘.\n" +
                "예시:\n양파\n당근\n토마토";

        // Content 생성 (텍스트 + 이미지)
        Content content = new Content.Builder()
                .addText(prompt)
                .addImage(bitmap)
                .build();

        // API 호출 (비동기)
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        // 결과 처리
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {

                String recognizedText = result.getText();
                callback.onSuccess(recognizedText);
            }

            @Override
            public void onFailure(Throwable t) {

                android.util.Log.e("GeminiService", "영수증 인식 실패", t);
                String errorMsg = t.getMessage() != null ? t.getMessage() : "알 수 없는 오류";
                callback.onError(errorMsg);
            }
        }, executor);
    }

    /**
     * 식재료 사진 인식 요청
     *
     * @param bitmap 인식할 이미지
     * @param callback 결과를 받을 콜백
     */
    public void recognizeIngredients(Bitmap bitmap, GeminiCallback callback) {
        // Gemini에게 보낼 프롬프트
        String prompt = "이 사진에 있는 식재료들을 인식해서 목록으로 알려줘. " +
                "오직 재료만 추출해줘. " +
                "각 식재료는 한 줄에 하나씩, 식재료 이름만 간단하게 작성해줘. " +
                "예시:\n양파\n당근\n토마토";

        // Content 생성 (텍스트 + 이미지)
        Content content = new Content.Builder()
                .addText(prompt)
                .addImage(bitmap)
                .build();

        // API 호출 (비동기)
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        // 결과 처리
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                // 성공: Gemini의 응답 텍스트 추출
                String recognizedText = result.getText();
                callback.onSuccess(recognizedText);
            }

            @Override
            public void onFailure(Throwable t) {
                // 실패: 에러 로그 출력
                android.util.Log.e("GeminiService", "식재료 인식 실패", t);
                String errorMsg = t.getMessage() != null ? t.getMessage() : "알 수 없는 오류";
                callback.onError(errorMsg);
            }
        }, executor);
    }


    public interface GeminiCallback {

        void onSuccess(String result);


        void onError(String error);
    }
}