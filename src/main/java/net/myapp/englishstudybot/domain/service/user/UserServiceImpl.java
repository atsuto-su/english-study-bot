package net.myapp.englishstudybot.domain.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.myapp.englishstudybot.domain.model.QuizStateName;
import net.myapp.englishstudybot.domain.model.UserEntity;
import net.myapp.englishstudybot.domain.repository.UserRepository;
import net.myapp.englishstudybot.domain.service.bot.BotMessageGenerator;
import net.myapp.englishstudybot.domain.service.bot.LineBotAgent;

@Slf4j
@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final BotMessageGenerator botMessageGenerator;

    @Autowired
    UserServiceImpl(UserRepository userRepository, BotMessageGenerator botMessageGenerator) {
        this.userRepository = userRepository;
        this.botMessageGenerator = botMessageGenerator;
    }

    /**
     * adds the user into users table who follows this bot
     * then sends a welcome message to the user.
     */
    @Override
    public void addUser(LineBotAgent lineBotAgent){
        log.info("START: UserServiceImpl#addUser");
        UserEntity user = new UserEntity(lineBotAgent.getLineUserId());
        user.setQuizStatus(QuizStateName.WAITING_START.getCode());
        userRepository.add(user);

        boolean isReplySucceeded;
        isReplySucceeded = lineBotAgent.replyMessage(botMessageGenerator.getWelcomeMessage());

        if (! isReplySucceeded) {
            log.error("Sending welcome message failed.");
        }
        log.info("END: UserServiceImpl#addUser");
    }

    /**
     * deletes the user from users table who unfollows this bot.
     */
    @Override
    public void deleteUser(String userId){
        log.info("START: UserServiceImpl#deleteUser");
        userRepository.delete(userId);
        log.info("END: UserServiceImpl#deleteUser");
    }
  
}
