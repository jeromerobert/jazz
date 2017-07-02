/**
 * Copyright 2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package edu.umd.cs.jazztest.animationtest;

import junit.framework.*;
import edu.umd.cs.jazz.animation.*;

public class ZAlphaTest extends TestCase {

    public ZAlphaTest(String name) {
        super(name);
    }

    public void setUp() {
    }

    public void testAlphaDecreasing() {
        long triggerTime = 3430;// + ZAlpha.ALPHA_START_TIME;
        long phaseDelay = 12;
        long offset = triggerTime + phaseDelay;
        long alphaAtZero = 500;
        long waveLenght = 1000 + alphaAtZero;
        long decreasingDuration = 1000;
        int loopCount = 2;

        ZAlpha alpha = new ZAlpha();

        alpha.setMode(ZAlpha.ALPHA_DECREASING);
        alpha.setDecreasingAlphaDuration(decreasingDuration);
        alpha.setLoopCount(loopCount);
        alpha.setPhaseDelayDuration(phaseDelay);
        alpha.setTriggerTime(triggerTime);
        alpha.setAlphaAtZeroDuration(alphaAtZero);
        alpha.setDecreasingAlphaRampDuration(500);

        assert(alpha.value(offset + 0) == 1);
        assert(alpha.value(offset + 1) < 1);
        assert(alpha.value(offset + 500) == 0.5);
        assert(alpha.value(offset + 999) > 0);
        assert(alpha.value(offset + 1000) == 0);
        assert(alpha.value(offset + 1499) == 0);

        assert(alpha.value(offset + 1500) == 1);
        assert(alpha.value(offset + 1501) < 1);
        assert(alpha.value(offset + 2000) == 0.5);
        assert(alpha.value(offset + 2499) > 0);
        assert(alpha.value(offset + 2500) == 0);
        assert(alpha.value(offset + 2999) == 0);

        assert(alpha.getStopTime() == triggerTime + phaseDelay + 3000);
        assert(alpha.value(alpha.getTriggerTime() + phaseDelay + 3000) == 0);
        assert(alpha.value(alpha.getTriggerTime() + phaseDelay + 3000 + 1) == 0);
    }

    public void testAlphaIncreasing() {
        long triggerTime = 3430;;// + ZAlpha.ALPHA_START_TIME;
        long phaseDelay = 12;
        long offset = triggerTime + phaseDelay;
        long alphaAtOne = 500;
        long waveLenght = 1000 + alphaAtOne;
        long increasingDuration = 1000;
        int loopCount = 2;

        ZAlpha alpha = new ZAlpha(loopCount, increasingDuration);

        alpha.setPhaseDelayDuration(phaseDelay);
        alpha.setTriggerTime(triggerTime);
        alpha.setAlphaAtOneDuration(alphaAtOne);
        alpha.setIncreasingAlphaRampDuration(500);

        assert(alpha.value(offset + 0) == 0);
        assert(alpha.value(offset + 1) > 0);
        assert(alpha.value(offset + 500) == 0.5);
        assert(alpha.value(offset + 999) < 1);
        assert(alpha.value(offset + 1000) == 1);
        assert(alpha.value(offset + 1499) == 1);

        assert(alpha.value(offset + 1500) == 0);
        assert(alpha.value(offset + 1501) > 0);
        assert(alpha.value(offset + 2000) == 0.5);
        assert(alpha.value(offset + 2499) < 1);
        assert(alpha.value(offset + 2500) == 1);
        assert(alpha.value(offset + 2999) == 1);

        assert(alpha.getStopTime() == triggerTime + phaseDelay + 3000);
        assert(alpha.value(alpha.getTriggerTime() + phaseDelay + 3000) == 1);
        assert(alpha.value(alpha.getTriggerTime() + phaseDelay + 3000 + 1) == 1);
    }

    public void testAlphaIncreasingAndDecreasing() {
        long triggerTime = 3430;// + ZAlpha.ALPHA_START_TIME;
        long phaseDelay = 12;
        long offset = triggerTime + phaseDelay;
        long alphaAtZero = 500;
        long alphaAtOne = 500;
        long decreasingDuration = 1000;
        long increasingDuration = 1000;
        long increasingwaveLenght = increasingDuration + alphaAtOne;
        long decreasingwaveLenght = decreasingDuration + alphaAtZero;
        int loopCount = 2;

        ZAlpha alpha = new ZAlpha();

        alpha.setMode(ZAlpha.ALPHA_INCREASING_AND_DECREASING);
        alpha.setDecreasingAlphaDuration(decreasingDuration);
        alpha.setIncreasingAlphaDuration(increasingDuration);
        alpha.setLoopCount(loopCount);
        alpha.setPhaseDelayDuration(phaseDelay);
        alpha.setTriggerTime(triggerTime);
        alpha.setAlphaAtOneDuration(alphaAtOne);
        alpha.setAlphaAtZeroDuration(alphaAtZero);
        alpha.setIncreasingAlphaRampDuration(500);
        alpha.setDecreasingAlphaRampDuration(500);

        assert(alpha.value(offset + 0) == 0);
        assert(alpha.value(offset + 1) > 0);
        assert(alpha.value(offset + 500) == 0.5);
        assert(alpha.value(offset + 999) < 1);
        assert(alpha.value(offset + 1000) == 1);
        assert(alpha.value(offset + 1499) == 1);

        assert(alpha.value(offset + 1500) == 1);
        assert(alpha.value(offset + 1501) < 1);
        assert(alpha.value(offset + 2000) == 0.5);
        assert(alpha.value(offset + 2499) > 0);
        assert(alpha.value(offset + 2500) == 0);
        assert(alpha.value(offset + 2999) == 0);

        // loop 2
        assert(alpha.value(offset + 3000) == 0);
        assert(alpha.value(offset + 3001) > 0);
        assert(alpha.value(offset + 3500) == 0.5);
        assert(alpha.value(offset + 3999) < 1);
        assert(alpha.value(offset + 4000) == 1);
        assert(alpha.value(offset + 4499) == 1);

        assert(alpha.value(offset + 4500) == 1);
        assert(alpha.value(offset + 4501) < 1);
        assert(alpha.value(offset + 5000) == 0.5);
        assert(alpha.value(offset + 5499) > 0);
        assert(alpha.value(offset + 5500) == 0);
        assert(alpha.value(offset + 5999) == 0);

        assert(alpha.getStopTime() == triggerTime + phaseDelay + (loopCount *(increasingwaveLenght + decreasingwaveLenght)));
        assert(alpha.value(alpha.getTriggerTime() + phaseDelay + (loopCount *(increasingwaveLenght + decreasingwaveLenght))) == 0);
        assert(alpha.value(alpha.getTriggerTime() + phaseDelay + (loopCount *(increasingwaveLenght + decreasingwaveLenght)) + 1) == 0);
    }

    public void testAlphaIncreasingRamp() {
        //ZAlpha.ALPHA_START_TIME = 0;

        ZAlpha alpha1 = new ZAlpha(1, 11);
        ZAlpha alpha2 = new ZAlpha(1, 11);

        alpha1.setTriggerTime(0);
        alpha2.setTriggerTime(0);

        alpha1.setIncreasingAlphaRampDuration(5);
        alpha2.setIncreasingAlphaRampDuration(4);

        assert(alpha1.value(0) == alpha2.value(0));

        assert(alpha1.value(1) < alpha2.value(1));
        assert(alpha1.value(2) < alpha2.value(2));
        assert(alpha1.value(3) < alpha2.value(3));
        assert(alpha1.value(4) < alpha2.value(4));
        assert(alpha1.value(5) < alpha2.value(5));

        assert(alpha1.value(6) > alpha2.value(6));
        assert(alpha1.value(7) > alpha2.value(7));
        assert(alpha1.value(8) > alpha2.value(8));
        assert(alpha1.value(9) > alpha2.value(9));
        assert(alpha1.value(10) > alpha2.value(10));

        assert(alpha1.value(11) == alpha2.value(11));
    }

    public void testAlphaOutput() {
        /*
        ZAlpha alpha = new ZAlpha(1, 10);
        alpha.setIncreasingAlphaRampDuration(5);
        alpha.setStartTime(0);

        for (int i = 0; i < 11; i++) {
            System.out.println(alpha.value(i));
        }
        */
    }
}