package net.myapp.englishstudybot.domain.repository;

import java.util.List;

import net.myapp.englishstudybot.domain.model.VocabEntity;

public interface VocabRepository {
    public List<VocabEntity> findAll();
    // public int add(VocabEntity vocab);
    // public int delete(int id);
    // public int update(VocabEntity vocab);
}
