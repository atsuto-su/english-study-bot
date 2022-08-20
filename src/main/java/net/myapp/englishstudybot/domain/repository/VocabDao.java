package net.myapp.englishstudybot.domain.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import net.myapp.englishstudybot.domain.model.VocabEntity;

@Repository
public class VocabDao implements VocabRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    VocabDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

   @Override
    public List<VocabEntity> findAll() {
        String query = "SELECT * FROM vocabularies";
        List<Map<String, Object>> vocabItems = jdbcTemplate.queryForList(query);
        List<VocabEntity> vocabLists = vocabItems.stream()
                                        .map( (row) -> new VocabEntity(
                                            (Integer) row.get("id"),
                                            (String) row.get("spelling"),
                                            (String) row.get("meaning"),
                                            (String) row.get("example_en"),
                                            (String) row.get("example_jp"),
                                            (String) row.get("users_id"),
                                            ((Timestamp) row.get("created_at")).toLocalDateTime(),
                                            ((Timestamp) row.get("updated_at")).toLocalDateTime()
                                        ))
                                        .toList();
        return vocabLists;
    }

   @Override
    public VocabEntity add(VocabEntity vocab) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                                        .withTableName("vocabularies")
                                        .usingGeneratedKeyColumns("id");
        SqlParameterSource param = new BeanPropertySqlParameterSource(vocab);
        Number key = insert.executeAndReturnKey(param);
        vocab.setId(key.intValue());
        return vocab;
    }

} 
