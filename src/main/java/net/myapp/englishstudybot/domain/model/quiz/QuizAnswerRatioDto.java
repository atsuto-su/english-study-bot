package net.myapp.englishstudybot.domain.model.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuizAnswerRatioDto {
    private Integer vocabularyId;
    private Double answerRatio; 
}
