package com.cyberone.android;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class List extends AppCompatActivity {

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // 뷰 초기화
        listView = findViewById(R.id.listView);

        // 리스트 뷰에 데이터 설정 등 필요한 로직을 구현하세요
        // ...
    }
}
