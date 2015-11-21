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

import com.bluelinelabs.logansquare.LoganSquare;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by mariotaku on 15/10/21.
 */
public class LoganSquareWrapperInitializerInfo {
    private final ClassName extensionName, initializerName;
    public boolean extensionFileCreated;
    public boolean initializerFileCreated;
    private HashMap<TypeElement, TypeName> mappers = new HashMap<>();
    private HashMap<TypeElement, TypeName> wrappers = new HashMap<>();
    private HashMap<TypeElement, TypeMirror> implementations = new HashMap<>();
    private HashSet<TypeElement> enums = new HashSet<>();

    LoganSquareWrapperInitializerInfo(String suffix) {
        final String packageName = LoganSquare.class.getPackage().getName();
        final String extBaseName = "JsonMapperLoaderExtensionImpl";
        final String initBaseName = LoganSquareWrapperInitializer.class.getSimpleName() + "Impl";
        if (suffix != null) {
            this.extensionName = ClassName.get(packageName, extBaseName + suffix);
            this.initializerName = ClassName.get(packageName, initBaseName + suffix);
        } else {
            this.extensionName = ClassName.get(packageName, extBaseName);
            this.initializerName = ClassName.get(packageName, initBaseName);
        }
    }

    public HashSet<TypeElement> getEnums() {
        return enums;
    }

    public ClassName getInitializerName() {
        return initializerName;
    }

    public ClassName getExtensionName() {
        return extensionName;
    }

    public HashMap<TypeElement, TypeName> getWrappers() {
        return wrappers;
    }

    public HashMap<TypeElement, TypeMirror> getImplementations() {
        return implementations;
    }

    public HashMap<TypeElement, TypeName> getMappers() {
        return mappers;
    }

    public void putMapper(TypeElement type, TypeName mapper) {
        mappers.put(type, mapper);
    }

    public void putImplementation(TypeElement type, TypeMirror impl) {
        implementations.put(type, impl);
    }

    public void putEnum(TypeElement type) {
        enums.add(type);
    }

    public void putWrapper(TypeElement type, TypeName wrapper) {
        wrappers.put(type, wrapper);
    }
}
