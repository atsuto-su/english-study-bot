package net.myapp.englishstudybot.domain.service.vocab;

import java.util.List;

import net.myapp.englishstudybot.domain.model.VocabEntity;

/**
 * VocabService is a service interface which provides CRUD methods for vocabularies table.
 * The purpose of this interface is to mediate between VocabRestController and VocabRepository.
 * Therefore, ths interface should be implemented by using repository class.
 */
public interface VocabService {

    /**
     * Provides reading operation to extract all records.
     * 
     * @return list of all vocabulary records
     */
    public List<VocabEntity> getAllVocabs();

    /**
     * Provides creating operation to insert one new record.
     * 
     * @param vocab a new vocabulary record 
     * @return an inserted vocabulary record
     */
    public VocabEntity addVocab(VocabEntity vocab);

    /**
     * Provides deleting operation to delete one specified record.
     * 
     * @param id the primary key of the record to be deleted
     */
    public void deleteVocab(Integer id);

    /**
     * Provides updateing opration to update one specified record.
     * 
     * @param vocab a vocabulary record with updated values
     * @return an updated vocabulary record
     */
    public VocabEntity update(VocabEntity vocab);

}
