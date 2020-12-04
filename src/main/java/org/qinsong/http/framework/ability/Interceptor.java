package org.qinsong.http.framework.ability;

import org.qinsong.http.framework.HttpException;
import org.qinsong.http.framework.RequestParams;
import org.qinsong.http.framework.ResponseParams;

/**
 * Created by song on 2017/5/17.
 * 拦截器
 */

public interface Interceptor {
    ResponseParams intercept(Chain chain) throws HttpException;

    interface Chain {
        RequestParams request();//请求参数

        ResponseParams proceed(RequestParams request) throws HttpException;//流程继续
    }
}
