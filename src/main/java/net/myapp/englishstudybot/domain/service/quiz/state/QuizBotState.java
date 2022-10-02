package net.myapp.englishstudybot.domain.service.quiz.state;

import net.myapp.englishstudybot.domain.model.QuizStateName;
import net.myapp.englishstudybot.domain.model.UserEntity;
import net.myapp.englishstudybot.domain.service.bot.LineBotAgent;
import net.myapp.englishstudybot.domain.service.quiz.QuizBotContext;

/**
 * QuizBotState is an interface to be implemented by concrete classes 
 * which embodies each state of quiz bot.
 * This interface provides methods which define 
 * state transition conditions, entry actionsA of each state.
 */
public interface QuizBotState {

    /**
     * Gets the state name.
     * 
     * @return the state name
     */
    public QuizStateName getStateName();

    /**
     * Changes the state triggered by a user LINE message.
     * 
     * @param quizBotContext an instance of context class to manage the quiz bot state
     * @param userMessage a LINE message sent from a user to this bot
     */
    public void changeState(QuizBotContext quizBotContext, String userMessage);

    /**
     * Performs entry action when the state changes to the one defined in the class.
     * 
     * @param user an entity of the user who is communicating with the quiz bot.
     * @param lineBotAgent an LineBotAgent instance which contains information 
     * of a specific user who sent a message to this bot
     * @return a lineBotAgent instance with reply token being null after sending message
     */
    public LineBotAgent entryAction(UserEntity user, LineBotAgent lineBotAgent);

    /**
     * Goes to the next state automatically, without any external triggers.
     * 
     * @param quizBotContext an instance of context class to manage the quiz bot state
     * @param user an entity of the user who is communicating with the quiz bot.
     * @param lineBotAgent an LineBotAgent instance which contains information 
     * of a specific user who sent a message to this bot
     */
    public void goNextAutomatically(QuizBotContext quizBotContext, UserEntity user, LineBotAgent lineBotAgent);


}
