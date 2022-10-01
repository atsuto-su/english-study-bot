package net.myapp.englishstudybot.domain.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuizAggregationEntity {

    private Integer vocabulariesId;
    private String usersId;
    private Integer totalCountQuestionEn;
    private Integer totalCountQuestionJp;
    private LocalDateTime lastQuestionDatetimeEn;
    private LocalDateTime lastQuestionDatetimeJp;
    private Integer totalCountCorrectEn;
    private Integer totalCountCorrectJp;
    private Boolean isLastAnswerCorrectEn;
    private Boolean isLastAnswerCorrectJp;
    private Boolean isQuizDisallowed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}