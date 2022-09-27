package net.myapp.englishstudybot.domain.service.bot;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.quickreply.QuickReply;
import com.linecorp.bot.model.message.quickreply.QuickReplyItem;
import com.linecorp.bot.model.response.BotApiResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * LineBotAgent provides LINE Bot functionalities.
 * This class contains necessary information and functionalities 
 * for the bot to reply to a user who sent a message.
 * All functionalities relating to LINE bot should be defined in this class.
 */
@Slf4j
@Getter
@AllArgsConstructor
public class LineBotAgent {

    private final LineMessagingClient lineMessagingClient;
    private String replyToken;
    private final String lineUserId;
    private final String userMessage;

    public LineBotAgent(LineMessagingClient lineMessagingClient, String replyToken, String lineUserId){
        this(lineMessagingClient, replyToken, lineUserId, null);
    }

    /**
     * Sets the consumed reply token to null
     * This method should be called after replying.
     */
    public void setReplyTokenNullAfterReply() {
        this.replyToken = null;
    }

    /**
     * Sends a single reply message to a bot user.
     * 
     * @param message a message to be sent
     * @return sending result (true/false)
     */
    public boolean replyMessage(String message) {
        message = applyMessageSaturation(message);
        return reply(singletonList(new TextMessage(message)));
    }

    /**
     * Sends multiple reply messages to a bot user.
     * 
     * @param messages a list of multiple messages to be sent
     * @return sending result (true/false)
     */
    public boolean replyMultiMessages(List<String> messages) {
        List<String> saturatedMessages = messages.stream().map(item -> applyMessageSaturation(item)).toList();
        // List<Message> message= new ArrayList<Message>(Arrays.asList(new TextMessage("text")));
        List<Message> messagesList 
         = new ArrayList<Message>(
            saturatedMessages.stream().map(item -> new TextMessage(item)).toList()
         );
        return reply(messagesList);
    }

    /**
     * Sends a single reply message with quick reply messages to a bot user.
     * 
     * @param message a message to be sent
     * @param quickReplyItems quic reply messages sent with a message
     * @return sending result (true/false)
     */
    public boolean replyMessageWithQuickReply(String message, List<String> quickReplyItems) {
        message = applyMessageSaturation(message);
        return reply(singletonList(buildQuickReplyTextMessage(message, quickReplyItems)));
    }

    /**
     * Saturates a reply message length and replaces the end into "......" if it exceeds the maximum
     * according to the LINE Bot specification.
     * 
     * @param message original reply message
     * @return saturated reply message
     */
    private String applyMessageSaturation(String message) {
        final int MESSAGE_MAX_LENGTH = 1000;
        if (message.length() > MESSAGE_MAX_LENGTH) {
            message = message.substring(0, MESSAGE_MAX_LENGTH - 2) + "……";
            log.warn("Message saturated (original message length: {}).", message.length());
        }

        return message;
    }

    /**
     * Executes reply messaging api by using line bot library method.
     * 
     * @param messages a list of reply messaging objects
     *  (e.g. one object can contain a single message or a message and quick reply messages, etc.)
     * @return execution result (true/false)
     */
    private boolean reply(List<Message> messages) {
        try {
            log.info("START: LineBotAgent#reply");
            BotApiResponse apiResponse = lineMessagingClient
                                        .replyMessage(new ReplyMessage(replyToken, messages))
                                        .get();
            log.info("Sending reply message response: {}", apiResponse);

            boolean replyResult;
            if (apiResponse.getMessage() == null) {
                log.debug("Sending reply message result: SUCCESS");
                replyResult = true;
            } else {
                log.error("Sending reply message result: FAILURE");
                replyResult = false;
            }

            log.info("END: LineBotAgent#reply");
            return replyResult;
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Composes a TextMessage instance which includes message and quick reply items.
     * 
     * @param message message string to be directly sent from bot to users
     * @param quickReplyItems list of quick reply items to be sent with message (any number of list items are allowed)
     * @return TextMessage instance where message and quick reply items are set.
     */
    private TextMessage buildQuickReplyTextMessage(String message, List<String> quickReplyItems) {

        List<QuickReplyItem> items = new ArrayList<QuickReplyItem>(quickReplyItems.size());
        for(String quickReplyItem: quickReplyItems) {
            items.add(QuickReplyItem.builder()
                                    .action(new MessageAction(quickReplyItem , quickReplyItem))
                                    .build());
        }
        final QuickReply quickReply = QuickReply.items(items);

        return TextMessage.builder()
                            .text(message)
                            .quickReply(quickReply)
                            .build();
    }
    
}
