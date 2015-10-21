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

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;

import static javax.tools.Diagnostic.Kind.ERROR;

public abstract class Processor {

    protected ProcessingEnvironment mProcessingEnv;

    protected Processor(ProcessingEnvironment processingEnv) {
        mProcessingEnv = processingEnv;
    }

    public abstract Class getAnnotation();

    public abstract void findAndParseObjects(RoundEnvironment env, LoganSquareWrapperInitializerInfo initializerInfo, Elements elements, Types types);

    public static List<Processor> allProcessors(ProcessingEnvironment processingEnvironment) {
        List<Processor> list = new ArrayList<>();
        list.add(new ImplementationClassProcessor(processingEnvironment));
        list.add(new MapperClassProcessor(processingEnvironment));
        list.add(new EnumClassProcessor(processingEnvironment));
        return list;
    }

    public void error(Element element, String message, Object... args) {
        mProcessingEnv.getMessager().printMessage(ERROR, String.format(message, args), element);
    }
}