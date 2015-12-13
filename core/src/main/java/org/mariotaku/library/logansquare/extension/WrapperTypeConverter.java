package org.mariotaku.library.logansquare.extension;

import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

/**
 * Created by mariotaku on 15/10/21.
 */
public class WrapperTypeConverter<T> implements TypeConverter<T> {

    private final Class<? extends ModelWrapper<? extends T>> mapperCls;

    public WrapperTypeConverter(Class<? extends ModelWrapper<? extends T>> mapperCls) {
        this.mapperCls = mapperCls;
    }

    @Override
    public T parse(JsonParser jsonParser) throws IOException {
        return LoganSquareExtension.mapperFor(mapperCls).parse(jsonParser).getWrapped(null);
    }

    @Override
    public void serialize(T object, String fieldName, boolean writeFieldNameForObject, JsonGenerator jsonGenerator) throws IOException {
        throw new UnsupportedOperationException();
    }
}
