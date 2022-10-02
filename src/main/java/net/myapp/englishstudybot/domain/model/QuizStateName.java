package net.myapp.englishstudybot.domain.model;

import java.util.Arrays;

/**
 * Definition of quiz status enum
 */
public enum QuizStateName {
    WAITING_START(0),
    WAITING_TYPE_SELECT(10),
    WAITING_ANSWER(20),
    CHECKING_ANSWER(30);

    private final int code;

    private QuizStateName(int code) {
        this.code = code;
    }

    /**
     * Gets compatible code for a status.
     * 
     * @return code value
     */
    public int getCode() {
        return code;
    }


    /**
     * Converts status code into name.
     * 
     * @param code status code to be converted
     * @return status name defined in enum
     */
    public static QuizStateName nameOf(int code) {
        return Arrays.stream(QuizStateName.values())
                .filter(data -> data.getCode()==code)
                .findFirst()
                .orElse(null);
    }
    
}
