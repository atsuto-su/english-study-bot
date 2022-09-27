package net.myapp.englishstudybot.domain.service.quiz.state;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.myapp.englishstudybot.domain.model.QuizAggregationEntity;
import net.myapp.englishstudybot.domain.model.QuizStateName;
import net.myapp.englishstudybot.domain.model.UserEntity;
import net.myapp.englishstudybot.domain.model.quiz.QuizDto;
import net.myapp.englishstudybot.domain.model.quiz.UserQuizConfigDto;
import net.myapp.englishstudybot.domain.repository.QuizAggregationRepository;
import net.myapp.englishstudybot.domain.repository.UserRepository;
import net.myapp.englishstudybot.domain.service.bot.BotMessageGenerator;
import net.myapp.englishstudybot.domain.service.bot.LineBotAgent;
import net.myapp.englishstudybot.domain.service.quiz.QuizBotContext;
import net.myapp.englishstudybot.domain.service.quiz.QuizGenerator;

/**
 * QuizBotWaitingAnswerState is an implementation of QuizBotState interface.
 */
@Slf4j
@Service
public class QuizBotWaitingAnswerState implements QuizBotState {

    private static final QuizStateName STATE_NAME = QuizStateName.WAITING_ANSWER;
    
    private final MessageSource messageSource;
    private final UserRepository userRepository;
    private final QuizAggregationRepository quizAggregationRepository;
    private final BotMessageGenerator botMessageGenerator;
    private final QuizGenerator quizGenerator;

