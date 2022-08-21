package net.myapp.englishstudybot.domain.repository;

import java.util.List;

import net.myapp.englishstudybot.domain.model.VocabEntity;

/**
 * VocabRepository is a repostory interface which provides CRUD methods for vocabularies table.
 * The purpose of this interface is to hide whch kinds of O/R mapper is used
 * so that users of this interface need not care.
 * 
 * NOTE:
 * If we just want to focus on Spring JDBC, this interface can extends prepared interfaces provided by Spring JDBC
 * or a new interface can be created extends this interface and the prepared interfaces.
 * (e.g. CrudRepository as described in the article https://qiita.com/dkurata38/items/33e43b6cfc6f2f2bb393)
 * If the above modification is necessary or not should be taken into account in the future.
 */
public interface VocabRepository {

   /**
     * Extracts all vocabularies data from vocabularies table.
     * 
     * @return List of all vocabulary entities
     */
    public List<VocabEntity> findAll();

    // public VocabEntity findById(int id);
    // public VocabEntity findBySpelling(String spelling);
    // public VocabEntity findByMeaning(String meaning);

    /**
     * Inserts new record into vocabularies table.
     * 
     * @param vocab vocabulary entity
     * @return inserted vocabulary record as entity
     */
    public VocabEntity add(VocabEntity vocab);

    /**
     * Deletes one record from vocabularies table.
     * 
     * @param id ID of the record to be deleted from the table
     */
    public void delete(Integer id);

    // public VocabEntity update(VocabEntity vocab);
}
