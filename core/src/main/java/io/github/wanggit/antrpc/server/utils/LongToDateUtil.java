package io.github.wanggit.antrpc.server.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LongToDateUtil {

    public static String toDateStr(Long value) {
        if (null == value) {
            return null;
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(value));
    }
}
