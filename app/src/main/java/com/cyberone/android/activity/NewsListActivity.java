package com.cyberone.android.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
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
import java.util.List;
import java.util.Map;

public class NewsListActivity extends AppCompatActivity {

    ArrayList<NewsItem> newsItemList;
    List<Map<String, Object>> newsList;  // newsList 변수를 멤버 변수로 선언

    private ListView listView;

    private final long finishmeed =1000;
    private long presstime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newslist);

        // 뷰 초기화
        listView = findViewById(R.id.newsListView);

        // newsAdapter 초기화 전에 newsDataList를 초기화합니다.
        this.InitializeNewsData();

        // newsAdapter 초기화 시점 변경
        final NewsAdapter newsAdapter = new NewsAdapter(this, newsItemList);

        // newsAdapter를 listView에 설정합니다.
        listView.setAdapter(newsAdapter);

        newsClipping();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent2 = new Intent(NewsListActivity.this, NewsDetailListActivity.class);
                intent2.putExtra("regDday", newsList.get(i).get("regDday").toString()); // 데이터 전달
                startActivity(intent2);
//                Toast.makeText(getApplicationContext(),
//                        newsDataList.get(i).getNewsTitle(),
//                        Toast.LENGTH_SHORT).show();
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

    void newsClipping() {
        Log.w("newClipping","뉴스 받아오는중");
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

    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;
        @Override
        // doInBackground의 매개변수 값이 여러개일 경우를 위해 배열로
        protected String doInBackground(String... strings) {
            try {
                String str = strings[0];
                String baseUrl = "http://10.0.2.2:8080/api/newsClipping";
                String urlString = baseUrl + "?regDdate=" + str; // 요청 URL
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
