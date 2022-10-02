package net.myapp.englishstudybot.application.contoller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.myapp.englishstudybot.application.form.VocabForm;
import net.myapp.englishstudybot.domain.model.VocabEntity;
import net.myapp.englishstudybot.domain.service.vocab.VocabService;


/**
 * VocabRestController provides CRUD api for vocabularies table.
 */
@RestController
@RequestMapping("/api/vocabs")
public class VocabRestController {

    private final VocabService vocabService;
    
    @Autowired
    VocabRestController(VocabService vocabService) {
        this.vocabService = vocabService;
    }

    /**
     * Lists all records.
     * 
     * @return a list of all vocabulary records formatted as json as defined in VocabEntity class
     */
    @GetMapping
    public List<VocabEntity> listVocabs() {
        return vocabService.getAllVocabs();
    }
    
    /**
     * Adds one new record.
     * 
     * @param vocabForm a new vocabulary record to be specified in api request body
     * @return inserted one vocabulary record formatted as json as defined in VocabEntity class
     */
    @PostMapping
    public VocabEntity addVocab(@RequestBody VocabForm vocabForm) {
        return vocabService.addVocab(vocabForm.toEntity(null));
    }

    /**
     * Deletes one existing record.
     * 
     * @param id the primary key of the record to be deleted
     */
    @DeleteMapping("/{id}")
    public void deleteVocab(@PathVariable Integer id) {
        vocabService.deleteVocab(id);
    }

    /**
     * Updates one existing record.
     * 
     * @param id the primary key of the record to be updated
     * @param vocabForm updated vocabulary column values to be specified in api request body
     * @return an updated vocabulary record formatted as json as defined in VocabEntity class.
     */
    @PatchMapping("/{id}")
    public VocabEntity updateVocab(@PathVariable Integer id, @RequestBody VocabForm vocabForm) {
        return vocabService.update(vocabForm.toEntity(id));
    }
}
