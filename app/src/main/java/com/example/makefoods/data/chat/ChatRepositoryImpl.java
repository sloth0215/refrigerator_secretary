
package com.example.makefoods.data.chat;

import androidx.annotation.NonNull;
import com.example.makefoods.network.OpenAIService;
import com.example.makefoods.network.RetrofitClient;
import com.example.makefoods.network.dto.ChatRequest;
import com.example.makefoods.network.dto.ChatResponse;
import java.util.Arrays;
import retrofit2.Call;
import retrofit2.Response;

public class ChatRepositoryImpl implements ChatRepository {

    private final OpenAIService api;

    public ChatRepositoryImpl(String openAiApiKey) {
        this.api = RetrofitClient.get(openAiApiKey).create(OpenAIService.class);
    }

    @Override
    public void askGpt(String userText, Callback cb) {  // ← 여기는 ChatRepository.Callback
        ChatRequest body = new ChatRequest();
        body.model = "gpt-4o-mini";
        body.messages = Arrays.asList(
                new ChatRequest.Message("system", "You are a helpful assistant that answers briefly."),
                new ChatRequest.Message("user", userText)
        );
        body.temperature = 0.7;

        // 명시적으로 retrofit2.Callback 사용
        api.chat(body).enqueue(new retrofit2.Callback<ChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call, @NonNull Response<ChatResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null &&
                        resp.body().choices != null && !resp.body().choices.isEmpty()) {
                    String reply = resp.body().choices.get(0).message.content;
                    cb.onSuccess(reply);  // ← ChatRepository.Callback 호출
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