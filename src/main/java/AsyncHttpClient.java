import org.apache.http.ExceptionLogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.protocol.HttpAsyncRequestExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Future;

/**
 * Created by hht on 2017.10.26.
 */
public class AsyncHttpClient {
    final static Logger LOGGER = LoggerFactory.getLogger(AsyncHttpClient.class);

    public static void main(String[] args) {
        IOReactorConfig.Builder b = IOReactorConfig.custom();
        b.setConnectTimeout(1000);
        b.setIoThreadCount(1);
        //b.setSoTimeout(1000);

        CloseableHttpAsyncClient asyncClient
                = HttpAsyncClients.custom()
                .setDefaultIOReactorConfig(b.build())
                .setMaxConnPerRoute(1)
                .setEventHandler(new HttpAsyncRequestExecutor(HttpAsyncRequestExecutor.DEFAULT_WAIT_FOR_CONTINUE, new ExceptionLogger() {
                    public void log(Exception ex) {
                        LOGGER.error("hht", ex);
                    }
                }))
                .build();


        try {
            asyncClient.start();

            int count = 1;
            ArrayList<HttpGet> gets = new ArrayList<>();
            for(int i = 0; i < count; i++) {
                gets.add(new HttpGet("http://10.7.0.136:1234/zone/wishPutGift" + i));
            }


            for(int i = 0; i < count; i++) {
                final int index = i;
                Future<HttpResponse> f0 = asyncClient.execute(gets.get(i), new FutureCallback<HttpResponse>() {
                    public void completed(HttpResponse result) {

                        LOGGER.info("get" + index + result.toString());
                    }

                    public void failed(Exception ex) {
                        LOGGER.error("hht2", ex);
                    }

                    public void cancelled() {
                        LOGGER.error("hht3");
                    }
                });
            }

            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                asyncClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
