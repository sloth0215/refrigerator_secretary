package com.example.makefoods.data.chat;

import androidx.annotation.NonNull;
import com.example.makefoods.model.Message;
import com.example.makefoods.network.OpenAIService;
import com.example.makefoods.network.RetrofitClient;
import com.example.makefoods.network.dto.ChatRequest;
import com.example.makefoods.network.dto.ChatResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import retrofit2.Call;
import retrofit2.Response;

public class ChatRepositoryImpl implements ChatRepository {

    private final OpenAIService api;

    public ChatRepositoryImpl(String openAiApiKey) {
        this.api = RetrofitClient.get(openAiApiKey).create(OpenAIService.class);
    }

    // ===== 기존 메서드: 일반 채팅용 =====
    @Override
    public void askGpt(String userText, String ingredientInfo, List<Message> chatHistory, Callback cb) {
        String systemMessage =
                "당신은 요리 전문 AI 어시스턴트입니다." +
                        ingredientInfo + "\n\n" +
                        "‼ 매우 중요한 답변 규칙 ‼" +

                        " 사용자가 '뭐 먹지', '추천', '음식', '요리' 같은 단어를 사용하면:" +
                        "반드시 아래 형식으로만 답변:" +
                        "RECIPE_LIST:\n" +
                        "- 음식이름1\n" +
                        "- 음식이름2\n" +
                        "- 음식이름3\n" +

                        " 냉장고에 있는 재료를 최대한 활용하고, 소비기한 임박한 재료 우선 사용" +

                        "요리 관련 질문에 친절하게 답변하세요";

        // 메시지 리스트 생성
        List<ChatRequest.Message> messageList = new ArrayList<>();

        // 1. 시스템 메시지 추가
        messageList.add(new ChatRequest.Message("system", systemMessage));

        // 2. 대화 히스토리 추가
        if (chatHistory != null && !chatHistory.isEmpty()) {
            for (Message msg : chatHistory) {
                String role = msg.getSender() == Message.Sender.USER ? "user" : "assistant";
                messageList.add(new ChatRequest.Message(role, msg.getText()));
            }
        }

        ChatRequest body = new ChatRequest();
        body.model = "gpt-4o-mini";
        body.messages = messageList;
        body.temperature = 0.7;

        api.chat(body).enqueue(new retrofit2.Callback<ChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call, @NonNull Response<ChatResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null &&
                        resp.body().choices != null && !resp.body().choices.isEmpty()) {
                    String reply = resp.body().choices.get(0).message.content;
                    cb.onSuccess(reply);
                } else {
                    cb.onError(new RuntimeException("Empty response"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                cb.onError(t);
            }
        });
    }


    @Override
    public void askGptRecipeDetail(String recipeName, String ingredientInfo, Callback cb) {
        // 레시피 전용 시스템 메시지
        String systemMessage =
                "당신은 요리 레시피 전문가입니다.\n\n" +
                        ingredientInfo + "\n\n" +
                        "사용자가 요청한 음식의 레시피를 다음 형식으로만 답변하세요:\n\n" +
                        "조리시간: [시간]\n" +
                        "재료:\n" +
                        "- [재료1]\n" +
                        "- [재료2]\n" +
                        "만드는방법:\n" +
                        "1. [단계1]\n" +
                        "2. [단계2]\n" +
                        "팁: [조리팁]\n\n" +
                        "절대 RECIPE_LIST 형식이나 다른 형식으로 답변하지 마세요.";

        ChatRequest body = new ChatRequest();
        body.model = "gpt-4o-mini";
        body.messages = Arrays.asList(
                new ChatRequest.Message("system", systemMessage),
                new ChatRequest.Message("user", recipeName + " 레시피 알려줘")
        );
        body.temperature = 0.7;

        api.chat(body).enqueue(new retrofit2.Callback<ChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call, @NonNull Response<ChatResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null &&
                        resp.body().choices != null && !resp.body().choices.isEmpty()) {
                    String reply = resp.body().choices.get(0).message.content;
                    cb.onSuccess(reply);
                } else {
                    cb.onError(new RuntimeException("Empty response"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                cb.onError(t);
            }
        });
    }
}