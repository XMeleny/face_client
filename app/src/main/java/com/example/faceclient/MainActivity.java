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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.security.spec.ECField;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

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

                    Response response=client.newCall(request).execute();//同步请求方式，若异步，execute改成enqueue
                    String responseData=response.body().string();//todo:maybe wrong, how to deal with json?

                    showResponse(responseData);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showResponse(String responseData) {
        Log.d(TAG, "showResponse: "+responseData);
        try{
            //todo:package it
            List<Location> locationList=new ArrayList<>();
            JSONObject jsonObject=new JSONObject(responseData);
            JSONArray jsonArray= jsonObject.getJSONArray("locations");
            for (int i=0;i<jsonArray.length();i++)
            {
                String temp=jsonArray.getString(i);
                temp=temp.replace("[","");
                temp=temp.replace("]","");
                Log.d(TAG, temp);
                String[] str_points=temp.split(",");
                int[] points=new int[4];
                for(int j=0;j<str_points.length;j++)
                {
                    points[j]=Integer.parseInt(str_points[j]);
                }
                Location location=new Location(points);
                locationList.add(location);
            }
            Log.d(TAG, ""+locationList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Location> handleLocation(String json_location) {
        List<Location> result=null;
        try {
            JSONObject jsonObject=new JSONObject(json_location);
            JSONArray jsonArray= jsonObject.getJSONArray("locations");
            for (int i=0;i<jsonArray.length();i++)
            {
                String temp=jsonArray.getString(i);
                temp=temp.replace("[","");
                temp=temp.replace("]","");
                Log.d(TAG, temp);
                String[] str_points=temp.split(",");
                int[] points=new int[4];
                for(int j=0;j<str_points.length;j++)
                {
                    points[j]=Integer.parseInt(str_points[j]);
                }
                Location location=new Location(points);
                result.add(location);
            }
            Log.d(TAG, "handleLocation: result length is :"+result.size());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return result;
    }

}
