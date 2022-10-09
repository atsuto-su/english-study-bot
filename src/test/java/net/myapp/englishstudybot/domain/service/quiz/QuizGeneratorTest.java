package net.myapp.englishstudybot.domain.service.quiz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.random.RandomGenerator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import net.myapp.englishstudybot.domain.model.VocabEntity;
import net.myapp.englishstudybot.domain.model.quiz.QuizDto;
import net.myapp.englishstudybot.domain.model.quiz.UserQuizConfigDto;
import net.myapp.englishstudybot.domain.repository.QuizAggregationRepository;
import net.myapp.englishstudybot.domain.repository.VocabRepository;

@SpringBootTest
public class QuizGeneratorTest {

    private UserQuizConfigDto userQuizConfigDto
     = new UserQuizConfigDto(null, null, null, null, null, null);

    private QuizDto quizDtoExpected
     = new QuizDto(null, null, null, null, null, null);

    private VocabEntity vocab
     = new VocabEntity(
            1, "test", "テスト", 
            null, null,
            "admin", null, null
        );
    
    private List<VocabEntity> vocabCandidates
     = new ArrayList<>(
        Arrays.asList(
            new VocabEntity(
                2, "pen", "ペン", 
                null, null,
                "admin", null, null
            ),
            new VocabEntity(
                3, "pencil", "鉛筆", 
                null, null,
                "admin", null, null
            ),
            new VocabEntity(
                4, "eraser", "消しゴム", 
                null, null,
                "admin", null, null
            )
          )
     );

    @Autowired
    private QuizGenerator quizGenerator;

    @MockBean
    private RandomGenerator testRnd;

    @MockBean
    private VocabRepository vocabRepository;

    @MockBean
    private QuizAggregationRepository quizAggregationRepository;
    
    // setup method for Random Quiz testing
    private void setUpForRandom() {
        doReturn(vocab).when(vocabRepository).findRandom();
        doReturn(vocabCandidates).when(vocabRepository).findSomeExceptForOne(anyInt(), anyInt());
    }
    
    // *** Need to test here every time a new quiz type is added *** //
    @Test
    @DisplayName("選択可能なクイズのタイプが正しく取得できるか検証")
    void getSelectableQuizTypes() {
        List<String> expected
         = Arrays.asList("ランダム", "出題日古い", "正答率低い", "誤答");
        List<String> actual = quizGenerator.getSelectableQuizTypes();
        assertThat(actual).isEqualTo(expected);
    }

    // *** Test for Random Quiz *** //
    @Test
    @DisplayName("生成されるクイズDTOが正しいか検証（ランダム-英訳-記述式の問題）")
    void generateQuizRandomJpQuizDescriptionCase() {
        //Arrange
        setUpForRandom();
        userQuizConfigDto.setQuizType("ランダム");
        userQuizConfigDto.setIsJpQuestionQuiz(true);
        userQuizConfigDto.setIsDescriptionQuiz(true);
        quizDtoExpected.setTargetVocabId(vocab.getId());
        quizDtoExpected.setQuizWord(vocab.getMeaning());
        quizDtoExpected.setQuizAnswer(vocab.getSpelling());

        //Act
        QuizDto quizDtoActual = quizGenerator.generateQuiz(userQuizConfigDto);

        //Assert
        assertThat(quizDtoActual).usingRecursiveComparison().isEqualTo(quizDtoExpected);
        verify(vocabRepository, times(1)).findRandom();

    }

    @Test
    @DisplayName("生成されるクイズDTOが正しいか検証（ランダム-英訳-選択式の問題）")
    void generateQuizRandomJpQuizSelectionCase() {
        //Arrange
        setUpForRandom();
        userQuizConfigDto.setQuizType("ランダム");
        userQuizConfigDto.setIsJpQuestionQuiz(true);
        userQuizConfigDto.setIsDescriptionQuiz(false);
        quizDtoExpected.setTargetVocabId(vocab.getId());
        quizDtoExpected.setQuizWord(vocab.getMeaning());
        quizDtoExpected.setQuizAnswer(vocab.getSpelling());
        List<String> answerCandidates
         = vocabCandidates.stream().map(item -> item.getSpelling()).toList();
        quizDtoExpected.setAnswerCandidates(answerCandidates);

        //Act
        QuizDto quizDtoActual = quizGenerator.generateQuiz(userQuizConfigDto);

        //Assert
        assertThat(quizDtoActual).usingRecursiveComparison().isEqualTo(quizDtoExpected);
        verify(vocabRepository, times(1)).findRandom();
        verify(vocabRepository, times(1)).findSomeExceptForOne(
                                        vocabCandidates.size(), 
                                        vocab.getId()
                                    );

    }

    @Test
    @DisplayName("生成されるクイズDTOが正しいか検証（ランダム-和訳-記述式の問題）")
    void generateQuizRandomEnQuizDescriptionCase() {
        //Arrange
        setUpForRandom();
        userQuizConfigDto.setQuizType("ランダム");
        userQuizConfigDto.setIsJpQuestionQuiz(false);
        userQuizConfigDto.setIsDescriptionQuiz(true);
        quizDtoExpected.setTargetVocabId(vocab.getId());
        quizDtoExpected.setQuizWord(vocab.getSpelling());
        quizDtoExpected.setQuizAnswer(vocab.getMeaning());
        //Act
        QuizDto quizDtoActual = quizGenerator.generateQuiz(userQuizConfigDto);

        //Assert
        assertThat(quizDtoActual).usingRecursiveComparison().isEqualTo(quizDtoExpected);
        verify(vocabRepository, times(1)).findRandom();
    }

    @Test
    @DisplayName("生成されるクイズDTOが正しいか検証（ランダム-和訳-選択式の問題）")
    void generateQuizRandomEnQuizSelectionCase() {
        //Arrange
        setUpForRandom();
        userQuizConfigDto.setQuizType("ランダム");
        userQuizConfigDto.setIsJpQuestionQuiz(false);
        userQuizConfigDto.setIsDescriptionQuiz(false);
        quizDtoExpected.setTargetVocabId(vocab.getId());
        quizDtoExpected.setQuizWord(vocab.getSpelling());
        quizDtoExpected.setQuizAnswer(vocab.getMeaning());
        List<String> answerCandidates
         = vocabCandidates.stream().map(item -> item.getMeaning()).toList();
        quizDtoExpected.setAnswerCandidates(answerCandidates);

        //Act
        QuizDto quizDtoActual = quizGenerator.generateQuiz(userQuizConfigDto);

        //Assert
        assertThat(quizDtoActual).usingRecursiveComparison().isEqualTo(quizDtoExpected);
        verify(vocabRepository, times(1)).findRandom();
        verify(vocabRepository, times(1)).findSomeExceptForOne(
                                        vocabCandidates.size(), 
                                        vocab.getId()
                                    );

    }

    // Note that tests for other quiz types are covereed by integration tests rather than unit tests
    // since unit tests are not effective to validate logics in those methods.


    // *** Test for non-defined Quiz *** //
    @Test
    @DisplayName("定義のないクイズ種類が指定された場合は例外をスロー")
    void generateQuizNondefinedQuizThrowsException() {
        //Arrange
        userQuizConfigDto.setQuizType("未定義のクイズ種類");
        String errorMsgExpected = "定義されていないクイズの種類です";

        //Act and Assert
        assertThatThrownBy( () -> quizGenerator.generateQuiz(userQuizConfigDto) )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(errorMsgExpected);

    }


}
