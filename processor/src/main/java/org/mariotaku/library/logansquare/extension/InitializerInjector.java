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

import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Map;

/**
 * Created by mariotaku on 15/10/21.
 */
public class InitializerInjector {

    private final LoganSquareWrapperInitializerInfo initializerInfo;

    public InitializerInjector(LoganSquareWrapperInitializerInfo initializerInfo) {
        this.initializerInfo = initializerInfo;
    }


    public String getJavaClassFile(Elements elements, Types types) {
        try {
            return JavaFile.builder(initializerInfo.getInitializerName().packageName(), getTypeSpec(elements, types)).build().toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private TypeSpec getTypeSpec(Elements elements, Types types) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(initializerInfo.getInitializerName().simpleName());
        builder.addModifiers(Modifier.PUBLIC);
        builder.addSuperinterface(ClassName.get(LoganSquareWrapperInitializer.class));

        final MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("init");
        methodBuilder.addModifiers(Modifier.PUBLIC);

        for (Map.Entry<TypeElement, TypeName> entry : initializerInfo.getWrappers().entrySet()) {
            final TypeElement type = entry.getKey();
            final TypeName wrapper = entry.getValue();
            methodBuilder.addStatement("$T.registerWrapper($T.class, $T.class)", LoganSquareWrapper.class,
                    types.erasure(type.asType()), wrapper);
        }

        for (Map.Entry<TypeElement, TypeMirror> entry : initializerInfo.getImplementations().entrySet()) {
            final TypeElement type = entry.getKey();
            final TypeMirror impl = entry.getValue();
            final TypeMirror erasure = types.erasure(type.asType());
            methodBuilder.addStatement("$T.registerTypeConverter($T.class, new $T<$T>($T.class))", LoganSquareWrapper.class,
                    erasure, ImplementationTypeConverter.class, erasure, impl);
        }

        for (Map.Entry<TypeElement, TypeName> entry : initializerInfo.getMappers().entrySet()) {
            final TypeElement type = entry.getKey();
            final TypeName mapper = entry.getValue();
            final TypeMirror erasure = types.erasure(type.asType());
            methodBuilder.addStatement("$T.registerTypeConverter($T.class, new $T<$T>($T.INSTANCE))", LoganSquareWrapper.class,
                    erasure, MapperTypeConverter.class, erasure, mapper);
        }
        for (Map.Entry<TypeElement, TypeName> entry : initializerInfo.getWrappers().entrySet()) {
            final TypeElement type = entry.getKey();
            final TypeName impl = entry.getValue();
            final TypeMirror erasure = types.erasure(type.asType());
            methodBuilder.addStatement("$T.registerTypeConverter($T.class, new $T<$T>($T.class))", LoganSquareWrapper.class,
                    erasure, WrapperTypeConverter.class, erasure, impl);
        }
        for (TypeElement type : initializerInfo.getEnums()) {
            methodBuilder.addStatement("$T.registerTypeConverter($T.class, $T.get($T.class))", LoganSquareWrapper.class,
                    types.erasure(type.asType()), EnumConverter.class, type);
        }

        builder.addMethod(methodBuilder.build());

        return builder.build();
    }


}
