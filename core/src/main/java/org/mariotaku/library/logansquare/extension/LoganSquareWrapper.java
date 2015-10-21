/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.library.logansquare.extension;

import com.bluelinelabs.logansquare.Constants;
import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.NoSuchMapperException;
import org.mariotaku.library.logansquare.extension.annotation.Implementation;
import org.mariotaku.library.logansquare.extension.annotation.Mapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Created by mariotaku on 15/10/21.
 */
public class LoganSquareWrapper extends LoganSquare {

    static {
        try {
            Class.forName(LoganSquareWrapper.class.getName() + "Initializer");
        } catch (ClassNotFoundException ignore) {

        }
    }

    /**
     * Parse an object from an InputStream.
     *
     * @param is              The InputStream, most likely from your networking library.
     * @param jsonObjectClass The @JsonObject class to parse the InputStream into
     */
    public static <E> E parse(InputStream is, Class<E> jsonObjectClass) throws IOException {
        return mapperFor(jsonObjectClass).parse(is);
    }

    /**
     * Parse an object from a String. Note: parsing from an InputStream should be preferred over parsing from a String if possible.
     *
     * @param jsonString      The JSON string being parsed.
     * @param jsonObjectClass The @JsonObject class to parse the InputStream into
     */
    public static <E> E parse(String jsonString, Class<E> jsonObjectClass) throws IOException {
        return mapperFor(jsonObjectClass).parse(jsonString);
    }

    /**
     * Parse a list of objects from an InputStream.
     *
     * @param is              The inputStream, most likely from your networking library.
     * @param jsonObjectClass The @JsonObject class to parse the InputStream into
     */
    public static <E> List<E> parseList(InputStream is, Class<E> jsonObjectClass) throws IOException {
        return mapperFor(jsonObjectClass).parseList(is);
    }

    /**
     * Parse a list of objects from a String. Note: parsing from an InputStream should be preferred over parsing from a String if possible.
     *
     * @param jsonString      The JSON string being parsed.
     * @param jsonObjectClass The @JsonObject class to parse the InputStream into
     */
    public static <E> List<E> parseList(String jsonString, Class<E> jsonObjectClass) throws IOException {
        return mapperFor(jsonObjectClass).parseList(jsonString);
    }

    /**
     * Parse a map of objects from an InputStream.
     *
     * @param is              The inputStream, most likely from your networking library.
     * @param jsonObjectClass The @JsonObject class to parse the InputStream into
     */
    public static <E> Map<String, E> parseMap(InputStream is, Class<E> jsonObjectClass) throws IOException {
        return mapperFor(jsonObjectClass).parseMap(is);
    }

    /**
     * Parse a map of objects from a String. Note: parsing from an InputStream should be preferred over parsing from a String if possible.
     *
     * @param jsonString      The JSON string being parsed.
     * @param jsonObjectClass The @JsonObject class to parse the InputStream into
     */
    public static <E> Map<String, E> parseMap(String jsonString, Class<E> jsonObjectClass) throws IOException {
        return mapperFor(jsonObjectClass).parseMap(jsonString);
    }

    /**
     * Serialize an object to a JSON String.
     *
     * @param object The object to serialize.
     */
    @SuppressWarnings("unchecked")
    public static <E> String serialize(E object) throws IOException {
        return mapperFor((Class<E>) object.getClass()).serialize(object);
    }

    /**
     * Serialize an object to an OutputStream.
     *
     * @param object The object to serialize.
     * @param os     The OutputStream being written to.
     */
    @SuppressWarnings("unchecked")
    public static <E> void serialize(E object, OutputStream os) throws IOException {
        mapperFor((Class<E>) object.getClass()).serialize(object, os);
    }

    /**
     * Serialize a list of objects to a JSON String.
     *
     * @param list            The list of objects to serialize.
     * @param jsonObjectClass The @JsonObject class of the list elements
     */
    public static <E> String serialize(List<E> list, Class<E> jsonObjectClass) throws IOException {
        return mapperFor(jsonObjectClass).serialize(list);
    }

    /**
     * Serialize a list of objects to an OutputStream.
     *
     * @param list            The list of objects to serialize.
     * @param os              The OutputStream to which the list should be serialized
     * @param jsonObjectClass The @JsonObject class of the list elements
     */
    public static <E> void serialize(List<E> list, OutputStream os, Class<E> jsonObjectClass) throws IOException {
        mapperFor(jsonObjectClass).serialize(list, os);
    }

    /**
     * Serialize a map of objects to a JSON String.
     *
     * @param map             The map of objects to serialize.
     * @param jsonObjectClass The @JsonObject class of the list elements
     */
    public static <E> String serialize(Map<String, E> map, Class<E> jsonObjectClass) throws IOException {
        return mapperFor(jsonObjectClass).serialize(map);
    }

    /**
     * Serialize a map of objects to an OutputStream.
     *
     * @param map             The map of objects to serialize.
     * @param os              The OutputStream to which the list should be serialized
     * @param jsonObjectClass The @JsonObject class of the list elements
     */
    public static <E> void serialize(Map<String, E> map, OutputStream os, Class<E> jsonObjectClass) throws IOException {
        mapperFor(jsonObjectClass).serialize(map, os);
    }

    /**
     * Returns a JsonMapper for a given class that has been annotated with @JsonObject.
     *
     * @param cls The class for which the JsonMapper should be fetched.
     */
    @SuppressWarnings("unchecked")
    public static <E> JsonMapper<E> mapperFor(Class<E> cls) throws NoSuchMapperException {
        JsonMapper<E> cachedMapper = getObjectMappers().get(cls);

        if (cachedMapper == null) {
            try {
                Implementation implAnnotation = cls.getAnnotation(Implementation.class);
                Class<?> implClass;
                if (implAnnotation != null) {
                    implClass = implAnnotation.value();
                } else {
                    implClass = cls;
                }
                Mapper mapperAnnotation = implClass.getAnnotation(Mapper.class);
                Class<?> mapperClass;
                if (mapperAnnotation != null) {
                    mapperClass = mapperAnnotation.value();
                } else {
                    mapperClass = Class.forName(implClass.getName() + Constants.MAPPER_CLASS_SUFFIX);
                }
                cachedMapper = (JsonMapper<E>) mapperClass.newInstance();
                getObjectMappers().put(cls, cachedMapper);
            } catch (Exception e) {
                throw new NoSuchMapperException(cls, e);
            }
        }

        return cachedMapper;
    }


    /**
     * Register a new JsonMapper for parsing and serialization.
     *
     * @param cls    The class for which the JsonMapper should be used.
     * @param mapper The JsonMapper
     */
    public static <E> void registerJsonMapper(Class<E> cls, JsonMapper<? extends E> mapper) {
        getObjectMappers().put(cls, mapper);
    }

    private static Map<Class, JsonMapper> getObjectMappers() {
        try {
            final Field field = LoganSquare.class.getDeclaredField("OBJECT_MAPPERS");
            field.setAccessible(true);
            //noinspection unchecked
            return (Map<Class, JsonMapper>) field.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static Class<?> getWrapperClass(Class<?> cls) {
        return null;
    }
}
