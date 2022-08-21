package net.myapp.englishstudybot.domain.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.myapp.englishstudybot.domain.model.VocabEntity;
import net.myapp.englishstudybot.domain.repository.VocabRepository;

/**
 * VocabServiceImpl is a service class which implements VocabService interface.
 * 
 * NOTE:
 * This class was prepared for uncertain functional extensions in the future.
 * Currently VocabServiceImpl just uses VocabRepository methods and seems to be unnecessary,
 * but I could not deny the possibility that some process will be necessary before 
 * using repository methods in the future.
 */
@Service
public class VocabServiceImpl implements VocabService {
    private final VocabRepository vocabRepository;

    @Autowired
    VocabServiceImpl(VocabRepository vocabRepository) {
        this.vocabRepository = vocabRepository;
    }

    @Override
    public List<VocabEntity> getAllVocabs() {
        return vocabRepository.findAll();
    }

    @Override
    public VocabEntity addVocab(VocabEntity vocab) {
        return vocabRepository.add(vocab);
    }

    @Override
    public void deleteVocab(Integer id) {
        vocabRepository.delete(id);
    }
    
}
