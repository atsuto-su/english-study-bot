package net.myapp.englishstudybot.application.form;

import lombok.Data;
import net.myapp.englishstudybot.domain.model.VocabEntity;

@Data
public class VocabForm {
    private String spelling;
    private String meaning;
    private String exampleEn;
    private String exampleJp;
    private String usersId;

    public VocabEntity toEntity(Integer id) {
        return new VocabEntity(
                id,
                this.spelling,
                this.meaning,
                this.exampleEn,
                this.exampleJp,
                this.usersId, 
                null,
                null
            );
        
    }
}
