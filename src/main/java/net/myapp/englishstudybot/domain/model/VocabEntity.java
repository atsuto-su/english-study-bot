package net.myapp.englishstudybot.domain.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VocabEntity {
    @Id
    private Integer id;
    private String spelling;
    private String meaning;
    private String exampleEn;
    private String exampleJp;
    private String usersId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
