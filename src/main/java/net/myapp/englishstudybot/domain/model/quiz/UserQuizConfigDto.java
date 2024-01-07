package net.myapp.englishstudybot.domain.model.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserQuizConfigDto {
	private String targetUserId;
	private Boolean isSelfWordOnly;
	private Boolean isExampleQuiz;
	private Boolean isJpQuestionQuiz;
	private Boolean isDescriptionQuiz;
	private String quizType;
}
