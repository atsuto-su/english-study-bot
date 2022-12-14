package net.myapp.englishstudybot.domain.service.quiz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import net.myapp.englishstudybot.domain.model.QuizStateName;
import net.myapp.englishstudybot.domain.model.UserEntity;
import net.myapp.englishstudybot.domain.repository.QuizAggregationRepository;
import net.myapp.englishstudybot.domain.repository.UserRepository;
import net.myapp.englishstudybot.domain.service.bot.BotMessageGenerator;
import net.myapp.englishstudybot.domain.service.bot.LineBotAgent;
import net.myapp.englishstudybot.domain.service.quiz.state.QuizBotCheckingAnswerState;
import net.myapp.englishstudybot.domain.service.quiz.state.QuizBotWaitingAnswerState;
import net.myapp.englishstudybot.domain.service.quiz.state.QuizBotWaitingStartState;
import net.myapp.englishstudybot.domain.service.quiz.state.QuizBotWaitingTypeSelectState;

@SpringBootTest
class QuizBotContextTest {

    @SpyBean
    private QuizBotWaitingStartState quizBotWaitingStartState;
    
    @SpyBean
    private QuizBotWaitingTypeSelectState quizBotWaitingTypeSelectState;

    @SpyBean
    private QuizBotWaitingAnswerState quizBotWaitingAnswerState;

    @SpyBean
    private QuizBotCheckingAnswerState quizBotCheckingAnswerState;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private QuizAggregationRepository quizAggregationRepository;

    @MockBean
    private BotMessageGenerator botMessageGenerator;

    @MockBean
    private QuizGenerator quizGenerator;

    @Mock
    private LineBotAgent lineBotAgent;

    @Autowired
    private QuizBotContext quizBotContext;

    @BeforeEach 
    void setUpEach() {
        MockitoAnnotations.openMocks(this);

        doReturn(lineBotAgent).when(quizBotWaitingStartState).entryAction(nullable(UserEntity.class), any(LineBotAgent.class));
        doReturn(lineBotAgent).when(quizBotWaitingTypeSelectState).entryAction(nullable(UserEntity.class), any(LineBotAgent.class));
        doReturn(lineBotAgent).when(quizBotWaitingAnswerState).entryAction(nullable(UserEntity.class), any(LineBotAgent.class));
        doReturn(lineBotAgent).when(quizBotCheckingAnswerState).entryAction(nullable(UserEntity.class), any(LineBotAgent.class));
    }

    /* Unit Test */
    @Test
    @DisplayName("?????????????????????????????????")
    void setState() {
        QuizStateName stateInitial = QuizStateName.WAITING_START;
        QuizStateName expectedState = QuizStateName.WAITING_TYPE_SELECT;

        quizBotContext.setState(stateInitial);
        QuizStateName actualStateBefore = quizBotContext.getState().getStateName();
        quizBotContext.setState(expectedState);
        QuizStateName actualStateAfter = quizBotContext.getState().getStateName();

        assertThat(actualStateBefore).isEqualTo(stateInitial);
        assertThat(actualStateAfter).isEqualTo(expectedState);

    }

    /* Integration Test with each QuizBotState */
    @Test
    @DisplayName("?????????????????????????????????????????????????????????????????????????????????????????????")
    void stateTransitionFromWaitingStartToWaitingTypeSelect() {
        //Arrange
        UserEntity user = null;
        String userMessage = "?????????";
        doReturn(userMessage).when(lineBotAgent).getUserMessage();
        QuizStateName initialState = QuizStateName.WAITING_START;
        QuizStateName expectedState = QuizStateName.WAITING_TYPE_SELECT;
 
        //Act
        quizBotContext.setState(initialState);
        quizBotContext.triggerStateMove(user, lineBotAgent);
        QuizStateName actualState = quizBotContext.getState().getStateName();

        //Assert
        verify(quizBotWaitingStartState, times(1)).changeState(quizBotContext, userMessage);
        verify(quizBotWaitingStartState, times(0)).entryAction(user, lineBotAgent);
        verify(quizBotWaitingTypeSelectState, times(0)).changeState(quizBotContext, userMessage);
        verify(quizBotWaitingTypeSelectState, times(1)).entryAction(user, lineBotAgent);
        assertThat(actualState).isEqualTo(expectedState);
    }

    @Test
    @DisplayName("???????????????????????????????????????????????????????????????")
    void stateStaysAtWaitingStart() {
        //Arrange
        UserEntity user = null;
        String userMessage = "???????????????????????????";
        QuizStateName initialState = QuizStateName.WAITING_START;
        QuizStateName expectedState = initialState;
        doReturn(userMessage).when(lineBotAgent).getUserMessage();
 
        //Act
        quizBotContext.setState(initialState);
        quizBotContext.triggerStateMove(user, lineBotAgent);
        QuizStateName actualState = quizBotContext.getState().getStateName();

        //Assert
        verify(quizBotWaitingStartState, times(1)).changeState(quizBotContext, userMessage);
        verify(quizBotWaitingStartState, times(1)).entryAction(user, lineBotAgent);
        verify(quizBotWaitingTypeSelectState, times(0)).changeState(quizBotContext, userMessage);
        verify(quizBotWaitingTypeSelectState, times(0)).entryAction(user, lineBotAgent);
        assertThat(actualState).isEqualTo(expectedState);
   }

