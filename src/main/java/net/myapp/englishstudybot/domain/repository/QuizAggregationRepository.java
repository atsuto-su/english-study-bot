package net.myapp.englishstudybot.domain.repository;

import net.myapp.englishstudybot.domain.model.QuizAggregationEntity;

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
