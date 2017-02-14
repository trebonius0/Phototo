package photato.helpers;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collector;

public class CollectorsHelper {

    public static <T> Collector<T, ?, List<T>> lastN(int n) {
        return Collector.<T, Deque<T>, List<T>>of(ArrayDeque::new, (acc, t) -> {
            if (acc.size() == n) {
                acc.pollFirst();
            }
            acc.add(t);
        }, (acc1, acc2) -> {
            while (acc2.size() < n && !acc1.isEmpty()) {
                acc2.addFirst(acc1.pollLast());
            }
            return acc2;
        }, ArrayList<T>::new);
    }
}
