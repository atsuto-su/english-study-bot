package net.myapp.englishstudybot.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VocabEntity {
    private int id;
    private String spelling;
    private String meaning;
    private String exampleEn;
    private String exampleJp;
    private String usersId;
}
