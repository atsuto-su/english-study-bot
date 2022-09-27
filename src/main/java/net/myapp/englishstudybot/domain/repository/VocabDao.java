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

import lombok.extern.slf4j.Slf4j;
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
 * 3) All add or update methods use to findById return entity, which might be better to be changed.
 *    This is because those methods access DB at least twice
 *    which could delay this class's behavior when DB records are huge.
 *    Instead, use RETURNING sentence for query to reduce DB access.
 * 4) Define table name and column names as constant values and use them
 *    instead of writing in the code directly.
 */
@Slf4j
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
        log.info("START: VocabDao#findAll");

        String query = "SELECT * FROM vocabularies";
        List<Map<String, Object>> extractedList = jdbcTemplate.queryForList(query);
        List<VocabEntity> vocabLists
         = extractedList.stream()
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

        log.info("END: VocabDao#findAll");
        return vocabLists;
    }

    /**
     * Extracts one record by executing the following SQL:
     * SELECT * FROM vocabularies WHERE id = {specified id};
     */
    @Override
    public VocabEntity findById(Integer id) {
        log.info("START: VocabDao#findById");

        VocabEntity vocabRecord;
        String query = "SELECT * FROM vocabularies WHERE id = ?";
        try {
            Map<String, Object> extractedItem = jdbcTemplate.queryForMap(query, id);
            vocabRecord
             = new VocabEntity(
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

        log.info("END: VocabDao#findById");
        return vocabRecord;
    }

    /**
     * Extracts one record by executing the following SQL:
     * SELECT * FROM vocabularies WHERE id = (SELECT id FROM vocabularies ORDER BY random() LIMIT 1);
     */
    @Override
    public VocabEntity findRandom() {
        log.info("START: VocabDao#findRandom");

        String query
         = """
            SELECT * FROM vocabularies 
            WHERE id = (
                SELECT id FROM vocabularies ORDER BY random() LIMIT 1
            )
            """;
        Map<String, Object> extractedItem = jdbcTemplate.queryForMap(query);
        VocabEntity vocabRecord
         = new VocabEntity(
            (Integer) extractedItem.get("id"),
            (String) extractedItem.get("spelling"),
            (String) extractedItem.get("meaning"),
            (String) extractedItem.get("example_en"),
            (String) extractedItem.get("example_jp"),
            (String) extractedItem.get("users_id"),
            ((Timestamp) extractedItem.get("created_at")).toLocalDateTime(),
            ((Timestamp) extractedItem.get("updated_at")).toLocalDateTime()
        );

        log.info("END: VocabDao#findRandom");
        return vocabRecord;
    }

    /**
     * Extracts some records by executing the following SQL:
     * SELECT * FROM vocabularies WHERE id in (SELECT id FROM vocabularies ORDER BY random() WHERE id != {specified id} LIMIT {specified number})"; 
     */
    @Override
    public List<VocabEntity> findSomeExceptForOne(int findNum, Integer vocabIdExcluded) {
        log.info("START: VocabDao#findSomeExceptForOne");

        String query
         = """
            SELECT * FROM vocabularies 
            WHERE id IN (
                SELECT id FROM vocabularies WHERE id != ? ORDER BY random() LIMIT ?
            )
            """;
        List<Map<String, Object>> extractedItems
         = jdbcTemplate.queryForList(query, vocabIdExcluded, findNum);
        List<VocabEntity> vocabLists
         = extractedItems.stream()
            .map(item -> new VocabEntity(
                (Integer) item.get("id"),
                (String) item.get("spelling"),
                (String) item.get("meaning"),
                (String) item.get("example_en"),
                (String) item.get("example_jp"),
                (String) item.get("users_id"),
                ((Timestamp) item.get("created_at")).toLocalDateTime(),
                ((Timestamp) item.get("updated_at")).toLocalDateTime())
            )
            .toList();

        if (vocabLists.size() < findNum) {
            throw new IllegalArgumentException(
                "The argument findNum exceeds the maximum records in the table."
            );
        }

        log.info("END: VocabDao#findSomeExceptForOne");
        return vocabLists;
    }


    /**
     * Inserts one new record by executing the following SQL:
     * INSERT INTO vocabularies {all columns} VALUES {each specified value};
     * 
     * NOTE: 
     * primary key should be null in the argument
     * because the key is automatically set by DB.
     */
    @Override
    public VocabEntity add(VocabEntity vocab) {
        log.info("START: VocabDao#add");

        SimpleJdbcInsert insert
         = new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("vocabularies")
            .usingGeneratedKeyColumns("id");
        SqlParameterSource param = new BeanPropertySqlParameterSource(vocab);

        // gets the primary key of the inserted record
        Number key = insert.executeAndReturnKey(param);

        log.info("END: VocabDao#add");
        return findById(key.intValue());
    }

    /**
     * Deletes one existing record by executing the following SQL:
     * DELETE FROM vocabularies WHERE id = {specified id};
     */
    @Override
    public void delete(Integer id) {
        log.info("START: VocabDao#delete");

        String query = "DELETE FROM vocabularies WHERE id = ?";
        jdbcTemplate.update(query, id);

        log.info("END: VocabDao#delete");
    }

    /**
     * Updates one existing record by executing the following SQL:
     * UPDATE vocabularies SET {specified column values} WHERE id = {specified id};
     */
    @Override
    public VocabEntity update(VocabEntity vocab) {
        log.info("START: VocabDao#update");

        String query
         = """
            UPDATE vocabularies SET 
                spelling=?, 
                meaning=?, 
                example_en=?, 
                example_jp=?, 
                updated_at=? 
            WHERE id = ?
            """;
        jdbcTemplate.update(
            query, 
            vocab.getSpelling(), 
            vocab.getMeaning(),
            vocab.getExampleEn(),
            vocab.getExampleJp(),
            vocab.getUpdatedAt(),
            vocab.getId()
        );

        log.info("END: VocabDao#update");
        return findById(vocab.getId());
    }

} 
