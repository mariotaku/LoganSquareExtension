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

    private final JsonMapper<T> mapper;

    public MapperTypeConverter(JsonMapper<T> mapper) {
        this.mapper = mapper;
    }

    @Override
    public T parse(JsonParser jsonParser) throws IOException {
        return mapper.parse(jsonParser);
    }

    @Override
    public void serialize(T object, String fieldName, boolean writeFieldNameForObject, JsonGenerator jsonGenerator) throws IOException {
        if (writeFieldNameForObject) {
            jsonGenerator.writeFieldName(fieldName);
        }
        mapper.serialize(object, jsonGenerator, writeFieldNameForObject);
    }
}
