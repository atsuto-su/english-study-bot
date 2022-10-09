package net.myapp.englishstudybot.domain.service.quiz;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.myapp.englishstudybot.domain.model.VocabEntity;
import net.myapp.englishstudybot.domain.model.quiz.QuizAnswerRatioDto;
import net.myapp.englishstudybot.domain.model.quiz.QuizDto;
import net.myapp.englishstudybot.domain.model.quiz.UserQuizConfigDto;
import net.myapp.englishstudybot.domain.repository.QuizAggregationRepository;
import net.myapp.englishstudybot.domain.repository.VocabRepository;
import net.myapp.englishstudybot.domain.util.RandomNumberGenerator;

/**
 * QuizGenerator is a class which provides logics to generate quiz data.
 */
@Slf4j
@Service
public class QuizGenerator {

    private final VocabRepository vocabRepository;
    private final QuizAggregationRepository quizAggregationRepository;
    private final MessageSource messageSource;
    private final RandomNumberGenerator myRnd;

    // the number of answer candidates except for a correct one
    private final int CANDIDATE_NUM = 3;
    // defines each quiz type name (add here when the type increases)
    private final String QUIZ_TYPE_RANDOM = "ランダム";
    private final String QUIZ_TYPE_LEAST_RECENT = "出題日古い";
    private final String QUIZ_TYPE_LOWEST_CORRECTION_RATIO = "正答率低い";
    private final String QUIZ_TYPE_LAST_INCORRECT = "誤答";
    // defines a list of quiz types (add the type to the list when it increases)
    private final List<String> SELECTABLE_QUIZ_TYPES
     = Arrays.asList(
        QUIZ_TYPE_RANDOM,
        QUIZ_TYPE_LEAST_RECENT,
        QUIZ_TYPE_LOWEST_CORRECTION_RATIO,
        QUIZ_TYPE_LAST_INCORRECT
    );

