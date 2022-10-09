package net.myapp.englishstudybot.domain.repository;

import java.util.List;

import net.myapp.englishstudybot.domain.model.QuizAggregationEntity;
import net.myapp.englishstudybot.domain.model.quiz.QuizAnswerRatioDto;

/**
 * QuizAggregationRepository is a repostory interface which provides CRUD methods 
 * for quiz_aggregations table.
 */
public interface QuizAggregationRepository {

    /**
     * Extracts one record by primary key.
     * 
     * @param vocabulariesId vocabularies ID of the record to be extracted
     * @param usersId users ID of the record to be extracted
     * @return a quiz aggregation record found
     */
    public QuizAggregationEntity findById(Integer vocabulariesId, String usersId);

    /**
     * Extracts all vocabulary IDs for a specified user.
     * 
     * @param userId ID of the user to be extracted
     * @return a list of all vocabularies IDs registered for the user
     */
    public List<Integer> findAllVocabIdsForOneUser(String userId);

    /**
     * Extracts one vocabulary ID for a specified user
     * where the column last_question_date_time_(en|jp) is oldest.
     * Which column "_jp" or "_en" becomes target depends on an argument of isJpQuestionQuiz.
     * When the boolean is true, "_jp" columns will be the target.
     * 
     * @param userId ID of the target user 
     * @param isJpQuestionQuiz a flag to configure which type of question, "_jp" or "_en" is target 
     * @return a vocabulary ID which satisfies the condition.
     */
    public Integer findLeastRecentGivenVocab(String userId, Boolean isJpQuestionQuiz);

    /**
     * Extracts all vocabularies IDs and calculated quiz incorrection ratio
     * ordered by the quiz incorrection ratio for a specified user.
     * Which column "_jp" or "_en" becomes target depends on an argument of isJpQuestionQuiz.
     * When the boolean is true, "_jp" columns will be the target.
      * 
     * @param userId ID of the target user
     * @param isJpQuestionQuiz a flag to configure which type of question, "_jp" or "_en" is target
     * @return a list of maps composed of vocabularies ID and incorrection ratio.
     */
    public List<QuizAnswerRatioDto> extractOrderedByIncorrectionRatio(
        String userId, Boolean isJpQuestionQuiz
    );

    /**
     * Extracts all vocabularies IDs for a user
     * where the column is_last_answer_correct_(en|jp) is false
     * Which column "_jp" or "_en" becomes target depends on an argument of isJpQuestionQuiz.
     * When the boolean is true, "_jp" columns will be the target.
      * 
     * @param userId ID of the target user
     * @param isJpQuestionQuiz a flag to configure which type of question, "_jp" or "_en" is target
     * @return a list of vocabularies IDs which satisfy the condition
     */
    public List<Integer> findLastIncorrectVocabs(
        String userId, Boolean isJpQuestionQuiz
    );

    /**
     * Inserts one new record.
     * 
     * @param quizAggregation a new quiz aggregation record
     * @return an inserted quiz aggregation record
     */
    public QuizAggregationEntity add(QuizAggregationEntity quizAggregation);

    /**
     * Updates one quiz record aggregation record when a quiz is given to a user.
     * The following column data will be updated
     *  - "total_count_question_en" or "total_count_question_jp" (to be incremented)
     *  - "last_question_datetime_en" or "last_question_datetime_jp" (to be updated)
     * which column "_jp" or "_en" to be updated depends on an argument of isJpQuestionQuiz.
     * When the boolean is true, "_jp" columns will be updated and vice versa.
     * 
     * @param vocablariesId vocabularies ID of the record to be udpated
     * @param usersId users ID of the record to be updated
     * @param isJpQuestionQuiz a flag to configure which type of question, "_jp" or "_en" is given
     * @return an updated quiz aggregation record
     */
    public QuizAggregationEntity updateGivenQuiz(
        Integer vocablariesId, 
        String usersId, 
        Boolean isJpQuestionQuiz
    );

    /**
     * Updates one quiz aggregation record when the user answers CORRECTLY.
     * The following column data will be udpated
     *  - "total_count_correct_en" or "total_count_correct_jp" to be incremented
     *  - "is_last_answer_correct_en" or "is_last_answer_correct_jp" to be incremented
     * which column "_jp" or "_en" to be updated depends on an argument of isJpQuestionQuiz.
     * When the boolean is true, "_jp" columns will be updated and vice versa.
     * 
     * @param vocablariesId vocabularies ID of the record to be udpated
     * @param usersId users ID of the record to be updated
     * @param isJpQuestionQuiz a flag to configure which type of question, "_jp" or "_en" is given
     * @return an updated quiz aggregation record
     */
    public QuizAggregationEntity updateCorrectCase(
        Integer vocabulariesId, 
        String usersId,
        Boolean isJpQuestionQuiz
    );

   /**
     * Updates one quiz aggregation record when the user answers INCORRECTLY.
     * The following column data will be udpated
     *  - "is_last_answer_correct_en" or "is_last_answer_correct_jp" to be incremented
     * which column "_jp" or "_en" to be updated depends on an argument of isJpQuestionQuiz.
     * When the boolean is true, "_jp" columns will be updated and vice versa.
     * 
     * @param vocablariesId vocabularies ID of the record to be udpated
     * @param usersId users ID of the record to be updated
     * @param isJpQuestionQuiz a flag to configure which type of question, "_jp" or "_en" is given
     * @return an updated quiz aggregation record
     */
    public QuizAggregationEntity updateIncorrectCase(
        Integer vocabulariesId, 
        String usersId,
        Boolean isJpQuestionQuiz
    );

    /**
     * Deletes one existing record.
     * 
     * @param vocabulariesId vocabularies ID of the record to be deleted 
     * @param usersId users ID of the record to be deleted
     */
    public void delete(Integer vocabulariesId, String usersId);
    
}
