package com.cyberone.android.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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

    private String id;

    private String param;
    String regDday = "";

    private String apiName;

    private ListView detailListView;

    private ImageView notiIconView;

    private List<Map<String, Object>> alarmList;  // newsList 변수를 멤버 변수로 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsdetaillist);

        Intent intent = getIntent();
        id = intent.getStringExtra("id"); // 데이터 추출
        regDday = intent.getStringExtra("regDday"); // 데이터 추출

        // 뷰 초기화
        detailListView = findViewById(R.id.newsDetailListView);

        notiIconView = findViewById(R.id.notificationIcon);

        // newsAdapter 초기화 전에 newsDataList를 초기화합니다.
        this.InitializeNewsDetailData();

        // newsAdapter 초기화 시점 변경
        final NewsDetailAdapter newsDetailAdapter = new NewsDetailAdapter(this, newsDetailItemList);

        // newsAdapter를 listView에 설정합니다.
        detailListView.setAdapter(newsDetailAdapter);

        selectNewsDetailList();
        detailListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewsDetailListActivity.this);
                builder.setTitle(String.valueOf(newsDetailList.get(i).get("bbsTit")));  // 팝업 제목
                builder.setMessage(String.valueOf(newsDetailList.get(i).get("bbsCont")));  // 팝업 메시지 내용

                // 확인 버튼 클릭 시 동작할 이벤트 리스너
                builder.setPositiveButton("뉴스로 이동", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 확인 버튼 클릭 시 수행할 동작
                        if(newsDetailList.get(i).get("rssLink") != null && newsDetailList.get(i).get("rssLink") != ""){
                            String link = String.valueOf(newsDetailList.get(i).get("rssLink")); // 링크 데이터 추출

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

            }
        });

        selectNewsAlarmList();

        notiIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(NewsDetailListActivity.this, notiIconView);
                // 메뉴 항목을 동적으로 추가합니다.
                for (int i = 0; i < alarmList.size(); i++) {
                    Map<String, Object> alarm = alarmList.get(i);
                    if("n".equals(alarm.get("alarmyn"))) {
                        String alarmDate = String.valueOf(alarm.get("alarmdate"));
                        String menuItemTitle = "[" + alarmDate + "] 보안 뉴스가 있습니다.";
                        popupMenu.getMenu().add(0, i, i, menuItemTitle);
                    }

                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int itemId = menuItem.getItemId();
                        Map<String, Object> selectedItem = alarmList.get(itemId);
                        String alarmDate = String.valueOf(selectedItem.get("alarmdate"));
                        AlertDialog.Builder builder = new AlertDialog.Builder(NewsDetailListActivity.this);
                        builder.setTitle("확인상자");  // 대화상자 제목
                        builder.setMessage("알림을 지우시겠습니까?");  // 대화상자 메시지

                        // 확인 버튼 클릭 시 동작할 이벤트 리스너
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 확인 버튼 클릭 시 수행할 동작
                                // TODO: 확인 버튼을 클릭한 경우에 대한 동작을 구현합니다.
                                updateNewsAlarm(alarmDate);
                            }
                        });

                        // 취소 버튼 클릭 시 동작할 이벤트 리스너
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 취소 버튼 클릭 시 수행할 동작
                                // TODO: 취소 버튼을 클릭한 경우에 대한 동작을 구현합니다.
                                // 대화상자 닫기
                                dialog.dismiss();
                            }
                        });

                        // AlertDialog 객체 생성
                        AlertDialog dialog = builder.create();

                        // 대화상자를 화면에 표시
                        dialog.show();

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
        // 이전 액티비티로 이동하려면 다음과 같이 startActivity() 메서드를 사용합니다.
        Intent intent = new Intent(this, NewsListActivity.class);
        intent.putExtra("id", id);

        startActivity(intent);
    }

    void selectNewsAlarmList(){
        Log.w("selectNewsAlarmList","계정할당 Rss알림 받아오는중");
        apiName = "selectNewsAlarmList";
        param = "id=" + id.trim();
        try {
            NewsDetailListActivity.CustomTask task = new NewsDetailListActivity.CustomTask();
            String result = task.execute(id).get();
            ObjectMapper objectMapper = new ObjectMapper();
            // JSON 문자열을 key-value 형태의 객체로 변환
            if (result == "" || result == null) {

            } else {
                alarmList = objectMapper.readValue(result, new TypeReference<List<Map<String, Object>>>(){});
                if (alarmList.size()> 0) {
                    for (int i = 0; i < alarmList.size(); i++) {
                        String alarmyn = String.valueOf(alarmList.get(i).get("alarmyn"));
                        if("n".equals(alarmyn)){
                            notiIconView.setImageResource(R.drawable.is_alarm);
                            break;
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


    void selectNewsDetailList() {
        Log.w("selectNewsDetailList","뉴스 받아오는중");
        apiName = "selectNewsDetailList";
        param = "regDday=" + regDday.trim();
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

    void updateNewsAlarm(String alarmdate){
        Log.w("updateNewsAlarm","뉴스 알림 확인");
        apiName = "updateNewsAlarm";
        param = "id=" + id.trim() +  "&alarmdate=" + alarmdate.trim();
        try {
            NewsDetailListActivity.CustomTask task = new NewsDetailListActivity.CustomTask();
            task.execute(alarmdate).get();
            // JSON 문자열을 key-value 형태의 객체로 변환
            selectNewsAlarmList();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str = strings[0];
                String baseUrl = "http://10.0.2.2:8080/api/" + apiName;;
                String urlString = baseUrl + "?"+param; // 요청 URL
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
