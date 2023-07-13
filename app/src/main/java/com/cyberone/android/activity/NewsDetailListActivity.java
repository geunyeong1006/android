package com.cyberone.android.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cyberone.android.listadapter.NewsDetailAdapter;
import com.cyberone.android.listitem.NewsDetailItem;
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

import android.net.Uri;

public class NewsDetailListActivity extends AppCompatActivity {
    ArrayList<NewsDetailItem> newsDetailItemList;
    List<Map<String, Object>> newsDetailList;  // newsList 변수를 멤버 변수로 선언

    String regDday = "";

    private ListView detailListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsdetaillist);

        // 뷰 초기화
        detailListView = findViewById(R.id.newsDetailListView);

        Intent intent = getIntent();
        regDday = intent.getStringExtra("regDday"); // 데이터 추출

        // newsAdapter 초기화 전에 newsDataList를 초기화합니다.
        this.InitializeNewsDetailData();

        // newsAdapter 초기화 시점 변경
        final NewsDetailAdapter newsDetailAdapter = new NewsDetailAdapter(this, newsDetailItemList);

        // newsAdapter를 listView에 설정합니다.
        detailListView.setAdapter(newsDetailAdapter);

        newsClippingDetail();
        detailListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewsDetailListActivity.this);
                builder.setTitle(newsDetailList.get(i).get("bbsTit").toString());  // 팝업 제목
                builder.setMessage(newsDetailList.get(i).get("bbsCont").toString());  // 팝업 메시지 내용

                // 확인 버튼 클릭 시 동작할 이벤트 리스너
                builder.setPositiveButton("뉴스로 이동", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 확인 버튼 클릭 시 수행할 동작
                        if(newsDetailList.get(i).get("rssLink") != null && newsDetailList.get(i).get("rssLink") != ""){
                            String link = newsDetailList.get(i).get("rssLink").toString(); // 링크 데이터 추출

                            // URI 생성
                            Uri uri = Uri.parse(link);

                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(uri);
                            startActivity(intent);
                        }else{
                            Toast.makeText(getApplicationContext(), "뉴스 링크가 없습니다.", Toast.LENGTH_SHORT).show();
                            // 팝업 닫기
                            dialog.dismiss();
                        }

                    }
                });

                // 취소 버튼 클릭 시 동작할 이벤트 리스너
                builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 취소 버튼 클릭 시 수행할 동작
                        // 팝업 닫기
                        dialog.dismiss();
                    }
                });

                // AlertDialog 객체 생성
                AlertDialog dialog = builder.create();

                // 팝업을 화면에 표시
                dialog.show();
//                Toast.makeText(getApplicationContext(),
//                        newsDetailItemList.get(i).getRegDtime() + " " + newsDetailItemList.get(i).getBbsTit(),
//                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // 이전 액티비티로 이동하려면 다음과 같이 startActivity() 메서드를 사용합니다.
        Intent intent = new Intent(this, NewsListActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    void newsClippingDetail() {
        Log.w("newClipping","뉴스 받아오는중");
        try {
            NewsDetailListActivity.CustomTask task = new NewsDetailListActivity.CustomTask();
            String result = task.execute(regDday).get();
            Log.w("받은값",result);
            ObjectMapper objectMapper = new ObjectMapper();
            // JSON 문자열을 key-value 형태의 객체로 변환
            if (result == "" || result == null) {
                Toast.makeText(this,
                        "오류 발생, 이전화면으로 돌아갑니다.",
                        Toast.LENGTH_LONG).show();

                Intent intent2 = new Intent(NewsDetailListActivity.this, NewsListActivity.class);
                startActivity(intent2);  // 새 액티비티를 열어준다.
            } else {
                newsDetailList = objectMapper.readValue(result, new TypeReference<List<Map<String, Object>>>(){});

                if (newsDetailList.size() > 0) {
                    for (int i = 0; i < newsDetailList.size(); i++) {
                        newsDetailItemList.add(new NewsDetailItem((String) newsDetailList.get(i).get("regDtime"),
                                (String) newsDetailList.get(i).get("bbsTit")));
                    }
                }else{
                    Toast.makeText(NewsDetailListActivity.this, "오류 발생, 이전페이지로 돌아갑니다.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this, NewsListActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str = strings[0];
                String baseUrl = "http://10.0.2.2:8080/api/newsClippingDetail";
                String urlString = baseUrl + "?regDdate=" + str; // 요청 URL
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuilder buffer = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    receiveMsg = buffer.toString();
                    reader.close();
                } else {
                    // 통신이 실패한 이유를 찍기위한 로그
                    Log.i("통신 결과", conn.getResponseCode() + "에러");
                }
                conn.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return receiveMsg;
        }
    }


    public void InitializeNewsDetailData()
    {
        newsDetailItemList = new ArrayList<NewsDetailItem>();

    }
}