    @Autowired
    QuizGenerator(
        VocabRepository vocabRepository, 
        QuizAggregationRepository quizAggregationRepository,
        MessageSource messageSource,
        RandomNumberGenerator randomNumberGenerator 
    ) {
        this.vocabRepository = vocabRepository;
        this.quizAggregationRepository = quizAggregationRepository;
        this.messageSource = messageSource;
        this.myRnd = randomNumberGenerator;
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
                log.info("Generates random vocabulary quiz.");
                vocab = getRandomVocab();
                break;
            case QUIZ_TYPE_LEAST_RECENT:
                log.info("Generates least recent vocabulary quiz.");
                vocab
                 = getLeastRecentVocab(
                    userQuizConfigDto.getTargetUserId(),
                    userQuizConfigDto.getIsJpQuestionQuiz()
                );
                break;
            case QUIZ_TYPE_LOWEST_CORRECTION_RATIO:
                log.info("Generates lowest correction ratio vocabulary quiz.");
                vocab
                 = getLowestCorrectionRatioVocab(
                    userQuizConfigDto.getTargetUserId(),
                    userQuizConfigDto.getIsJpQuestionQuiz()
                );
                break;
            case QUIZ_TYPE_LAST_INCORRECT:
                log.info("Generates last incorrect vocabulary quiz.");
                vocab
                 = getLastAnswerIncorrectVocab(
                    userQuizConfigDto.getTargetUserId(),
                    userQuizConfigDto.getIsJpQuestionQuiz()
                );
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
     * Gets a vocabulary entity randomly selected from the DB table.
     * 
     * @return a vocabulary entity randomly selected from the DB table.
     */
    private VocabEntity getRandomVocab() {
        return vocabRepository.findRandom();
    }


    /**
     * Gets a vocabulary entity whose question date is oldest for a user.
     * A user's specified config data, isJpQuestion, decides which side of table column data, en or jp, is used.
     * For vocabularies which have not been given as a quiz to a user, question dates are regarded as the oldest.
     * 
     * The algorithm of this method is as follows:
     *  1) Gets a list of vocabulary IDs which have not been given as a quiz yet.
     *  2) Confirms the number of the list obtained in 1).
     *  3) If the number is greater than zero, returns a vocabulary randomly selected from the list.
     *  4) If the number is zero or less, returns a vocabulary whose question date is oldest.
     * 
     * @param userId a user's ID who is communicating with this bot
     * @param isJpQuestionQuiz a flag to decide which question date of the table column, en or jp, is used (if true, jp is used and vice versa)
     * @return A vocabulary entity obtained
     */
    private VocabEntity getLeastRecentVocab(String userId, Boolean isJpQuestionQuiz) {
        List<Integer> notGivenQuizVocabs = getNotGivenQuizIds(userId);
        final Integer targetVocabId;

        if (notGivenQuizVocabs.size() > 0) {
            log.info("At least one vocabulary has not been given to the user yet.");

            // the target is randomly selected from vocabularies not given as a quiz yet.
            targetVocabId
             = notGivenQuizVocabs.get(myRnd.generateIntRandomNumber(notGivenQuizVocabs.size()));
        } else {
            log.info("All vocabularies had already been given to the user.");

            // the target is a vocabulary which is the least recent given as a quiz.
            targetVocabId
             = quizAggregationRepository.findLeastRecentGivenVocab(userId, isJpQuestionQuiz);
        }

        if (targetVocabId == null) {
            log.warn("Switched the quiz type to random because no data found");
            return getRandomVocab();
        } else {
            return vocabRepository.findById(targetVocabId);
        }
    }

    /**
     * Gets a vocabulary entity whose correction ratio of a user is lowest.
     * A user's specified config data, isJpQuestion, decides which side of table column data, en or jp, is used.
     * For vocabularies which have not been given as a quiz to a user, the correction ratios are regarded as zero.
     * 
     * The algorithm of this method is as follows:
     *  1) Gets a list of vocabulary IDs which have not been given as a quiz yet.
     *  2) Confirms the number of the list obtained in 1).
     *  3) If the number is greater than zero, creates a new list which contains the following vocabularies:
     *      - those in the list obtained in 1).
     *      - those which had been given as a quiz but whose correction ratio is zero 
     *     Then, returns a vocabulary randomly selected from the new list.
     *  4) If the number is zero or less, returns a vocabulary whose correction ratio is lowest.
     *
     * @param userId a user's ID who is communicating with this bot
     * @param isJpQuestionQuiz a flag to decide which question date of the table column, en or jp, is used (if true, jp is used and vice versa)
     * @return A vocabulary entity obtained
     */
    private VocabEntity getLowestCorrectionRatioVocab(String userId, Boolean isJpQuestionQuiz) {
        List<Integer> notGivenQuizVocabs = getNotGivenQuizIds(userId);
        List<QuizAnswerRatioDto> extractedQuizAggregationByRatio
         = quizAggregationRepository.extractOrderedByIncorrectionRatio(userId, isJpQuestionQuiz);
        final Integer targetVocabId;

        if (notGivenQuizVocabs.size() > 0) {
            List<Integer> zeroAnswerRatioVocabs;
            zeroAnswerRatioVocabs = notGivenQuizVocabs.stream().collect(Collectors.toList());

            // adds vocabularies to a list which had been given as a quiz resulting in zero correction ratio.
            extractedQuizAggregationByRatio.stream()
                .filter(dto -> dto.getAnswerRatio() == 0)
                .toList().stream()
                .forEach(dto -> zeroAnswerRatioVocabs.add(dto.getVocabularyId()));

            // the target is randomly selected from those which are not given as a quiz,
            // or had been given as a quiz resulting in zero correction ratio.
            targetVocabId
             = zeroAnswerRatioVocabs.get(myRnd.generateIntRandomNumber(zeroAnswerRatioVocabs.size()));
        } else {
            // the target is the one whose correction late is lowest.
            targetVocabId = extractedQuizAggregationByRatio.get(0).getVocabularyId();
        }
 
        if (targetVocabId == null) {
            log.warn("Switched the quiz type to random because no data found");
            return getRandomVocab();
        } else {
            return vocabRepository.findById(targetVocabId);
        }
     }

    /**
     * Gets a vocabulary entity of which a user's last answer was incorrect.
     * A user's specified config data, isJpQuestion, decides which side of table column data, en or jp, is used.
     * For vocabularies which have not been given as a quiz to a user, last answer results are regarded as incorrect.
     * If a user have answered correctly for all vocabularies, a vocabulary is randomly selected from all.
     * 
     * The algorithm of this method is as follows:
     *  1) Gets a list of vocabulary IDs of which a user's last answer was incorrect.
     *  2) Confirms the number of the list obtained in 1).
     *  3) If the number is greater than zero, returns a vocabulary randomly selected from the list.
     *  4) If the number is zero or less, then next gets a list of vocabulary IDs which have not been given as a quiz yet.
     *  5) Confirms the number of the list newly obtained in 4).
     *  6) If the number is greater than zero, returns a vocabulary randomly selected from the list.
     *  7) If the number is zero or less, return a vocabulary randomly selected from all vocabularies.
     * 
     * @param userId a user's ID who is communicating with this bot
     * @param isJpQuestionQuiz a flag to decide which question date of the table column, en or jp, is used (if true, jp is used and vice versa)
     * @return A vocabulary entity obtained
     */
    private VocabEntity getLastAnswerIncorrectVocab(String userId, Boolean isJpQuestionQuiz) {
        List<Integer> lastIncorrectVocabs
         = quizAggregationRepository.findLastIncorrectVocabs(userId, isJpQuestionQuiz);
        final Integer targetVocabId;

        if (lastIncorrectVocabs.size() > 0) {
            log.info("At least one incorrect quiz exists for the user.");

            // the target is randomly selected from the last incorrect vocabulary.
            targetVocabId
             = lastIncorrectVocabs.get(myRnd.generateIntRandomNumber(lastIncorrectVocabs.size()));
        } else {
            log.info("No incorrect quiz exists for the user.");

            List<Integer> notGivenQuizVocabs = getNotGivenQuizIds(userId);
            if (notGivenQuizVocabs.size() > 0) {
                log.info("At least one vocabulary has not been given to the user yet.");

                // the target is randomly selected from vocabularies not given as a quiz yet.
                targetVocabId
                 = notGivenQuizVocabs.get(myRnd.generateIntRandomNumber(notGivenQuizVocabs.size()));
            } else {
                log.info("All vocabularies had already been given to the user.");

                // the target is randomly selected from 
                /*  TO BE IMPROVED IN THE FUTURE 
                    This case should notify to the user that there are no targets and 
                    and encourage to chose different quiz type.
                */
                List<Integer> allVocabIds = vocabRepository.findAllIds();
                targetVocabId = allVocabIds.get(myRnd.generateIntRandomNumber(allVocabIds.size()));

            }
        }

        if (targetVocabId == null) {
            log.warn("Switched the quiz type to random because no data found");
            return getRandomVocab();
        } else {
            return vocabRepository.findById(targetVocabId);
        }
 
    }

    /**
     * Gets vocabulary IDs which are not given to a user as a quiz yet.
     * When no data which satisfy the cnodition are found, returns an empty list. 
     * 
     * @param userId a target user id
     * @return a list of vocabulary IDs which is not given yet
     */
    private List<Integer> getNotGivenQuizIds(String userId) {
        List<Integer> allVocabIds = vocabRepository.findAllIds();
        List<Integer> allVocabIdsOneUser
         = quizAggregationRepository.findAllVocabIdsForOneUser(userId);

        final List<Integer> notGivenQuizIds;
        if (allVocabIdsOneUser == null) {
            // vocabularies not given as a quiz are all vocabularies in the table
            notGivenQuizIds = allVocabIds.stream().collect(Collectors.toList());
        } else {
            // vocabularies not given as a quiz are differences between
            // all vocabularies records and all quiz_aggregations records for a user. 
            notGivenQuizIds
             = allVocabIds.stream()
                .filter(item -> ! allVocabIdsOneUser.contains(item))
                .collect(Collectors.toList());
        }

        return notGivenQuizIds;
    }
    
}
