package net.myapp.englishstudybot.domain.repository;

import net.myapp.englishstudybot.domain.model.QuizStateName;
import net.myapp.englishstudybot.domain.model.UserEntity;

/**
 * UserbRepository is a repostory interface which provides CRUD methods for users table.
 *
 */
public interface UserRepository {

    /**
     * Extracts one record by primary key.
     *  
     * @param id the primary key of the record to be extracted
     * @return a user record found
     */
    public UserEntity findById(String id);

    /**
     * Inserts one new record.
     *
     * @param user a new user record
     * @return an inserted user record
     */
    public UserEntity add(UserEntity user);

    /**
     * Updates quiz_status column for a specified user.
     * 
     * @param id the primary key of the record to be updated
     * @param quizStatus new quiz_status to be updated with
     * @return an updated user record
     */
    public UserEntity updateUserStatus(String id, QuizStateName quizStatus);

    /**
     * Updates the folloeing columns for a specified user.
     *  - last_vocabularies_id
     *  - last_quiz_sentence
     *  - last_quiz_answer
     * 
     * @param user a user record with updated values
     * @return an updated user record
     */
    public UserEntity updateLastQuizInfo(UserEntity user);
    
    /**
     * Deletes one existing record.
     * 
     * @param id the primary key of the record to be deleted
     */
    public void delete(String id);

}
