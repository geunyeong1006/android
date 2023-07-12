package com.cyberone.android;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class Login extends AppCompatActivity implements View.OnClickListener {

    Button signup_btn;                 // 회원가입 버튼
    Button login_btn;                // 로그인 버튼

    EditText id_edit;                // id 에디트
    EditText pw_edit;                // pw 에디트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signup_btn = (Button)findViewById(R.id.buttonSignUp);    // 회원가입 버튼을 찾고
        login_btn = (Button)findViewById(R.id.buttonLogin);  // 로그인 버튼을 찾고

        signup_btn.setOnClickListener(this);                 // 리스너를 달아줌.
        login_btn.setOnClickListener(this);                // 리스너를 달아줌.

        id_edit = (EditText)findViewById(R.id.editTextId);    // id 에디트를 찾음.
        pw_edit = (EditText)findViewById(R.id.editTextPassword);    // pw 에디트를 찾음.
    }

    public void onClick(View v) {
        if (v.getId() == R.id.buttonSignUp) {     // 회원가입 버튼을 눌렀을 때
            Intent intent = new Intent(Login.this, Signup.class);
            startActivity(intent);  // 새 액티비티를 열어준다.
            finish();               // 현재의 액티비티는 끝내준다.
        } else if (v.getId() == R.id.buttonLogin) {    // 로그인 버튼을 눌렀을 때
            login();
        }
    }

    void login() {
        Log.w("login","로그인 하는중");
        try {
            String id = id_edit.getText().toString();
            String pw = pw_edit.getText().toString();
            Log.w("앱에서 보낸값",id+", "+pw);

            Login.CustomTask task = new Login.CustomTask();
            String result = task.execute(id,pw).get();
            Log.w("받은값",result);
            if(result.isEmpty()){
                Toast.makeText(this, "회원이 아닙니다.", Toast.LENGTH_SHORT).show();
            }else{
                ObjectMapper objectMapper = new ObjectMapper();
                // JSON 문자열을 key-value 형태의 객체로 변환
                HashMap myObject = objectMapper.readValue(result, HashMap.class);
                if(myObject.isEmpty()){

                }
                Intent intent2 = new Intent(Login.this, List.class);
                startActivity(intent2);
                finish();
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
                String str;
                URL url = new URL("http://10.0.2.2:8080/api/login");  // 어떤 서버에 요청할지(localhost 안됨.)
                // ex) http://123.456.789.10:8080/hello/android
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");                              //데이터를 POST 방식으로 전송합니다.
                conn.setDoOutput(true);

                // 서버에 보낼 값 포함해 요청함.
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "id="+strings[0]+"&pw="+strings[1]; // GET방식으로 작성해 POST로 보냄 ex) "id=admin&pwd=1234";
                osw.write(sendMsg);                           // OutputStreamWriter에 담아 전송
                osw.flush();

                // jsp와 통신이 잘 되고, 서버에서 보낸 값 받음.
                if(conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();
                } else {    // 통신이 실패한 이유를 찍기위한 로그
                    Log.i("통신 결과", conn.getResponseCode()+"에러");
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
}