package com.example.faceclient;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import java.io.File;
import java.io.FileNotFoundException;
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
    TextView count;
    public static int TAKE_PHOTO_REQUEST_CODE = 1; //拍照
    public static int PHOTO_REQUEST_CUT = 2; //裁切
    public static int PHOTO_REQUEST_GALLERY = 3; //相册
    public Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etIp=findViewById(R.id.et_ip);
//        btnSend=findViewById(R.id.btn_send);
        btnTakePhoto=findViewById(R.id.btn_take_photo);
//        btnGetPhoto=findViewById(R.id.btn_get_photo);
        ivPhoto=findViewById(R.id.iv_photo);
        ivTemp=findViewById(R.id.iv_temp);
        count=findViewById(R.id.count);

//        String img_str=ImageEncodeToString(BitmapFactory.decodeResource(getResources(), R.drawable.aaa, null));
//        Log.d(TAG, img_str);
//
//        Bitmap bitmap=stringToBitmap(img_str);
//        ivPhoto.setImageBitmap(bitmap);

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                dispatchTakePictureIntent();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy( builder.build() );
                }
                takePhotos();
            }
        });
    }
//
//    ////////////////////////////////////////////////////////////////////////////////////////////////
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private void takePhotos() {

        imageUri = Uri.fromFile(getImageStoragePath(this));
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //指定照片存储路径
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent,PHOTO_REQUEST_CUT);
    }
    private File getImageStoragePath(Context context){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"temp.jpg");
            return file;
        }
        return null;
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PHOTO_REQUEST_CODE){
            if (imageUri != null){
                startPhotoZoom(imageUri);

            }
        }
        //使用了这个显示图片
        else if (requestCode == PHOTO_REQUEST_CUT){
            if (imageUri != null) {
                Bitmap bitmap = decodeUriBitmap(imageUri);
                ivPhoto.setImageBitmap(bitmap);
                String image_str=ImageEncodeToString(bitmap);
                sendRequestWithOKHttp(image_str);
            }
        }else if (requestCode == PHOTO_REQUEST_GALLERY){
            if (data != null) {
                imageUri = data.getData();
                Bitmap bitmap = decodeUriBitmap(imageUri);
                ivPhoto.setImageBitmap(bitmap);
                String image_str=ImageEncodeToString(bitmap);
                sendRequestWithOKHttp(image_str);
            }
        }
    }

    private Bitmap decodeUriBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    //裁剪图片（可不要）
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");

        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", 800);
        intent.putExtra("outputY", 800);

        //设置了true的话直接返回bitmap，可能会很占内存
        intent.putExtra("return-data", false);
        //设置输出的格式
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        //设置输出的地址
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        //不启用人脸识别
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }
//选择相册图片（可选）
    private void choicePicFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
    }

    //    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
//
//    }
//
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            //显示略缩图
//            ivPhoto.setImageBitmap(imageBitmap);
//            String image_str=ImageEncodeToString(imageBitmap);
//            sendRequestWithOKHttp(image_str);
//
//        }
//    }

    private String ImageEncodeToString(Bitmap bitmap)
    {
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        byte[] bytes=byteArrayOutputStream.toByteArray();
        byte[] encode= Base64.encode(bytes,Base64.DEFAULT);
        return new String(encode);
    }
    public static Bitmap stringToBitmap(String string) {
//        Bitmap bitmap = null;
//        try {
//            byte[] bitmapArray = Base64.decode(string.split(",")[1], Base64.DEFAULT);
//            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return bitmap;
        byte[] decode = Base64.decode(string,Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
        return bitmap;
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
                    Log.d(TAG, "request");
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
//                Log.d(TAG, temp);
                String[] str_points=temp.split(",");
                int[] points=new int[4];
                for(int j=0;j<str_points.length;j++)
                {
                    points[j]=Integer.parseInt(str_points[j]);
                }
                Location location=new Location(points);
                locationList.add(location);
            }
//            JSONS jsonArrayImage= jsonObject.getJSONArray("image_str");
            String image_str=jsonObject.optString("image_str");
//            for (int i=0;i<jsonArrayImage.length();i++)
//            {
//                String temp=jsonArray.getString(i);
                Bitmap bitmap=stringToBitmap(image_str);
                ivPhoto.setImageBitmap(bitmap);
//            }
            String count1=jsonObject.optString("count");
            count.setText("人数："+count1);
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
