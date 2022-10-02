package net.myapp.englishstudybot.domain.service.quiz;

import net.myapp.englishstudybot.domain.service.bot.LineBotAgent;

/**
 * QuizService is an interface which provides quiz bot service.
 */
public interface QuizService {

    /**
     * Provides a quiz service.
     * 
     * @param lineBotAgent an LineBotAgent instance which contains information 
     * of a specific user who sent a message to this bot
     */
    public void provideQuizService(LineBotAgent lineBotAgent);
    
}
