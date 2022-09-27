package net.myapp.englishstudybot.domain.service.quiz.state;

import java.util.List;
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
import net.myapp.englishstudybot.domain.service.quiz.QuizGenerator;

/**
 * QuizBotWaitingTypeSelectState is an implementation of QuizBotState interface.
 */
@Slf4j
@Service
public class QuizBotWaitingTypeSelectState implements QuizBotState {

    private static final QuizStateName STATE_NAME = QuizStateName.WAITING_TYPE_SELECT;

    private final MessageSource messageSource;
    private final UserRepository userRepository;
    private final BotMessageGenerator botMessageGenerator;
    private final QuizGenerator quizGenerator;
    
    @Autowired
    QuizBotWaitingTypeSelectState(
        MessageSource messageSource,
        UserRepository userRepository,
        BotMessageGenerator botMessageGenerator,
        QuizGenerator quizGenerataor
    ) {
        this.messageSource = messageSource;
        this.userRepository = userRepository;
        this.botMessageGenerator = botMessageGenerator;
        this.quizGenerator = quizGenerataor;
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
     *  - if a user's LINE message is the same as any of the predefined quiz types string,
     *    then changes it to the next state, WAITING_ANSWER.
     *  - if the message is the same as the predefined quiz cancel request message,
     *    then changes it to the initial state, WAITING_START.
     *  - otherwise, the state remains to the same.
     */
    @Override
    public void changeState(QuizBotContext quizBotContext, String userMessage) {
        log.info("START: QuizBotWaitingTypeSelectState#changeState");

        List<String> goNextStateMessages = quizGenerator.getSelectableQuizTypes();
        String goInitialStateMessage 
         = messageSource.getMessage("user.quizCancelMessage", null , Locale.JAPAN);
        if (goNextStateMessages.contains(userMessage)) {
            quizBotContext.setState(QuizStateName.WAITING_ANSWER);
        } else if (userMessage.equals(goInitialStateMessage)) {
            quizBotContext.setState(QuizStateName.WAITING_START);
        } else {
            quizBotContext.setState(QuizStateName.WAITING_TYPE_SELECT);
        }

        log.info("END: QuizBotWaitingTypeSelectState#changeState");
    }

    /**
     * Performs the following entry actions.
     * - sending a message with quick reply messages to indicate selectable quiz types.
     * - if the sending succeeded, update the quiz_status in the user table.
     */
    @Override
    public LineBotAgent entryAction(UserEntity user, LineBotAgent lineBotAgent) {
        log.info("START: QuizBotWaitingTypeSelectState#entryAction");

        boolean isSendingSucceeded = sendSelectableQuizTypesMessage(lineBotAgent);
        lineBotAgent.setReplyTokenNullAfterReply();
        if (isSendingSucceeded) {
            updateUserStatusToThis(user);
        } else {
            // if sending LINE reply message failed, throws an exception.
            String errorMessage
             = messageSource.getMessage(
                "error.replyMessageFailed.waitingTypeSelect", null, Locale.JAPAN
            );
            throw new RuntimeException(errorMessage); 
        }

        log.info("END: QuizBotWaitingTypeSelectState#entryAction");
        return lineBotAgent;
    }

    /**
     * Sending a message with quick reply messages.
     * How a user can select a quiz type is indicated by a message and
     * selectable quiz types are indicated by quick reply messages.
     * 
     * @param lineBotAgent an instance which provides LINE Bot messaging functionalities
     * @return the result of the sending
     */
    private boolean sendSelectableQuizTypesMessage(LineBotAgent lineBotAgent) {
        String message = botMessageGenerator.getTypeSelectMessage();
        List<String> quickReplyItems = quizGenerator.getSelectableQuizTypes();

        return lineBotAgent.replyMessageWithQuickReply(message, quickReplyItems);
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
        //do nothing
    }

}
