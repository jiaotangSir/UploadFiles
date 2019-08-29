package com.haocang.uploadfiles.uploadutil;


import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * 上传工具类
 *
 */
public class UploadUtil extends Request<String> {

    private MultipartEntity entity = new MultipartEntity();

    private final Response.Listener<String> mListener;

    private List<File> mFileParts;
    private String mFilePartName;
    private JSONObject mParams;
    /**
     * 单个文件
     * @param url
     * @param errorListener
     * @param listener
     * @param filePartName
     * @param file
     * @param params
     */
    public UploadUtil(String url, Response.ErrorListener errorListener,
                      Response.Listener<String> listener, String filePartName, File file,
                      JSONObject params) {
        super(Method.POST, url, errorListener);
        mFileParts = new ArrayList<File>();
        if (file != null) {
            mFileParts.add(file);
            mFileParts.add(file);
        }
        mFilePartName = filePartName;
        mListener = listener;
        mParams = params;
        buildMultipartEntity();
    }
    /**
     * 多个文件，对应一个key
     * @param url
     * @param errorListener
     * @param listener
     * @param filePartName
     * @param files
     * @param params
     */
    public UploadUtil(String url, Response.ErrorListener errorListener,
                      Response.Listener<String> listener, String filePartName,
                      List<File> files, JSONObject params) {
        super(Method.POST, url, errorListener);
        mFilePartName = filePartName;
        mListener = listener;
        mFileParts = files;
        mParams = params;
        buildMultipartEntity();
    }

    private void buildMultipartEntity() {
        if (mFileParts != null && mFileParts.size() > 0) {
            for (File file : mFileParts) {
                entity.addPart(mFilePartName, new FileBody(file));
            }
            long l = entity.getContentLength();
        }

        try {
            entity.addPart("inputObj",new StringBody(mParams.toString(), Charset
                    .forName("UTF-8")));
//            if (mParams != null && mParams.size() > 0) {  
//                for (Map.Entry<String, String> entry : mParams.entrySet()) {  
//                    entity.addPart(  
//                            entry.getKey(),  
//                            new StringBody(entry.getValue(), Charset  
//                                    .forName("UTF-8")));  
//                }  
//            }  
        } catch (UnsupportedEncodingException e) {
            VolleyLog.e("UnsupportedEncodingException");
        }
    }

//    @Override
//    public String getBodyContentType() {
//        return entity.getContentType().getValue();
//    }


    @Override
    public String getBodyContentType() {
        //return super.getBodyContentType();
        return entity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            entity.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        if (VolleyLog.DEBUG) {
            if (response.headers != null) {
                for (Map.Entry<String, String> entry : response.headers
                        .entrySet()) {
                    VolleyLog.d(entry.getKey() + "=" + entry.getValue());
                }
            }
        }

        String parsed;
        try {
            parsed = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed,
                HttpHeaderParser.parseCacheHeaders(response));
    }


    /* 
     * (non-Javadoc) 
     *  
     * @see com.android.volley.Request#getHeaders() 
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        VolleyLog.d("getHeaders");
        Map<String, String> headers = super.getHeaders();

        if (headers == null || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<String, String>();
        }


        return headers;
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}  