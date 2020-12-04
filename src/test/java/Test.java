import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.song.http.Log;
import org.song.http.QSHttp;
import org.song.http.framework.HttpException;
import org.song.http.framework.ResponseParams;
import org.song.http.framework.ability.HttpCallback;

import java.util.concurrent.ExecutionException;

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
                .resultByBytes()
                .asyncExecute(new HttpCallback() {
                    @Override
                    public void onSuccess(ResponseParams response) {
                        Log.i(response.string());

                    }

                    @Override
                    public void onFailure(HttpException e) {

                    }
                });
        Log.i(QSHttp.get("https://baidu.com").resultByBytes().futureExecute().get().string());
        Log.i(QSHttp.get("https://baidu.com").resultByBytes().syncExecute().string());
    }
}
