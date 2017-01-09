package com.trebonius.phototo.helpers;

import java.util.Collection;

public class StringHelper {

    public static String join(Collection<? extends Object> strs, String separator) {
        return join(strs.toArray(new Object[strs.size()]), separator);
    }

    public static String join(Object[] strs, String separator) {
        return join(strs, separator, false);
    }

    public static String join(Object[] strs, String separator, boolean joinEmptyStrings) {
        StringBuilder builder = new StringBuilder();

        for (Object o : strs) {
            String str = o.toString();
            if (str != null && (!str.isEmpty() || joinEmptyStrings)) {
                builder.append(str).append(separator);
            }
        }
        return builder.substring(0, Math.max(0, builder.length() - separator.length()));
    }

}
