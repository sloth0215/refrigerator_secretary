// ChatRequest.java
package com.example.makefoods.network.dto;
import java.util.List;
import java.util.Map;

public class ChatRequest {
    public String model;
    public List<Message> messages;
    public Integer max_tokens;  // 옵션
    public Double temperature;  // 옵션

    public static class Message {
        public String role;   // "user" | "assistant" | "system"
        public String content;
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
