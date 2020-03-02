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
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.stereotype.Component;

/** 测试 ls ref lsp reg unreg 命令 */
public class LsRefLspRegUnregCmdTest {

    @Test
    public void testAllAutowiredInterfaces() throws InterruptedException {
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
        GenericBeanDefinition serverZImplBeanDefinition = new GenericBeanDefinition();
        serverZImplBeanDefinition.setBeanClass(ServerZImpl.class);
        serverApplicationContext.registerBeanDefinition(
                ServerZImpl.class.getName(), serverZImplBeanDefinition);
        GenericBeanDefinition serverXImplBeanDefinition = new GenericBeanDefinition();
        serverXImplBeanDefinition.setBeanClass(ServerXImpl.class);
        serverApplicationContext.registerBeanDefinition(
                ServerXImpl.class.getName(), serverXImplBeanDefinition);
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
        GenericBeanDefinition clientRefZBeanDefinition = new GenericBeanDefinition();
        clientRefZBeanDefinition.setBeanClass(ClientRefZ.class);
        clientApplicationContext.registerBeanDefinition(
                ClientRefZ.class.getName(), clientRefZBeanDefinition);
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

        // ref
        AbsServerResponseListener listener =
                new AbsServerResponseListener() {
                    @Override
                    protected void internalCheckBuffer(String content) {
                        Assert.assertTrue(content.contains(ZInterface.class.getName()));
                    }
                };
        TelnetClient telnetClient = new TelnetClient("localhost", clientTelnetPort, listener);
        telnetClient.send("ref");
        WaitUtil.wait(2, 1);
        listener.checkBuffer();
        WaitUtil.wait(2, 1);
        telnetClient.close();

        // lsp
        AbsServerResponseListener serverListener =
                new AbsServerResponseListener() {
                    @Override
                    protected void internalCheckBuffer(String content) {
                        Assert.assertTrue(
                                content.contains("all provided interfaces of the current service"));
                    }
                };
        TelnetClient telnetClientForServer =
                new TelnetClient("localhost", serverTelnetPort, serverListener);
        telnetClientForServer.send("lsp");
        WaitUtil.wait(2, 1);
        serverListener.checkBuffer();
        WaitUtil.wait(2, 1);
        telnetClientForServer.close();

        // lsp XInterface
        AbsServerResponseListener lspArgumentsListener =
                new AbsServerResponseListener() {
                    @Override
                    protected void internalCheckBuffer(String content) {
                        Assert.assertTrue(content.contains("XInterface"));
                        Assert.assertFalse(content.contains("ZInterface"));
                    }
                };
        TelnetClient lspArgumentsTelnetClient =
                new TelnetClient("localhost", serverTelnetPort, lspArgumentsListener);
        lspArgumentsTelnetClient.send("lsp XInterface");
        WaitUtil.wait(2, 1);
        lspArgumentsListener.checkBuffer();
        WaitUtil.wait(2, 1);
        lspArgumentsTelnetClient.close();

        // client ls
        AbsServerResponseListener clientLsListener =
                new AbsServerResponseListener() {
                    @Override
                    protected void internalCheckBuffer(String content) {
                        Assert.assertTrue(
                                content.contains("XInterface") && content.contains("ZInterface"));
                    }
                };
        TelnetClient lsClientTelnetClient =
                new TelnetClient("localhost", clientTelnetPort, clientLsListener);
        lsClientTelnetClient.send("ls");
        WaitUtil.wait(2, 1);
        clientLsListener.checkBuffer();
        WaitUtil.wait(2, 1);
        lsClientTelnetClient.close();
        // server ls
        AbsServerResponseListener serverLsListener =
                new AbsServerResponseListener() {
                    @Override
                    protected void internalCheckBuffer(String content) {
                        Assert.assertTrue(
                                content.contains("XInterface") && content.contains("ZInterface"));
                    }
                };
        TelnetClient lsServerTelnetClient =
                new TelnetClient("localhost", serverTelnetPort, serverLsListener);
        lsServerTelnetClient.send("ls");
        WaitUtil.wait(2, 1);
        serverLsListener.checkBuffer();
        WaitUtil.wait(2, 1);
        lsServerTelnetClient.close();

        // unreg
        AbsServerResponseListener unregListener =
                new AbsServerResponseListener() {
                    @Override
                    protected void internalCheckBuffer(String content) {
                        Assert.assertFalse(content.contains("Suspend Registration ***false***"));
                    }
                };
        TelnetClient unregTelnetClient =
                new TelnetClient("localhost", serverTelnetPort, unregListener);
        unregTelnetClient.send("unreg");
        WaitUtil.wait(2, 1);
        unregListener.checkBuffer();
        WaitUtil.wait(2, 1);
        unregTelnetClient.close();

        // reg arguments
        AbsServerResponseListener regArgumentsListener =
                new AbsServerResponseListener() {
                    @Override
                    protected void internalCheckBuffer(String content) {
                        Assert.assertTrue(content.contains("Suspend Registration ***false***"));
                    }
                };
        TelnetClient regArgumentsTelnetClient =
                new TelnetClient("localhost", serverTelnetPort, regArgumentsListener);
        regArgumentsTelnetClient.send("reg " + XInterface.class.getName());
        WaitUtil.wait(2, 1);
        regArgumentsListener.checkBuffer();
        WaitUtil.wait(2, 1);
        regArgumentsTelnetClient.close();

        // reg
        AbsServerResponseListener regListener =
                new AbsServerResponseListener() {
                    @Override
                    protected void internalCheckBuffer(String content) {
                        Assert.assertFalse(content.contains("Suspend Registration ***true***"));
                    }
                };
        TelnetClient regTelnetClient = new TelnetClient("localhost", serverTelnetPort, regListener);
        regTelnetClient.send("reg");
        WaitUtil.wait(2, 1);
        regListener.checkBuffer();
        WaitUtil.wait(2, 1);
        regTelnetClient.close();

        //
    }

    // ------------------------ api ----------------------------
    public interface ZInterface {
        String hello(String name);
    }

    public interface XInterface {
        void test();
    }

    // ------------------------server beans--------------------------
    @RpcService
    public static class ServerXImpl implements XInterface {

        @Override
        @RpcMethod
        public void test() {}
    }

    @RpcService
    public static class ServerZImpl implements ZInterface {

        @Override
        @RpcMethod
        public String hello(String name) {
            return "Hello " + name;
        }
    }

    // ------------------------client beans-------------------------
    @Component
    public static class ClientRefZ {

        @RpcAutowired private ZInterface zInterface;

        public void test() {}
    }
}
