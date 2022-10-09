package net.myapp.englishstudybot.domain.service.bot;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import net.myapp.englishstudybot.domain.model.quiz.QuizDto;
import net.myapp.englishstudybot.domain.model.quiz.UserQuizConfigDto;

@SpringBootTest
class BotMessageGeneratorTest {

    private UserQuizConfigDto userQuizConfigDto
     = new UserQuizConfigDto(null, null, null, null, null, null);
    
    private QuizDto quizDto
     = new QuizDto(null, null, null, null, null, null);

    @Autowired
    private BotMessageGenerator botMessageGenerator;

    @Test
    @DisplayName("友だち追加時の送信メッセージを正しく取得できるか検証")
    void getWelComeMessage() {

        //Arrange
        String expected = "友だち登録ありがとう！\n英語学習クイズボットでは、チャット形式で英単語クイズを出題して英語学習をサポートするよ。\nさっそく「クイズ」と送信してクイズを始めよう！";

        //Act
        String actual = botMessageGenerator.getWelcomeMessage();

        //Assert
        assertThat(actual).isEqualTo(expected);

    }

    @Test
    @DisplayName("クイズ開始メッセージを正しく取得できるか検証")
    void getQuizStartMessage() {

        //Arrange
        String expected = "クイズを開始するには「クイズ」と送信しよう！";

        //Act
        String actual = botMessageGenerator.getQuizStartMessage();

        //Assert
        assertThat(actual).isEqualTo(expected);

    }

    @Test
    @DisplayName("クイズ種類選択メッセージを正しく取得できるか検証")
    void getTypeSelectMessage() {

        //Arrange
        String expected = "出題するクイズのタイプを選択しよう！\nクイズを中断したい場合は「中断」と送信しよう。";

        //Act
        String actual = botMessageGenerator.getTypeSelectMessage();

        //Assert
        assertThat(actual).isEqualTo(expected);

    }

    @Test
    @DisplayName("クイズ問題文と回答を正しく生成できるか検証（英訳-記述式問題）")
    void generateQuizAndAnswerMessagesJpDescriptionQuiz() {
        //Arrange
        userQuizConfigDto.setIsJpQuestionQuiz(true);
        userQuizConfigDto.setIsDescriptionQuiz(true);
        String quizWord = "テスト";
        String quizAnswer = "test";
        String quizMessage
         =  String.format(
                """
                つぎの意味の英単語は？
                　%s""",
                quizWord
            );

        quizDto.setQuizWord(quizWord);
        quizDto.setQuizAnswer(quizAnswer);

        //Act
        QuizDto actual
         = botMessageGenerator
            .generateQuizAndAnswerMessages(quizDto, userQuizConfigDto);

        //Assert
        assertThat(actual.getQuizMessage()).isEqualTo(quizMessage);
        assertThat(actual.getAnswerMessage()).isEqualTo(quizAnswer);


    }

    @Test
    @DisplayName("クイズ問題文と回答を正しく生成できるか検証（和訳-記述式問題）")
    void generateQuizAndAnswerMessagesEnDescriptionQuiz() {
        //Arrange
        userQuizConfigDto.setIsJpQuestionQuiz(false);
        userQuizConfigDto.setIsDescriptionQuiz(true);
        String quizWord = "test";
        String quizAnswer = "テスト";
        String quizMessage
         =  String.format(
                """
                つぎの英単語の意味は？
                　%s""",
                quizWord
            );

        quizDto.setQuizWord(quizWord);
        quizDto.setQuizAnswer(quizAnswer);

        //Act
        QuizDto actual
         = botMessageGenerator
            .generateQuizAndAnswerMessages(quizDto, userQuizConfigDto);

        //Assert
        assertThat(actual.getQuizMessage()).isEqualTo(quizMessage);
        assertThat(actual.getAnswerMessage()).isEqualTo(quizAnswer);

    }

    @Test
    @DisplayName("クイズ問題文と回答を正しく生成できるか検証（選択式問題）")
    void generateQuizAndAnswerMessagesEnSelectionQuiz() {
        //Arrange
        userQuizConfigDto.setIsJpQuestionQuiz(false);
        userQuizConfigDto.setIsDescriptionQuiz(false);
        String quizWord = "test";
        String quizAnswer = "テスト";
        List<String> answerCandidates
         = new ArrayList<>(
                Arrays.asList("選択肢_ダミー1", "選択肢_ダミー2", "選択肢_ダミー3")
            );
        String selectionQuizMsgFormat
         =  """
                つぎの英単語の意味は？
                　%s
                [選択肢]
                (1)%s 
                (2)%s
                (3)%s
                (4)%s""";

        quizDto.setQuizWord(quizWord);
        quizDto.setQuizAnswer(quizAnswer);
        quizDto.setAnswerCandidates(answerCandidates);

        //Act
        QuizDto actual
         = botMessageGenerator
            .generateQuizAndAnswerMessages(quizDto, userQuizConfigDto);

        //Arrange after Act
        //set expected value after acting because random is used in the method
        List<String> stringFormatArgs = answerCandidates.stream().collect(Collectors.toList());
        stringFormatArgs.add(0, quizWord);
        String regex = "\\(|\\)";
        int answerNo = Integer.parseInt(actual.getAnswerMessage().split(regex, 0)[1]);
        stringFormatArgs.add(answerNo, quizAnswer);
        String quizMessageExpected = String.format(selectionQuizMsgFormat, stringFormatArgs.toArray());

        //Assert
        assertThat(actual.getQuizMessage()).isEqualTo(quizMessageExpected);
        assertThat(actual.getAnswerMessage().endsWith(quizDto.getQuizAnswer())).isTrue();
        assertThat(actual.getQuizMessage().contains(actual.getAnswerMessage())).isTrue();

    }

    @Test
    @DisplayName("クイズ回答時のクイックリプライ用Prfixesを正しく生成できるか検証")
    void generateSelectionQuizPrefixes() {
        int itemNum = 4;
        List<String> expected = new ArrayList<>(Arrays.asList("(1)", "(2)", "(3)", "(4)"));

        List<String> actual = botMessageGenerator.generateSelectionQuizPrefixes(itemNum);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("クイズ正解時のメッセージを正しく取得できるか検証")
    void getCorrectMessage() {
        String expected = "正解！";

        String actual = botMessageGenerator.getCorrectMessage();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("クイズ不正解時のメッセージを正しく生成できるか検証")
    void generateIncorrectMessage() {
        String quizAnswer = "テスト";
        String expected = "残念、はずれ！\n答えは・・・\n　" + quizAnswer + "\nでした！";

        String actual = botMessageGenerator.generateIncorrectMessage(quizAnswer);

        assertThat(actual).isEqualTo(expected);
    }
    
}
