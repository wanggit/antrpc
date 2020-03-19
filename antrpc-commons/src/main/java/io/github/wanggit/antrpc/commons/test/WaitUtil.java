package io.github.wanggit.antrpc.commons.test;

import java.util.concurrent.TimeUnit;

public class WaitUtil {

    public static void wait(int total, int unit) throws InterruptedException {
        wait(total, unit, true);
    }

    public static void wait(int total, int unit, boolean println) throws InterruptedException {
        if (total < unit) {
            throw new IllegalArgumentException("total >= unit, must be true.");
        }
        int len = total / unit;
        for (int i = 0; i < len; i++) {
            if (println) {
                System.out.println("Wait for " + (total - i * unit) + " seconds.");
            }
            TimeUnit.SECONDS.sleep(unit);
        }
    }
}
