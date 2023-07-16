package com.cyberone.android.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cyberone.android.R;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    Button signup_btn;                 // 회원가입 버튼

    EditText id_edit;                   // id 에디트
    EditText userName_edit;            // userName 에디트
    EditText pw_edit;                  // pw 에디트
    EditText pw_conf_edit;             // pw 확인 에디트
    EditText email_edit;               // email 에디트


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signup_btn = (Button)findViewById(R.id.buttonSignUp);

        signup_btn.setOnClickListener(this);                 // 리스너를 달아줌.

        id_edit = (EditText)findViewById(R.id.editTextId);    // id 에디트를 찾음.
        userName_edit = (EditText)findViewById(R.id.editTextUserName);
        pw_edit = (EditText)findViewById(R.id.editTextPassword);
        pw_conf_edit =(EditText)findViewById(R.id.editTextConfirmPassword);
        email_edit = (EditText)findViewById(R.id.editTextEmail);

    }

    @Override
    public void onBackPressed() {
        // 이전 액티비티로 이동하려면 다음과 같이 startActivity() 메서드를 사용합니다.
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.buttonSignUp) {     // 회원가입 버튼을 눌렀을 때
            if(id_edit.getText().length() <= 3){
                Toast.makeText(this, "아아디는 최소 4글자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            }else if(pw_edit.getText().length() <= 7){
                Toast.makeText(this, "비밀번호는 최소 8글자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            }else if(!isEmail(String.valueOf(email_edit.getText()))){
                Toast.makeText(this, "이메일은 알맞은 형식으로 적어주세요", Toast.LENGTH_SHORT).show();
            } else if (!String.valueOf(pw_edit.getText()).equals(String.valueOf(pw_conf_edit.getText()))) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            } else{
                signup();
            }

        }
    }

    void signup() {
        Log.w("signup","회원가입 하는중");
        try {
            String id = String.valueOf(id_edit.getText());
            String username = String.valueOf(userName_edit.getText());
            String pw = String.valueOf(pw_edit.getText());
            String email = String.valueOf(email_edit.getText());

            Log.w("앱에서 보낸값",id+", "+username +", "+pw +", " + email);

            SignupActivity.CustomTask task = new SignupActivity.CustomTask();
            String result = task.execute(id,username,pw,email).get();
            if(result == "" || result == null){
                Toast.makeText(this, "회원가입 실패.", Toast.LENGTH_SHORT).show();
            }else{
                ObjectMapper objectMapper = new ObjectMapper();
                // JSON 문자열을 key-value 형태의 객체로 변환
                HashMap myObject = objectMapper.readValue(result, HashMap.class);
                if(myObject.get("fail") != null){
                    Toast.makeText(this, String.valueOf(myObject.get("fail")), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "회원가입 성공.", Toast.LENGTH_SHORT).show();
                    Intent intent2 = new Intent(SignupActivity.this, LoginActivity.class);
                    startActivity(intent2);
                }

            }
            Log.w("받은값",result);


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
                URL url = new URL("http://10.0.2.2:8080/api/signup");  // 어떤 서버에 요청할지(localhost 안됨.)
                // ex) http://123.456.789.10:8080/hello/android
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");                              //데이터를 POST 방식으로 전송합니다.
                conn.setDoOutput(true);

                // 서버에 보낼 값 포함해 요청함.
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "id="+strings[0]+"&username="+strings[1]+"&pw="+strings[2]+"&email="+strings[3]; // GET방식으로 작성해 POST로 보냄 ex) "id=admin&pwd=1234";
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

    public static boolean isEmail(String email){
        boolean returnValue = false;
        String regex = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        if(m.matches()){
            returnValue = true;
        }
        return returnValue;
    }
}