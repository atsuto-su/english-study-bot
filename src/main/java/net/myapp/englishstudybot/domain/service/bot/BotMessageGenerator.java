package net.myapp.englishstudybot.domain.service.bot;

import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;

import net.myapp.englishstudybot.domain.model.quiz.QuizDto;
import net.myapp.englishstudybot.domain.model.quiz.UserQuizConfigDto;

/**
 * BotMessageGenerator provides getter or generator methods of bot messages.
 * All messages sent by the bot should be generated in this class.
 */
@Component
public class BotMessageGenerator {

    private final MessageSource messageSource;

    @Autowired
    BotMessageGenerator(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Gets a welcome message to be sent to a new follower.
     * 
     * @return welcome message
     */
    public String getWelcomeMessage() {
        // TO BE IMPROVED IN THE FUTURE:
        // might be better to obtain bot name by using LINE API (https://developers.line.biz/ja/reference/messaging-api/#get-bot-info)
        MessageSourceResolvable botName = new DefaultMessageSourceResolvable("bot.name");
        MessageSourceResolvable userQuizStartMessage
         = new DefaultMessageSourceResolvable("user.quizStartMessage");

        String welcomeMessage
         = this.messageSource.getMessage(
            "bot.welcomeMessage", 
            new MessageSourceResolvable[] {botName, userQuizStartMessage},
            Locale.JAPAN
        );

        return welcomeMessage;
    }

    /**
     * Gets a guide message which indicates that how a user can start a quiz.
     * 
     * @return a guide message when a user stars a quiz.
     */
    public String getQuizStartMessage() {
        MessageSourceResolvable userQuizStartMessage
         = new DefaultMessageSourceResolvable("user.quizStartMessage");
        String quizStartMessage
         = this.messageSource.getMessage(
            "bot.quizStartMessage", 
            new MessageSourceResolvable[] {userQuizStartMessage},
            Locale.JAPAN
        );
        return quizStartMessage;
    }

    /**
     * Gets a guide message which indicates that how a user can select a quiz type.
     * 
     * @return a guide message when a user selects a quiz type.
     */
    public String getTypeSelectMessage() {
        MessageSourceResolvable userQuizCancelmessage
         = new DefaultMessageSourceResolvable("user.quizCancelMessage");
        String quizTypeSelectMessage
         = this.messageSource.getMessage(
            "bot.quizTypeSelectMessage",
            new MessageSourceResolvable[] {userQuizCancelmessage},
            Locale.JAPAN
        );
        return quizTypeSelectMessage;
    }

    /**
     * Generates a quiz a quiz message and an answer message and sets them to quiz dto.
     * The messages changes according to a user's quiz configuration such as 
     * is_description_quiz and is_jp_question_quiz registed in the user table.
     * 
     * @param quizDto a quiz dto 
     * @param userQuizConfigDto a dto of user configuration for a quiz
     * @return a quiz dto where a quiz and answer messages are set
     */
    public QuizDto generateQuizAndAnswerMessages(
        QuizDto quizDto, 
        UserQuizConfigDto userQuizConfigDto
    ) {  

        String quizMessage = generateQuizMessage(quizDto.getQuizWord(), userQuizConfigDto);
        String quizAnswer  = quizDto.getQuizAnswer();
        
        // for an selection quiz, a message to shoq answer candidate are added to a quiz message
        // and prefixes are added to a quiz answer
        if (! userQuizConfigDto.getIsDescriptionQuiz()) {
            HashMap<String, String> map 
             = generateAnswerCandidateMessage(quizDto.getQuizAnswer(), quizDto.getAnswerCandidates());
            quizMessage += "\n" + map.get("message");
            quizAnswer = map.get("answer");
        }

        quizDto.setQuizMessage(quizMessage);
        quizDto.setAnswerMessage(quizAnswer);
        return quizDto;
    }

    /**
     * Generates a quiz message to be sent to a user.
     * This message does not contain answer candidates character string.
     * 
     * @param userQuizConfigDto a dto of user configucaion for a quiz
     * @return a quiz message
     */
    private String generateQuizMessage(String quizWord, UserQuizConfigDto userQuizConfigDto) {
        String quizQuestionMessageFormat
         = this.messageSource.getMessage("bot.quizQuestionMessageFormat", null, Locale.JAPAN);
        String spellingWord
         = this.messageSource.getMessage("spelling", null, Locale.JAPAN);
        String meaningWord
         = this.messageSource.getMessage("meaning", null, Locale.JAPAN);

        String quizMessage;
        if (userQuizConfigDto.getIsJpQuestionQuiz()) {
            // when the question is in Japanese, or English translation quiz,
            // the quiz word is a meaning and the answer word is a spellinng.
            quizMessage = String.format(quizQuestionMessageFormat, meaningWord, spellingWord, quizWord);
        } else {
            // when the question is in English, or Japanese translation quiz,
            // the quiz word is spelling and the answer word is a meaning.
             quizMessage = String.format(quizQuestionMessageFormat, spellingWord, meaningWord, quizWord);
        }

        return quizMessage;
    }

    /**
     * Generates a quiz answer candidates message to be sent to a user.
     * This message just contains answer canidates and does not contains a quiz message.
     * A quiz answer and dummy multiple answer candidatres are combined to form the answer candidates message.
     * 
     * @param quizAnswer a quiz answer
     * @param answerCandidates a list of dummy answer candidates
     * @return a quiz answer candidates message
     */
    private HashMap<String, String> generateAnswerCandidateMessage(
            String quizAnswer, 
            List<String> answerCandidates
    ) {
        String quizAnswerCandidateTitle
         = this.messageSource.getMessage("bot.quizAnswerCandidateTitle", null, Locale.JAPAN);

        // creates a new list instance so as not to affect argument, answerCandidates
        List<String> allAnswerCandidates
         = answerCandidates.stream().collect(Collectors.toList());

        // TO BE IMPROVED: Random should be replaced with RandomNumberGenerator class.
        Random rnd = new Random();
        int insertIndex = rnd.nextInt(allAnswerCandidates.size()+1);
        // the correct answer is inserted into the list at a random index
        allAnswerCandidates.add(insertIndex, quizAnswer);

        List<String> answerPrefixes
         = generateSelectionQuizPrefixes(allAnswerCandidates.size());
        // answer prefixes are added to each answer candidates and then combined with a new line delimiter
        String quizAnswerCandidateMessage
        = allAnswerCandidates.stream()
            .map( item -> answerPrefixes.get(allAnswerCandidates.indexOf(item)) + item)
            .collect(Collectors.joining("\n"));
        
        String message = quizAnswerCandidateTitle + quizAnswerCandidateMessage;
        String answer = answerPrefixes.get(insertIndex) + quizAnswer;

        HashMap<String, String> map = new HashMap<>();
        map.put("message", message);
        map.put("answer", answer);
        return map;
    }

    /**
     * Generates prefixes to be added to each quiz answer candidate.
     * The prefix format is defined in the message properties file.
     * 
     * @param itemNum the number of prefixes to be generated
     * @return a list of generated prefixes
     */
    public List<String> generateSelectionQuizPrefixes(int itemNum) {
        List<String> quickReplyItems = new ArrayList<>(itemNum);
        String format
         = messageSource.getMessage("bot.quizAnswerEachCandidatePrefix", null, Locale.JAPAN);

        for(int i=0; i<itemNum; i++) {
            String quickReplyItem = String.format(format, Integer.toString(i+1));
            quickReplyItems.add(quickReplyItem);
        }

        return quickReplyItems;
    }

    /**
     * Gets a message to be sent when a user answers a quiz correctly.
     * 
     * @return a message to be sent when a user answers a quiz correctly
     */
    public String getCorrectMessage() {
        return this.messageSource.getMessage("bot.quizCorrectMessage", null, Locale.JAPAN);
    }

    /**
     * Generates a message to be sent when a user answers a quiz incorrectly.
     * A quiz answer message is added to the end of the sent message.
     * 
     * @param quizAnswer a quiz answer
     * @return a message to be sent when a user answers incorrectly
     */
    public String generateIncorrectMessage(String quizAnswer) {
        String quizIncorrectMessageFormat
         = this.messageSource.getMessage("bot.quizIncorrectMessageFormat", null, Locale.JAPAN);
        return String.format(quizIncorrectMessageFormat, quizAnswer);
    }

}
