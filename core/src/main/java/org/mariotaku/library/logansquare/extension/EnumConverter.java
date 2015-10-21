package org.mariotaku.library.logansquare.extension;

import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EnumConverter<T extends Enum<T>> implements TypeConverter<T> {
    private final Class<T> cls;

    EnumConverter(Class<T> cls) {
        this.cls = cls;
    }

    public static <T extends Enum<T>> EnumConverter<T> get(Class<T> cls) {
        return new EnumConverter<>(cls);
    }

    @SuppressWarnings({"unchecked", "TryWithIdenticalCatches"})
    @Override
    public T parse(JsonParser jsonParser) throws IOException {
        try {
            final Method method = cls.getMethod("parse", String.class);
            return (T) method.invoke(null, jsonParser.getValueAsString());
        } catch (NoSuchMethodException e) {
            return Enum.valueOf(cls, jsonParser.getValueAsString());
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void serialize(T object, String fieldName, boolean writeFieldNameForObject, JsonGenerator jsonGenerator) {
        throw new UnsupportedOperationException();
    }
}