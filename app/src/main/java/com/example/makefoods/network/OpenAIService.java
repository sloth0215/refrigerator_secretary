package com.example.makefoods.network;

import com.example.makefoods.network.dto.ChatRequest;
import com.example.makefoods.network.dto.ChatResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface OpenAIService {
    @POST("v1/chat/completions")
    Call<ChatResponse> chat(@Body ChatRequest body);
}
