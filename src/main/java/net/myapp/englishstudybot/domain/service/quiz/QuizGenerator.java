package net.myapp.englishstudybot.domain.service.quiz;

import java.util.List;
import java.util.Locale;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.myapp.englishstudybot.domain.model.VocabEntity;
import net.myapp.englishstudybot.domain.model.quiz.QuizDto;
import net.myapp.englishstudybot.domain.model.quiz.UserQuizConfigDto;
import net.myapp.englishstudybot.domain.repository.VocabRepository;

/**
 * QuizGenerator is a class which generates quiz data.
 */
@Slf4j
@Service
public class QuizGenerator {

    private final VocabRepository vocabRepository;
    private final MessageSource messageSource;

    // the number of answer candidates except for a correct one
    private final int CANDIDATE_NUM = 3;
    // defines each quiz type name (add here when the type increases)
    private final String QUIZ_TYPE_RANDOM = "ランダム";
    // defines a list of quiz types (add the type to the list when it increases)
    private final List<String> SELECTABLE_QUIZ_TYPES
     = Arrays.asList(QUIZ_TYPE_RANDOM);

    @Autowired
    QuizGenerator(VocabRepository vocabRepository, MessageSource messageSource) {
        this.vocabRepository = vocabRepository;
        this.messageSource = messageSource;
    }

    /**
     * Gets a list of quiz types which a user can choose
     * 
     * @return a list of quiz types
     */
    public List<String> getSelectableQuizTypes() {
        return SELECTABLE_QUIZ_TYPES;
    }

    /**
     * Generates a quiz data which includes question and answer pairs 
     * Which types of answer, spelling or meaning, is required depends on
     * a user configuration dto in an argument.
     * The algorithm of selecting a target vocabulary for a quiz depends on the quiz type 
     * specifeid in the user configuration dto.
     * 
     * @param userQuizConfigDto a quiz config dto for a user
     * @return a QuizDto entity which includes question and answer pairs for a quiz
     */
    public QuizDto generateQuiz(UserQuizConfigDto userQuizConfigDto) {
        log.info("START: QuizGenerator#generateQuiz");

        VocabEntity vocab;
        //obtains vocabulary data for a quiz according to the quiz type
        switch (userQuizConfigDto.getQuizType()) {
            case QUIZ_TYPE_RANDOM: 
                log.info("Generates Random quiz.");
                vocab = getsRandomVocab();
                break;
            default:
                String errorMessage
                 = messageSource.getMessage("error.quizTypeNotDefined", null, Locale.JAPAN);
                throw new IllegalArgumentException(errorMessage);
        }

        String quizWord;
        String quizAnswer;
        // sets quiz word and answer pairs according to which kinds of quiz,
        // Japanese quiz (English translation quiz) or English quiz (Japanese translation quiz) is requested.
        if (userQuizConfigDto.getIsJpQuestionQuiz()) {
            quizWord = vocab.getMeaning();
            quizAnswer = vocab.getSpelling();
        } else {
            quizWord = vocab.getSpelling();
            quizAnswer = vocab.getMeaning();
        }

        List<String> answerCandidates = null; 
        // obtains quiz answer candidates data if requested.
        if(! userQuizConfigDto.getIsDescriptionQuiz()) {
            List<VocabEntity> vocabCandidates
             = vocabRepository.findSomeExceptForOne(CANDIDATE_NUM, vocab.getId());
            if (userQuizConfigDto.getIsJpQuestionQuiz()) {
                answerCandidates
                 = vocabCandidates.stream().map( item -> item.getSpelling()).toList();
            } else {
                answerCandidates
                 = vocabCandidates.stream().map( item -> item.getMeaning()).toList();
            }
        }

        log.info("END: QuizGenerator#generateQuiz");
        return new QuizDto(
            vocab.getId(), 
            quizWord, 
            quizAnswer, 
            answerCandidates, 
            null, 
            null
        );

    }

    /**
     * Gets a vocabulary data randomly from the DB table.
     * 
     * @return a vocabulary entity selected randomly from the DB table.
     */
    private VocabEntity getsRandomVocab() {
        return vocabRepository.findRandom();
    }
    
}
