package net.myapp.englishstudybot.domain.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;
import net.myapp.englishstudybot.domain.model.QuizAggregationEntity;

/**
 * QuizAggregationDao is an implementation of QuizAggregationRepository by using JdbcTemplate.
 * 
 * ROOM FOR IMPROVEMENT :
 * 1) Use NamedSpaceJdbcTemplate Instead of using JdbcTemplate.
 *    Thereby, readability and maintainability is expected to improve.
 *    (e.g. query parameters can be specified with combination of keys and values)
 * 2) Change O/R mapper method to MyBatis or JPA, etc. instead of Spring JDBC.
 *    Thereby, maintanability is expected to improve
 *    because query and each DB parameters such as table name could be independent
 *    of repository class file.
 * 3) All add or update methods use to findById return entity, which might be better to be changed.
 *    This is because those methods access DB at least twice
 *    which could delay this class's behavior when DB records are huge.
 *    Instead, use RETURNING sentence for query to reduce DB access.
 */
@Slf4j
@Repository
public class QuizAggregationDao implements QuizAggregationRepository {

    private final String TABLE_NAME = "quiz_aggregations";
    private final String COL_NAME_VOCABULARIES_ID = "vocabularies_id";
    private final String COL_NAME_USERS_ID = "users_id";

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    QuizAggregationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    /**
     * Extracts one record by executing the following SQL:
     * SELECT * FROM quiz_aggregations 
     *  WHERE vocabularies_id = {specified id} AND users_id = {specified id};
     */
    @Override
    public QuizAggregationEntity findById(Integer vocabulariesId, String usersId) {
        log.info("START: QuizAggregationDao#findById");

        QuizAggregationEntity quizAggregation;
        String query
         = String.format(
            """
            SELECT * FROM %s
            WHERE %s = ? AND %s = ?
            """,
            TABLE_NAME, COL_NAME_VOCABULARIES_ID, COL_NAME_USERS_ID
        );
        try {
            Map<String, Object> item
             = jdbcTemplate.queryForMap(query, vocabulariesId, usersId);
            quizAggregation 
             = new QuizAggregationEntity(
                (Integer) item.get(COL_NAME_VOCABULARIES_ID),
                (String) item.get(COL_NAME_USERS_ID),
                (Integer) item.get("total_count_question_en"),
                (Integer) item.get("total_count_question_jp"),
                Optional.ofNullable((Timestamp) item.get("last_question_datetime_en"))
                    .map(Timestamp::toLocalDateTime).orElse(null),
                Optional.ofNullable((Timestamp) item.get("last_question_datetime_jp"))
                    .map(Timestamp::toLocalDateTime).orElse(null),
                (Integer) item.get("total_count_correct_en"),
                (Integer) item.get("total_count_correct_jp"),
                (Boolean) item.get("is_last_answer_correct_en"),
                (Boolean) item.get("is_last_answer_correct_jp"),
                (Boolean) item.get("is_quiz_disallowed"),
                ((Timestamp) item.get("created_at")).toLocalDateTime(),
                ((Timestamp) item.get("updated_at")).toLocalDateTime()
            );
        } catch(EmptyResultDataAccessException e) {
            quizAggregation = null;
        }  

        log.info("END: QuizAggregationDao#findById");
        return quizAggregation;
    }

    /**
     * Inserts one new record by executing the following SQL:
     * INSERT INTO vocabularies {all columns} VALUES {each specified value};
     */
    @Override
    public QuizAggregationEntity add(QuizAggregationEntity quizAggregation) {
        log.info("START: QuizAggregationDao#add");

        LocalDateTime currentTime = LocalDateTime.now();
        quizAggregation.setCreatedAt(currentTime);
        quizAggregation.setUpdatedAt(currentTime);
        
        SimpleJdbcInsert insert
         = new SimpleJdbcInsert(jdbcTemplate)
            .withTableName(TABLE_NAME);
        SqlParameterSource param = new BeanPropertySqlParameterSource(quizAggregation);
        insert.execute(param);

        log.info("END: QuizAggregationDao#add");
        return findById(quizAggregation.getVocabulariesId(), quizAggregation.getUsersId());
    }

    /**
     * Updates one existing record when a quiz is given by executing the following SQL:
     * UPDATE quiz_aggregations SET 
     *  total_count_questions_(en|jp) = total_count_questions_(en|jp) + 1
     *  last_question_datetime_(en|jp) = {current time}
     *  updated_at = {current time}
     *  WHERE vocabularies_id = {specified id} AND users_id = {specified id};
     */
    @Override
    public QuizAggregationEntity updateGivenQuiz(
        Integer vocabulariesId, 
        String usersId, 
        Boolean isJpQuestionQuiz
    ) {
        log.info("START: QuizAggregationDao#updateGivenQuiz");

        String updateColumnSuffix;
        if (isJpQuestionQuiz) {
            updateColumnSuffix = "jp";
        } else {
            updateColumnSuffix = "en";
        }
        String query
         = String.format(
            """
            UPDATE %1$s SET
                total_count_question_%2$s = total_count_question_%2$s + 1,
                last_question_datetime_%2$s = ?,
                updated_at = ?
            WHERE %3$s = ? AND %4$s = ?                
            """,
            TABLE_NAME, updateColumnSuffix, COL_NAME_VOCABULARIES_ID, COL_NAME_USERS_ID
        );
        LocalDateTime currentTime = LocalDateTime.now();
        jdbcTemplate.update(query, currentTime, currentTime, vocabulariesId, usersId);

        log.info("END: QuizAggregationDao#updateGivenQuiz");
        return findById(vocabulariesId, usersId);
    }

