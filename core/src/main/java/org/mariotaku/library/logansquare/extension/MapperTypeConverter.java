package org.mariotaku.library.logansquare.extension;

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

/**
 * Created by mariotaku on 15/10/21.
 */
public class MapperTypeConverter<T> implements TypeConverter<T> {

    private final Class<? extends JsonMapper<? extends T>> mapperCls;

    public MapperTypeConverter(Class<? extends JsonMapper<? extends T>> mapperCls) {
        this.mapperCls = mapperCls;
    }

    @Override
    public T parse(JsonParser jsonParser) throws IOException {
        final JsonMapper<? extends T> mapper = getJsonMapper();
        return mapper.parse(jsonParser);
    }

    private JsonMapper<? extends T> getJsonMapper() {
        try {
            return mapperCls.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void serialize(T object, String fieldName, boolean writeFieldNameForObject, JsonGenerator jsonGenerator) throws IOException {
        //noinspection unchecked
        final JsonMapper<T> mapper = (JsonMapper<T>) getJsonMapper();
        if (writeFieldNameForObject) {
            jsonGenerator.writeFieldName(fieldName);
        }
        mapper.serialize(object, jsonGenerator, writeFieldNameForObject);
    }
}
