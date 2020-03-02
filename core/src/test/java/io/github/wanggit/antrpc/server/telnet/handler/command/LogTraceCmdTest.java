package io.github.wanggit.antrpc.server.telnet.handler.command;

import io.github.wanggit.antrpc.AntrpcContext;
import io.github.wanggit.antrpc.commons.annotations.RpcAutowired;
import io.github.wanggit.antrpc.commons.annotations.RpcMethod;
import io.github.wanggit.antrpc.commons.annotations.RpcService;
import io.github.wanggit.antrpc.commons.config.Configuration;
import io.github.wanggit.antrpc.commons.config.TelnetConfig;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import io.github.wanggit.antrpc.server.telnet.client.TelnetClient;
import io.github.wanggit.antrpc.server.telnet.client.handler.AbsServerResponseListener;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.stereotype.Component;

/** 测试 log trace 命令 */
public class LogTraceCmdTest {

    @Test
    public void testLogTraceCmd() throws InterruptedException {
        // server
        int serverPort = RandomUtils.nextInt(3000, 30000);
        int serverAntRpcPort = RandomUtils.nextInt(3000, 30000);
        int serverTelnetPort = RandomUtils.nextInt(3000, 30000);
        GenericApplicationContext serverApplicationContext = new GenericApplicationContext();
        MockEnvironment serverEnvironment = new MockEnvironment();
        serverEnvironment.setProperty("spring.application.name", "serverTest");
        serverEnvironment.setProperty("server.port", String.valueOf(serverPort));
        serverEnvironment.setProperty("antrpc.port", String.valueOf(serverAntRpcPort));
        serverApplicationContext.setEnvironment(serverEnvironment);
        GenericBeanDefinition serverCImplBeanDefinition = new GenericBeanDefinition();
        serverCImplBeanDefinition.setBeanClass(ServerCImpl.class);
        serverApplicationContext.registerBeanDefinition(
                ServerCImpl.class.getName(), serverCImplBeanDefinition);
        Configuration configuration = new Configuration();
        configuration.setEnvironment(serverEnvironment);
        configuration.setPort(serverAntRpcPort);
        TelnetConfig serverTelnetConfig = new TelnetConfig();
        serverTelnetConfig.setEnable(true);
        serverTelnetConfig.setPort(serverTelnetPort);
        configuration.setTelnetConfig(serverTelnetConfig);
        serverApplicationContext.refresh();
        AntrpcContext serverAntrpcContext = new AntrpcContext(configuration);
        serverAntrpcContext.init(serverApplicationContext);
        WaitUtil.wait(3, 1);

        // client
        int clientPort = RandomUtils.nextInt(3000, 30000);
        int clientAntRpcPort = RandomUtils.nextInt(3000, 30000);
        int clientTelnetPort = RandomUtils.nextInt(3000, 30000);
        GenericApplicationContext clientApplicationContext = new GenericApplicationContext();
        MockEnvironment clientEnvironment = new MockEnvironment();
        clientEnvironment.setProperty("spring.application.name", "telnetClient");
        clientEnvironment.setProperty("server.port", String.valueOf(clientPort));
        clientEnvironment.setProperty("antrpc.port", String.valueOf(clientAntRpcPort));
        clientApplicationContext.setEnvironment(clientEnvironment);
        GenericBeanDefinition clientRefCBeanDefinition = new GenericBeanDefinition();
        clientRefCBeanDefinition.setBeanClass(ClientRefC.class);
        clientApplicationContext.registerBeanDefinition(
                ClientRefC.class.getName(), clientRefCBeanDefinition);
        Configuration clientConfiguration = new Configuration();
        clientConfiguration.setEnvironment(clientEnvironment);
        clientConfiguration.setPort(clientAntRpcPort);
        TelnetConfig clientTelnetConfig = new TelnetConfig();
        clientTelnetConfig.setEnable(true);
        clientTelnetConfig.setPort(clientTelnetPort);
        clientConfiguration.setTelnetConfig(clientTelnetConfig);
        clientApplicationContext.refresh();
        AntrpcContext clientAntrpcContext = new AntrpcContext(clientConfiguration);
        clientAntrpcContext.init(clientApplicationContext);
        WaitUtil.wait(3, 1);

        // telnet log
        AbsServerResponseListener logListener =
                new AbsServerResponseListener() {
                    @Override
                    protected void internalCheckBuffer(String content) {
                        Assert.assertEquals(
                                StringUtils.countMatches(content, CInterface.class.getName()), 5);
                    }
                };
        TelnetClient logTelnetClient = new TelnetClient("localhost", clientTelnetPort, logListener);
        logTelnetClient.send("log " + CInterface.class.getName() + "#test() 5");
        WaitUtil.wait(2, 1);
        ClientRefC clientRefC = clientApplicationContext.getBean(ClientRefC.class);
        for (int i = 0; i < 10; i++) {
            clientRefC.test();
        }
        WaitUtil.wait(5, 1);
        logListener.checkBuffer();
        WaitUtil.wait(2, 1);
        logTelnetClient.close();

        // telnet trace
        AbsServerResponseListener traceListener =
                new AbsServerResponseListener() {
                    @Override
                    protected void internalCheckBuffer(String content) {
                        Assert.assertEquals(5, StringUtils.countMatches(content, "argumentValues"));
                    }
                };
        TelnetClient traceTelnetClient =
                new TelnetClient("localhost", serverTelnetPort, traceListener);
        traceTelnetClient.send("trace " + CInterface.class.getName() + "#test() 5");
        WaitUtil.wait(2, 1);
        for (int i = 0; i < 10; i++) {
            clientRefC.test();
        }
        WaitUtil.wait(2, 1);
        traceListener.checkBuffer();
        WaitUtil.wait(2, 1);
        traceTelnetClient.close();
    }

    // ----------------api-----------------------
    public interface CInterface {
        void test();
    }
    // ----------------server bean----------------
    @RpcService
    public static class ServerCImpl implements CInterface {

        @Override
        @RpcMethod
        public void test() {
            System.out.println("ServerCImpl Invoking...");
        }
    }
    // -----------------client bean------------------
    @Component
    public static class ClientRefC {

        @RpcAutowired private CInterface cInterface;

        public void test() {
            cInterface.test();
        }
    }
}
