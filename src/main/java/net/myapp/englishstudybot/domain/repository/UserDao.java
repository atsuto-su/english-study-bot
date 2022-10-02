package net.myapp.englishstudybot.domain.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;
import net.myapp.englishstudybot.domain.model.QuizStateName;
import net.myapp.englishstudybot.domain.model.UserEntity;

@Slf4j
@Repository
public class UserDao implements UserRepository{

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    UserDao(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Extracts one record by executing the following SQL:
     * SELECT * FROM users WHERE id = {specified id};
     */
    @Override
    public UserEntity findById(String id) {
        log.info("START: UserDao#findById");

        UserEntity user;
        String query = "SELECT * FROM users WHERE id = ?";
        try{
           Map<String, Object> extractedItem = jdbcTemplate.queryForMap(query, id);
           user = new UserEntity(
                    (String) extractedItem.get("id"),
                    (Boolean) extractedItem.get("is_self_word_only"),
                    (Boolean) extractedItem.get("is_example_quiz"),
                    (Boolean) extractedItem.get("is_jp_question_quiz"),
                    (Boolean) extractedItem.get("is_description_quiz"),
                    (Integer) extractedItem.get("quiz_status"),
                    (Integer) extractedItem.get("last_vocabularies_id"),
                    (String) extractedItem.get("last_quiz"),
                    (String) extractedItem.get("last_quiz_answer"),
                    ((Timestamp) extractedItem.get("created_at")).toLocalDateTime(),
                    ((Timestamp) extractedItem.get("updated_at")).toLocalDateTime()
                );
        } catch (EmptyResultDataAccessException e) {
            user = null;
        }

        log.info("END: UserDao#findById");
        return user;
    }

    /**
     * Inserts one new record by executing the following SQL:
     * INSERT INTO users {all columns} VALUES {each specified value};
     */
    @Override
    public UserEntity add(UserEntity user) {
        log.info("START: UserDao#add");

        LocalDateTime currentTime = LocalDateTime.now();
        user.setCreatedAt(currentTime);
        user.setUpdatedAt(currentTime);

        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                                        .withTableName("users");
        SqlParameterSource param = new BeanPropertySqlParameterSource(user);
        insert.execute(param);

        log.info("END: UserDao#add");
        return findById(user.getId());
    }

    /**
     * Updates quiz_status column of one existing record by executing the following SQL:
     * UDPATE users SET quiz_status = {specified value} WHERE id = {specified id};
     */
    @Override
    public UserEntity updateUserStatus(String id, QuizStateName quizStatus){
        log.info("START: UserDao#updateUserStatus");

        String query = """
                        UPDATE users SET 
                            quiz_status = ?,
                            updated_at = ?
                        WHERE id = ?
                        """;
        jdbcTemplate.update(query, quizStatus.getCode(), LocalDateTime.now(), id);

        log.info("END: UserDao#updateUserStatus");
        return findById(id);
    }

    /**
     * Updates one existing record related to quiz info. by executing the following SQL:
     * UPDATE users SET {last_vocabularies_id, last_quiz_sentence, last_quiz_answer} WHERE id = {specified id};
     */
    @Override
    public UserEntity updateLastQuizInfo(UserEntity user){
        log.info("START: UserDao#updateLastQuizInfo");

        String query = """
                        UPDATE users SET
                           last_vocabularies_id = ?,
                           last_quiz = ?,
                           last_quiz_answer = ?,
                           updated_at = ? 
                        WHERE id = ?
                        """;
        jdbcTemplate.update(query, 
                            user.getLastVocabulariesId(),
                            user.getLastQuizSentence(),
                            user.getLastQuizAnswer(),
                            LocalDateTime.now(),
                            user.getId());

        log.info("END: UserDao#updateLastQuizInfo");
        return findById(user.getId());
    }

    /**
     * Deletes one existing record by executing the following SQL:
     * DELETE FROM users WHERE id = {specified id};
     */
    @Override
    public void delete(String id){
        log.info("START: UserDao#delete");

        String query = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(query, id);

        log.info("END: UserDao#delete");
    }
    
}
