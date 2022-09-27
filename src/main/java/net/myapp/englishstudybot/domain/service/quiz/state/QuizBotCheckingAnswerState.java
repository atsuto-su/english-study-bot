package net.myapp.englishstudybot.domain.service.quiz.state;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.myapp.englishstudybot.domain.model.QuizStateName;
import net.myapp.englishstudybot.domain.model.UserEntity;
import net.myapp.englishstudybot.domain.repository.QuizAggregationRepository;
import net.myapp.englishstudybot.domain.repository.UserRepository;
import net.myapp.englishstudybot.domain.service.bot.BotMessageGenerator;
import net.myapp.englishstudybot.domain.service.bot.LineBotAgent;
import net.myapp.englishstudybot.domain.service.quiz.QuizAnswerChecker;
import net.myapp.englishstudybot.domain.service.quiz.QuizBotContext;

/**
 * QuizBotCheckingAnswerState is an implementation of QuizBotState interface.
 */
@Slf4j
@Service
public class QuizBotCheckingAnswerState implements QuizBotState {
    
    private static final QuizStateName STATE_NAME = QuizStateName.CHECKING_ANSWER;
    
    private final MessageSource messageSource;
    private final UserRepository userRepository;
    private final QuizAggregationRepository quizAggregationRepository;
    private final BotMessageGenerator botMessageGenerator;
    private final QuizAnswerChecker quizAnswerChecker;

    @Autowired
    private QuizBotCheckingAnswerState(
        MessageSource messageSource,
        UserRepository userRepository,
        QuizAggregationRepository quizAggregationRepository,
        BotMessageGenerator botMessageGenerator,
        QuizAnswerChecker quizAnswerChecker
    ) {
        this.messageSource = messageSource;
        this.userRepository = userRepository;
        this.quizAggregationRepository = quizAggregationRepository;
        this.botMessageGenerator = botMessageGenerator;
        this.quizAnswerChecker = quizAnswerChecker;
    }

    @Override
    public QuizStateName getStateName() {
        return STATE_NAME;
    }

    /**
     * Changes the state to the next state, WAITING_START,
     * regardless of a user's LINE message contents.
     * This is because the trigger of state change from this is not a user's message,
     * but entry action completion of this state.
     */
    @Override
    public void changeState(QuizBotContext quizBotContext, String userMessage) {
        log.info("START: QuizBotCheckingAnswerState#changeState");
        quizBotContext.setState(QuizStateName.WAITING_START);
        log.info("END: QuizBotCheckingAnswerState#changeState");
    }

    /**
     * Performs the following entry actions.
     * - checking if the user's answer is correct or not.
     * - sending a message to let the user know the checking result.
     * - if the sending succeeded, update the quiz_status in the user table.
     */
    @Override
    public LineBotAgent entryAction(UserEntity user, LineBotAgent lineBotAgent) {
        log.info("START: QuizBotCheckingAnswerState#entryAction");

        boolean isAnswerCorrect = checkUserAnswer(user, lineBotAgent.getUserMessage());
        boolean isReplySucceeded
         = sendCheckResultMessage(lineBotAgent, isAnswerCorrect, user.getLastQuizAnswer());
        lineBotAgent.setReplyTokenNullAfterReply();
        if (isReplySucceeded) {
            updateUserStatusToThis(user);
            updateQuizResult(user, isAnswerCorrect);
        } else {
            // if sending LINE reply message failed, throws an exception.
            String errorMessage
             = messageSource.getMessage(
                "error.replyMessageFailed.checkingAnswer", null, Locale.JAPAN
            );
            throw new RuntimeException(errorMessage); 
        }

        log.info("END: QuizBotCheckingAnswerState#entryAction");
        return lineBotAgent;
    }

    /**
     * Checks the user's answer
     * 
     * @param user an entity of a user who is communicating with this bot
     * @param userMessage a user's message sent to this bot
     * @return the checking result
     */
    private boolean checkUserAnswer(UserEntity user, String userMessage) {
        boolean isAnswerCorrect;
        if (user.getIsDescriptionQuiz()) {
            isAnswerCorrect
             = quizAnswerChecker.checkDescriptionQuizAnswer(
                userMessage,
                user.getLastQuizAnswer()
                );
         } else {
            isAnswerCorrect
             = quizAnswerChecker.checkSelectionQuizAnswer(
                userMessage,
                user.getLastQuizAnswer()
                );
        }
        return isAnswerCorrect;
    }

    /**
     * Sends a message to indicate a user's quiz answer result.
     * 
     * @param lineBotAgent an instance which provides LINE Bot messaging functionalities
     * @param isAnswerCorrect a flag of whether a user's answer is correct or not
     * @param quizAnswer a quiz answer
     * @return the result of the sending
     */
    private boolean sendCheckResultMessage(
        LineBotAgent lineBotAgent, 
        boolean isAnswerCorrect,
        String quizAnswer
    ) {
        String message;
        if (isAnswerCorrect) {
            message = botMessageGenerator.getCorrectMessage();
        } else {
            message = botMessageGenerator.generateIncorrectMessage(quizAnswer);
        }
        String initialMessage = botMessageGenerator.getQuizStartMessage();
        List<String> messages = Arrays.asList(message, initialMessage);

        return lineBotAgent.replyMultiMessages(messages);
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
     * Updates quiz_aggreagtions table record according to the quiz answer result.
     * 
     * @param user an entity of a user who is communicating with this bot
     * @param isAnswerCorrect a flag of whether a user's answer is correct or not
     */
    private void updateQuizResult(UserEntity user, boolean isAnswerCorrect) {
        if (isAnswerCorrect) {
            quizAggregationRepository.updateCorrectCase(
                user.getLastVocabulariesId(), 
                user.getId(), 
                user.getIsJpQuestionQuiz() 
            );
        } else {
            quizAggregationRepository.updateIncorrectCase(
                user.getLastVocabulariesId(), 
                user.getId(), 
                user.getIsJpQuestionQuiz() 
            );
        }
    }

    /**
     * Automatically changes the state to the next by invoking a method of 
     * the state context class.
     */
    @Override
    public void goNextAutomatically(QuizBotContext quizBotContext, UserEntity user, LineBotAgent lineBotAgent) {
        quizBotContext.triggerStateMove(user, lineBotAgent);
    }

}
