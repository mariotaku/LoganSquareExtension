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

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Map;

/**
 * Created by mariotaku on 15/10/21.
 */
public class ObjectMapperInjector {
    private final LoganSquareWrapperInitializerInfo initializerInfo;

    public ObjectMapperInjector(LoganSquareWrapperInitializerInfo initializerInfo) {
        this.initializerInfo = initializerInfo;
    }


    public String getJavaClassFile() {
        try {
            return JavaFile.builder(LoganSquareWrapper.class.getPackage().getName(), getTypeSpec()).build().toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private TypeSpec getTypeSpec() {
        TypeSpec.Builder builder = TypeSpec.classBuilder("LoganSquareWrapperInitializer");
        builder.addModifiers(Modifier.PUBLIC);
        builder.addStaticBlock(getAddImplementationsCode());
        return builder.build();
    }

    private CodeBlock getAddImplementationsCode() {
        final CodeBlock.Builder builder = CodeBlock.builder();
        for (Map.Entry<TypeElement, Class<?>> entry : initializerInfo.getMappers().entrySet()) {
            final TypeElement type = entry.getKey();
            final Class<?> impl = entry.getValue();
            builder.addStatement("$T.registerJsonMapper(%T.class, %T.class)", LoganSquareWrapper.class, type, impl);
        }
        return builder.build();
    }

}
