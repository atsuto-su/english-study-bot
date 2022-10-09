package net.myapp.englishstudybot.domain.util;

import java.util.Random;

import org.springframework.stereotype.Component;

/**
 * RandomNumberGenerator is a class which generates random number.
 * This class is independently prepared to make testing easy for classes which use random values.
 */
@Component
public class RandomNumberGenerator {

    // ROOM FOR IMPROVEMENT //
    // In Java SE 17, an interface RandomGenerator is prepared
    // and might to be better than Random class (needs more detailed investigation)
    static private Random rnd = new Random();

    /**
     * Generates integer random value which is greater than or equal to 0 
     * and less than a specified upper bound.
     * 
     * @param upperBound an upper bound of random value range
     * @return an integer random number generated
     */
    public int generateIntRandomNumber(int upperBound) {
        return rnd.nextInt(upperBound);
    }
    
}
