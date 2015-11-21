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

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.internal.JsonMapperLoader;
import com.bluelinelabs.logansquare.internal.objectmappers.*;
import com.bluelinelabs.logansquare.processor.JsonMapperLoaderInjector;
import com.bluelinelabs.logansquare.util.SimpleArrayMap;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mariotaku on 15/10/21.
 */
public class ExtensionLoaderInjector {
    private final LoganSquareWrapperInitializerInfo initializerInfo;
    private final Map<Class, Class> mBuiltInMapperMap;

    public ExtensionLoaderInjector(LoganSquareWrapperInitializerInfo initializerInfo) {
        this.initializerInfo = initializerInfo;

        mBuiltInMapperMap = new HashMap<>();
        mBuiltInMapperMap.put(String.class, StringMapper.class);
        mBuiltInMapperMap.put(Integer.class, IntegerMapper.class);
        mBuiltInMapperMap.put(Long.class, LongMapper.class);
        mBuiltInMapperMap.put(Float.class, FloatMapper.class);
        mBuiltInMapperMap.put(Double.class, DoubleMapper.class);
        mBuiltInMapperMap.put(Boolean.class, BooleanMapper.class);
        mBuiltInMapperMap.put(Object.class, ObjectMapper.class);
        mBuiltInMapperMap.put(List.class, ListMapper.class);
        mBuiltInMapperMap.put(ArrayList.class, ListMapper.class);
        mBuiltInMapperMap.put(Map.class, MapMapper.class);
        mBuiltInMapperMap.put(HashMap.class, MapMapper.class);
    }


    public String getJavaClassFile(Elements elements, Types types) {
        try {
            return JavaFile.builder(initializerInfo.name.packageName(), getTypeSpec(elements, types)).build().toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private TypeSpec getTypeSpec(Elements elements, Types types) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(initializerInfo.name.simpleName());
        builder.addModifiers(Modifier.PUBLIC);
        builder.addSuperinterface(ClassName.get(JsonMapperLoader.class));

        addAllBuiltInMappers(builder);
//        builder.addStaticBlock(getRegisterMappersCode(elements, types));
//        builder.addStaticBlock(getRegisterConverterCode(elements, types));
//        builder.addStaticBlock(getRegisterWrappersCode(elements, types));
        builder.addMethod(getPutAllJsonMappersMethod(elements, types));

        return builder.build();
    }

//    private CodeBlock getRegisterMappersCode(Elements elements, Types types) {
//        final CodeBlock.Builder builder = CodeBlock.builder();
//        for (Map.Entry<TypeElement, TypeName> entry : initializerInfo.getMappers().entrySet()) {
//            final TypeElement type = entry.getKey();
//            final TypeName mapper = entry.getValue();
//            builder.addStatement("$T.registerJsonMapper($T.class, $T.class)", LoganSquareWrapper.class,
//                    types.erasure(type.asType()), mapper);
//        }
//        return builder.build();
//    }
//
//    private CodeBlock getRegisterWrappersCode(Elements elements, Types types) {
//        final CodeBlock.Builder builder = CodeBlock.builder();
//        for (Map.Entry<TypeElement, TypeName> entry : initializerInfo.getWrappers().entrySet()) {
//            final TypeElement type = entry.getKey();
//            final TypeName wrapper = entry.getValue();
//            builder.addStatement("$T.registerWrapper($T.class, $T.class)", LoganSquareWrapper.class,
//                    types.erasure(type.asType()), wrapper);
//        }
//        return builder.build();
//    }
//
//    private CodeBlock getRegisterConverterCode(Elements elements, Types types) {
//        final CodeBlock.Builder builder = CodeBlock.builder();
//        for (Map.Entry<TypeElement, TypeMirror> entry : initializerInfo.getImplementations().entrySet()) {
//            final TypeElement type = entry.getKey();
//            final TypeMirror impl = entry.getValue();
//            final TypeMirror erasure = types.erasure(type.asType());
//            builder.addStatement("$T.registerTypeConverter($T.class, new $T<$T>($T.class))", LoganSquareWrapper.class,
//                    erasure, ImplementationTypeConverter.class, erasure, impl);
//        }
//        for (Map.Entry<TypeElement, TypeName> entry : initializerInfo.getMappers().entrySet()) {
//            final TypeElement type = entry.getKey();
//            final TypeName impl = entry.getValue();
//            final TypeMirror erasure = types.erasure(type.asType());
//            builder.addStatement("$T.registerTypeConverter($T.class, new $T<$T>($T.class))", LoganSquareWrapper.class,
//                    erasure, MapperTypeConverter.class, erasure, impl);
//        }
//        for (Map.Entry<TypeElement, TypeName> entry : initializerInfo.getWrappers().entrySet()) {
//            final TypeElement type = entry.getKey();
//            final TypeName impl = entry.getValue();
//            final TypeMirror erasure = types.erasure(type.asType());
//            builder.addStatement("$T.registerTypeConverter($T.class, new $T<$T>($T.class))", LoganSquareWrapper.class,
//                    erasure, WrapperTypeConverter.class, erasure, impl);
//        }
//        for (TypeElement type : initializerInfo.getEnums()) {
//            builder.addStatement("$T.registerTypeConverter($T.class, new $T<>($T.class))", LoganSquareWrapper.class,
//                    types.erasure(type.asType()), EnumConverter.class, type);
//        }
//        return builder.build();
//    }

    private MethodSpec getPutAllJsonMappersMethod(Elements elements, Types types) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("putAllJsonMappers")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterizedTypeName.get(ClassName.get(SimpleArrayMap.class), ClassName.get(Class.class), ClassName.get(JsonMapper.class)), "map");

        for (Class cls : mBuiltInMapperMap.keySet()) {
            builder.addStatement("map.put($T.class, $L)", cls, JsonMapperLoaderInjector.getMapperVariableName(mBuiltInMapperMap.get(cls)));
        }

        for (Map.Entry<TypeElement, TypeName> entry : initializerInfo.getMappers().entrySet()) {
            final TypeElement type = entry.getKey();
            final TypeName mapper = entry.getValue();
            builder.addStatement("map.put($T.class, $T.INSTANCE)", types.erasure(type.asType()), mapper);
        }

        return builder.build();
    }



    private void addAllBuiltInMappers(TypeSpec.Builder typeSpecBuilder) {
        addBuiltInMapper(typeSpecBuilder, StringMapper.class);
        addBuiltInMapper(typeSpecBuilder, IntegerMapper.class);
        addBuiltInMapper(typeSpecBuilder, LongMapper.class);
        addBuiltInMapper(typeSpecBuilder, FloatMapper.class);
        addBuiltInMapper(typeSpecBuilder, DoubleMapper.class);
        addBuiltInMapper(typeSpecBuilder, BooleanMapper.class);
        addBuiltInMapper(typeSpecBuilder, ObjectMapper.class);
        addBuiltInMapper(typeSpecBuilder, ListMapper.class);
        addBuiltInMapper(typeSpecBuilder, MapMapper.class);
    }

    private void addBuiltInMapper(TypeSpec.Builder typeSpecBuilder, Class mapperClass) {
        typeSpecBuilder.addField(FieldSpec.builder(mapperClass, JsonMapperLoaderInjector.getMapperVariableName(mapperClass))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T()", mapperClass)
                .build()
        );
    }

}
