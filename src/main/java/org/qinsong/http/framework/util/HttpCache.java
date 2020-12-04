package org.qinsong.http.framework.util;


import org.qinsong.http.Log;
import org.qinsong.http.framework.HttpEnum.CacheMode;
import org.qinsong.http.framework.RequestParams;
import org.qinsong.http.framework.ResponseParams;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by song on 2016/9/18.
 * 缓存
 * 目前实现 ERR_CACHE CLIENT_CACHE
 * <p>
 * 后期把okhttp的缓存移到这里?
 */
public class HttpCache {

    private static HttpCache instance;
    private final String TAG = "HttpCache";
    private String path;

    public static HttpCache instance() {
        if (instance == null)
            instance = new HttpCache();
        instance.path = Utils.getDiskCacheDir();
        return instance;
    }

    private HttpCache() {
    }

    //检测使用缓存-联网前
    public ResponseParams checkAndGetCache(RequestParams request) {
        if (request.cacheMode() == CacheMode.CLIENT_CACHE) {
            int cacheTime = request.cacheTime();
            String file_name = getRequestMD5(request) + "_" + cacheTime;
            File file = new File(path, file_name);
            if (!file.exists())
                return null;
            long lastTime = file.lastModified();
            if (lastTime + cacheTime * 1000 < System.currentTimeMillis()) {
                file.delete();
                return null;
            }

            ResponseParams response = new ResponseParams();
            response.setRequestParams(request);
            response.setResultType(request.resultType());
            if (read(response, file_name))
                return response;
        }
        return null;
    }


    //检测使用缓存-联网失败后
    public boolean checkAndGetErrCache(ResponseParams response) {
        if (response.requestParams().cacheMode() != CacheMode.ERR_CACHE)
            return false;
        String MD5 = getRequestMD5(response.requestParams());
        return read(response, MD5);
    }

    //检测持久化缓存-联网成功后
    public boolean checkAndSaveCache(ResponseParams response) {
        if (response.isCache())
            return false;
        String MD5 = getRequestMD5(response.requestParams());

        if (response.requestParams().cacheMode() == CacheMode.ERR_CACHE) {
            return write(response, MD5);

        }

        if (response.requestParams().cacheMode() == CacheMode.CLIENT_CACHE) {
            int time = response.requestParams().cacheTime();
            String file_name = MD5 + "_" + time;
            return write(response, file_name);

        }
        //...

        return false;
    }


    private boolean read(ResponseParams response, String fileName) {
        RequestParams request = response.requestParams();
        String filePath = path + "/" + fileName;
        switch (request.resultType()) {
            case STRING:
                String s = Utils.readString(filePath);
                if (s == null)
                    return false;
                Log.i(TAG, "getCache->" + request.url() + "\n" + request.cacheMode() + "->" + s);
                response.setString(s);
                return true;
            case FILE:
                response.setFile(request.downloadPath());
                if (Utils.fileCopy(filePath, response.file())) {
                    Log.i(TAG, "getCache->" + request.url() + "\n" + request.cacheMode() + "->" + response.file());
                    return true;
                }
            case BYTES:
                byte[] b = Utils.readBytes(filePath);
                if (b == null || b.length == 0)
                    return false;
                Log.i(TAG, "getCache->" + request.url() + "\n" + request.cacheMode() + "->" + b.length);
                response.setBytes(b);
                return true;
        }
        return false;
    }

    private boolean write(ResponseParams response, String fileName) {
        String filePath = path + "/" + fileName;
        Log.i(TAG, "saveCache->" + response.requestParams().url() + "\n" + response.requestParams().cacheMode() + "->" + filePath);
        switch (response.requestParams().resultType()) {
            case STRING:
                return Utils.writerString(filePath, response.string());
            case FILE:
                return Utils.fileCopy(response.file(), filePath);
            case BYTES:
                return Utils.writerBytes(filePath, response.bytes());
        }
        return false;
    }


    private String getRequestMD5(RequestParams params) {
        if (params.requestBody() != null)
            return StringToMD5(params.url() + params.requestBody().getContent());
        else
            return StringToMD5(params.urlEncode());
    }

    private static String StringToMD5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(
                    string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }
}
