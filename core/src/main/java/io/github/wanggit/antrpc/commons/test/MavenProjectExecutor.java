package io.github.wanggit.antrpc.commons.test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

public class MavenProjectExecutor {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void start(
            final String pomPath, int waitTimes, String healthUrl, final List<String> args)
            throws InterruptedException, TimeoutException, ExecutionException {
        Future<Boolean> future =
                executorService.submit(
                        new Callable<Boolean>() {
                            @Override
                            public Boolean call() throws Exception {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            StringBuilder builder = new StringBuilder();
                                            for (String arg : args) {
                                                builder.append(" -D").append(arg).append(" ");
                                            }
                                            CmdExecutor
                                                    .executeWindowsCmdAtPathAndPrintResultToConsole(
                                                            "mvn -f "
                                                                    + pomPath
                                                                    + builder.toString()
                                                                    + " spring-boot:run");
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();
                                WaitUtil.wait(waitTimes, 5);
                                return HealthChecker.isHealth(healthUrl);
                            }
                        });
        Boolean result = future.get(waitTimes + 10, TimeUnit.SECONDS);
        if (null == result || !result) {
            throw new RuntimeException(pomPath + "启动失败....");
        }
    }

    public static void stop(int port) throws IOException {
        String result =
                CmdExecutor.executeWindowsCmdReturnResult(
                        "netstat -ano | findstr \"" + port + "\"");
        result = result.split(System.lineSeparator())[0];
        String pid =
                result.substring(result.lastIndexOf("LISTENING"))
                        .replaceFirst("LISTENING", "")
                        .trim();
        CmdExecutor.executeWindowsCmdReturnResult("taskkill /F /pid " + pid);
    }
}
