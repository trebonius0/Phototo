package photato.helpers;

import java.lang.reflect.Array;

public class ArrayHelper {

    public static <T> T[] concatenate(T[] a, T[] b) {
        if (a == null) {
            if (b == null) {
                return null;
            } else {
                return b;
            }
        } else {
            if (b == null) {
                return a;
            } else {
                int aLen = a.length;
                int bLen = b.length;

                @SuppressWarnings("unchecked")
                T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
                System.arraycopy(a, 0, c, 0, aLen);
                System.arraycopy(b, 0, c, aLen, bLen);

                return c;
            }
        }

    }
}
