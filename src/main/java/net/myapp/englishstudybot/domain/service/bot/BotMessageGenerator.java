package net.myapp.englishstudybot.domain.service.bot;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;

/**
 * BotMessageGenerator provides bot messages.
 * All messages sent by the bot should be generated in this class.
 */
@Component
public class BotMessageGenerator {

    private final MessageSource messageSource;
    private final String WELCOME_MESSAGE;

    @Autowired
    BotMessageGenerator(MessageSource messageSource) {
        this.messageSource = messageSource;
        // TO BE IMPROVED IN THE FUTURE:
        // might be better to obtain bot name by using LINE API (https://developers.line.biz/ja/reference/messaging-api/#get-bot-info)
        MessageSourceResolvable botName = new DefaultMessageSourceResolvable("bot.name");
        MessageSourceResolvable quizStartMessage = new DefaultMessageSourceResolvable("user.quizStartMessage");
        this.WELCOME_MESSAGE = this.messageSource.getMessage("bot.welcomeMessage", new MessageSourceResolvable[] {botName, quizStartMessage}, Locale.JAPAN);
    }

    /**
     * Gets a welcome message to be sent to a new follower.
     * 
     * @return welcome message
     */
    public String getWelcomeMessage() {
        return WELCOME_MESSAGE;
    }

}
