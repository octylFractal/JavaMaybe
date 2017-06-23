package com.techshroom.javamaybe;

import java.math.BigInteger;

import com.techshroom.javamaybe.compile.Any;

public class TestClass {

    private static final class Fork {

        public int getProngs() {
            return 1;
        }

    }

    private static final class Spoon {

        public int getCurvature() {
            return 1;
        }

    }

    public int sporkInTheRoad(Any target) {
        if (target.typeFork()) {
            return target.<Number> as().intValue();
        } else if (target.typeFork()) {
            return target.<Integer> as();
        }
        return target.typeFork() ? target.<Fork> as().getProngs() : target.<Spoon> as().getCurvature();
    }

    public void numberToIL(Any nTIL) {
        // initially cast as Number, later specialized as Integer and Long
        // Here, the end parameters should be Integer and Long
        Number number = nTIL.as();
        long someLong = nTIL.typeFork() ? nTIL.<Integer> as() : nTIL.<Long> as();
        System.err.println(number + " == " + someLong);
    }

    public void numberToL(Any nTL) {
        // initially number, end as long
        Number number = nTL.<Number> as();
        long someLong = nTL.<Long> as();
        System.err.println(number + " == " + someLong);
    }

    public void numberToNL(Any nTNL) {
        // initially number, re-cast as number and long
        Number number = nTNL.<Number> as();
        long someLong = nTNL.typeFork() ? nTNL.<Number> as().longValue() : nTNL.<Long> as();
        System.err.println(number + " == " + someLong);
    }

    public BigInteger largeMod(Any number, Any divisor) {
        BigInteger numberInt = number.typeFork() ? BigInteger.valueOf(number.<Number> as().longValue())
                : new BigInteger(number.<String> as());
        BigInteger divInt = divisor.typeFork() ? BigInteger.valueOf(divisor.<Number> as().longValue())
                : new BigInteger(divisor.<String> as());
        return numberInt.mod(divInt);
    }

}
