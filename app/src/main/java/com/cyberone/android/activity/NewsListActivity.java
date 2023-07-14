package com.cyberone.android.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cyberone.android.listadapter.NewsAdapter;
import com.cyberone.android.listitem.NewsItem;
import com.cyberone.android.R;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsListActivity extends AppCompatActivity {

    private String id;

    private String param;

    private ArrayList<NewsItem> newsItemList;
    private List<Map<String, Object>> newsList;  // newsList 변수를 멤버 변수로 선언

    private List<Map<String, Object>> alarmList;  // alarmList 변수를 멤버 변수로 선언

    private String apiName;

    private ListView newsListView;

    private ImageView notiIconView;

    private final long finishmeed =1000;

    private long presstime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newslist);


        Intent intent = getIntent();
        id = intent.getStringExtra("id"); // 데이터 추출

        // 뷰 초기화
        newsListView = findViewById(R.id.newsListView);

        notiIconView = findViewById(R.id.notificationIcon);

        // newsAdapter 초기화 전에 newsDataList를 초기화합니다.
        this.InitializeNewsData();

        // newsAdapter 초기화 시점 변경
        final NewsAdapter newsAdapter = new NewsAdapter(this, newsItemList);

        // newsAdapter를 listView에 설정합니다.
        newsListView.setAdapter(newsAdapter);

        selectNewsList();
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent2 = new Intent(NewsListActivity.this, NewsDetailListActivity.class);
                intent2.putExtra("regDday", newsList.get(i).get("regDday").toString()); // 데이터 전달
                intent2.putExtra("id", id);
                startActivity(intent2);

            }
        });

        selectNewsAlarmList();

        notiIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(NewsListActivity.this, notiIconView);
                // 메뉴 항목을 동적으로 추가합니다.
                for (int i = 0; i < alarmList.size(); i++) {
                    Map<String, Object> alarm = alarmList.get(i);
                    String alarmDate = alarm.get("alarmdate").toString();
                    String menuItemTitle = "[" + alarmDate + "] 보안 뉴스가 있습니다.";
                    popupMenu.getMenu().add(0, i, i, menuItemTitle);

                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int itemId = menuItem.getItemId();
                        Map<String, Object> selectedItem = alarmList.get(itemId);
                        String alarmDate = selectedItem.get("alarmdate").toString();
                        updateNewsAlarm(id , alarmDate);
                        // 선택한 메뉴 항목에 대한 동작을 정의합니다.
                        // 예를 들어, 선택한 항목의 alarmDate를 사용하여 특정 작업을 수행할 수 있습니다.
                        return true;
                    }
                });

                popupMenu.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - presstime;

        if(0 <= intervalTime && finishmeed >= intervalTime){
            finish();
        }else{

            presstime = tempTime;
            Toast.makeText(getApplicationContext(), "한번더 누르시면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    void selectNewsList() {
        Log.w("selectNewsList","뉴스 받아오는중");
        apiName = "selectNewsList";
        param = "regDay = " + "''";
        try {
            NewsListActivity.CustomTask task = new NewsListActivity.CustomTask();
            String result = task.execute("").get();
            Log.w("받은값",result);
            ObjectMapper objectMapper = new ObjectMapper();
            // JSON 문자열을 key-value 형태의 객체로 변환
            if (result == "" || result == null) {

            } else {
                newsList = objectMapper.readValue(result, new TypeReference<List<Map<String, Object>>>(){});
                if (newsList != null) {
                    for (int i = 0; i < newsList.size(); i++) {
                        newsItemList.add(new NewsItem((String) newsList.get(i).get("newsTitle")));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void selectNewsAlarmList(){
        Log.w("selectNewsAlarmList","계정할당 Rss알림 받아오는중");
        apiName = "selectNewsAlarmList";
        param = "id = " + id;
        try {
            NewsListActivity.CustomTask task = new NewsListActivity.CustomTask();
            String result = task.execute(id).get();
            ObjectMapper objectMapper = new ObjectMapper();
            // JSON 문자열을 key-value 형태의 객체로 변환
            if (result == "" || result == null) {

            } else {
                alarmList = objectMapper.readValue(result, new TypeReference<List<Map<String, Object>>>(){});
                alarmList = objectMapper.readValue(result, new TypeReference<List<Map<String, Object>>>(){});
                if (alarmList.size()> 0) {
                    for (int i = 0; i < alarmList.size(); i++) {
                        String alarmyn = alarmList.get(i).get("alarmyn").toString();
                        if(alarmyn == "n"){
                            notiIconView.setImageResource(R.drawable.is_alarm);
                            continue;
                        }else{
                            notiIconView.setImageResource(R.drawable.no_alarm);
                        }
                    }
                }else {
                    notiIconView.setImageResource(R.drawable.no_alarm);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    void updateNewsAlarm(String id, String alarmdate){
        Log.w("updateNewsAlarm","뉴스 알림 확인");
        apiName = "updateNewsAlarm";
        param = "id = " + id + "&alarmdate = " + alarmdate;
        try {
            NewsListActivity.CustomTask task = new NewsListActivity.CustomTask();
            String result = task.execute(id , alarmdate).get();
            ObjectMapper objectMapper = new ObjectMapper();
            // JSON 문자열을 key-value 형태의 객체로 변환
            if (result == "" || result == null) {

            } else {
                alarmList = objectMapper.readValue(result, new TypeReference<List<Map<String, Object>>>(){});
                if (alarmList.size()> 0) {
                    notiIconView.setImageResource(R.drawable.is_alarm);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;
        @Override
        // doInBackground의 매개변수 값이 여러개일 경우를 위해 배열로
        protected String doInBackground(String... strings) {
            try {
                String str = strings[0];
                String baseUrl = "http://10.0.2.2:8080/api/" + apiName;
                String urlString = baseUrl + "?" +param ; // 요청 URL
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("GET");

                // 서버에 보낼 값이 없으므로 요청 본문에 아무 내용도 작성하지 않습니다.

                // jsp와 통신이 잘 되고, 서버에서 보낸 값 받음.
                if (conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();
                } else {
                    // 통신이 실패한 이유를 찍기위한 로그
                    Log.i("통신 결과", conn.getResponseCode() + "에러");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 서버에서 보낸 값을 리턴합니다.
            return receiveMsg;
        }
    }

    public void InitializeNewsData()
    {
        newsItemList = new ArrayList<NewsItem>();

    }
}
