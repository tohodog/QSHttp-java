package org.song.http.framework;

import org.song.http.Log;
import org.song.http.framework.java.JavaHttpClient;
import org.song.http.framework.ok.OkHttpClient;
import org.song.http.framework.util.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by song on 2016/9/18.
 * http框架全局配置
 */
public class QSHttpManage {


    private static Map<String, QSHttpClient> mapQSHttpClient = new HashMap<>();

    public static void init(QSHttpConfig qsHttpConfig) {
        addClient(null, qsHttpConfig);
    }

    /**
     * 初始化多个客户端,使用不同配置
     * 使用.qsClient("key")进行选择
     */
    public static void addClient(String key, QSHttpConfig qsHttpConfig) {
        Log.i("addClient:" + key + "," + qsHttpConfig);

        QSHttpClient qsHttpClient;
        switch (qsHttpConfig.xxHttp()) {
            case JAVA_HTTP:
                qsHttpClient = new JavaHttpClient(qsHttpConfig);
                break;
            case OK_HTTP:
            default:
                qsHttpClient = new OkHttpClient(qsHttpConfig);

        }
        mapQSHttpClient.put(key, qsHttpClient);
    }

    public static QSHttpClient getQSHttpClient() {
        if (mapQSHttpClient.get(null) == null) {//兼容下不初始化
            init(QSHttpConfig.Build().build());
        }
        return mapQSHttpClient.get(null);
    }

    public static QSHttpClient getQSHttpClient(String key) {
        if (key == null) return getQSHttpClient();
        return mapQSHttpClient.get(key);
    }

    public static void cleanCache() {
        Utils.cleanCache();
    }

}
