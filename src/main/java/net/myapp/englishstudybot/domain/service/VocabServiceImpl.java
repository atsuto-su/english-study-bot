package net.myapp.englishstudybot.domain.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.myapp.englishstudybot.domain.model.VocabEntity;
import net.myapp.englishstudybot.domain.repository.VocabRepository;

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
    
}
