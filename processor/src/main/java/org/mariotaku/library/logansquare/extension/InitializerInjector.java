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

import com.bluelinelabs.logansquare.ParameterizedType;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Map;

/**
 * Created by mariotaku on 15/10/21.
 */
public class ObjectMapperInjector {
    private final LoganSquareExtensionInitializerInfo initializerInfo;

    public ObjectMapperInjector(LoganSquareExtensionInitializerInfo initializerInfo) {
        this.initializerInfo = initializerInfo;
    }


    public String getJavaClassFile(Elements elements, Types types) {
        try {
            return JavaFile.builder(LoganSquareExtension.class.getPackage().getName(), getTypeSpec(elements, types)).build().toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private TypeSpec getTypeSpec(Elements elements, Types types) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(LoganSquareExtensionInitializerInfo.getInitializerClassDeclarationName());
        builder.addModifiers(Modifier.PUBLIC);
        builder.addStaticBlock(getRegisterMappersCode(elements, types));
        builder.addStaticBlock(getRegisterConverterCode(elements, types));
        return builder.build();
    }

    private CodeBlock getRegisterMappersCode(Elements elements, Types types) {
        final CodeBlock.Builder builder = CodeBlock.builder();
        for (Map.Entry<TypeElement, TypeName> entry : initializerInfo.getMappers().entrySet()) {
            final TypeElement type = entry.getKey();
            final TypeName mapper = entry.getValue();
            builder.addStatement("$T.registerMapper(new $T<$T>() {}, new $T())", LoganSquareExtension.class,
                    ParameterizedType.class, type, mapper);
        }
        return builder.build();
    }

    private CodeBlock getRegisterConverterCode(Elements elements, Types types) {
        final CodeBlock.Builder builder = CodeBlock.builder();
        for (Map.Entry<TypeElement, TypeMirror> entry : initializerInfo.getImplementations().entrySet()) {
            final TypeElement type = entry.getKey();
            final TypeMirror impl = entry.getValue();
            final TypeMirror erasure = types.erasure(type.asType());
            builder.addStatement("$T.registerTypeConverter($T.class, new $T<$T>($T.class))", LoganSquareExtension.class,
                    erasure, ImplementationTypeConverter.class, erasure, impl);
        }
        for (Map.Entry<TypeElement, TypeName> entry : initializerInfo.getMappers().entrySet()) {
            final TypeElement type = entry.getKey();
            final TypeName impl = entry.getValue();
            final TypeMirror erasure = types.erasure(type.asType());
            builder.addStatement("$T.registerTypeConverter($T.class, new $T<$T>($T.class))", LoganSquareExtension.class,
                    erasure, MapperTypeConverter.class, erasure, impl);
        }
        for (TypeElement type : initializerInfo.getEnums()) {
            builder.addStatement("$T.registerTypeConverter($T.class, new $T<>($T.class))", LoganSquareExtension.class,
                    types.erasure(type.asType()), EnumConverter.class, type);
        }
        return builder.build();
    }

}
