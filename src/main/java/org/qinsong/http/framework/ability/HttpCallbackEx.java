package org.qinsong.http.framework.ability;

/**
 * Created by song on 2017/4/13.
 */
public interface HttpCallbackEx extends HttpCallback {

    void onStart();

    void onEnd();

}
