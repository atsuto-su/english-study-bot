package net.myapp.englishstudybot.domain.model.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserQuizConfigDto {
    String targetUserId;
	Boolean isSelfWordOnly;
	Boolean isExampleQuiz;
	Boolean isJpQuestionQuiz;
	Boolean isDescriptionQuiz;
    String quizType;
}
