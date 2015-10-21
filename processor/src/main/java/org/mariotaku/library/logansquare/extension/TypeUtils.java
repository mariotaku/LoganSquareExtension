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

import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

public class TypeUtils {

    public static String getSimpleClassName(TypeElement type, String packageName) {
        return type.getQualifiedName().toString().substring(packageName.length() + 1).replace('.', '$');
    }

    @SuppressWarnings("unchecked")
    public static TypeMirror getTypeFromCollection(TypeMirror typeMirror) {
        if (!(typeMirror instanceof DeclaredType)) {
            return null;
        }

        DeclaredType declaredType = (DeclaredType) typeMirror;
        List<TypeMirror> genericTypes = (List<TypeMirror>) declaredType.getTypeArguments();

        if (genericTypes.size() > 0) {
            String genericClassName = declaredType.toString().substring(0, declaredType.toString().indexOf('<'));

            switch (genericClassName) {
                case "java.util.List":
                case "java.util.ArrayList":
                case "java.util.LinkedList":
                case "java.util.Set":
                case "java.util.HashSet":
                case "java.util.Deque":
                case "java.util.Queue":
                case "java.util.ArrayDeque":
                    return genericTypes.get(0);
                case "java.util.Map":
                case "java.util.HashMap":
                case "java.util.TreeMap":
                case "java.util.LinkedHashMap":
                    if (!"java.lang.String".equals(genericTypes.get(0).toString())) {
                        throw new IllegalStateException("JsonField Map collections must use Strings as keys");
                    }
                    return genericTypes.get(1);
            }
        }

        return null;
    }
}