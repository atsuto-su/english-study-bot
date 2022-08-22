package net.myapp.englishstudybot.domain.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import net.myapp.englishstudybot.domain.model.VocabEntity;

/**
 * VocabDao is an implementation of VocabRepository by using JdbcTemplate.
 * 
 * ROOM FOR IMPROVEMENT :
 * 1) Use NamedSpaceJdbcTemplate Instead of using JdbcTemplate.
 *    Thereby, readability and maintainability is expected to improve.
 *    (e.g. query parameters can be specified with combination of keys and values)
 * 2) Change O/R mapper method to MyBatis or JPA, etc. instead of Spring JDBC.
 *    Thereby, maintanability is expected to improve
 *    because query and each DB parameters such as table name could be independent
 *    of repository class file.
 */
@Repository
public class VocabDao implements VocabRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    VocabDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Extracts all records by the following SQL:
     * SELECT * FROM vocabularies;
     */
    @Override
    public List<VocabEntity> findAll() {
        String query = "SELECT * FROM vocabularies";
        List<Map<String, Object>> extractedList = jdbcTemplate.queryForList(query);
        List<VocabEntity> vocabLists = extractedList.stream()
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

    /**
     * Extracts one record by executing the following SQL:
     * SELECT * FROM vocabularies WHERE id = {specified id};
     */
    @Override
    public VocabEntity findById(Integer id) {

        VocabEntity vocabRecord;

        String query = "SELECT * FROM vocabularies WHERE id = ?";
        try {
            Map<String, Object> extractedItem = jdbcTemplate.queryForMap(query, id);
            vocabRecord = new VocabEntity(
                            id,
                            (String) extractedItem.get("spelling"),
                            (String) extractedItem.get("meaning"),
                            (String) extractedItem.get("example_en"),
                            (String) extractedItem.get("example_jp"),
                            (String) extractedItem.get("users_id"),
                            ((Timestamp) extractedItem.get("created_at")).toLocalDateTime(),
                            ((Timestamp) extractedItem.get("updated_at")).toLocalDateTime()
                        );
        } catch (EmptyResultDataAccessException e) {
            vocabRecord = null;
        }

        return vocabRecord;
    }

    /**
     * Inserts one new record by executing the following SQL:
     * INSERT INTO vocabularies {all columns} VALUES {each specified value};
     */
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

    /**
     * Deletes one existing record by executing the following SQL:
     * DELETE FROM vocabularies WHERE id = {specified id};
     */
    @Override
    public void delete(Integer id) {
        String query = "DELETE FROM vocabularies WHERE id = ?";
        jdbcTemplate.update(query, id);
    }

    /**
     * Updates one existing record by executing the following SQL:
     * UPDATE vocabularies SET {specified column values} WHERE id = {specified id};
     */
    @Override
    public VocabEntity update(VocabEntity vocab) {
        String query = """
                            UPDATE vocabularies SET 
                                spelling=?, 
                                meaning=?, 
                                example_en=?, 
                                example_jp=?, 
                                updated_at=? 
                            WHERE id = ?
                        """;
        jdbcTemplate.update(query, 
                            vocab.getSpelling(), 
                            vocab.getMeaning(),
                            vocab.getExampleEn(),
                            vocab.getExampleJp(),
                            vocab.getUpdatedAt(),
                            vocab.getId());

        return vocab;

    }

} 
