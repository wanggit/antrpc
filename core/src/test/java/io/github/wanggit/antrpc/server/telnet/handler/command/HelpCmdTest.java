package io.github.wanggit.antrpc.server.telnet.handler.command;

import io.github.wanggit.antrpc.AntrpcContext;
import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.commons.config.Configuration;
import io.github.wanggit.antrpc.commons.config.TelnetConfig;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import io.github.wanggit.antrpc.server.telnet.client.ITelnetClient;
import io.github.wanggit.antrpc.server.telnet.client.TelnetClient;
import io.github.wanggit.antrpc.server.telnet.client.handler.AbsServerResponseListener;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

/** 测试 help 命令 */
public class HelpCmdTest {

    @Test
    public void testHelpCmd() throws InterruptedException {
        int telnetPort = RandomUtils.nextInt(3000, 30000);
        int serverPort = RandomUtils.nextInt(3000, 30000);
        int serverRpcPort = RandomUtils.nextInt(3000, 30000);
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("spring.application.name", "telnetServerTest");
        environment.setProperty("server.port", String.valueOf(serverPort));
        environment.setProperty("antrpc.port", String.valueOf(serverRpcPort));
        applicationContext.setEnvironment(environment);
        Configuration configuration = new Configuration();
        TelnetConfig telnetConfig = new TelnetConfig();
        telnetConfig.setEnable(true);
        telnetConfig.setPort(telnetPort);
        configuration.setTelnetConfig(telnetConfig);
        configuration.setEnvironment(environment);
        configuration.setPort(serverRpcPort);
        IAntrpcContext antrpcContext = new AntrpcContext(configuration);
        applicationContext.refresh();
        antrpcContext.init(applicationContext);

        AbsServerResponseListener listener =
                new AbsServerResponseListener() {
                    @Override
                    protected void internalCheckBuffer(String content) {
                        Assert.assertTrue(content.contains("AntRpc TelnetServer Command Help"));
                    }
                };

        ITelnetClient telnetClient = new TelnetClient("localhost", telnetPort, listener);
        WaitUtil.wait(2, 1);
        telnetClient.send("h");
        WaitUtil.wait(2, 1);
        listener.checkBuffer();
        telnetClient.close();
    }
}
