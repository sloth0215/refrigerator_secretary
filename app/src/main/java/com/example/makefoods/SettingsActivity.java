package com.example.makefoods;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * 설정 화면 Activity
 * - 뒤로가기 버튼 포함
 * - 나중에 Firebase 로그인 기능 추가 예정
 */
public class SettingsActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Toolbar 설정
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 뒤로가기 버튼 표시
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("설정");
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 뒤로가기 버튼 클릭 시 현재 Activity 종료
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}