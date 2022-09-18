package net.myapp.englishstudybot.domain.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserEntity {
   String id;
   Boolean isSelfWordOnly;
   Boolean isExampleQuiz;
   Boolean isAnswerJpQuiz;
   Boolean isDescriptionQuiz;
   Integer quizStatus;
   Integer lastVocabulariesId;
   String lastQuizSentence;
   String lastQuizAnswer;
   LocalDateTime createdAt;
   LocalDateTime updatedAt; 

	public UserEntity(String id) {
		this.id = id;
		this.quizStatus = QuizStateName.WAITING_START.getCode();
      this.isSelfWordOnly = false;
      this.isExampleQuiz = false;
      this.isAnswerJpQuiz = false;
      this.isDescriptionQuiz = false;
	}

}
