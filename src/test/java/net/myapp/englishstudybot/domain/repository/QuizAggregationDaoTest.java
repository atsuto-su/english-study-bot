package net.myapp.englishstudybot.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
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
import net.myapp.englishstudybot.domain.model.quiz.QuizAnswerRatioDto;

@SpringBootTest
@Transactional
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class
})
@DbUnitConfiguration(dataSetLoader = CsvDataSetLoader.class)
@DatabaseSetup("/db/data/")
class QuizAggregationDaoTest {

    private static final LocalDateTime testCurrentTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    private static final MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class,
            Mockito.CALLS_REAL_METHODS);

    @Autowired
    private QuizAggregationDao quizAggregationDao;

    @BeforeAll
    static void setUpAll() {
        mockedLocalDateTime.when(LocalDateTime::now).thenReturn(testCurrentTime);
    }

    @AfterAll
    static void tearDownAll() {
        mockedLocalDateTime.close();
    }

    @Test
    @DisplayName("指定IDのクイズ結果集計データを1件取得")
    void findByIdOneQuizAggregation() {
        // Arrange
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Integer vocabulariesId = 1;
        String usersId = "admin";
        QuizAggregationEntity expected = new QuizAggregationEntity(
                vocabulariesId,
                usersId,
                1,
                2,
                LocalDateTime.parse("2022-09-19 19:00:05", formatter),
                LocalDateTime.parse("2022-09-18 08:10:00", formatter),
                3,
                4,
                false,
                true,
                false,
                LocalDateTime.parse("2022-09-01 09:00:00", formatter),
                LocalDateTime.parse("2022-09-19 19:00:05", formatter));

        // Act
        QuizAggregationEntity actual = quizAggregationDao.findById(vocabulariesId, usersId);

        // Assert
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    }

    @Test
    @DisplayName("存在しないレコードのクイズ結果集計データを取得してnullを返却")
    void findByIdNoData() {
        // Arrange
        Integer vocabulariesId = 1000;
        String usersId = "admin";

        // Act
        QuizAggregationEntity actual = quizAggregationDao.findById(vocabulariesId, usersId);

        // Assert
        assertThat(actual).isNull();
        ;

    }

    /* Test for findAllVocabids method */
    @Test
    @DisplayName("指定ユーザーの英単語IDを全取得")
    void findAllVocabIdsByUser() {
        String userId = "testUserA2";
        List<Integer> expected = Arrays.asList(new Integer[] { 1, 2, 5 });
        List<Integer> actual = quizAggregationDao.findAllVocabIdsForOneUser(userId);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("指定ユーザーのレコード0件の場合に空のリスト取得")
    void findAllVocabIdsByUserNoData() {
        String userId = "testUserA4";
        List<Integer> expected = List.of();
        List<Integer> actual = quizAggregationDao.findAllVocabIdsForOneUser(userId);
        assertThat(actual).isEqualTo(expected);
    }

    /* Test for findLeastRecentGivenVocab method */
    @Test
    @DisplayName("クイズ出題日が最も古い英単語のID取得（和訳、nullデータなし）")
    void findLeastRecentGivenVocabEnQuizWithoutNull() {
        String userId = "testUserA2";
        Boolean isJpQuestionQuiz = false;
        Integer vocabIdExpected = 2;

        Integer vocabIdActual = quizAggregationDao.findLeastRecentGivenVocab(userId, isJpQuestionQuiz);

        assertThat(vocabIdActual).isEqualTo(vocabIdExpected);
    }

    @Test
    @DisplayName("クイズ出題日が最も古い英単語のID取得（英訳、nullデータなし）")
    void findLeastRecentGivenVocabJpQuizWithoutNull() {
        String userId = "testUserA3";
        Boolean isJpQuestionQuiz = true;
        Integer vocabIdExpected = 6;

        Integer vocabIdActual = quizAggregationDao.findLeastRecentGivenVocab(userId, isJpQuestionQuiz);

        assertThat(vocabIdActual).isEqualTo(vocabIdExpected);
    }

    @Test
    @DisplayName("クイズ出題日が最も古い英単語のID取得（英訳、一部nullデータ）")
    void findLeastRecentGivenVocabJpQuizIncludingNull() {
        String userId = "testUserA2";
        Boolean isJpQuestionQuiz = true;
        Integer vocabIdExpected = 2;

        Integer vocabIdActual = quizAggregationDao.findLeastRecentGivenVocab(userId, isJpQuestionQuiz);

        assertThat(vocabIdActual).isEqualTo(vocabIdExpected);
    }

    @Test
    @DisplayName("クイズ出題日が最も古い英単語のID取得（すべてnull）")
    void findLeastRecentGivenVocabEnQuizAllNull() {
        String userId = "testUserA3";
        Boolean isJpQuestionQuiz = false;

        Integer vocabIdActual = quizAggregationDao.findLeastRecentGivenVocab(userId, isJpQuestionQuiz);

        assertThat(vocabIdActual).isNull();
    }

    @Test
    @DisplayName("クイズ出題日が最も古い英単語のID取得（対象ユーザーデータなし）")
    void findLeastRecentGivenVocabEnQuizNoUserData() {
        String userId = "testUserA4";
        Boolean isJpQuestionQuiz = false;

        Integer vocabIdActual = quizAggregationDao.findLeastRecentGivenVocab(userId, isJpQuestionQuiz);

        assertThat(vocabIdActual).isNull();
    }

    /* Test for extractOrderedByIncorrectionRatio method */
    @Test
    @DisplayName("正答率が低い順にデータ取得（和訳、正答率計算に0割なし）")
    void extractOrderedByIncorrectionRatioEnQuiz() {
        String userId = "testUserA3";
        Boolean isJpQuestionQuiz = false;
        List<QuizAnswerRatioDto> expected = Arrays.asList(new QuizAnswerRatioDto[] {
                new QuizAnswerRatioDto(
                        6,
                        0.0),
                new QuizAnswerRatioDto(
                        5,
                        10.0),
                new QuizAnswerRatioDto(
                        9,
                        20.0)
        });

        List<QuizAnswerRatioDto> actual = quizAggregationDao.extractOrderedByIncorrectionRatio(userId,
                isJpQuestionQuiz);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("正答率が低い順にデータ取得（英訳、正答率計算に0割あり）")
    void extractOrderedByIncorrectionRatioJpQuizWith0Division() {
        String userId = "testUserA3";
        Boolean isJpQuestionQuiz = true;
        List<QuizAnswerRatioDto> expected = Arrays.asList(new QuizAnswerRatioDto[] {
                new QuizAnswerRatioDto(
                        5,
                        0.0),
                new QuizAnswerRatioDto(
                        9,
                        0.0),
                new QuizAnswerRatioDto(
                        6,
                        50.0)
        });

        List<QuizAnswerRatioDto> actual = quizAggregationDao.extractOrderedByIncorrectionRatio(userId,
                isJpQuestionQuiz);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("正答率が低い順にデータ取得（対象ユーザーデータなし）")
    void extractOrderedByIncorrectionRatioNoUserData() {
        String userId = "testUserA4";
        Boolean isJpQuestionQuiz = true;
        List<QuizAnswerRatioDto> expected = List.of();

        List<QuizAnswerRatioDto> actual = quizAggregationDao.extractOrderedByIncorrectionRatio(userId,
                isJpQuestionQuiz);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    /* Test for findLastIncorrectVocabs method */
    @Test
    @DisplayName("不正解データの英単語IDを取得（和訳）")
    void findLastIncorrectVocabsEnQuiz() {
        String userId = "testUserA3";
        Boolean isJpQuestionQuiz = false;
        List<Integer> expected = Arrays.asList(new Integer[] { 6, 9 });

        List<Integer> actual = quizAggregationDao.findLastIncorrectVocabs(userId, isJpQuestionQuiz);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("不正解データの英単語IDを取得（英訳）")
    void findLastIncorrectVocabsJpQuiz() {
        String userId = "testUserA3";
        Boolean isJpQuestionQuiz = true;
        List<Integer> expected = Arrays.asList(new Integer[] { 5, 9 });

        List<Integer> actual = quizAggregationDao.findLastIncorrectVocabs(userId, isJpQuestionQuiz);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("不正解データの英単語IDを取得（不正解データなし）")
    void findLastIncorrectVocabsNoIncorrectData() {
        String userId = "testUserA2";
        Boolean isJpQuestionQuiz = false;
        List<Integer> expected = List.of();

        List<Integer> actual = quizAggregationDao.findLastIncorrectVocabs(userId, isJpQuestionQuiz);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("不正解データの英単語IDを取得（対象ユーザーデータなし）")
    void findLastIncorrectVocabsNoUserData() {
        String userId = "testUserA4";
        Boolean isJpQuestionQuiz = false;

        List<Integer> expected = List.of();

        List<Integer> actual = quizAggregationDao.findLastIncorrectVocabs(userId, isJpQuestionQuiz);

        assertThat(actual).isEqualTo(expected);
    }

    /* Test for add method */
    @Test
    @DisplayName("クイズ結果集計データを1件新規登録")
    void addOneQuizAggregation() {
        // Arrange
        Integer vocabulariesId = 5;
        String usersId = "testUserB";
        QuizAggregationEntity expected = new QuizAggregationEntity(
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
                testCurrentTime);

        // Act
        QuizAggregationEntity actual = quizAggregationDao.add(expected);

        // Assert
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("既存データと重複するクイズ結果集計データを登録して例外発生")
    void addExistingQuizAggregationThrowsException() {
        // Arrange
        Integer vocabulariesId = 1;
        String usersId = "admin";
        QuizAggregationEntity expected = new QuizAggregationEntity(
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
                testCurrentTime);
        String errorMsgExpected = "duplicate key value violates unique constraint";

        // Act and Assert
        assertThatThrownBy(() -> quizAggregationDao.add(expected))
                .isInstanceOf(DuplicateKeyException.class)
                .hasMessageContaining(errorMsgExpected);

    }

    /* Test for updateGivenQuiz method */
    @Test
    @DisplayName("和訳問題のデータ更新（最終出題回数+1して最終出題日時を現在時刻に更新）")
    void updateGivenQuizEnQuiz() {
        // Arrange
        Integer vocabulariesId = 1;
        String usersId = "testUserA";
        Boolean isJpQuestionQuiz = false;
        QuizAggregationEntity expected = quizAggregationDao.findById(vocabulariesId, usersId);
        expected.setTotalCountQuestionEn(expected.getTotalCountQuestionEn() + 1);
        expected.setLastQuestionDatetimeEn(testCurrentTime);
        expected.setUpdatedAt(testCurrentTime);

        // Act
        QuizAggregationEntity actual = quizAggregationDao.updateGivenQuiz(vocabulariesId, usersId, isJpQuestionQuiz);

        // Assert
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    }

    @Test
    @DisplayName("英訳問題のデータ更新（最終出題回数+1と最終出題日時を現在時刻に更新）")
    void updateGivenQuizJpQuiz() {
        // Arrange
        Integer vocabulariesId = 1;
        String usersId = "testUserA";
        Boolean isJpQuestionQuiz = true;
        QuizAggregationEntity expected = quizAggregationDao.findById(vocabulariesId, usersId);
        expected.setTotalCountQuestionJp(expected.getTotalCountQuestionJp() + 1);
        expected.setLastQuestionDatetimeJp(testCurrentTime);
        expected.setUpdatedAt(testCurrentTime);

        // Act
        QuizAggregationEntity actual = quizAggregationDao.updateGivenQuiz(vocabulariesId, usersId, isJpQuestionQuiz);

        // Assert
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    }

    /* Test for updateCorrectCase method */
    @Test
    @DisplayName("和訳問題正答時のデータ更新（正答数+1と正誤結果True）")
    void updateCorrectCaseEnQuiz() {
        // Arrange
        Integer vocabulariesId = 1;
        String usersId = "testUserA";
        Boolean isJpQuestionQuiz = false;
        QuizAggregationEntity expected = quizAggregationDao.findById(vocabulariesId, usersId);
        expected.setTotalCountCorrectEn(expected.getTotalCountCorrectEn() + 1);
        expected.setIsLastAnswerCorrectEn(true);
        expected.setUpdatedAt(testCurrentTime);

        // Act
        QuizAggregationEntity actual = quizAggregationDao.updateCorrectCase(vocabulariesId, usersId, isJpQuestionQuiz);

        // Assert
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    }

    @Test
    @DisplayName("英訳問題正答時のデータ更新（正答数+1と正誤結果True）")
    void updateCorrectCaseJpQuiz() {
        // Arrange
        Integer vocabulariesId = 1;
        String usersId = "testUserA";
        Boolean isJpQuestionQuiz = true;
        QuizAggregationEntity expected = quizAggregationDao.findById(vocabulariesId, usersId);
        expected.setTotalCountCorrectJp(expected.getTotalCountCorrectJp() + 1);
        expected.setIsLastAnswerCorrectJp(true);
        expected.setUpdatedAt(testCurrentTime);

        // Act
        QuizAggregationEntity actual = quizAggregationDao.updateCorrectCase(vocabulariesId, usersId, isJpQuestionQuiz);

        // Assert
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    }

    /* Test for updateIncorrectCase method */
    @Test
    @DisplayName("和訳問題誤答時のデータ更新（正誤結果False")
    void updateIncorrectCaseEnQuiz() {
        // Arrange
        Integer vocabulariesId = 2;
        String usersId = "admin";
        Boolean isJpQuestionQuiz = false;
        QuizAggregationEntity expected = quizAggregationDao.findById(vocabulariesId, usersId);
        expected.setIsLastAnswerCorrectEn(false);
        expected.setUpdatedAt(testCurrentTime);

        // Act
        QuizAggregationEntity actual = quizAggregationDao.updateIncorrectCase(vocabulariesId, usersId,
                isJpQuestionQuiz);

        // Assert
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    }

    @Test
    @DisplayName("英訳問題誤答時のデータ更新（正誤結果False")
    void updateIncorrectCaseJpQuiz() {
        // Arrange
        Integer vocabulariesId = 2;
        String usersId = "admin";
        Boolean isJpQuestionQuiz = true;
        QuizAggregationEntity expected = quizAggregationDao.findById(vocabulariesId, usersId);
        expected.setIsLastAnswerCorrectJp(false);
        expected.setUpdatedAt(testCurrentTime);

        // Act
        QuizAggregationEntity actual = quizAggregationDao.updateIncorrectCase(vocabulariesId, usersId,
                isJpQuestionQuiz);

        // Assert
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    }

    /* Test for delete method */
    @Test
    @DisplayName("クイズ結果集計データを1件削除")
    void deleteOneQuizAggregation() {
        // Arrange
        Integer vocabulariesId = 1;
        String usersId = "admin";

        // Act
        QuizAggregationEntity recordBeforeDelete = quizAggregationDao.findById(vocabulariesId, usersId);
        quizAggregationDao.delete(vocabulariesId, usersId);
        QuizAggregationEntity recordAfterDelete = quizAggregationDao.findById(vocabulariesId, usersId);

        // Assert
        assertThat(recordBeforeDelete).isNotNull();
        assertThat(recordAfterDelete).isNull();

    }

}
