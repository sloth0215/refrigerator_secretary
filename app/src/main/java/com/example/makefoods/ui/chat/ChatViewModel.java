package com.example.makefoods.ui.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.makefoods.BuildConfig;
import com.example.makefoods.data.chat.ChatRepository;
import com.example.makefoods.data.chat.ChatRepositoryImpl;
import com.example.makefoods.model.Message;

import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends ViewModel {

    private final MutableLiveData<List<Message>> messages = new MutableLiveData<>(new ArrayList<>());

    //Gradle 빌드 설정에서 불러온 OpenAI API 키를 이용해
    //ChatRepositoryImpl 객체를 생성하고
    //repo라는 이름의 변하지 않는( final ) 변수로 저장
    private final ChatRepository repo = new ChatRepositoryImpl(BuildConfig.OPENAI_API_KEY);

    public LiveData<List<Message>> getMessages() { return messages; }

    public void sendUserMessage(String text) {
        List<Message> list = new ArrayList<>(messages.getValue());
        list.add(new Message(text, Message.Sender.USER));
        messages.setValue(list);

        repo.askGpt(text, new ChatRepository.Callback() {
            @Override
            public void onSuccess(String reply) {
                List<Message> cur = new ArrayList<>(messages.getValue());
                cur.add(new Message(reply, Message.Sender.BOT));
                messages.postValue(cur);
            }

            @Override
            public void onError(Throwable t) {
                List<Message> cur = new ArrayList<>(messages.getValue());
                cur.add(new Message("오류가 발생했어요. 잠시 후 다시 시도해주세요.", Message.Sender.BOT));
                messages.postValue(cur);
            }
        });
    }
}