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

import org.mariotaku.library.logansquare.extension.LoganSquareWrapperInitializerInfo;
import org.mariotaku.library.logansquare.extension.annotation.Implementation;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.lang.model.element.Modifier.PRIVATE;

/**
 * Created by mariotaku on 15/10/21.
 */
public class ImplementationClassProcessor extends Processor {

    public ImplementationClassProcessor(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    public Class getAnnotation() {
        return Implementation.class;
    }

    @Override
    public void findAndParseObjects(RoundEnvironment env, LoganSquareWrapperInitializerInfo initializerInfo, Elements elements, Types types) {
        for (Element element : env.getElementsAnnotatedWith(Implementation.class)) {
            processImplementationAnnotation(element, initializerInfo, elements, types);
        }
    }

    private void processImplementationAnnotation(Element element, LoganSquareWrapperInitializerInfo initializerInfo, Elements elements, Types types) {
        TypeElement typeElement = (TypeElement) element;

        if (element.getModifiers().contains(PRIVATE)) {
            error(element, "%s: %s annotation can't be used on private classes.", typeElement.getQualifiedName(), getAnnotation().getSimpleName());
        }
        initializerInfo.putImplementation(typeElement, typeElement.getAnnotation(Implementation.class).value());


    }
}
