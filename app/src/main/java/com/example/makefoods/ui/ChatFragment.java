package com.example.makefoods.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

        //  RecyclerView & Adapter
        RecyclerView rv = view.findViewById(R.id.recyclerViewChat);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ChatAdapter();
        rv.setAdapter(adapter);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        viewModel.getMessages().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            rv.scrollToPosition(Math.max(0, adapter.getItemCount() - 1));
        });

        // 입력창 & 전송버튼
        EditText etMessage = view.findViewById(R.id.etMessage);
        ImageButton btnSend = view.findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                viewModel.sendUserMessage(text);
                etMessage.setText("");
            }
        });
    }
}
