// ChatResponse.java
package com.example.makefoods.network.dto;
import java.util.List;

public class ChatResponse {
    public List<Choice> choices;
    public static class Choice {
        public int index;
        public Message message;
    }
    public static class Message {
        public String role;
        public String content;
    }
}
