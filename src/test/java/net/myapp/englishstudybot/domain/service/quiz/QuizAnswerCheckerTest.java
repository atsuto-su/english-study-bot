package net.myapp.englishstudybot.domain.service.quiz;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class QuizAnswerCheckerTest {

    @Autowired
    private QuizAnswerChecker quizAnswerChecker;

    @Test
    @DisplayName("選択式問題で正答時にtrue判定となることを検証")
    void checkSelectionQuizAnswerCorrectCase() {
        String userMessage = "(1)";
        String quizAnswer = "(1)テスト";

        boolean actual
         = quizAnswerChecker.checkSelectionQuizAnswer(userMessage, quizAnswer);

        assertThat(actual).isTrue();
    }
    
    @Test
    @DisplayName("選択式問題で誤答時にfalse判定となることを検証")
    void checkSelectionQuizAnswerIncorrectCase() {
        String userMessage = "(1)";
        String quizAnswer = "(2)テスト";

        boolean actual
         = quizAnswerChecker.checkSelectionQuizAnswer(userMessage, quizAnswer);

        assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("記述式問題で正答時にtrue判定となることを検証")
    void checkDescriptionQuizAnswerCorrectCase() {
        String userMessage = "答え";
        String quizAnswer = "答え";

        boolean actual
         = quizAnswerChecker.checkDescriptionQuizAnswer(userMessage, quizAnswer);

        assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("記述式問題で誤答時にfalse判定となることを検証")
    void checkDescriptionQuizAnswerIncorrectCase() {
        String userMessage = "答え（誤答）";
        String quizAnswer = "答え";

        boolean actual
         = quizAnswerChecker.checkDescriptionQuizAnswer(userMessage, quizAnswer);

        assertThat(actual).isFalse();
    }

}
