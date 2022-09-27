package net.myapp.englishstudybot.domain.model.quiz;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuizDto {
    Integer targetVocabId;
    String quizWord;
    String quizAnswer;
    List<String> answerCandidates;
    String quizMessage;
    String answerMessage;
}
