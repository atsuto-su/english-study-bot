package net.myapp.englishstudybot.domain.model.quiz;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuizDto {
    private Integer targetVocabId;
    private String quizWord;
    private String quizAnswer;
    private List<String> answerCandidates;
    private String quizMessage;
    private String answerMessage;
}
