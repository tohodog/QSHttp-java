package org.qinsong.http.framework.ok;

import okhttp3.OkHttpClient;
import okhttp3.*;
import okhttp3.internal.Util;
import okio.Okio;
import org.qinsong.http.Log;
import org.qinsong.http.framework.*;
import org.qinsong.http.framework.ability.IHttpProgress;
import org.qinsong.http.framework.ability.IHttpTask;
import org.qinsong.http.framework.util.Utils;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by song on 2016/9/14.
 * 具体联网逻辑
 */
public class OkHttpTask implements IHttpTask {

    private QSHttpConfig qsHttpConfig;
    private OkHttpClient mOkHttpClient;

    public OkHttpTask(QSHttpConfig qsHttpConfig) {
        this.qsHttpConfig = qsHttpConfig;
        mOkHttpClient = buildOkHttpClient();
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    private OkHttpClient buildOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(qsHttpConfig.connectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(qsHttpConfig.readTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(qsHttpConfig.writeTimeout(), TimeUnit.MILLISECONDS)
                //缓存 需服务器支持(头多个Cache-Control就可以了) 或自己拦截响应请求加上缓存标记 不推荐
                .cache(new Cache(new File(Utils.getDiskCacheDir()),
                        qsHttpConfig.cacheSize()));
        if (qsHttpConfig.hostnameVerifier() != null)
            builder.hostnameVerifier(qsHttpConfig.hostnameVerifier());
        if (qsHttpConfig.socketFactory() != null)
            builder.socketFactory(qsHttpConfig.socketFactory());
        return builder.build();

    }

    @Override
    public ResponseParams GET(RequestParams params, IHttpProgress hp) throws HttpException {
        Request request = getRequest(params, null);
        Response response = getResponse(editOkHttpClient(params, hp), request);
        return dealResponse(params, response);
    }

    @Override
    public ResponseParams P_FORM(RequestParams params, IHttpProgress hp) throws HttpException {
        RequestBody requestBody = buildFormBody(params.params(), params.charset());
        Request request = getRequest(params, requestBody);
        Response response = getResponse(editOkHttpClient(params, hp), request);
        return dealResponse(params, response);
    }

    @Override
    public ResponseParams P_BODY(RequestParams params, IHttpProgress hp) throws HttpException {
        RequestBody requestBody = buildRequestBody(params.requestBody().getContentType(), params.requestBody().getContent());
        if (hp != null)
            requestBody = new RequestBodyProgress(requestBody, hp);
        Request request = getRequest(params, requestBody);
        Response response = getResponse(editOkHttpClient(params, hp), request);
        return dealResponse(params, response);
    }

    @Override
    public ResponseParams P_MULTIPART(RequestParams params, IHttpProgress hp) throws HttpException {
        RequestBody multipartBody = buildMultipartBody(params.multipartType(), params.multipartBody(), hp);
        Request request = getRequest(params, multipartBody);
        Response response = getResponse(editOkHttpClient(params, null), request);
        return dealResponse(params, response);
    }

    @Override
    public ResponseParams HEAD(RequestParams params) throws HttpException {
        Request request = getRequest(params, null);
        Response response = getResponse(editOkHttpClient(params, null), request);
        return dealResponse(params, response);
    }

    @Override
    public ResponseParams DELETE(RequestParams params) throws HttpException {
        Request request = getRequest(params, null);
        Response response = getResponse(editOkHttpClient(params, null), request);
        return dealResponse(params, response);
    }

    @Override
    public ResponseParams OPTIONS(RequestParams params) throws HttpException {
        Request request = getRequest(params, null);
        Response response = getResponse(editOkHttpClient(params, null), request);
        return dealResponse(params, response);
    }

    private Request getRequest(RequestParams params, RequestBody requestBody) {
        Request.Builder builder = new Request.Builder();

        switch (params.requestMethod()) {
            case GET:
                builder.url(params.urlEncode());
                builder.get();
                break;
            case POST:
                builder.url(params.urlAndPath());
                builder.post(requestBody);
                break;
            case PUT:
                builder.url(params.urlAndPath());
                builder.put(requestBody);
                break;
            case PATCH:
                builder.url(params.urlAndPath());
                builder.patch(requestBody);
                break;
            case HEAD:
                builder.url(params.urlEncode());
                builder.head();
                break;
            case DELETE:
                builder.url(params.urlEncode());
                builder.delete();
                break;
            case OPTIONS:
                builder.url(params.urlEncode());
                builder.method("OPTIONS", Util.EMPTY_REQUEST);
                break;
        }
        builder.headers(getHeaders(params.headers()));

        //缓存控制 要放在headers后面...不然设置会被覆盖掉
        switch (params.cacheMode()) {
            case SERVER_CACHE:
                break;
            case NO_CACHE:
                builder.cacheControl(new CacheControl.Builder().noCache().build());
                break;
            case ONLY_CACHE:
                builder.cacheControl(new CacheControl.Builder().onlyIfCached().build());
                break;
            case NO_STORE:
            default:
                builder.cacheControl(new CacheControl.Builder().noStore().build());
                break;
        }

        return builder.build();
    }

    private Response getResponse(OkHttpClient mOkHttpClient, Request request) throws HttpException {
        try {
            Call call = mOkHttpClient.newCall(request);
            Response response = call.execute();
            if (response.isSuccessful())
                return response;
            else {
                String result = response.body().string();
                int code = response.code();
                response.body().close();
                throw HttpException.HttpCode(code, result);
            }
        } catch (SocketTimeoutException e) {
            throw HttpException.HttpTimeOut(e);
        } catch (UnknownHostException e) {
            throw HttpException.NetWork(e);
        } catch (ConnectException e) {
            throw HttpException.NetWork(e);
        } catch (BindException e) {
            throw HttpException.NetWork(e);
        } catch (SocketException e) {
            throw HttpException.NetWork(e);
        } catch (SSLException e) {
            throw HttpException.NetWork(e);
        } catch (IOException e) {
            throw HttpException.IO(e);
        }
    }

    /**
     * 处理Response
     *
     * @param params   params
     * @param response response
     * @return 返回响应内容 / 下载file、byte[]
     * @throws HttpException
     */
    private ResponseParams dealResponse(RequestParams params, Response response) throws HttpException {
        ResponseParams responseParams = new ResponseParams();
        responseParams.setHeaders(response.headers().toMultimap());
        HttpEnum.ResultType type = params.resultType();
        try {
            if (type == HttpEnum.ResultType.STRING)
                responseParams.setString(response.body().string());
            if (type == HttpEnum.ResultType.BYTES)
                responseParams.setBytes(response.body().bytes());
            if (type == HttpEnum.ResultType.FILE) {
                String filePath = params.downloadPath();
                File downFile = new File(filePath);
                //File tempFile = new File(filePath + ".temp");
                downFile.getParentFile().mkdirs();
                response.body().source().readAll(Okio.sink(downFile));
                responseParams.setFile(filePath);
                //if (downFile.exists())
                //    downFile.delete();
                //tempFile.renameTo(downFile);
            }
            return responseParams;
        } catch (SocketTimeoutException e) {
            throw HttpException.HttpTimeOut(e);
        } catch (IOException e) {
            throw HttpException.IO(e);
        } finally {
            response.body().close();
        }
    }

    /**
     * 非Content-Type的头参数
     * Content-Type由RequestBody决定
     *
     * @param headers headers
     * @return Headers
     */
    private Headers getHeaders(Map<String, String> headers) {
        if (headers == null) headers = new HashMap<>();
        return Headers.of(headers);
    }

    /**
     * 基本post一个内容的表单
     *
     * @param content     一个json 视频 图片等
     * @param contentType Content-Type
     * @return RequestBody
     */
    private RequestBody buildRequestBody(String contentType, Object content) {
        RequestBody requestBody;
        if (content instanceof File)
            requestBody = RequestBody.create(MediaType.parse(contentType), (File) content);
        else if (content instanceof byte[])
            requestBody = RequestBody.create(MediaType.parse(contentType), (byte[]) content);
        else if (content != null)
            requestBody = RequestBody.create(MediaType.parse(contentType), content.toString());
        else
            requestBody = RequestBody.create(MediaType.parse(contentType), new byte[0]);
        return requestBody;
    }

    /**
     * 普通post 键值对的表单
     * Content-Type:application/x-www-form-urlencoded
     *
     * @param values 参数
     * @return FormBody
     */
    private FormBody2 buildFormBody(Map<String, Object> values, String charset) {
        FormBody2.Builder builder = new FormBody2.Builder(Charset.forName(charset));
        if (values != null) {
            for (Map.Entry<String, ?> entry : values.entrySet()) {
                builder.add(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return builder.build();
    }


    /**
     * 多文件/文件参数混合 post的表单 上传文件带进度监听
     * Content-Type:multipart/form-data
     *
     * @param multipartType 参数
     * @param content       上传文内容参数
     * @param hp            进度回调
     * @return MultipartBody
     */
    private MultipartBody buildMultipartBody(String multipartType, Map<String, RequestParams.RequestBody> content, IHttpProgress hp) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        MediaType m = MediaType.parse(multipartType);
        if (m == null) m = MultipartBody.FORM;
        builder.setType(m);
//        if (params != null) {//
//            for (Map.Entry<String, ?> entry : params.entrySet()) {
//                builder.addFormDataPart(entry.getKey(), String.valueOf(entry.getValue()));
//            }
//        }
        if (content != null) {
            for (Map.Entry<String, RequestParams.RequestBody> entry : content.entrySet()) {
                RequestParams.RequestBody body = entry.getValue();
                RequestBody requestBody = buildRequestBody(body.getContentType(), body.getContent());
                if (hp != null)
                    requestBody = new RequestBodyProgress(requestBody, hp, entry.getKey());
                builder.addFormDataPart(entry.getKey(), body.getFilename(), requestBody);
            }
        }
        return builder.build();
    }

    /**
     * 根据访问需求 改变mOkHttpClient
     */
    private OkHttpClient editOkHttpClient(RequestParams params, final IHttpProgress iHttpProgress) {
        SSLSocketFactory ssl = Utils.checkSSL(params.url(), qsHttpConfig);
        if (iHttpProgress == null & ssl == null & params.timeOut() <= 0)
            return mOkHttpClient;
        OkHttpClient.Builder ob = mOkHttpClient.newBuilder();
        //添加https规则
        if (ssl != null) {
            ob.sslSocketFactory(ssl);
            if (qsHttpConfig.debug()) {
                Log.i(this.getClass().getName(), params.url() + "-使用ssl配置" + ssl.toString());
            }
        }
        //超时
        if (params.timeOut() > 0) {
            ob.connectTimeout(params.timeOut(), TimeUnit.MILLISECONDS)
                    .readTimeout(params.timeOut(), TimeUnit.MILLISECONDS)
                    .writeTimeout(params.timeOut(), TimeUnit.MILLISECONDS);
        }
        //增加拦截器
        if (iHttpProgress != null)
            ob.addNetworkInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    //拦截
                    Response originalResponse = chain.proceed(chain.request());
                    //包装响应体并返回
                    return originalResponse.newBuilder()
                            .body(new ResponseBodyProgress(originalResponse.body(), iHttpProgress))
                            .build();
                }
            });
        return ob.build();
    }

//    private OkHttpClient createCacheOkHttpClient(final IHttpProgress iHttpProgress) {
//        //克隆一个client 不和原来的混
//        //增加拦截器
//        return mOkHttpClient.newBuilder().addNetworkInterceptor(new Interceptor() {
//            @Override
//            public Response intercept(Chain chain) throws IOException {
//                //拦截
//                Request request = chain.request();
//                Response response = chain.proceed(request);
//                Response response1 = response.newBuilder()
//                        .removeHeader("Pragma")
//                        .removeHeader("Cache-Control")
//                        .header("Cache-Control", "max-age=" + 30)
//                        .build();
//                return response1;
//            }
//        }).build();
//    }
//
//    public class CacheInterceptor implements Interceptor {
//        @Override
//        public Response intercept(Chain chain) throws IOException {
//            Request request = chain.request();
//            Response response = chain.proceed(request);
//            Response response1 = response.newBuilder()
//                    .removeHeader("Pragma")
//                    .removeHeader("Cache-Control")
//                    //cache for 30 days
//                    .header("Cache-Control", "max-age=" + 30)
//                    .build();
//            return response1;
//        }
//    }


}
