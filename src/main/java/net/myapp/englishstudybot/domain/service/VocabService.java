package net.myapp.englishstudybot.domain.service;

import java.util.List;

import net.myapp.englishstudybot.domain.model.VocabEntity;

/**
 * VocabService is a service interface which provides CRUD methods for vocabularies table.
 * The purpose of this interface is to mediate between VocabRestController and VocabRepository.
 * 
 */
public interface VocabService {

    /**
     * Extracts all vocabularies data from vocabularies table.
     * 
     * @return List of all vocabulary entities
     */
    public List<VocabEntity> getAllVocabs();

    /**
     * Inserts new record into vocabularies table.
     * 
     * @param vocab vocabulary data entity
     * @return inserted vocabulary record
     */
    public VocabEntity addVocab(VocabEntity vocab);

    /**
     * Deletes one record from vocabularies table.
     * 
     * @param id ID of the record to be deleted from the table
     */
    public void deleteVocab(Integer id);
    // public VocabEntity update(VocabEntity vocab);

}