    /**
     * Updates one record when a user answers correctly by executing the following SQL:
     * UPDATE quiz_aggregations SET 
     *  total_count_correct_(en|jp) = total_count_correct_(en|jp) + 1
     *  is_last_answer_correct_(en|jp) = TRUE
     *  updated_at = {current time}
     *  WHERE vocabularies_id = {specified id} AND users_id = {specified id};
     */
    @Override
    public QuizAggregationEntity updateCorrectCase(
        Integer vocabulariesId, 
        String usersId,
        Boolean isJpQuestionQuiz
    ) {
        log.info("START: QuizAggregationDao#updateCorrectCase");
        QuizAggregationEntity quizAggregation
         = updateAnswerHistory(
            vocabulariesId, 
            usersId, 
            isJpQuestionQuiz, 
            true, 
            1
        );
        log.info("END: QuizAggregationDao#updateCorrectCase");
        return quizAggregation;
    }

    /**
     * Updates one record when a user answers incorrectly by executing the following SQL:
     * UPDATE quiz_aggregations SET 
     *  is_last_answer_correct_(en|jp) = FALSE
     *  updated_at = {current time}
     *  WHERE vocabularies_id = {specified id} AND users_id = {specified id};
     */
    @Override
    public QuizAggregationEntity updateIncorrectCase(
        Integer vocabulariesId, 
        String usersId,
        Boolean isJpQuestionQuiz
    ) {
        log.info("START: QuizAggregationDao#updateIncorrectCase");
        QuizAggregationEntity quizAggregation
         = updateAnswerHistory(
            vocabulariesId, 
            usersId, 
            isJpQuestionQuiz, 
            false, 
            0
        );
        log.info("END: QuizAggregationDao#updateIncorrectCase");
        return quizAggregation;
    }

    /**
     * Updates the following columns in one record according to whether a user answers correctly or not.
     *  (1) toal_count_correct_(en|jp)
     *  (2) is_last_answer_correct_(en|jp)
     *  (3) updated_at 
     * The column (1) is incremneted by a specified value in an argument incrementNum.
     * The column (2) is set to true if an argument isCorrected is true,
     * and set to false if the argument is false
     * 
     * @param vocablariesId vocabularies ID of the record to be udpated
     * @param usersId users ID of the record to be updated
     * @param isJpQuestionQuiz a flag to configure which type of question, "_jp" or "_en" is given
     * @param isCorrected a flag of whether a user answers correctly or not
     * @param incrementNum an increment for the column, total_count_correct_(en|jp)
     * @return an updated quiz aggregation record
     */
    private QuizAggregationEntity updateAnswerHistory(
        Integer vocabulariesId, 
        String usersId,
        Boolean isJpQuestionQuiz,
        Boolean isCorrected,
        int incrementNum
    ) {

        String updateColumnSuffix;
        if (isJpQuestionQuiz) {
            updateColumnSuffix = "jp";
        } else {
            updateColumnSuffix = "en";
        }
        String query
         = String.format(
            """
            UPDATE %1$s SET
                total_count_correct_%2$s = total_count_correct_%2$s + ?,
                is_last_answer_correct_%2$s = ?,
                updated_at = ?
            WHERE %3$s = ? AND %4$s = ?                
            """,
            TABLE_NAME, updateColumnSuffix, COL_NAME_VOCABULARIES_ID, COL_NAME_USERS_ID
        );
        jdbcTemplate.update(
            query, 
            incrementNum, 
            isCorrected, 
            LocalDateTime.now(), 
            vocabulariesId, 
            usersId
        );

        return findById(vocabulariesId, usersId);
 
    }

    /**
     * Deletes one existing record by executing the following SQL:
     * DELETE FROM question_aggregations 
     *  WHERE vocabularies_id = {specified id} AND users_id {specified id};
     */
    @Override
    public void delete(Integer vocabulariesId, String usersId) {
        log.info("START: QuizAggregationDao#delete");

        String query
         = String.format(
            """
                DELETE FROM %1$s
                WHERE %2$s = ? AND %3$s = ?
            """,
            TABLE_NAME, COL_NAME_VOCABULARIES_ID, COL_NAME_USERS_ID 
        );
        jdbcTemplate.update(query, vocabulariesId, usersId);

        log.info("END: QuizAggregationDao#delete");
    }

    
}
