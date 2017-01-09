package com.trebonius.phototo.helpers;

import java.util.Objects;

public class Tuple<T, U> {

    public T o1;
    public U o2;

    public Tuple(T o1, U o2) {
        this.o1 = o1;
        this.o2 = o2;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.o1);
        hash = 71 * hash + Objects.hashCode(this.o2);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Tuple<T, U> other = (Tuple<T, U>) obj;
        if (!Objects.equals(this.o1, other.o1)) {
            return false;
        }
        if (!Objects.equals(this.o2, other.o2)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "(" + o1 + ", " + o2 + ")";
    }
}