    @Autowired
    private QuizBotWaitingAnswerState(
        MessageSource messageSource,
        UserRepository userRepository,
        QuizAggregationRepository quizAggregationRepository,
        BotMessageGenerator botMessageGenerator,
        QuizGenerator quizGenerataor
    ) {
        this.messageSource = messageSource;
        this.userRepository = userRepository;
        this.quizAggregationRepository = quizAggregationRepository;
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
     * Changes the state to the next state, CHECKING_ANSWER,
     * regardless of a user's LINE message contents.
     * This is because the bot cannot know if the user intended to answer the question 
     * or not from the message in this state, 
     * and hence any messages from the user is regarded as a quiz answer.
     */
    @Override
    public void changeState(QuizBotContext quizBotContext, String userMessage) {
        log.info("START: QuizBotWaitingAnswerState#changeState");
        quizBotContext.setState(QuizStateName.CHECKING_ANSWER);
        log.info("END: QuizBotWaitingAnswerState#changeState");
    }

    /**
     * Performs the following entry actions.
     * - generating a quiz and its message according to a quiz type which a user selected.
     * - sending the quiz message with quick reply messages for answering to the user.
     * - if the sending succeeded, update the tables as follows:
     *    users table: quiz_status and the generated quiz data in the user table.
     *    quiz_aggregations table: adds or updates the quiz aggregation data.
     */
    @Override
    public LineBotAgent entryAction(UserEntity user, LineBotAgent lineBotAgent) {
        log.info("START: QuizBotWaitingAnswerState#entryAction");

        QuizDto quizDto = generateQuizDto(user, lineBotAgent.getUserMessage());
        boolean isReplySucceeded = sendQuizMessage(lineBotAgent, quizDto);
        lineBotAgent.setReplyTokenNullAfterReply();
        if (isReplySucceeded) {
            user = updateUserStatusAndLastQuizInfo(user, quizDto);
            updateGivenQuizAggregation(user, quizDto);
        } else {
            // if sending LINE reply message failed, throws an exception.
            String errorMessage
             = messageSource.getMessage(
                "error.replyMessageFailed.waitingAnswer", null, Locale.JAPAN
            );
            throw new RuntimeException(errorMessage); 
        }

        log.info("END: QuizBotWaitingAnswerState#entryAction");
        return lineBotAgent;
    }

    /**
     * Generates a quiz dto with all necessary fields being set
     * by using quizGenerator methods and botMessagegenerator methods.
     * 
     * @param user an entity of a user who is communicating with this bot
     * @param userMessage a user message sent to this bot.
     * @return a quiz dto with all necessary fields being set
     */
    private QuizDto generateQuizDto(UserEntity user, String userMessage) {
        UserQuizConfigDto userQuizConfigDto
         = new UserQuizConfigDto(
            user.getId(), 
            user.getIsSelfWordOnly(), 
            user.getIsExampleQuiz(), 
            user.getIsJpQuestionQuiz(), 
            user.getIsDescriptionQuiz(), 
            userMessage);
        QuizDto quizDto = quizGenerator.generateQuiz(userQuizConfigDto);
        quizDto = botMessageGenerator.generateQuizAndAnswerMessages(quizDto, userQuizConfigDto);
        return quizDto;
    }

    /**
     * Sends a quiz message.
     * 
     * @param lineBotAgent an instance which provides LINE Bot messaging functionalities
     * @param quizDto a quiz dto which contains a generated quiz mesasge and answer candidates
     * @return the result of the sending
     */
    private boolean sendQuizMessage(LineBotAgent lineBotAgent, QuizDto quizDto) {
        String quizMessage = quizDto.getQuizMessage();
        List<String> quickReplyItems
         = botMessageGenerator
           .generateSelectionQuizPrefixes(quizDto.getAnswerCandidates().size()+1);
        return lineBotAgent.replyMessageWithQuickReply(quizMessage, quickReplyItems);
    }

    /**
     * Updates the quiz_status in the user table to this class state.
     * 
     * @param user an entity of a user to be updated
     * @param quizDto a quiz dto which contains a quiz data given to a user
     * @return a user entity with updated data in this method
     */
    private UserEntity updateUserStatusAndLastQuizInfo(UserEntity user, QuizDto quizDto) {
        user = userRepository.updateUserStatus(user.getId(), getStateName());

        user.setLastVocabulariesId(quizDto.getTargetVocabId());
        user.setLastQuizSentence(quizDto.getQuizWord());
        user.setLastQuizAnswer(quizDto.getAnswerMessage());
        return userRepository.updateLastQuizInfo(user);
    }

    /**
     * Updates the quiz_aggragations table when a quiz is given to a user.
     * 
     * @param user an entity of a user who is communicating with the bot
     * @param quizDto 
     */
    private void updateGivenQuizAggregation(UserEntity user, QuizDto quizDto) {
        Boolean isJpQuestionQuiz = user.getIsJpQuestionQuiz();
        QuizAggregationEntity record
         = quizAggregationRepository.findById(user.getLastVocabulariesId(), user.getId());
        if (record == null) {
            //if the quiz aggregation is not found, adds a new record.
            LocalDateTime lastQuestionDatetimeEn;
            LocalDateTime lastQuestionDatetimeJp;
            if (isJpQuestionQuiz) {
                lastQuestionDatetimeEn = null; 
                lastQuestionDatetimeJp = LocalDateTime.now();
            } else {
                lastQuestionDatetimeEn = LocalDateTime.now();
                lastQuestionDatetimeJp = null;
            }
            QuizAggregationEntity quizAggregation
             = new QuizAggregationEntity(
                user.getLastVocabulariesId(), 
                user.getId(), 
                0, 
                0, 
                lastQuestionDatetimeEn, 
                lastQuestionDatetimeJp, 
                0, 
                0, 
                null,
                null, 
                false, 
                null, 
                null
            );
            quizAggregationRepository.add(quizAggregation);
        } else {
            //if the quiz aggregation is found, updates the existing record.
            quizAggregationRepository.updateGivenQuiz(
                user.getLastVocabulariesId(), 
                user.getId(), 
                isJpQuestionQuiz
            );
        }
    }

    /**
     * Does nothing because this state does not transition automatically.
     */
    @Override
    public void goNextAutomatically(QuizBotContext quizBotContext, UserEntity user, LineBotAgent lineBotAgent) {
        // do nothing
    }


}
