package io.github.wanggit.antrpc.server.telnet.handler.command;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import io.github.wanggit.antrpc.AntrpcContext;
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

import java.math.BigDecimal;
import java.util.List;

/** 测试 test 命令 */
public class TestInvokeCmdTest {

    @Test
    public void testSayHello() throws InterruptedException {
        int serverTelnetPort = RandomUtils.nextInt(3000, 30000);
        createServer(serverTelnetPort, Lists.newArrayList(ServerVImpl.class));
        int clientTelnetPort = RandomUtils.nextInt(3000, 30000);
        createClient(clientTelnetPort, Lists.newArrayList());

        AbsServerResponseListener listener =
                new AbsServerResponseListener() {
                    @Override
                    protected void internalCheckBuffer(String content) {
                        Assert.assertTrue(content.contains("Hello wanggang"));
                    }
                };
        TelnetClient telnetClient = new TelnetClient("localhost", clientTelnetPort, listener);
        telnetClient.send(
                "test io.github.wanggit.antrpc.server.telnet.handler.command.TestInvokeCmdTest$VInterface#sayHello(java.lang.String) | wanggang");
        WaitUtil.wait(2, 1);
        listener.checkBuffer();
        WaitUtil.wait(1, 1);
        telnetClient.close();
    }

    // -------------------------server-----------------------
    public interface VInterface {
        String sayHello(String name);
    }

    @RpcService
    public static class ServerVImpl implements VInterface {

        @Override
        @RpcMethod
        public String sayHello(String name) {
            return "Hello " + name;
        }
    }

    @Test
    public void testTest() throws InterruptedException {
        int serverTelnetPort = RandomUtils.nextInt(3000, 30000);
        createServer(serverTelnetPort, Lists.newArrayList(ServerBImpl.class));
        int clientTelnetPort = RandomUtils.nextInt(3000, 30000);
        createClient(clientTelnetPort, Lists.newArrayList());
        AbsServerResponseListener listener =
                new AbsServerResponseListener() {
                    @Override
                    protected void internalCheckBuffer(String content) {
                        Assert.assertTrue(content.contains("Result: wanggang, 20, 100"));
                    }
                };
        TelnetClient telnetClient = new TelnetClient("localhost", clientTelnetPort, listener);
        telnetClient.send(
                "test io.github.wanggit.antrpc.server.telnet.handler.command.TestInvokeCmdTest$BInterface#test(java.lang.String, int, java.lang.Integer) | wanggang $$ 20 $$ 100");
        WaitUtil.wait(2, 1);
        listener.checkBuffer();
        WaitUtil.wait(2, 1);
        telnetClient.close();
    }
    // ------------------server bean--------------
    public interface BInterface {
        String test(String name, int age, Integer count);
    }

    @RpcService
    public static class ServerBImpl implements BInterface {

        @Override
        @RpcMethod
        public String test(String name, int age, Integer count) {
            return "Result: " + name + ", " + age + ", " + count;
        }
    }

    @Test
    public void testDoTest() throws InterruptedException {
        int serverTelnetPort = RandomUtils.nextInt(3000, 30000);
        createServer(serverTelnetPort, Lists.newArrayList(ServerNImpl.class));
        int clientTelnetPort = RandomUtils.nextInt(3000, 30000);
        createClient(clientTelnetPort, Lists.newArrayList());
        AbsServerResponseListener listener =
                new AbsServerResponseListener() {
                    @Override
                    protected void internalCheckBuffer(String content) {
                        Assert.assertTrue(content.contains("wanggang"));
                    }
                };
        TelnetClient telnetClient = new TelnetClient("localhost", clientTelnetPort, listener);
        telnetClient.send(
                "test io.github.wanggit.antrpc.server.telnet.handler.command.TestInvokeCmdTest$NInterface#doTest(java.lang.String, io.github.wanggit.antrpc.server.telnet.handler.command.TestInvokeCmdTest$UserDTO) | wang gang $$ {\"id\":100,\"name\":\"wanggang\"}");
        WaitUtil.wait(1, 1);
        listener.checkBuffer();
        WaitUtil.wait(1, 1);
        telnetClient.close();
    }

    // ------------------------server bean -----------------------
    public interface NInterface {
        String doTest(String label, UserDTO userDTO);
    }

    @RpcService
    public static class ServerNImpl implements NInterface {

        @Override
        @RpcMethod
        public String doTest(String label, UserDTO userDTO) {
            return "Result: " + label + ", " + JSONObject.toJSONString(userDTO);
        }
    }

