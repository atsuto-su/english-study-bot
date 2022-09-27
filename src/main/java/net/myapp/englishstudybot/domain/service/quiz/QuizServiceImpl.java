package net.myapp.englishstudybot.domain.service.quiz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.myapp.englishstudybot.domain.model.QuizStateName;
import net.myapp.englishstudybot.domain.model.UserEntity;
import net.myapp.englishstudybot.domain.repository.UserRepository;
import net.myapp.englishstudybot.domain.service.bot.LineBotAgent;

/**
 * QuizServiceImpl is an implementation of QuizService.
 */
@Slf4j
@Service
public class QuizServiceImpl implements QuizService{

    private final UserRepository userRepository;
    private final QuizBotContext quizBotContext;

    @Autowired
    QuizServiceImpl(UserRepository userRepository, QuizBotContext quizBotContext) {
        this.userRepository = userRepository;
        this.quizBotContext = quizBotContext;
    }

    /**
     * Provides a quiz service.
     */
    @Override
    public void provideQuizService(LineBotAgent lineBotAgent) {
        log.info("START: QuizServiceImpl#provideQuizService");

        UserEntity user = userRepository.findById(lineBotAgent.getLineUserId());
        quizBotContext.setState(QuizStateName.nameOf(user.getQuizStatus()));
        quizBotContext.triggerStateMove(user, lineBotAgent);

        log.info("END: QuizServiceImpl#provideQuizService");
    }
    
}
