package net.myapp.englishstudybot.domain.service.quiz.state;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.myapp.englishstudybot.domain.model.QuizStateName;
import net.myapp.englishstudybot.domain.model.UserEntity;
import net.myapp.englishstudybot.domain.repository.UserRepository;
import net.myapp.englishstudybot.domain.service.bot.BotMessageGenerator;
import net.myapp.englishstudybot.domain.service.bot.LineBotAgent;
import net.myapp.englishstudybot.domain.service.quiz.QuizBotContext;

/**
 * QuizBotWaitingStartState is an implementation of QuizBotState interface.
 */
@Slf4j
@Service
public class QuizBotWaitingStartState implements QuizBotState {

    private static final QuizStateName STATE_NAME = QuizStateName.WAITING_START;

    private final MessageSource messageSource;
    private final UserRepository userRepository;
    private final BotMessageGenerator botMessageGenerator;
    
    @Autowired
    QuizBotWaitingStartState(
        MessageSource messageSource,
        UserRepository userRepository,
        BotMessageGenerator botMessageGenerator
    ) {
        this.messageSource = messageSource;
        this.userRepository = userRepository;
        this.botMessageGenerator = botMessageGenerator;
    }

    /**
     * Gets the state name of this class.
     */
    @Override
    public QuizStateName getStateName() {
        return STATE_NAME;
    }

    /**
     * Changes the state by the following rule:
     *  - if a user's LINE message is the same as the predefined quiz start request message, 
     *    then changes it to the next state, WAITING_TYPE_SELECT.
     *  - otherwise, the state remains to the same.
     */
    @Override
    public void changeState(QuizBotContext quizBotContext, String userMessage) {
        log.info("START: QuizBotWaitingStartState#changeState");

        String goNextStateMessage 
         = messageSource.getMessage("user.quizStartMessage", null , Locale.JAPAN);
        if (userMessage.equals(goNextStateMessage)) {
            quizBotContext.setState(QuizStateName.WAITING_TYPE_SELECT);
            
        } else {
            quizBotContext.setState(QuizStateName.WAITING_START);
        }

        log.info("END: QuizBotWaitingStartState#changeState");
    }

    /**
     * Performs the following entry actions.
     * - sending a message to indicate how a user can start a quiz.
     * - if the sending succeeded, updates a user's quiz state to this.
     */
    @Override
    public LineBotAgent entryAction(UserEntity user, LineBotAgent lineBotAgent) {
        log.info("START: QuizBotWaitingStartState#entryAction");

        boolean isSendingSucceeded;
        if (lineBotAgent.getReplyToken() != null) {
            isSendingSucceeded = sendQuizStartMessage(lineBotAgent);
            lineBotAgent.setReplyTokenNullAfterReply();
        } else {
            // if reply token is null after sending a message in other states,
            // this state cannot send another message and hence skip it.
            isSendingSucceeded = true;
            log.info("Skipped sending start message");
        }

        if (isSendingSucceeded) {
            updateUserStatusToThis(user);
        } else {
            // if sending LINE reply message failed, throws an exception.
            String errorMessage
            = messageSource.getMessage(
                "error.replyMessageFailed.waitingStart", null, Locale.JAPAN
            );
            throw new RuntimeException(errorMessage); 
        }

        log.info("END: QuizBotWaitingStartState#entryAction");
        return lineBotAgent;
    }

    /**
     * Sends a message to indicate how a user can start a quiz.
     * 
     * @param lineBotAgent an instance which provides LINE Bot messaging functionalities
     * @return the result of the sending
     */
    private boolean sendQuizStartMessage(LineBotAgent lineBotAgent) {
        return lineBotAgent.replyMessage(botMessageGenerator.getQuizStartMessage());
    }

    /**
     * Updates the quiz_status in the user table to this class state.
     * 
     * @param user an entity of a user to be updated
     */
    private void updateUserStatusToThis(UserEntity user) {
        userRepository.updateUserStatus(user.getId(), getStateName());
    }

    /**
     * Does nothing because this state does not transition automatically.
     */
    @Override
    public void goNextAutomatically(QuizBotContext quizBotContext, UserEntity user, LineBotAgent lineBotAgent) {
        // do nothing
    }

}
