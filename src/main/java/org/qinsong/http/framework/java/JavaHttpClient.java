package org.qinsong.http.framework.java;


import org.qinsong.http.framework.QSHttpClient;
import org.qinsong.http.framework.QSHttpConfig;

/*
 * Created by song on 2016/9/21.
 */
public class JavaHttpClient extends QSHttpClient {

    //开放实例
    public JavaHttpClient(QSHttpConfig qsHttpConfig) {
        super(new HttpURLConnectionTask(qsHttpConfig), qsHttpConfig);
    }

}
