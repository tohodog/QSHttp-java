package org.qinsong.http.framework;

import org.qinsong.http.framework.ability.HttpCallback;
import org.qinsong.http.framework.ability.HttpCallbackEx;
import org.qinsong.http.framework.ability.IHttpProgress;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Created by song on 2016/9/18.
 * 子线程联网任务 回调主线程 类
 */
public class ThreadHandler {

    private int mThreadWhat = 19930411;
    private Map<Integer, HttpCallback> sparseArray = new ConcurrentHashMap<>();

    private static ThreadHandler instance;

    private static ThreadHandler getInstance() {
        if (instance == null)
            instance = new ThreadHandler();
        return instance;
    }

    public synchronized int addHttpDataCallback(final HttpCallback cb) {
        if (cb == null) return 0;
        mThreadWhat++;
        sparseArray.put(mThreadWhat, cb);
        if (cb instanceof HttpCallbackEx) {
            ((HttpCallbackEx) cb).onStart();
        }
        return mThreadWhat;
    }

    public void handleMessage(Message message) {
        final int what = message.what;
        HttpCallback cb = sparseArray.get(what);
        if (cb == null) {
            sparseArray.remove(what);
            return;
        }
        switch (message.arg1) {
            case HttpEnum.HTTP_SUCCESS:
                sparseArray.remove(what);
                ResponseParams responseParams = (ResponseParams) message.obj;
                try {
                    cb.onSuccess(responseParams);
                } catch (Exception e) {
                    e.printStackTrace();
                    cb.onFailure(HttpException.Run(e).responseParams(responseParams));
                }
                if (cb instanceof HttpCallbackEx)
                    ((HttpCallbackEx) cb).onEnd();
                break;
            case HttpEnum.HTTP_FAILURE:
                sparseArray.remove(what);
                cb.onFailure((HttpException) message.obj);
                if (cb instanceof HttpCallbackEx)
                    ((HttpCallbackEx) cb).onEnd();
                break;
            case HttpEnum.HTTP_PROGRESS:
                if (cb instanceof IHttpProgress) {
                    Object[] arr = (Object[]) message.obj;
                    ((IHttpProgress) cb).onProgress((long) arr[0], (long) arr[1], (String) arr[2]);
                }
                break;
        }

    }


    /**
     * 添加回调类
     *
     * @return 返回int 需保存 处理完网络数据回调主线程时带上
     */
    public static int AddHttpCallback(HttpCallback cb) {
        return getInstance().addHttpDataCallback(cb);
    }

    /**
     * 移除回调类
     */
    public static void removeHttpCallback(int mThreadWhat) {
        getInstance().sparseArray.remove(mThreadWhat);
    }

    /**
     * 成功的处理
     */
    public static void Success(ResponseParams obj, boolean sync) {
        Message msg = Message.obtain();
        msg.what = obj.requestID();
        msg.arg1 = HttpEnum.HTTP_SUCCESS;
        msg.obj = obj;
        getInstance().handleMessage(msg);
    }

    /**
     * 失败的处理
     */
    public static void Failure(ResponseParams obj, boolean sync) {
        Message msg = Message.obtain();
        msg.what = obj.requestID();
        msg.arg1 = HttpEnum.HTTP_FAILURE;
        Exception e = obj.exception();
        if (!(e instanceof HttpException))
            e = HttpException.Run(e);
        msg.obj = ((HttpException) e).responseParams(obj);
        getInstance().handleMessage(msg);
    }

    /**
     * 进度的处理
     * 这里应该加限制 太快貌似会崩溃
     * 或者不跳主线程 直接调用 唔...
     */
    public static void Progress(long var1, long var2, String var3, int mThreadWhat) {
        if (var2 == 0)
            var2 = -1;
        Message msg = Message.obtain();
        msg.what = mThreadWhat;
        msg.arg1 = HttpEnum.HTTP_PROGRESS;
        msg.obj = new Object[]{var1, var2, var3};
        getInstance().handleMessage(msg);
    }

    private static class Message {
        int what;
        int arg1;
        Object obj;

        static Message obtain() {
            return new Message();
        }
    }
}
