package io.github.wanggit.antrpc.commons.bean;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SerialNumberThreadLocal {

    private static final ThreadLocal<TraceEntity> threadLocal = new ThreadLocal<>();
    private static final String CALLER_ROOT = "root";

    public static TraceEntity get() {
        TraceEntity entity = threadLocal.get();
        if (null == entity) {
            entity = new TraceEntity();
            entity.setSerialNumber(IdGenHelper.getInstance().getUUID());
            entity.setCaller(CALLER_ROOT);
            set(entity);
        }
        return threadLocal.get();
    }

    public static void set(TraceEntity entity) {
        threadLocal.set(entity);
        if (log.isInfoEnabled()) {
            log.info(
                    "threadId = "
                            + Thread.currentThread().getId()
                            + " thread local set data "
                            + JSONObject.toJSONString(entity));
        }
    }

    public static void clean() {
        threadLocal.remove();
        if (log.isInfoEnabled()) {
            log.info(
                    "threadId = " + Thread.currentThread().getId() + " thread local data removed.");
        }
    }

    public static class TraceEntity {
        private String serialNumber;
        private String caller;

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        public String getCaller() {
            return caller;
        }

        public void setCaller(String caller) {
            this.caller = caller;
        }
    }
}
