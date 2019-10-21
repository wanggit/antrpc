package antrpc.commons.bean;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class IdGenHelper {

    private IdGenHelper() {}

    private static final IdGenHelper instance = new IdGenHelper();

    public static IdGenHelper getInstance() {
        return instance;
    }

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    public int getId() {
        return atomicInteger.getAndIncrement();
    }

    public String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
