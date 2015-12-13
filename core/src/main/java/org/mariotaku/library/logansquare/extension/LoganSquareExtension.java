package org.mariotaku.library.logansquare.extension;

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.ParameterizedType;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoganSquareExtension extends LoganSquare {

    private static Map<Class, JsonMapper> OBJECT_MAPPERS;
    private static ConcurrentHashMap<ParameterizedType, JsonMapper> PARAMETERIZED_OBJECT_MAPPERS;

    public static <E> void registerMapper(Class<E> cls, JsonMapper<? extends E> converter) {
        if (OBJECT_MAPPERS == null) {
            OBJECT_MAPPERS = getFieldValue("OBJECT_MAPPERS");
        }
        OBJECT_MAPPERS.put(cls, converter);
    }

    public static <E> void registerMapper(ParameterizedType<E> cls, JsonMapper<? extends E> converter) {
        if (PARAMETERIZED_OBJECT_MAPPERS == null) {
            PARAMETERIZED_OBJECT_MAPPERS = getFieldValue("PARAMETERIZED_OBJECT_MAPPERS");
        }
        PARAMETERIZED_OBJECT_MAPPERS.put(cls, converter);
    }

    private static <T> T getFieldValue(String name) {
        try {
            final Field field = LoganSquare.class.getDeclaredField(name);
            field.setAccessible(true);
            //noinspection unchecked
            return (T) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
