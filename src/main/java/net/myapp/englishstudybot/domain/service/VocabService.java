package net.myapp.englishstudybot.domain.service;

import java.util.List;

import net.myapp.englishstudybot.domain.model.VocabEntity;

public interface VocabService {
    public List<VocabEntity> getAllVocabs();
    // public int addVocab(VocabEntity vocab);
    // public int deleteVocab(int id);
    // public int updateVocab(VocabEntity vocab);
}
