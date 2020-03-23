package io.github.wanggit.antrpc.commons.utils;

import com.alibaba.fastjson.JSONObject;
import org.springframework.util.ClassUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public abstract class CastValueTo {

    public static Object cast(String value, Class parameterType) {
        if (short.class.equals(parameterType) || Short.class.equals(parameterType)) {
            return Short.parseShort(value);
        }
        if (int.class.equals(parameterType) || Integer.class.equals(parameterType)) {
            return Integer.parseInt(value);
        }
        if (long.class.equals(parameterType) || Long.class.equals(parameterType)) {
            return Long.parseLong(value);
        }
        if (float.class.equals(parameterType) || Float.class.equals(parameterType)) {
            return Float.parseFloat(value);
        }
        if (double.class.equals(parameterType) || Double.class.equals(parameterType)) {
            return Long.parseLong(value);
        }
        if (BigDecimal.class.equals(parameterType)) {
            return new BigDecimal(value);
        }
        if (BigInteger.class.equals(parameterType)) {
            return new BigInteger(value);
        }
        if (String.class.equals(parameterType)) {
            return value;
        }
        if (ClassUtils.isPrimitiveArray(parameterType)
                || ClassUtils.isPrimitiveWrapperArray(parameterType)
                || List.class.equals(parameterType)
                || Map.class.equals(parameterType)
                || Set.class.equals(parameterType)
                || Vector.class.equals(parameterType)) {
            return JSONObject.parseObject(value, parameterType);
        }
        return JSONObject.parseObject(value, parameterType);
    }
}
