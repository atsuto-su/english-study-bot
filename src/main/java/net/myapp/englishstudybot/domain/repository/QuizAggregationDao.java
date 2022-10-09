package net.myapp.englishstudybot.domain.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
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
import net.myapp.englishstudybot.domain.model.quiz.QuizAnswerRatioDto;

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
     * When no data are found, return null.
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
     * Extracts all vocabulary IDS for a specified user by the following SQL:
     *  SELECT id FROM quiz_aggregations WHERE users_id = {specified id};
     * When no data which satisfy the cnodition are found, return an empty list. 
     */
    @Override
    public List<Integer> findAllVocabIdsForOneUser(String userId) {
        log.info("START: QuizAggregationDao#findAllVocabIdsByUser");

        String query
         = String.format(
            "SELECT %s FROM %s WHERE %s = ?",
            COL_NAME_VOCABULARIES_ID, TABLE_NAME, COL_NAME_USERS_ID
        );

        List<Integer> vocabularyIds;
        try {
            List<Map<String, Object>> extractedList
             = jdbcTemplate.queryForList(query, userId);
            vocabularyIds = extractedList.stream()
                .map(item -> (Integer) item.get(COL_NAME_VOCABULARIES_ID)).toList();
        } catch (EmptyResultDataAccessException e) {
            vocabularyIds = List.of();
        }

        log.info("END: QuizAggregationDao#findAllVocabIdsByUser");
        return vocabularyIds;

    }

    /**
     * Extracts one vocabulary ID for a specified user
     * where the column last_question_date_time_(en|jp) is oldest.
     * by executing the following SQL:
     *  SELECT vocabularies_id FROM quiz_aggregations
     *  WHERE users_id = {specified ID} AND last_question_datetime_(en|jp) = 
     *   (SELECT MIN(last_question_datetime_(en|jp)) FROM quiz_aggregations)
     *  LIMIT 1
     * When no data which satisfy the condition are found, returns an empty list. 
     */
    @Override
    public Integer findLeastRecentGivenVocab(String userId, Boolean isJpQuestionQuiz) {
        log.info("START: QuizAggregationDao#findLeastRecentGivenVocab");

        String targetColumnSuffix;
        if (isJpQuestionQuiz) {
            targetColumnSuffix = "jp";
        } else {
            targetColumnSuffix = "en";
        }
        String targetColumnName = "last_question_datetime_" + targetColumnSuffix;
        String query
         = String.format(
            """
                SELECT %1$s FROM %2$s WHERE %3$s = ? AND %4$s = 
                    (SELECT MIN(%4$s) FROM %2$s WHERE %3$s = ?) 
                LIMIT 1
            """,
            COL_NAME_VOCABULARIES_ID, TABLE_NAME,
            COL_NAME_USERS_ID, targetColumnName
        );

        Integer leastRecentvocabularyId;
        try {
            leastRecentvocabularyId 
             = (Integer) jdbcTemplate.queryForMap(query, userId, userId)
                .get(COL_NAME_VOCABULARIES_ID);
        } catch (EmptyResultDataAccessException e) {
            leastRecentvocabularyId = null;
        }

        log.info("END: QuizAggregationDao#findLeastRecentGivenVocab");
        return leastRecentvocabularyId;

    }
    
    /**
     * Extracts all vocabularies IDs and calculated quiz incorrection ratio
     * ordered by the quiz incorrection ratio for a specified user.
     * by executing the following SQL:
     *  SELECT 
     *      vocabularies_id, 
     *      CAST( COALSESCE(
     *          CAST(total_count_correct_(en|jp)*100 AS NUMERIC) / 
     *          NULLIF(total_count_question_(en|jp) ,0) , 0
     *          ) AS NUMERIC(5,2)
     *      ) AS ratio 
     *  FROM quiz_aggregations 
     *  WHERE users_id = {specified ID} 
     *  ORDER BY ratio ASC;
     * 
     * When total_count_question_(en|jp) is 0, the ratio returns 0 for the row.
     * When no data are found for the user, returns an empty list. 
     */
    @Override
    public List<QuizAnswerRatioDto> extractOrderedByIncorrectionRatio(
        String userId, Boolean isJpQuestionQuiz
    ) {
        log.info("START: QuizAggregationDao#extractOrderedByIncorrectionRatio");

        String targetColumnSuffix;
        if (isJpQuestionQuiz) {
            targetColumnSuffix = "jp";
        } else {
            targetColumnSuffix = "en";
        }
        final String COL_NAME_RATIO = "ratio";
        String query = String.format(
            """
                SELECT %1$s, 
                    CAST( 
                        COALESCE(
                            CAST(total_count_correct_%2$s * 100 AS NUMERIC) / 
                                NULLIF(total_count_question_%2$s, 0)
                            , 0
                        ) AS DOUBLE PRECISION
                    ) AS %3$s from %4$s 
                WHERE %5$s = ?
                ORDER BY %3$s ASC;
            """,
            COL_NAME_VOCABULARIES_ID, targetColumnSuffix,
            COL_NAME_RATIO, TABLE_NAME, COL_NAME_USERS_ID
        );

        List<QuizAnswerRatioDto> correctionRecordsList;
        try {
            List<Map<String, Object>> records = jdbcTemplate.queryForList(query, userId);
            correctionRecordsList 
             = records.stream()
                .map( item -> new QuizAnswerRatioDto(
                                (Integer) item.get(COL_NAME_VOCABULARIES_ID),
                                (Double) item.get(COL_NAME_RATIO)
                            )
                ).toList();
        } catch (EmptyResultDataAccessException e) {
            correctionRecordsList = List.of();
        }

        log.info("END: QuizAggregationDao#extractOrderedByIncorrectionRatio");
        return correctionRecordsList;

    }

    /**
     * Extracts all vocabularies IDs for a user
     * where the column is_last_answer_correct_(en|jp) is false
     * by executing the following SQL: 
     *  SELECT vocabularies_id FROM quiz_aggregations
     *  WHERE user_id = {specified ID} AND is_last_correct_(en|jp) = false
     * When no data which satisfy the condition are found, returns an empty list. 
     */
    @Override
    public List<Integer> findLastIncorrectVocabs(
        String userId, Boolean isJpQuestionQuiz
    ) {
        log.info("START: QuizAggregationDao#extractLastIncorrectQuiz");

        String targetColumnSuffix;
        if (isJpQuestionQuiz) {
            targetColumnSuffix = "jp";
        } else {
            targetColumnSuffix = "en";
        }
        String query = String.format(
            """
                SELECT %1$s FROM %2$s
                WHERE %3$s = ? AND is_last_answer_correct_%4$s = false
                    
            """,
                COL_NAME_VOCABULARIES_ID, TABLE_NAME,
                COL_NAME_USERS_ID, targetColumnSuffix
        );

        List<Integer> vocabularyIds;
        try {
            List<Map<String, Object>> extractedItems
             = jdbcTemplate.queryForList(query, userId);
            vocabularyIds
             = extractedItems.stream()
                .map( item -> (Integer) item.get(COL_NAME_VOCABULARIES_ID))
                .toList();
        } catch (EmptyResultDataAccessException e) {
            vocabularyIds = List.of();
        }

        log.info("END: QuizAggregationDao#extractLastIncorrectQuiz");
        return vocabularyIds;
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
