package com.haocang.uploadfiles;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.haocang.uploadfiles.uploadutil.FileUtils;
import com.haocang.uploadfiles.uploadutil.UploadUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 0;

    private RequestQueue mQueue;
    private Context context;

    private TextView file_url;
    private TextView upload_info;
    private Button start_upload;
    private Button  select_file;

    private String URL="";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        file_url= findViewById(R.id.file_url);
        upload_info = findViewById(R.id.upload_info);
        start_upload = findViewById(R.id.start_upload);
        select_file = findViewById(R.id.select_file);




        select_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showFileChooser();

            }
        });


        start_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 URL = file_url.getText().toString();
                if(!URL.equals("")){
                    requestPermission();//申请权限
                }else{
                    Toast.makeText(context, "请选择文件", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }



    /**
     * 请求授权
     */
    private void requestPermission(){

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){ //表示未授权时
            //进行授权
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else{
            //调用打电话的方法
            StartUp(new File(URL));
        }
    }


    /**===============================================
     * 创建时间：2018/8/27 9:58
     * 编 写 人：ShenC
     * 方法说明：选择图片
     *================================================
     * 修改内容：      修改时间：      修改人：
     *===============================================*/
    private void showFileChooser() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, FILE_SELECT_CODE);
    }



     /**===============================================
      * 创建时间：2018/8/24 10:50
      * 编 写 人：ShenC
      * 方法说明：上传单个文件，
      *================================================
      * 修改内容：      修改时间：      修改人：
      *===============================================*/
    private void StartUp(final File  file) {
        mQueue = Volley.newRequestQueue(context);

        //使用线程池来上传文件
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);
            final int index = 1;
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    String threadName = Thread.currentThread().getName();
                    Log.i("run", "线程：" + threadName + ",正在执行第" + index + "个任务");
                    List<File> files = new ArrayList<File>();
                    files.add(file);
                    uploadFile(files);
                }
            });

    }


        private void  	uploadFile(List<File> f_list) {
            try {

                JSONObject params = new JSONObject();
                UploadUtil request = new UploadUtil("http://218.202.112.190:8001/PhotoUpload.aspx",                      new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(context,"附件提交失败,请求出错！", Toast.LENGTH_LONG).show();
                                error.printStackTrace();
                            }
                        }, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // TODO 自动生成的方法存根
                        try {
                            JSONObject josnobject = new JSONObject(response);
                            if (josnobject.getBoolean("Result")) {

                                    Toast.makeText(context,"附件上传成功！", Toast.LENGTH_SHORT).show();
                                    upload_info.setText(josnobject.getString("NAME") +","
                                            +  josnobject.getString("TYPE")+ ","
                                            +josnobject.getString("URL"));

                            } else {
                                Toast.makeText(context,"附件上传失败！"+ josnobject.getString("Msg"),Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(context,"附件上传失败！" + e.toString(),Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }, "f_file[]", f_list, params);

                mQueue.add(request);

            } catch (Exception e) {
                e.printStackTrace();
            }



        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {

                    if(data != null){
                        Uri uri = data.getData();
                        String url = FileUtils.getRealFilePath(context,uri);
                        file_url.setText(url);
                    }else{
                        file_url.setText("");
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }




    /**
     * 权限申请返回结果
     * @param requestCode 请求码
     * @param permissions 权限数组
     * @param grantResults  申请结果数组，里面都是int类型的数
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){ //同意权限申请
                    StartUp(new File(URL));
                }else { //拒绝权限申请
                    Toast.makeText(this,"权限被拒绝了",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }




}
