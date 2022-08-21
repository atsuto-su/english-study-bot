package net.myapp.englishstudybot.application.contoller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.myapp.englishstudybot.application.form.VocabForm;
import net.myapp.englishstudybot.domain.model.VocabEntity;
import net.myapp.englishstudybot.domain.service.VocabService;


/**
 * VocabRestController provides CRUD function api for vocabularies table.
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
     * Lists all records registered in vocabularies table.
     * 
     * @return Vocabulary entity
     */
    @GetMapping
    public List<VocabEntity> listVocabs() {
        return vocabService.getAllVocabs();
    }
    
    /**
     * Adds one vocabulary record into vocabularies table.
     * 
     * @param vocabForm
     * @return
     */
    @PostMapping
    public VocabEntity addVocab(@RequestBody VocabForm vocabForm) {
        return vocabService.addVocab(vocabForm.toEntity(null, LocalDateTime.now(), LocalDateTime.now()));
    }

    /**
     * 
     * @param id
     */
    @DeleteMapping("/{id}")
    public void deleteVocab(@PathVariable Integer id) {
        vocabService.deleteVocab(id);
    }
}
