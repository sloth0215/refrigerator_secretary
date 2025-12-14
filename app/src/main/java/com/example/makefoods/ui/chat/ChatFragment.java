package com.example.makefoods.ui.chat;

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


        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomInset = imeInsets.bottom;
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    bottomInset
            );
            return insets;
        });

        // ViewModel 설정
        viewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(ChatViewModel.class);

        // 메시지 리스트 관찰
        viewModel.getMessages().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            rv.scrollToPosition(Math.max(0, adapter.getItemCount() - 1));
        });

        // 레시피 버튼 클릭 리스너 설정
        adapter.setOnRecipeClickListener(recipeName -> {
            android.util.Log.d("ChatFragment", "레시피 버튼 클릭: " + recipeName);
            viewModel.requestRecipeDetail(recipeName);
        });

        // 입력창 & 전송버튼 설정
        EditText etMessage = view.findViewById(R.id.etMessage);
        ImageButton btnSend = view.findViewById(R.id.btnSend);

        // 전송 버튼 클릭 이벤트
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                viewModel.sendUserMessage(text);
                etMessage.setText("");
            }
        });
    }
}