    @Test
    @DisplayName("?????????????????????????????????????????????????????????????????????????????????????????????")
    void stateTransitionFromWaitingTypeSelectToWaitingAnswer() {
        //Arrange
        UserEntity user = null;
        String userMessage = "????????????";
        List<String> goNextStateMessages = new ArrayList<>(Arrays.asList(userMessage, "?????????????????????"));

        doReturn(userMessage).when(lineBotAgent).getUserMessage();
        doReturn(goNextStateMessages).when(quizGenerator).getSelectableQuizTypes();

        QuizStateName initialState = QuizStateName.WAITING_TYPE_SELECT;
        QuizStateName expectedState = QuizStateName.WAITING_ANSWER;
 
        //Act
        quizBotContext.setState(initialState);
        quizBotContext.triggerStateMove(user, lineBotAgent);
        QuizStateName actualState = quizBotContext.getState().getStateName();

        //Assert
        verify(quizBotWaitingTypeSelectState, times(1)).changeState(quizBotContext, userMessage);
        verify(quizBotWaitingTypeSelectState, times(0)).entryAction(user, lineBotAgent);
        verify(quizBotWaitingAnswerState, times(0)).changeState(quizBotContext, userMessage);
        verify(quizBotWaitingAnswerState, times(1)).entryAction(user, lineBotAgent);
        assertThat(actualState).isEqualTo(expectedState);
    }

    @Test
    @DisplayName("?????????????????????????????????????????????????????????????????????????????????????????????")
    void stateTransitionFromWaitingTypeSelectToWaitingStart() {
        //Arrange
        UserEntity user = null;
        String userMessage = "??????";
        List<String> goNextStateMessages = new ArrayList<>(Arrays.asList("????????????", "?????????????????????"));

        doReturn(userMessage).when(lineBotAgent).getUserMessage();
        doReturn(goNextStateMessages).when(quizGenerator).getSelectableQuizTypes();

        QuizStateName initialState = QuizStateName.WAITING_TYPE_SELECT;
        QuizStateName expectedState = QuizStateName.WAITING_START;
 
        //Act
        quizBotContext.setState(initialState);
        quizBotContext.triggerStateMove(user, lineBotAgent);
        QuizStateName actualState = quizBotContext.getState().getStateName();

        //Assert
        verify(quizBotWaitingTypeSelectState, times(1)).changeState(quizBotContext, userMessage);
        verify(quizBotWaitingTypeSelectState, times(0)).entryAction(user, lineBotAgent);
        verify(quizBotWaitingStartState, times(0)).changeState(quizBotContext, userMessage);
        verify(quizBotWaitingStartState, times(1)).entryAction(user, lineBotAgent);
        assertThat(actualState).isEqualTo(expectedState);
    }

    @Test
    @DisplayName("?????????????????????????????????????????????????????????????????????")
    void stateStaysAtWaitingTypeSelect() {
        //Arrange
        UserEntity user = null;
        String userMessage = "???????????????????????????";
        List<String> goNextStateMessages = new ArrayList<>(Arrays.asList("????????????", "?????????????????????"));

        doReturn(userMessage).when(lineBotAgent).getUserMessage();
        doReturn(goNextStateMessages).when(quizGenerator).getSelectableQuizTypes();

        QuizStateName initialState = QuizStateName.WAITING_TYPE_SELECT;
        QuizStateName expectedState = initialState;
 
        //Act
        quizBotContext.setState(initialState);
        quizBotContext.triggerStateMove(user, lineBotAgent);
        QuizStateName actualState = quizBotContext.getState().getStateName();

        //Assert
        verify(quizBotWaitingTypeSelectState, times(1)).changeState(quizBotContext, userMessage);
        verify(quizBotWaitingTypeSelectState, times(1)).entryAction(user, lineBotAgent);
        verify(quizBotWaitingAnswerState, times(0)).changeState(quizBotContext, userMessage);
        verify(quizBotWaitingAnswerState, times(0)).entryAction(user, lineBotAgent);
        assertThat(actualState).isEqualTo(expectedState);
   }

    @Test
    @DisplayName("????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????")
    void stateTransitionFromWaitingAnswerToWaitingStartViaCheckingAnswer() {
        //Arrange
        UserEntity user = null;
        String userMessage = null;

        doReturn(userMessage).when(lineBotAgent).getUserMessage();

        QuizStateName initialState = QuizStateName.WAITING_ANSWER;
        QuizStateName expectedState = QuizStateName.WAITING_START;
 
        //Act
        quizBotContext.setState(initialState);
        quizBotContext.triggerStateMove(user, lineBotAgent);
        QuizStateName actualState = quizBotContext.getState().getStateName();

        //Assert
        verify(quizBotWaitingAnswerState, times(1)).changeState(quizBotContext, userMessage);
        verify(quizBotWaitingAnswerState, times(0)).entryAction(user, lineBotAgent);
        verify(quizBotCheckingAnswerState, times(1)).changeState(quizBotContext, userMessage);
        verify(quizBotCheckingAnswerState, times(1)).entryAction(user, lineBotAgent);
        verify(quizBotWaitingStartState, times(0)).changeState(quizBotContext, userMessage);
        verify(quizBotWaitingStartState, times(1)).entryAction(user, lineBotAgent);
        assertThat(actualState).isEqualTo(expectedState);
    }


}
