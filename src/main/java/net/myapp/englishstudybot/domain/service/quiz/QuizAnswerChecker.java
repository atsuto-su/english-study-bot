package net.myapp.englishstudybot.domain.service.quiz;

import org.springframework.stereotype.Service;

/**
 * QuizAnswerCheker is a class which plays a role of checking quiz answer.
 */
@Service
public class QuizAnswerChecker {
    
    /**
     * Checks if a user's answer is correct or not for the case of a selection quiz.
     * This method asserts that the answer is correct 
     * if a user's message is the same as the prefix of quiz answer, 
     * 
     * @param userMessage a user's quiz answer message
     * @param quizAnswer a quiz answer registered in user table after a quiz is given
     * @return check result, true if correct and false if incorrect
     */
    public boolean checkSelectionQuizAnswer(String userMessage, String quizAnswer) {

        if (quizAnswer.startsWith(userMessage)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if a user's answer is correct or not for the case of a description quiz.
     * This method asserts that the answer is correct
     * if the message is completely the same as the quiz answer.
     * 
     * NOTE:
     * this method is for the future use since a description quiz is not allowed currently.
     *  
     * @param userMessage a user's quiz answer message
     * @param quizAnswer a quiz answer registered in user table after a quiz is given
     * @return check result, true if correct and false if incorrect
     */
    public boolean checkDescriptionQuizAnswer(String userMessage, String quizAnswer) {
        // TO BE IMPROVED in the future.
        if (quizAnswer.equals(userMessage)) {
            return true;
        } else {
            return false;
        }
    }
}