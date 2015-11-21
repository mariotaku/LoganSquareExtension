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
import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.ParameterizedType;
import com.bluelinelabs.logansquare.internal.JsonMapperLoader;
import com.bluelinelabs.logansquare.util.SimpleArrayMap;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mariotaku on 15/10/21.
 */
public class ExtensionLoaderInjector {

    private static final String PARAMETERIZED_MAPPERS_VARIABLE_NAME = "PARAMETERIZED_OBJECT_MAPPERS";

    private final LoganSquareWrapperInitializerInfo initializerInfo;

    public ExtensionLoaderInjector(LoganSquareWrapperInitializerInfo initializerInfo) {
        this.initializerInfo = initializerInfo;
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


        builder.addField(FieldSpec.builder(ParameterizedTypeName.get(ConcurrentHashMap.class, ParameterizedType.class, JsonMapper.class), PARAMETERIZED_MAPPERS_VARIABLE_NAME)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T()", ParameterizedTypeName.get(ConcurrentHashMap.class, ParameterizedType.class, JsonMapper.class))
                .build());

        builder.addMethod(getPutAllJsonMappersMethod(elements, types));
        builder.addMethod(getParameterizedMethodGetterMethod());
        builder.addMethod(getStaticParameterizedMapperGetterMethod());
        builder.addMethod(getStaticParameterizedMapperWithPartialGetterMethod());
        builder.addMethod(getGetClassMapperMethod());

        return builder.build();
    }

