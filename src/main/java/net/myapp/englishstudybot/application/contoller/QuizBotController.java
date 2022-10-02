package net.myapp.englishstudybot.application.contoller;

import org.springframework.beans.factory.annotation.Autowired;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.UnfollowEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.extern.slf4j.Slf4j;
import net.myapp.englishstudybot.domain.service.bot.LineBotAgent;
import net.myapp.englishstudybot.domain.service.quiz.QuizService;
import net.myapp.englishstudybot.domain.service.user.UserService;

/**
 * QuizBotController handles line bot events and triggers quiz bot services.
 */
@Slf4j
@LineMessageHandler
public class QuizBotController {

    private final LineMessagingClient lineMessagingClient;
    private final UserService userService;
    private final QuizService quizService;

    @Autowired
    QuizBotController(
        LineMessagingClient lineMessagingClient, 
        UserService userService,
        QuizService quizService
    ) {
        this.lineMessagingClient = lineMessagingClient;
        this.userService = userService;
        this.quizService = quizService;
    }
    
    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        log.info("START: QuizBotController#handldeTextMessageEvent");
        log.info("A user sent a message to this bot: {}", event);

        String replyToken = event.getReplyToken();
        String userId = event.getSource().getUserId();
        String userMessage = event.getMessage().getText();
        LineBotAgent lineBotAgent = new LineBotAgent(lineMessagingClient, replyToken, userId, userMessage);
        
        quizService.provideQuizService(lineBotAgent);
        log.info("EVENT: QuizBotController#handldeTextMessageEvent");
    }

    @EventMapping
    public void handleFollowEvent(FollowEvent event) {
        log.info("START: QuizBotController#handldeFollowEvent");
        log.info("A new user followed this bot: {}", event);

        String replyToken = event.getReplyToken();
        String userId = event.getSource().getUserId();
        LineBotAgent lineBotAgent = new LineBotAgent(lineMessagingClient, replyToken, userId);

        userService.addUser(lineBotAgent);
        log.info("END: QuizBotController#handldeFollowEvent");
    }

    @EventMapping
    public void handleUnFollowEvent(UnfollowEvent event) {
        log.info("START: QuizBotController#handldeUnFollowEvent");
        log.info("A user unfollowed this bot: {}", event);

        String userId = event.getSource().getUserId();

        userService.deleteUser(userId);
        log.info("END: QuizBotController#handldeUnFollowEvent");
    }

    @EventMapping
    public void handleOtherEvent(Event event) {
        log.info("Received message(ignored): {}", event);

    }
   
}
