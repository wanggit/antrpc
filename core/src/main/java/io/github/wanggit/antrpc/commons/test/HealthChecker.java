package io.github.wanggit.antrpc.commons.test;

import java.io.IOException;

public class HealthChecker {

    public static boolean isHealth(String healthUrl) throws IOException {
        String result = CmdExecutor.executeWindowsCmdReturnResult("curl " + healthUrl);
        return null != result && result.contains("UP");
    }
}
