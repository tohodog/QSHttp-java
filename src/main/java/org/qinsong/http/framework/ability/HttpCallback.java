package org.qinsong.http.framework.ability;

import org.qinsong.http.framework.HttpException;
import org.qinsong.http.framework.ResponseParams;

/**
 * Created by song on 2016/9/18.
 */
public interface HttpCallback {

    void onSuccess(ResponseParams response);

    void onFailure(HttpException e);

}
