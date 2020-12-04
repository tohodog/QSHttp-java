import com.alibaba.fastjson.JSONObject;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.qinsong.http.Log;
import org.qinsong.http.QSHttp;
import org.qinsong.http.framework.HttpException;
import org.qinsong.http.framework.QSHttpConfig;
import org.qinsong.http.framework.RequestParams;
import org.qinsong.http.framework.ResponseParams;
import org.qinsong.http.framework.ability.HttpCallback;
import org.qinsong.http.framework.ability.Interceptor;
import org.qinsong.http.framework.util.TrustAllCerts;
import org.qinsong.http.framework.util.Utils;

import java.io.File;
import java.util.concurrent.Future;

/**
 * Created by QSong
 * Contact github.com/tohodog
 * Date 2020/11/30
 */
public class Test extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public Test(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static TestSuite suite() {
        return new TestSuite(Test.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() throws Exception {
        QSHttp.get("https://baidu.com")
                .asyncExecute(new HttpCallback() {
                    @Override
                    public void onSuccess(ResponseParams response) {

                    }

                    @Override
                    public void onFailure(HttpException e) {

                    }
                });
        QSHttp.get("https://baidu.com").futureExecute().get();
        QSHttp.get("https://baidu.com").syncExecute().string();

//        String url = "https://api.reol.top/api_test";
//        JSONObject resultJSON;
//        String resultStr;
//        resultJSON = QSHttp.get(url).param("name", "qshttp").syncExecute().jsonModel();
//        resultStr = QSHttp.post(url).param("name", "qshttp").syncExecute().string();
//        resultStr = QSHttp.postJSON(url).param("name", "qshttp").syncExecute().string();
//
//        resultJSON = QSHttp.upload(url)
//                //文本参数
//                .param("userName", 10086)
//                .param("password", "qwe123456")
//                //文件参数
//                .param("file", new File("xx.jpg"))
//                .param("bytes", new byte[1024])//上传一个字节数组
//                //指定上传的文件名,content-type参数
//                .multipartBody("icon", "image/*", "icon.jpg", new File("xx.jpg"))
//                .multipartBody(new String("icon"), "image/*", "icon2.jpg", new byte[1024])//icon文件数组上传
//                .syncExecute()
//                .jsonModel();
//
//
//        Future<ResponseParams> future = QSHttp.get("https://baidu.com").futureExecute();
//        resultJSON = future.get().jsonModel();

    }
}
