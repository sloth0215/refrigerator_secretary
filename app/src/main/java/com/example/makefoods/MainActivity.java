package com.example.makefoods;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.makefoods.ui.CameraFragment;
import com.example.makefoods.ui.ChatFragment;
import com.example.makefoods.ui.FridgeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // BottomNavigationView 먼저 찾기
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setItemTextColor(null);

        // 루트 뷰에 인셋 적용, 키보드 보이면 바텀바 숨기기
        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            boolean imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());

            // 상단/좌우는 시스템 바 여백만, 하단은 직접 조절
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);

            // 바텀바는 네비게이션바 높이만큼만 padding
            bottomNavigationView.setPadding(0, 0, 0, systemBars.bottom);

            // 키보드 보이면 바텀바 숨기고, 키보드 없으면 다시 보이게
            bottomNavigationView.setVisibility(imeVisible ? View.GONE : View.VISIBLE);

            return insets;
        });

        // Toolbar 설정
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Fragment Manager 초기화
        fragmentManager = getSupportFragmentManager();

        // BottomNavigationView 초기화
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setItemTextColor(null);

        // 기본 Fragment 로드
        if (savedInstanceState == null) {
            loadFragment(new FridgeFragment());
        }

        // BottomNavigationView 아이템 선택 리스너
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                selectedFragment = new FridgeFragment();
            } else if (itemId == R.id.navigation_recipe) {
                selectedFragment = new ChatFragment();
            } else if (itemId == R.id.navigation_profile) {
                selectedFragment = new CameraFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    /**
     * 메뉴 생성 (설정 아이콘 표시)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    /**
     * 메뉴 아이템 클릭 처리
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            //SettingsActivity로 이동 (Fragment 대신 Activity)
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Fragment를 로드하는 메서드
     */
    private void loadFragment(@NonNull Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}