    private MethodSpec getPutAllJsonMappersMethod(Elements elements, Types types) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("putAllJsonMappers")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterizedTypeName.get(ClassName.get(SimpleArrayMap.class), ClassName.get(Class.class), ClassName.get(JsonMapper.class)), "map");

        for (Map.Entry<TypeElement, TypeName> entry : initializerInfo.getMappers().entrySet()) {
            final TypeElement type = entry.getKey();
            final TypeName mapper = entry.getValue();
            builder.addStatement("map.put($T.class, $T.INSTANCE)", types.erasure(type.asType()), mapper);
        }

        for (Map.Entry<TypeElement, TypeName> entry : initializerInfo.getWrappers().entrySet()) {
            final TypeElement type = entry.getKey();
            final TypeName wrapper = entry.getValue();
            builder.addStatement("$T.registerWrapper($T.class, $T.class)", LoganSquareWrapper.class,
                    types.erasure(type.asType()), wrapper);
        }


        for (Map.Entry<TypeElement, TypeMirror> entry : initializerInfo.getImplementations().entrySet()) {
            final TypeElement type = entry.getKey();
            final TypeMirror impl = entry.getValue();
            final TypeMirror erasure = types.erasure(type.asType());
            builder.addStatement("$T.registerTypeConverter($T.class, new $T<$T>($T.class))", LoganSquareWrapper.class,
                    erasure, ImplementationTypeConverter.class, erasure, impl);
        }

        for (Map.Entry<TypeElement, TypeName> entry : initializerInfo.getMappers().entrySet()) {
            final TypeElement type = entry.getKey();
            final TypeName mapper = entry.getValue();
            final TypeMirror erasure = types.erasure(type.asType());
            builder.addStatement("$T.registerTypeConverter($T.class, new $T<$T>($T.INSTANCE))", LoganSquareWrapper.class,
                    erasure, MapperTypeConverter.class, erasure, mapper);
        }
        for (Map.Entry<TypeElement, TypeName> entry : initializerInfo.getWrappers().entrySet()) {
            final TypeElement type = entry.getKey();
            final TypeName impl = entry.getValue();
            final TypeMirror erasure = types.erasure(type.asType());
            builder.addStatement("$T.registerTypeConverter($T.class, new $T<$T>($T.class))", LoganSquareWrapper.class,
                    erasure, WrapperTypeConverter.class, erasure, impl);
        }
        for (TypeElement type : initializerInfo.getEnums()) {
            builder.addStatement("$T.registerTypeConverter($T.class, $T.get($T.class))", LoganSquareWrapper.class,
                    types.erasure(type.asType()), EnumConverter.class, type);
        }

        return builder.build();
    }

    private MethodSpec getParameterizedMethodGetterMethod() {
        return MethodSpec.methodBuilder("mapperFor")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(TypeVariableName.get("T"))
                .returns(ParameterizedTypeName.get(ClassName.get(JsonMapper.class), TypeVariableName.get("T")))
                .addParameter(ParameterizedTypeName.get(ClassName.get(ParameterizedType.class), TypeVariableName.get("T")), "type")
                .addParameter(ParameterizedTypeName.get(ClassName.get(SimpleArrayMap.class), ClassName.get(ParameterizedType.class), ClassName.get(JsonMapper.class)), "partialMappers")
                .addStatement("return _mapperFor(type, partialMappers)")
                .build();
    }


    private MethodSpec getStaticParameterizedMapperGetterMethod() {
        return MethodSpec.methodBuilder("_mapperFor")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariable(TypeVariableName.get("T"))
                .returns(ParameterizedTypeName.get(ClassName.get(JsonMapper.class), TypeVariableName.get("T")))
                .addParameter(ParameterizedTypeName.get(ClassName.get(ParameterizedType.class), TypeVariableName.get("T")), "type")
                .addStatement("return _mapperFor(type, null)")
                .build();

    }

    private MethodSpec getStaticParameterizedMapperWithPartialGetterMethod() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("_mapperFor")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariable(TypeVariableName.get("T"))
                .returns(ParameterizedTypeName.get(ClassName.get(JsonMapper.class), TypeVariableName.get("T")))
                .addParameter(ParameterizedTypeName.get(ClassName.get(ParameterizedType.class), TypeVariableName.get("T")), "type")
                .addParameter(ParameterizedTypeName.get(ClassName.get(SimpleArrayMap.class), ClassName.get(ParameterizedType.class), ClassName.get(JsonMapper.class)), "partialMappers");

        methodBuilder
                .beginControlFlow("if (type.typeParameters.size() == 0)")
                .addStatement("return getMapper((Class<T>)type.rawType)")
                .endControlFlow()
                .beginControlFlow("if (partialMappers == null)")
                .addStatement("partialMappers = new $T()", ParameterizedTypeName.get(ClassName.get(SimpleArrayMap.class), ClassName.get(ParameterizedType.class), ClassName.get(JsonMapper.class)))
                .endControlFlow()
                .addStatement("$T mapper", ParameterizedTypeName.get(ClassName.get(JsonMapper.class), TypeVariableName.get("T")))
                .beginControlFlow("if (partialMappers.containsKey(type))")
                .addStatement("mapper = partialMappers.get(type)")
                .nextControlFlow("else if ($L.containsKey(type))", PARAMETERIZED_MAPPERS_VARIABLE_NAME)
                .addStatement("mapper = $L.get(type)", PARAMETERIZED_MAPPERS_VARIABLE_NAME)
                .nextControlFlow("else");

        boolean conditionalStarted = false;
        for (Map.Entry<TypeElement, TypeName> entry : initializerInfo.getMappers().entrySet()) {
            final List<? extends TypeParameterElement> typeParameters = entry.getKey().getTypeParameters();
            if (typeParameters.size() > 0) {
                String conditional = String.format("if (type.rawType == %s.class)", entry.getKey().toString().replaceAll("<(.*?)>", ""));
                if (conditionalStarted) {
                    methodBuilder.nextControlFlow("else " + conditional);
                } else {
                    conditionalStarted = true;
                    methodBuilder.beginControlFlow(conditional);
                }

                methodBuilder.beginControlFlow("if (type.typeParameters.size() == $L)", typeParameters.size());

                StringBuilder constructorArgs = new StringBuilder();
                for (int i = 0; i < typeParameters.size(); i++) {
                    constructorArgs.append(", type.typeParameters.get(").append(i).append(")");
                }
                methodBuilder.addStatement("mapper = new $T(type" + constructorArgs.toString() + ", partialMappers)", entry.getValue());

                methodBuilder.nextControlFlow("else");
                methodBuilder.addStatement(
                        "throw new $T(\"Invalid number of parameter types. Type $T expects $L parameter types, received \" + type.typeParameters.size())",
                        RuntimeException.class, entry.getKey(), typeParameters.size()
                );
                methodBuilder.endControlFlow();
            }
        }

        if (conditionalStarted) {
            methodBuilder.nextControlFlow("else")
                    .addStatement("mapper = null")
                    .endControlFlow();
        } else {
            methodBuilder.addStatement("mapper = null");
        }

        methodBuilder.beginControlFlow("if (mapper != null)")
                .addStatement("$L.put(type, mapper)", PARAMETERIZED_MAPPERS_VARIABLE_NAME)
                .endControlFlow();
        methodBuilder.endControlFlow();

        methodBuilder.addStatement("System.out.println(\"type = \" + type + \"rawType = \" + type.rawType + \"; mapper = \" + mapper)");

        methodBuilder.addStatement("return mapper");

        return methodBuilder.build();
    }

    private MethodSpec getGetClassMapperMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getMapper")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addTypeVariable(TypeVariableName.get("T"))
                .returns(ParameterizedTypeName.get(ClassName.get(JsonMapper.class), TypeVariableName.get("T")))
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), TypeVariableName.get("T")), "cls")
                .addStatement("$T<T> mapper = $T.getMapper(cls)", JsonMapper.class, LoganSquare.class)
                .beginControlFlow("if (mapper == null)");

        boolean ifStatementStarted = false;

        for (Map.Entry<TypeElement, TypeName> entry : initializerInfo.getMappers().entrySet()) {
            if (entry.getKey().getTypeParameters().size() == 0) {
                if (!ifStatementStarted) {
                    builder.beginControlFlow("if (cls == $T.class)", entry.getValue());
                    ifStatementStarted = true;
                } else {
                    builder.nextControlFlow("else if (cls == $T.class)", entry.getValue());
                }
                builder.addStatement("mapper = ($T<T>) $T.INSTANCE", JsonMapper.class, entry.getValue());
            }
        }

        builder.endControlFlow();
        builder.endControlFlow();

        builder.addStatement("return mapper");

        return builder.build();
    }
}
