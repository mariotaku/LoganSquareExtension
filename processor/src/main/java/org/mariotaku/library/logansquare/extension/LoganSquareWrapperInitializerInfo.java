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

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by mariotaku on 15/10/21.
 */
public class LoganSquareWrapperInitializerInfo {
    public final String name = getInitializerClassName();
    public boolean fileCreated;
    public HashMap<TypeElement, String> mappers = new HashMap<>();
    public HashMap<TypeElement, TypeMirror> implementations = new HashMap<>();
    public HashSet<TypeElement> enums = new HashSet<>();

    private static String getInitializerClassName() {
        final Class<LoganSquareWrapper> cls = LoganSquareWrapper.class;
        return cls.getPackage().getName() + "." + cls.getSimpleName() + "Initializer";
    }

    public HashSet<TypeElement> getEnums() {
        return enums;
    }

    public String getName() {
        return name;
    }

    public HashMap<TypeElement, TypeMirror> getImplementations() {
        return implementations;
    }

    public HashMap<TypeElement, String> getMappers() {
        return mappers;
    }

    public void putMapper(TypeElement type, String mapper) {
        mappers.put(type, mapper);
    }

    public void putImplementation(TypeElement type, TypeMirror impl) {
        implementations.put(type, impl);
    }

    public void putEnum(TypeElement type) {
        enums.add(type);
    }
}
