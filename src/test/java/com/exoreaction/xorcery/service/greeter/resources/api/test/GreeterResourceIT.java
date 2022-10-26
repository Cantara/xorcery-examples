package com.exoreaction.xorcery.service.greeter.resources.api.test;

import com.exoreaction.xorcery.core.Xorcery;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.Fields;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class GreeterResourceIT {

    static private Xorcery xorcery;
    static private HttpClient httpClient = new HttpClient();

    @BeforeAll
    public static void setUp() throws Exception {
        xorcery = new Xorcery(null);
        httpClient.start();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        xorcery.close();
        httpClient.stop();

/* For debugging
        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
*/
    }


    @Test
    void get() throws Exception {
        httpClient.GET("http://localhost:8889/api/greeter").getContentAsString();
    }

    @Test
    void post() throws Exception {
        httpClient.FORM("http://localhost:8889/api/greeter", new Fields() {{
            put("greeting", "HelloWorld!");
        }}).getContentAsString();
    }

}