    @Test
    public void testTestIntArray() throws InterruptedException {
        int serverTelnetPort = RandomUtils.nextInt(3000, 30000);
        createServer(serverTelnetPort, Lists.newArrayList(ServerMImpl.class));
        int clientTelnetPort = RandomUtils.nextInt(3000, 30000);
        createClient(clientTelnetPort, Lists.newArrayList());
        AbsServerResponseListener listener =
                new AbsServerResponseListener() {
                    @Override
                    protected void internalCheckBuffer(String content) {
                        Assert.assertTrue(content.contains("Result: wanggang, [1,2,3,4,5]"));
                    }
                };
        TelnetClient telnetClient = new TelnetClient("localhost", clientTelnetPort, listener);
        telnetClient.send(
                "test io.github.wanggit.antrpc.server.telnet.handler.command.TestInvokeCmdTest$MInterface#testIntArray(java.lang.String, int[]) | wanggang $$ [1,2,3,4,5]");
        WaitUtil.wait(2, 1);
        listener.checkBuffer();
        WaitUtil.wait(1, 1);
        telnetClient.close();
    }

    // --------------------------server beans---------------------
    public interface MInterface {
        String testIntArray(String label, int[] arr);
    }

    @RpcService
    public static class ServerMImpl implements MInterface {

        @Override
        @RpcMethod
        public String testIntArray(String label, int[] arr) {
            return "Result: " + label + ", " + JSONObject.toJSONString(arr);
        }
    }

    @Test
    public void testTestIntegerArray() throws InterruptedException {
        int serverTelnetPort = RandomUtils.nextInt(3000, 30000);
        createServer(serverTelnetPort, Lists.newArrayList(ServerLImpl.class));
        int clientTelnetPort = RandomUtils.nextInt(3000, 30000);
        createClient(clientTelnetPort, Lists.newArrayList());
        AbsServerResponseListener listener =
                new AbsServerResponseListener() {
                    @Override
                    protected void internalCheckBuffer(String content) {
                        Assert.assertTrue(content.contains("Result: wanggang, [1,2,3]"));
                    }
                };
        TelnetClient telnetClient = new TelnetClient("localhost", clientTelnetPort, listener);
        telnetClient.send(
                "test io.github.wanggit.antrpc.server.telnet.handler.command.TestInvokeCmdTest$LInterface#testIntegerArray(java.lang.String, java.lang.Integer[]) | wanggang $$ [1,2,3]");
        WaitUtil.wait(1, 1);
        telnetClient.send(
                "test io.github.wanggit.antrpc.server.telnet.handler.command.TestInvokeCmdTest$LInterface#testIntegerArray(java.lang.String, [Ljava.lang.Integer;) | wanggang $$ [1,2,3]");
        WaitUtil.wait(1, 1);
        listener.checkBuffer();
        WaitUtil.wait(1, 1);
        telnetClient.close();
    }

    // -------------------------server bean--------------------
    public interface LInterface {
        String testIntegerArray(String label, Integer[] arr);
    }

    @RpcService
    public static class ServerLImpl implements LInterface {
        @Override
        @RpcMethod
        public String testIntegerArray(String label, Integer[] arr) {
            return "Result: " + label + ", " + JSONObject.toJSONString(arr);
        }
    }

    @Test
    public void testTestBigDecimal() throws InterruptedException {
        int serverTelnetPort = RandomUtils.nextInt(3000, 30000);
        createServer(serverTelnetPort, Lists.newArrayList(ServerKImpl.class));
        int clientTelnetPort = RandomUtils.nextInt(3000, 30000);
        createClient(clientTelnetPort, Lists.newArrayList());
        AbsServerResponseListener listener =
                new AbsServerResponseListener() {
                    @Override
                    protected void internalCheckBuffer(String content) {
                        Assert.assertTrue(content.contains("Result: wanggang, 120.55"));
                    }
                };
        TelnetClient telnetClient = new TelnetClient("localhost", clientTelnetPort, listener);
        telnetClient.send(
                "test io.github.wanggit.antrpc.server.telnet.handler.command.TestInvokeCmdTest$KInterface#testBigDecimal(java.lang.String, java.math.BigDecimal) | wanggang $$ 120.55");
        WaitUtil.wait(2, 1);
        listener.checkBuffer();
        WaitUtil.wait(1, 1);
        telnetClient.close();
    }

    public interface KInterface {
        String testBigDecimal(String label, BigDecimal decimal);
    }

    @RpcService
    public static class ServerKImpl implements KInterface {

        @Override
        @RpcMethod
        public String testBigDecimal(String label, BigDecimal decimal) {
            return "Result: " + label + ", " + decimal.doubleValue();
        }
    }

