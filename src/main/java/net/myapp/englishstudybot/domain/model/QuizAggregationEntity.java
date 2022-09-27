package net.myapp.englishstudybot.domain.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuizAggregationEntity {

    Integer vocabulariesId;
    String usersId;
    Integer totalCountQuestionEn;
    Integer totalCountQuestionJp;
    LocalDateTime lastQuestionDatetimeEn;
    LocalDateTime lastQuestionDatetimeJp;
    Integer totalCountCorrectEn;
    Integer totalCountCorrectJp;
    Boolean isLastAnswerCorrectEn;
    Boolean isLastAnswerCorrectJp;
    Boolean isQuizDisallowed;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

}