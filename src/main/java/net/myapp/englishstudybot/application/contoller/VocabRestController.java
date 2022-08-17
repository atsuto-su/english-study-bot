package net.myapp.englishstudybot.application.contoller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.myapp.englishstudybot.domain.model.VocabEntity;
import net.myapp.englishstudybot.domain.service.VocabService;

@RestController
@RequestMapping("/api/vocabs")
public class VocabRestController {

    private final VocabService vocabService;
    
    @Autowired
    VocabRestController(VocabService vocabService){
        this.vocabService = vocabService;
    }

    @GetMapping("/list")
    public List<VocabEntity> listVocabs() {
        return vocabService.getAllVocabs();
    }
    
}
