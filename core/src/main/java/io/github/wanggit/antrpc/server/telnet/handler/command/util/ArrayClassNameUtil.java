package io.github.wanggit.antrpc.server.telnet.handler.command.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class ArrayClassNameUtil {

    private static final Map<String, String> ARRAY_CLASS_NAMES;

    static {
        Map<String, String> m = new HashMap<>();
        m.put("int[]", "[I");
        m.put("boolean[]", "[Z");
        m.put("float[]", "[F");
        m.put("long[]", "[J");
        m.put("short[]", "[S");
        m.put("byte[]", "[B");
        m.put("double[]", "[D");
        m.put("char[]", "[C");
        ARRAY_CLASS_NAMES = Collections.unmodifiableMap(m);
    }

    public static String replaceArrayClassName(String fullMethodName) {
        for (Map.Entry<String, String> entry : ARRAY_CLASS_NAMES.entrySet()) {
            fullMethodName = StringUtils.replace(fullMethodName, entry.getKey(), entry.getValue());
        }
        fullMethodName = fullMethodName.replaceAll("([\\w.]+)\\[\\]", "[L$1;");
        return fullMethodName;
    }
}
