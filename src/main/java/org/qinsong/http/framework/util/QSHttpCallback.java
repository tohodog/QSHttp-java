package org.qinsong.http.framework.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;

import org.qinsong.http.framework.HttpEnum;
import org.qinsong.http.framework.HttpException;
import org.qinsong.http.framework.ResponseParams;
import org.qinsong.http.framework.ability.HttpCallbackEx;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by song
 * Contact github.com/tohodog
 * Date 2019/4/11
 * 回调封装,支持解析,泛型
 * 生命周期销毁(外部类是activity销毁了不会回调),不想销毁覆盖isDestroy方法
 */
public abstract class QSHttpCallback<T> implements HttpCallbackEx {

    protected ResponseParams response;


    public abstract void onComplete(T dataBean);


    @Override
    public void onSuccess(ResponseParams response) {
        this.response = response;
        try {
            if (response.resultType() == HttpEnum.ResultType.STRING) {
                onComplete(map(response.string()));
            } else if (response.resultType() == HttpEnum.ResultType.BYTES) {
                onComplete((T) response.bytes());
            } else if (response.resultType() == HttpEnum.ResultType.FILE) {
                onComplete((T) new File(response.file()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            onFailure(HttpException.Parser(e).responseParams(response));
        } catch (HttpException e1) {
            onFailure(e1.responseParams(response));
        }
    }

    protected T map(String result) throws HttpException {
        return parserT(result);
    }

    protected T parserT(String json) throws JSONException {
        T dataBean = null;
        Type type = findT();
        if (type == String.class) {
            dataBean = (T) json;
        } else {
            dataBean = JSON.parseObject(json, type);
        }
        return dataBean;
    }

    protected Type findT() {
        Type type = getClass().getGenericSuperclass();

        if (!(type instanceof ParameterizedType)) {
            type = String.class;
        } else {
            ParameterizedType parameterizedType = (ParameterizedType) type;

//            if (parameterizedType.getActualTypeArguments()[0] instanceof ParameterizedType) {
//                ParameterizedType parameterizedType1 = (ParameterizedType) parameterizedType.getActualTypeArguments()[0];
//                type = parameterizedType1.getRawType(); //T=List<xxx>
//                Class<?> clazz = (Class<?>) parameterizedType1.getActualTypeArguments()[0];
//            } else {
            type = parameterizedType.getActualTypeArguments()[0];
//            }
        }
        return type;
    }


    @Override
    public void onFailure(HttpException e) {
        e.show();
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onEnd() {

    }

    public static <T> List<T> parserList(JSONArray jsonArray, Class<T> _class) {
        List<T> list = new ArrayList<>();
        if (jsonArray != null)
            for (int i = 0; i < jsonArray.size(); i++) {
                list.add(jsonArray.getJSONObject(i).toJavaObject(_class));
            }
        return list;
    }

}
