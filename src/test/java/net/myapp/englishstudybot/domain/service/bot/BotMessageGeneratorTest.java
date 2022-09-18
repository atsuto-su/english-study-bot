package net.myapp.englishstudybot.domain.service.bot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BotMessageGeneratorTest {

    @Autowired
    private BotMessageGenerator botMessageGenerator;
    
    @Test
    @DisplayName("友だち追加時の送信メッセージが取得できるか検証")
    void getWelComeMessage() {

        //Arrange
        String welcomeMessageExpect = "友だち登録ありがとう！\nEnglish Study Botでは、チャット形式で英単語クイズを出題して英語学習をサポートするよ。\nさっそく「クイズ」と送信してクイズを始めよう！";

        //Act
        String welcomeMessageActual = botMessageGenerator.getWelcomeMessage();

        //Assert
        assertThat(welcomeMessageActual).isEqualTo(welcomeMessageExpect);

    }
    
}
