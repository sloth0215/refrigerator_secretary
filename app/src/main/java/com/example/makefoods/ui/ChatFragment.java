package com.example.makefoods.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.makefoods.R;
import com.example.makefoods.model.Message;
import com.example.makefoods.ui.chat.ChatAdapter;
import com.example.makefoods.ui.chat.ChatViewModel;

public class ChatFragment extends Fragment {

    private ChatViewModel viewModel;
    private ChatAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //  RecyclerView & Adapter 설정
        RecyclerView rv = view.findViewById(R.id.recyclerViewChat);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ChatAdapter();
        rv.setAdapter(adapter);

        // 키보드가 올라올 때 Fragment 전체를 위로 올리기
        // 루트 뷰(LinearLayout)에 WindowInsets 적용
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            // IME(키보드)의 높이를 가져옴
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomInset = imeInsets.bottom;

            // 루트 뷰의 하단 패딩을 키보드 높이만큼 설정
            // 이렇게 하면 입력창이 키보드 위로 올라감
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    bottomInset
            );

            // WindowInsets를 소비했다고 알림
            return insets;
        });

        // ViewModel 설정
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        // 메시지 리스트 관찰 및 화면 업데이트
        viewModel.getMessages().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            // 새 메시지가 추가되면 맨 아래로 스크롤
            rv.scrollToPosition(Math.max(0, adapter.getItemCount() - 1));
        });

        // 입력창 & 전송버튼 설정
        EditText etMessage = view.findViewById(R.id.etMessage);
        ImageButton btnSend = view.findViewById(R.id.btnSend);

        // 전송 버튼 클릭 이벤트
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                viewModel.sendUserMessage(text);
                etMessage.setText("");  // 입력창 비우기
            }
        });
    }
}