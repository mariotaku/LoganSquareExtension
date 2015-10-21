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

package org.mariotaku.library.logansquare.extension.processor;

import com.bluelinelabs.logansquare.Constants;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.mariotaku.library.logansquare.extension.LoganSquareWrapperInitializerInfo;
import org.mariotaku.library.logansquare.extension.annotation.Implementation;
import org.mariotaku.library.logansquare.extension.annotation.Mapper;
import org.mariotaku.library.logansquare.extension.annotation.Wrapper;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;

import static javax.lang.model.element.Modifier.PRIVATE;

/**
 * Created by mariotaku on 15/10/21.
 */
public class ImplementationClassProcessor extends Processor {

    public ImplementationClassProcessor(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    public Class<? extends Annotation> getAnnotation() {
        return Implementation.class;
    }

    @Override
    public void findAndParseObjects(RoundEnvironment env, LoganSquareWrapperInitializerInfo initializerInfo, Elements elements, Types types) {
        for (Element element : env.getElementsAnnotatedWith(getAnnotation())) {
            processImplementationAnnotation(element, initializerInfo, elements, types);
        }
    }

    private void processImplementationAnnotation(Element element, LoganSquareWrapperInitializerInfo initializerInfo, Elements elements, Types types) {
        TypeElement type = (TypeElement) element;

        if (element.getModifiers().contains(PRIVATE)) {
            error(element, "%s: %s annotation can't be used on private classes.", type.getQualifiedName(), getAnnotation().getSimpleName());
        }
        final Implementation implAnnotation = type.getAnnotation(Implementation.class);
        TypeMirror implCls = null;
        try {
            implAnnotation.value();
        } catch (MirroredTypeException e) {
            implCls = e.getTypeMirror();
        }
        if (implCls != null) {
            initializerInfo.putImplementation(type, implCls);
            final TypeElement implElement = elements.getTypeElement(implCls.toString());
            final Mapper mapperAnnotation = implElement.getAnnotation(Mapper.class);
            final Wrapper wrapperAnnotation = implElement.getAnnotation(Wrapper.class);
            TypeName mapperCls = null, wrapperCls = null;
            if (mapperAnnotation != null) {
                try {
                    mapperAnnotation.value();
                } catch (MirroredTypeException e) {
                    mapperCls = TypeName.get(e.getTypeMirror());
                }
            }
            if (mapperCls == null) {
                mapperCls = getMapperClass(elements, types, implCls);
            }
            initializerInfo.putMapper(type, mapperCls);
            if (wrapperAnnotation != null) {
                try {
                    wrapperAnnotation.value();
                } catch (MirroredTypeException e) {
                    wrapperCls = TypeName.get(e.getTypeMirror());
                }
            }
            if (wrapperCls != null) {
                initializerInfo.putWrapper(type, wrapperCls);
            }
        }
    }

    private TypeName getMapperClass(Elements elements, Types types, TypeMirror implCls) {
        final Element element = types.asElement(implCls);
        final String packageName = String.valueOf(elements.getPackageOf(element).getQualifiedName());
        String implClsSimpleName = String.valueOf(element.getSimpleName()).replace('.', '$');
        return ClassName.get(packageName, implClsSimpleName + Constants.MAPPER_CLASS_SUFFIX);
    }
}
