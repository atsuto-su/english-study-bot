package net.myapp.englishstudybot.domain.service.user;

import net.myapp.englishstudybot.domain.service.bot.LineBotAgent;

/**
 * UserService is a service interface to provides bot actions
 * when a user's following/unfollowing events happen.
 */
public interface UserService {

    /**
     * Provides bot actions when a new user follows this bot.
     * 
     * @param lineBotAgent An instance which includes the LINE user's info 
     *                     such as ID, message, etc (See the class).
     */
    public void addUser(LineBotAgent lineBotAgent);

    /**
     * Provides bot actions when an existing user unfollows.
     * 
     * @param userId the user's LINE ID who unfollows this bot.
     * 
     */
    public void deleteUser(String userId);
    
}
