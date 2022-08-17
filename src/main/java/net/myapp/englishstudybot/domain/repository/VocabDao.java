package net.myapp.englishstudybot.domain.repository;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import net.myapp.englishstudybot.domain.model.VocabEntity;

@Repository
public class VocabDao implements VocabRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    VocabDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

   /**
     * Extracts all vocabularies data from DB.
     * 
     * @return List of vocabulary entities
     */
    @Override
    public List<VocabEntity> findAll() {
        String query = "SELECT * FROM vocabularies";
        List<Map<String, Object>> vocabItems = jdbcTemplate.queryForList(query);
        List<VocabEntity> vocabLists = vocabItems.stream()
                                        .map( (row) -> new VocabEntity(
                                            (int) row.get("id"),
                                            (String) row.get("spelling"),
                                            (String) row.get("meaning"),
                                            (String) row.get("example_en"),
                                            (String) row.get("example_jp"),
                                            (String) row.get("users_id")
                                        ))
                                        .toList();
        return vocabLists;
    }

} 
