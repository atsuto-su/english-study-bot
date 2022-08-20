package net.myapp.englishstudybot.domain.repository;

import java.util.List;

import net.myapp.englishstudybot.domain.model.VocabEntity;

public interface VocabRepository {

   /**
     * Extracts all vocabularies data from DB.
     * 
     * @return List of all vocabulary entities
     */
    public List<VocabEntity> findAll();

    /**
     * Inserts new vocabulary record into DB.
     * 
     * @param vocab vocabulary data entity
     * @return inserted vocabulary record
     */
     public VocabEntity add(VocabEntity vocab);

    // public VocabEntity delete(int id);
    // public VocabEntity update(VocabEntity vocab);
}
