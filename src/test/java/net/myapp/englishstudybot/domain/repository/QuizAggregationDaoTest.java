package net.myapp.englishstudybot.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;

import net.myapp.englishstudybot.domain.model.QuizAggregationEntity;

@SpringBootTest
@Transactional
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class
})
@DbUnitConfiguration(
    dataSetLoader = CsvDataSetLoader.class
)
@DatabaseSetup("/db/data/")
class QuizAggregationDaoTest {

    private static final LocalDateTime testCurrentTime
     = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    @Autowired
    private QuizAggregationDao quizAggregationDao;

    @BeforeAll
    static void setUpAll() {
        MockedStatic<LocalDateTime> mock
        = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS);
        mock.when(LocalDateTime::now).thenReturn(testCurrentTime);
    }

    @Test
    @DisplayName("指定IDのクイズ結果集計データを1件取得")
    void findByIdOneQuizAggregation() {
        //Arrange
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Integer vocabulariesId = 1;
        String usersId = "admin";
        QuizAggregationEntity expected 
        = new QuizAggregationEntity(
            vocabulariesId, 
            usersId, 
            1, 
            2, 
            LocalDateTime.parse("2022-09-19 19:00:05",formatter), 
            LocalDateTime.parse("2022-09-18 08:10:00",formatter), 
            3, 
            4, 
            false, 
            true,
            false,
            LocalDateTime.parse("2022-09-01 09:00:00",formatter), 
            LocalDateTime.parse("2022-09-19 19:00:05",formatter)
        );

        //Act
        QuizAggregationEntity actual = quizAggregationDao.findById(vocabulariesId, usersId);

        //Assert
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    }
   
    @Test
    @DisplayName("存在しないレコードのクイズ結果集計データを取得してnullを返却")
    void findByIdNoData() {
        //Arrange
        Integer vocabulariesId = 5;
        String usersId = "admin";

        //Act
        QuizAggregationEntity actual = quizAggregationDao.findById(vocabulariesId, usersId);

        //Assert
        assertThat(actual).isNull();;

    }

    @Test
    @DisplayName("クイズ結果集計データを1件新規登録")
    void addOneQuizAggregation() {
        //Arrange
        Integer vocabulariesId = 5;
        String usersId = "testUserB";
        QuizAggregationEntity expected 
        = new QuizAggregationEntity(
            vocabulariesId, 
            usersId, 
            0, 
            0, 
            null,
            null,
            0, 
            0, 
            false, 
            false,
            false,
            testCurrentTime,
            testCurrentTime
        );

        //Act
        QuizAggregationEntity actual = quizAggregationDao.add(expected);

        //Assert
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("既存データと重複するクイズ結果集計データを登録して例外発生")
    void addExistingQuizAggregationThrowsException() {
        //Arrange
        Integer vocabulariesId = 1;
        String usersId = "admin";
        QuizAggregationEntity expected 
        = new QuizAggregationEntity(
            vocabulariesId, 
            usersId, 
            0, 
            0, 
            null,
            null,
            0, 
            0, 
            false, 
            false,
            false,
            testCurrentTime,
            testCurrentTime
        );
        String errorMsgExpected = "duplicate key value violates unique constraint";

        //Act and Assert
        assertThatThrownBy( () ->  quizAggregationDao.add(expected))
        .isInstanceOf(DuplicateKeyException.class)
        .hasMessageContaining(errorMsgExpected);

    }

    @Test
    @DisplayName("和訳問題のデータ更新（最終出題回数+1して最終出題日時を現在時刻に更新）")
    void updateGivenQuizEnQuiz() {
        //Arrange
        Integer vocabulariesId = 1;
        String usersId = "testUserA";
        Boolean isJpQuestionQuiz = false;
        QuizAggregationEntity expected = quizAggregationDao.findById(vocabulariesId, usersId);
        expected.setTotalCountQuestionEn(expected.getTotalCountQuestionEn() + 1);
        expected.setLastQuestionDatetimeEn(testCurrentTime);
        expected.setUpdatedAt(testCurrentTime);
 
        //Act
        QuizAggregationEntity actual
         = quizAggregationDao.updateGivenQuiz(vocabulariesId, usersId, isJpQuestionQuiz);

        //Assert
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    }

    @Test
    @DisplayName("英訳問題のデータ更新（最終出題回数+1と最終出題日時を現在時刻に更新）")
    void updateGivenQuizJpQuiz() {
        //Arrange
        Integer vocabulariesId = 1;
        String usersId = "testUserA";
        Boolean isJpQuestionQuiz = true;
        QuizAggregationEntity expected = quizAggregationDao.findById(vocabulariesId, usersId);
        expected.setTotalCountQuestionJp(expected.getTotalCountQuestionJp() + 1);
        expected.setLastQuestionDatetimeJp(testCurrentTime);
        expected.setUpdatedAt(testCurrentTime);
 
        //Act
        QuizAggregationEntity actual
         = quizAggregationDao.updateGivenQuiz(vocabulariesId, usersId, isJpQuestionQuiz);

        //Assert
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    }

    @Test
    @DisplayName("和訳問題正答時のデータ更新（正答数+1と正誤結果True）")
    void updateCorrectCaseEnQuiz() {
        //Arrange
        Integer vocabulariesId = 1;
        String usersId = "testUserA";
        Boolean isJpQuestionQuiz = false;
        QuizAggregationEntity expected = quizAggregationDao.findById(vocabulariesId, usersId);
        expected.setTotalCountCorrectEn(expected.getTotalCountCorrectEn() + 1);
        expected.setIsLastAnswerCorrectEn(true);
        expected.setUpdatedAt(testCurrentTime);
 
        //Act
        QuizAggregationEntity actual
         = quizAggregationDao.updateCorrectCase(vocabulariesId, usersId, isJpQuestionQuiz);

        //Assert
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    }

    @Test
    @DisplayName("英訳問題正答時のデータ更新（正答数+1と正誤結果True）")
    void updateCorrectCaseJpQuiz() {
        //Arrange
        Integer vocabulariesId = 1;
        String usersId = "testUserA";
        Boolean isJpQuestionQuiz = true;
        QuizAggregationEntity expected = quizAggregationDao.findById(vocabulariesId, usersId);
        expected.setTotalCountCorrectJp(expected.getTotalCountCorrectJp() + 1);
        expected.setIsLastAnswerCorrectJp(true);
        expected.setUpdatedAt(testCurrentTime);
 
        //Act
        QuizAggregationEntity actual
         = quizAggregationDao.updateCorrectCase(vocabulariesId, usersId, isJpQuestionQuiz);

        //Assert
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    }

    @Test
    @DisplayName("和訳問題誤答時のデータ更新（正誤結果False")
    void updateIncorrectCaseEnQuiz() {
        //Arrange
        Integer vocabulariesId = 2;
        String usersId = "admin";
        Boolean isJpQuestionQuiz = false;
        QuizAggregationEntity expected = quizAggregationDao.findById(vocabulariesId, usersId);
        expected.setIsLastAnswerCorrectEn(false);
        expected.setUpdatedAt(testCurrentTime);
 
        //Act
        QuizAggregationEntity actual
         = quizAggregationDao.updateIncorrectCase(vocabulariesId, usersId, isJpQuestionQuiz);

        //Assert
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    
    }

    @Test
    @DisplayName("英訳問題誤答時のデータ更新（正誤結果False")
    void updateIncorrectCaseJpQuiz() {
        //Arrange
        Integer vocabulariesId = 2;
        String usersId = "admin";
        Boolean isJpQuestionQuiz = true;
        QuizAggregationEntity expected = quizAggregationDao.findById(vocabulariesId, usersId);
        expected.setIsLastAnswerCorrectJp(false);
        expected.setUpdatedAt(testCurrentTime);
 
        //Act
        QuizAggregationEntity actual
         = quizAggregationDao.updateIncorrectCase(vocabulariesId, usersId, isJpQuestionQuiz);

        //Assert
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    }

    @Test
    @DisplayName("クイズ結果集計データを1件削除")
    void deleteOneQuizAggregation() {
        //Arrange
        Integer vocabulariesId = 1;
        String usersId = "admin";

        //Act
        QuizAggregationEntity recordBeforeDelete = quizAggregationDao.findById(vocabulariesId, usersId);
        quizAggregationDao.delete(vocabulariesId, usersId);
        QuizAggregationEntity recordAfterDelete = quizAggregationDao.findById(vocabulariesId, usersId);
        
        //Assert
        assertThat(recordBeforeDelete).isNotNull();
        assertThat(recordAfterDelete).isNull();

    }


}
