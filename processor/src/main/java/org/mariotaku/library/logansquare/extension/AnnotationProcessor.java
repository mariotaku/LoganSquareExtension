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


import org.mariotaku.library.logansquare.extension.processor.Processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static javax.tools.Diagnostic.Kind.ERROR;


public class AnnotationProcessor extends AbstractProcessor {
    private Elements mElementUtils;
    private Types mTypeUtils;
    private Filer mFiler;
    private List<Processor> mProcessors;
    private LoganSquareWrapperInitializerInfo mInitializerInfo;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        mElementUtils = env.getElementUtils();
        mTypeUtils = env.getTypeUtils();
        mFiler = env.getFiler();
        mProcessors = Processor.allProcessors(processingEnv);
        mInitializerInfo = new LoganSquareWrapperInitializerInfo();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportTypes = new LinkedHashSet<>();
        for (Processor processor : mProcessors) {
            supportTypes.add(processor.getAnnotation().getCanonicalName());
        }
        return supportTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> elementsSet, RoundEnvironment env) {
        try {
            for (Processor processor : mProcessors) {
                processor.findAndParseObjects(env, mInitializerInfo, mElementUtils, mTypeUtils);
            }

            LoganSquareWrapperInitializerInfo initializerInfo = mInitializerInfo;

            if (!initializerInfo.fileCreated) {
                initializerInfo.fileCreated = true;
                try {
                    JavaFileObject jfo = mFiler.createSourceFile(initializerInfo.name);
                    Writer writer = jfo.openWriter();
                    writer.write(new ObjectMapperInjector(initializerInfo).getJavaClassFile(mTypeUtils));
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    error(mInitializerInfo.name, "Exception occurred while attempting to write injector for type %s. Exception message: %s", mInitializerInfo.name, e.getMessage());
                }
            }

            return true;
        } catch (Throwable e) {
            StringWriter stackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTrace));
            error("Exception while processing Json classes. Stack trace incoming:\n%s", stackTrace.toString());
            return false;
        }
    }

    private void error(String message, Object... args) {
        processingEnv.getMessager().printMessage(ERROR, String.format(message, args));
    }
}
