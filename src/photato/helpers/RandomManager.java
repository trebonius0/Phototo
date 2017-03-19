package photato.helpers;

import java.util.Random;

public class RandomManager {

    private static final Random random = new Random(System.currentTimeMillis());

    public static synchronized int nextInt() {
        return random.nextInt();
    }

    public static synchronized int nextInt(int sup) {
        return random.nextInt(sup);
    }

    public static synchronized int nextInt(int inf, int sup) {
        return random.nextInt(sup - inf) + inf;
    }

    public static void randomSleep(int inf, int sup) {
        try {
            Thread.sleep(nextInt(inf, sup));
        } catch (InterruptedException ex) {
        }
    }
}
