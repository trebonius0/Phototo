package com.trebonius.phototo.core.gps;

import com.trebonius.phototo.helpers.Tuple;
import org.junit.Assert;
import org.junit.Test;

public class GpsCoordinatesHelperTest {

    @Test
    public void test() {
        Tuple<Double, Double> coordinates = GpsCoordinatesHelper.getCoordinates("40.71381944 N, 74.00571667 W");
        Assert.assertEquals(40.71381944, coordinates.o1, 0.0001);
        Assert.assertEquals(-74.00571667, coordinates.o2, 0.0001);
    }

}
