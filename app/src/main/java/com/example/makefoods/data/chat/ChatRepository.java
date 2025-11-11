// ChatRepository.java
package com.example.makefoods.data.chat;

public interface ChatRepository {
    interface Callback { void onSuccess(String reply); void onError(Throwable t); }
    void askGpt(String userText, Callback cb);
}
