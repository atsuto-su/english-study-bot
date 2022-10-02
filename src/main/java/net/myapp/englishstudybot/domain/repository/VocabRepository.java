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
     * Extracts all records.
     * 
     * @return a list of all vocabulary records
     */
    public List<VocabEntity> findAll();

    /**
     * Extracts one record by primary key.
     * 
     * @param id the primary key of the record to be extracted
     * @return a vocabulary record found
     */
    public VocabEntity findById(Integer id);

    // public VocabEntity findBySpelling(String spelling);
    // public VocabEntity findByMeaning(String meaning);

    /**
     * Extracts one record randomly
     * 
     * @return a vocabulary record found
     */
    public VocabEntity findRandom();

    /**
     * Extracts some records randomly except for one specified record.
     * 
     * @param vocabIdExcluded the primary key of one record excluded from the extracting
     * @param findNum the number of records to be extracted
     * @return
     */
    public List<VocabEntity> findSomeExceptForOne(int findNum, Integer vocabIdExcluded);

    /**
     * Inserts one new record.
     * 
     * @param vocab a new vocabulary record 
     * @return an inserted vocabulary record
     */
    public VocabEntity add(VocabEntity vocab);

    /**
     * Deletes one existing record.
     * 
     * @param id the primary key of the record to be deleted
     */
    public void delete(Integer id);

    /**
     * Updates one existing record.
     * 
     * @param vocab a vocabulary record with updated values
     * @return an updated vocabulary record
     */
    public VocabEntity update(VocabEntity vocab);


}
