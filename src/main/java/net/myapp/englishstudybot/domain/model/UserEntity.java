package net.myapp.englishstudybot.domain.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserEntity {
   private String id;
   private Boolean isSelfWordOnly;
   private Boolean isExampleQuiz;
   private Boolean isJpQuestionQuiz;
   private Boolean isDescriptionQuiz;
   private Integer quizStatus;
   private Integer lastVocabulariesId;
   private String lastQuizSentence;
   private String lastQuizAnswer;
   private LocalDateTime createdAt;
   private LocalDateTime updatedAt; 

	public UserEntity(String id) {
		this.id = id;
		this.quizStatus = QuizStateName.WAITING_START.getCode();
      this.isSelfWordOnly = false;
      this.isExampleQuiz = false;
      this.isJpQuestionQuiz = false;
      this.isDescriptionQuiz = false;
	}

}
