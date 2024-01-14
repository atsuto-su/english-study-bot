package net.myapp.englishstudybot.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;

import net.myapp.englishstudybot.domain.model.QuizStateName;
import net.myapp.englishstudybot.domain.model.UserEntity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * UserDaoTest is a test class for UserDao.
 * 
 * TO BE IMPROVED:
 * Configurations for component scanning should be imoroved (but no idea
 * currently).
 * This is because classes to be excluded from repository test (such as
 * UserServiceImpl, BotMessageGenerator)
 * are regarded as targets of component scanning (this is not actually unit
 * test).
 */
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
class UserDaoTest {

    @Autowired
    private UserDao userDao;

    private static final LocalDateTime testCurrentTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    private static final MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class,
            Mockito.CALLS_REAL_METHODS);

    @BeforeAll
    static void setUpAll() {
        // Fix time with LocalDateTime.now()
        mockedLocalDateTime.when(LocalDateTime::now).thenReturn(testCurrentTime);
    }

    @AfterAll
    static void tearDownAll() {
        mockedLocalDateTime.close();
    }

    @Test
    @DisplayName("指定IDのユーザーデータを1件取得")
    void findByIdOneUser() {
        // Arrange
        String userId = "testUserA";
        UserEntity userExpected = new UserEntity(
                userId,
                false,
                false,
                false,
                false,
                QuizStateName.WAITING_START.getCode(),
                null,
                null,
                null,
                LocalDateTime.of(2023, 9, 1, 9, 0, 0),
                LocalDateTime.of(2022, 9, 1, 9, 0, 0));

        // Act
        UserEntity userActual = userDao.findById(userId);

        // Assert
        assertThat(userActual).usingRecursiveComparison().isEqualTo(userExpected);
    }

    @Test
    @DisplayName("存在しないIDのユーザーデータ取得でnull返却")
    void findByIdNoData() {
        // Arrange
        String userId = "non-existing-id";

        // Act
        UserEntity userActual = userDao.findById(userId);

        // Assert
        assertThat(userActual).isNull();
    }

    @Test
    @DisplayName("ユーザーデータを1件登録")
    void addOneUser() {

        // Arrange
        String userId = "testUserZ";
        UserEntity userExpected = new UserEntity(
                userId,
                false,
                false,
                false,
                false,
                QuizStateName.WAITING_START.getCode(),
                null,
                null,
                null,
                testCurrentTime,
                testCurrentTime);
        // Act
        UserEntity userActual = userDao.add(new UserEntity(userId));

        // Assert
        assertThat(userActual).usingRecursiveComparison().isEqualTo(userExpected);

    }

    @Test
    @DisplayName("1ユーザーのクイズステータスを更新")
    void updateUserStatusToAnother() {

        // Arrange
        String userId = "testUserB";
        QuizStateName quizStatus = QuizStateName.WAITING_TYPE_SELECT;
        UserEntity userExpected = userDao.findById(userId);
        userExpected.setQuizStatus(quizStatus.getCode());
        userExpected.setUpdatedAt(testCurrentTime);

        // Act
        UserEntity userActual = userDao.updateUserStatus(userId, quizStatus);

        // Assert
        assertThat(userActual).usingRecursiveComparison().isEqualTo(userExpected);

    }

    /**
     * tests if only the following methods are updated.
     * last_vocabularies_id, last_quiz_sentence, last_quiz_answer, updated_at
     */
    @Test
    @DisplayName("ユーザー最終回答クイズ情報の更新")
    void updateUserQuizInfo() {

        // Arrange
        String userId = "testUserB";
        Integer vocabId = 5;
        String lastQuizSentence = "クイズhoge";
        String lastQuizAnswer = "正解はpoyo";
        UserEntity userExpected = userDao.findById(userId);
        userExpected.setLastVocabulariesId(vocabId);
        userExpected.setLastQuizSentence(lastQuizSentence);
        userExpected.setLastQuizAnswer(lastQuizAnswer);
        userExpected.setUpdatedAt(testCurrentTime);

        // Act
        UserEntity userActual = userDao.updateLastQuizInfo(userExpected);

        // Assert
        assertThat(userActual).usingRecursiveComparison().isEqualTo(userExpected);

    }

    @Test
    @DisplayName("ユーザーデータを1件削除")
    void deleteOneUser() {
        // Arrange
        String userId = "testUserA";

        // Act
        userDao.delete(userId);
        UserEntity userActual = userDao.findById(userId);

        // Assert
        assertThat(userActual).isNull();
    }

}