    @Test
    public void testTestList() throws InterruptedException {
        int serverTelnetPort = RandomUtils.nextInt(3000, 30000);
        createServer(serverTelnetPort, Lists.newArrayList(ServerJImpl.class));
        int clientTelnetPort = RandomUtils.nextInt(3000, 30000);
        createClient(clientTelnetPort, Lists.newArrayList());
        AbsServerResponseListener listener =
                new AbsServerResponseListener() {
                    @Override
                    protected void internalCheckBuffer(String content) {
                        Assert.assertTrue(content.contains("Result: wanggang, [1,2,3]"));
                    }
                };
        TelnetClient telnetClient = new TelnetClient("localhost", clientTelnetPort, listener);
        telnetClient.send(
                "test io.github.wanggit.antrpc.server.telnet.handler.command.TestInvokeCmdTest$JInterface#testList(java.lang.String, java.util.List) | wanggang $$ [1, 2, 3]");
        WaitUtil.wait(1, 1);
        listener.checkBuffer();
        WaitUtil.wait(1, 1);
        telnetClient.close();
    }

    // -----------------------------server bean-----------------
    public interface JInterface {
        String testList(String label, List<Integer> list);
    }

    @RpcService
    public static class ServerJImpl implements JInterface {

        @Override
        @RpcMethod
        public String testList(String label, List<Integer> list) {
            return "Result: " + label + ", " + JSONObject.toJSONString(list);
        }
    }

    @Test
    public void testTestListUserDTOs() throws InterruptedException {
        int serverTelnetPort = RandomUtils.nextInt(3000, 30000);
        createServer(serverTelnetPort, Lists.newArrayList(ServerHImpl.class));
        int clientTelnetPort = RandomUtils.nextInt(3000, 30000);
        createClient(clientTelnetPort, Lists.newArrayList());
        AbsServerResponseListener listener =
                new AbsServerResponseListener() {
                    @Override
                    protected void internalCheckBuffer(String content) {
                        Assert.assertTrue(content.contains("Result: wanggang, [{"));
                    }
                };
        TelnetClient telnetClient = new TelnetClient("localhost", clientTelnetPort, listener);
        telnetClient.send(
                "test io.github.wanggit.antrpc.server.telnet.handler.command.TestInvokeCmdTest$HInterface#testListUserDTOs(java.lang.String, java.util.List) | wanggang $$ [{\"id\":100,\"name\":\"wanggang\"}]");
        WaitUtil.wait(1, 1);
        listener.checkBuffer();
        WaitUtil.wait(1, 1);
        telnetClient.close();
    }

    // --------------------------server bean-------------------
    public interface HInterface {
        String testListUserDTOs(String label, List<UserDTO> userDTOS);
    }

    @RpcService
    public static class ServerHImpl implements HInterface {
        @Override
        @RpcMethod
        public String testListUserDTOs(String label, List<UserDTO> userDTOS) {
            return "Result: " + label + ", " + JSONObject.toJSONString(userDTOS);
        }
    }

    // ---------------- api------------------
    public static class UserDTO {

        private Long id;

        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private void createClient(int clientTelnetPort, List<Class> classes)
            throws InterruptedException {
        int clientPort = RandomUtils.nextInt(3000, 30000);
        int clientAntRpcPort = RandomUtils.nextInt(3000, 30000);
        GenericApplicationContext clientApplicationContext = new GenericApplicationContext();
        MockEnvironment clientEnvironment = new MockEnvironment();
        clientEnvironment.setProperty("spring.application.name", "telnetClient");
        clientEnvironment.setProperty("server.port", String.valueOf(clientPort));
        clientEnvironment.setProperty("antrpc.port", String.valueOf(clientAntRpcPort));
        clientApplicationContext.setEnvironment(clientEnvironment);
        for (Class aClass : classes) {
            GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
            genericBeanDefinition.setBeanClass(aClass);
            clientApplicationContext.registerBeanDefinition(
                    aClass.getName(), genericBeanDefinition);
        }
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
    }

    private void createServer(int serverTelnetPort, List<Class> classes)
            throws InterruptedException {
        int serverPort = RandomUtils.nextInt(3000, 30000);
        int serverAntRpcPort = RandomUtils.nextInt(3000, 30000);
        GenericApplicationContext serverApplicationContext = new GenericApplicationContext();
        MockEnvironment serverEnvironment = new MockEnvironment();
        serverEnvironment.setProperty("spring.application.name", "serverTest");
        serverEnvironment.setProperty("server.port", String.valueOf(serverPort));
        serverEnvironment.setProperty("antrpc.port", String.valueOf(serverAntRpcPort));
        serverApplicationContext.setEnvironment(serverEnvironment);
        for (Class aClass : classes) {
            GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
            genericBeanDefinition.setBeanClass(aClass);
            serverApplicationContext.registerBeanDefinition(
                    aClass.getName(), genericBeanDefinition);
        }
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
    }
}
