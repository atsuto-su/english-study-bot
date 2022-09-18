package net.myapp.englishstudybot.domain.service.vocab;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.myapp.englishstudybot.domain.model.VocabEntity;
import net.myapp.englishstudybot.domain.repository.VocabRepository;

/**
 * VocabServiceImpl is a service class which implements VocabService interface
 * and uses CRUD methods defined in VocabRepository intefaces.
 * 
 * NOTE:
 * Non-specified argument values for CU operation should be supplemented here, 
 * not in controller class (e.g. createdAt, updatedAt, etc.).
 * 
 */
@Service
public class VocabServiceImpl implements VocabService {
    private final VocabRepository vocabRepository;

    @Autowired
    VocabServiceImpl(VocabRepository vocabRepository) {
        this.vocabRepository = vocabRepository;
    }

    /**
     * Uses a reading method to extract all vocabularies data.
     */
    @Override
    public List<VocabEntity> getAllVocabs() {
        return vocabRepository.findAll();
    }

    /**
     * Uses a creating method to insert one new record.
     * Non-specified argument values are assigned here as follows:
     * - exampleEn: empty
     * - exampleJp: empty
     * - usersId: "admin"
     */
    @Override
    public VocabEntity addVocab(VocabEntity vocab) {
        if (vocab.getExampleEn() == null ) vocab.setExampleEn("");
        if (vocab.getExampleJp() == null ) vocab.setExampleJp("");
        if (vocab.getUsersId() == null) vocab.setUsersId("admin");

        vocab.setCreatedAt(LocalDateTime.now());
        vocab.setUpdatedAt(LocalDateTime.now());

        return vocabRepository.add(vocab);
    }

    /**
     * Uses a deleting method to delete one specified record.
     */
    @Override
    public void deleteVocab(Integer id) {
        vocabRepository.delete(id);
    }

    /**
     * Uses an updating method to update one specified record.
     * Only columns specified as not null in an argument are replaced into new values here.
     * In addition, Updated time is assigned here.
     */
    @Override
    public VocabEntity update(VocabEntity vocab) {
        VocabEntity newVocab = vocabRepository.findById(vocab.getId());

        if (vocab.getSpelling() != null) newVocab.setSpelling(vocab.getSpelling());
        if (vocab.getMeaning() != null) newVocab.setMeaning(vocab.getMeaning());
        if (vocab.getExampleEn() != null) newVocab.setExampleEn(vocab.getExampleEn());
        if (vocab.getExampleJp() != null) newVocab.setExampleJp(vocab.getExampleJp());
        newVocab.setUpdatedAt(LocalDateTime.now());

        vocabRepository.update(newVocab);
        
        return newVocab;
    }
    
}
