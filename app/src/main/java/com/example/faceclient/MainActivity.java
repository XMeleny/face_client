package com.example.faceclient;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    EditText etIp;
    Button btnSend;
    Button btnTakePhoto;
    Button btnGetPhoto;
    ImageView ivPhoto;
    ImageView ivTemp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etIp=findViewById(R.id.et_ip);
        btnSend=findViewById(R.id.btn_send);
        btnTakePhoto=findViewById(R.id.btn_take_photo);
        btnGetPhoto=findViewById(R.id.btn_get_photo);
        ivPhoto=findViewById(R.id.iv_photo);
        ivTemp=findViewById(R.id.iv_temp);

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

    }
//
//    ////////////////////////////////////////////////////////////////////////////////////////////////
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }
//
//
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ivPhoto.setImageBitmap(imageBitmap);

            String image_str=ImageEncodeToString(imageBitmap);
            sendRequestWithOKHttp(image_str);

        }
    }

    private String ImageEncodeToString(Bitmap bitmap)
    {
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        byte[] bytes=byteArrayOutputStream.toByteArray();
        byte[] encode= Base64.encode(bytes,Base64.DEFAULT);
        return new String(encode);
    }

        private void sendRequestWithOKHttp(final String image_str) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client=new OkHttpClient();
                    FormBody body=new FormBody.Builder()
                            .add("image_str",image_str)
                            .build();

                    String url=etIp.getText().toString();
                    Request request=new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();

                    Response response=client.newCall(request).execute();
                    String responseData=response.body().string();
                    showResponse(responseData);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showResponse(String responseData) {

        Toast.makeText(this, ""+responseData, Toast.LENGTH_SHORT).show();
//        Log.d(TAG, "showResponse: "+responseData);
    }

}
