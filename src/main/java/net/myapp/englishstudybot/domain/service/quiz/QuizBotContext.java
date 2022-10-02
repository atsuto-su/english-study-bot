package net.myapp.englishstudybot.domain.service.quiz;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.myapp.englishstudybot.domain.model.QuizStateName;
import net.myapp.englishstudybot.domain.model.UserEntity;
import net.myapp.englishstudybot.domain.service.bot.LineBotAgent;
import net.myapp.englishstudybot.domain.service.quiz.state.QuizBotState;

/**
 * QuizBotContext is a class which manages quiz bot state.
 * 
 * NOTE:
 * The role of this class is to use each state class through an interface.
 * Therefore, concrete actions and transition conditions for each state should NOT be implemented in this class.
 */
@Service
public class QuizBotContext {

    private QuizBotState state;
    private final Map<QuizStateName, QuizBotState> stateBeans;

    // Injects all beans of QuizBotState concrete classes with the format of
    // Map<QuizStateName, QuizBotState>
    @Autowired
    QuizBotContext(List<QuizBotState> states) {
        stateBeans = states.stream().collect(
            Collectors.toMap(QuizBotState::getStateName, Function.identity())
        );
    }

    /**
     * Sets a state of the quiz bot.
     * 
     * @param stateName the name of the state to be set
     */
    public void setState(QuizStateName stateName) {
        this.state = stateBeans.get(stateName);
    }

    /**
     * Gets the current state of the quiz bot.
     * 
     * @return the state being set
     */
    public QuizBotState getState() {
        return state;
    }

    /**
     * Triggers a state transition of the bot
     * 
     * @param user a user entity who sent a message to this bot
     * @param lineBotAgent an LineBotAgent instance which contains information 
     * of a specific user who sent a message to this bot
     */
    public void triggerStateMove(UserEntity user, LineBotAgent lineBotAgent) {
        state.changeState(this, lineBotAgent.getUserMessage());
        lineBotAgent = state.entryAction(user, lineBotAgent);
        state.goNextAutomatically(this, user, lineBotAgent);
    }


    
}
