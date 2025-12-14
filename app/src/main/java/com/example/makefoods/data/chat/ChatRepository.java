package com.example.makefoods.data.chat;

import com.example.makefoods.model.Message;
import java.util.List;

public interface ChatRepository {
    interface Callback {
        void onSuccess(String reply);
        void onError(Throwable t);
    }

    // 일반 채팅
    void askGpt(String userText, String ingredientInfo, List<Message> chatHistory, Callback cb);

    // 🆕 레시피 상세 전용 (대화 히스토리 무시)
    void askGptRecipeDetail(String recipeName, String ingredientInfo, Callback cb);
}