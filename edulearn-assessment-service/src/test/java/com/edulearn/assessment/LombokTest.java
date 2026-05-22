package com.edulearn.assessment;

import com.edulearn.assessment.dto.*;
import com.edulearn.assessment.entity.*;
import com.edulearn.assessment.event.NotificationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Lombok-generated Methods Coverage Test")
class LombokTest {

    @Test
    @DisplayName("Verify DTOs and Entities coverage")
    void testDataClasses() {
        // Quiz
        Quiz quiz = new Quiz();
        UUID qId = UUID.randomUUID();
        quiz.setQuizId(qId);
        quiz.setTitle("Title");
        quiz.setDescription("Desc");
        quiz.setTimeLimitMinutes(30);
        quiz.setPassingScore(60.0);
        quiz.setMaxAttempts(3);
        quiz.setIsPublished(true);
        assertEquals(qId, quiz.getQuizId());
        assertEquals("Title", quiz.getTitle());
        assertEquals("Desc", quiz.getDescription());
        assertEquals(30, quiz.getTimeLimitMinutes());
        assertEquals(60.0, quiz.getPassingScore());
        assertEquals(3, quiz.getMaxAttempts());
        assertTrue(quiz.getIsPublished());
        assertNotNull(quiz.toString());

        Quiz quiz2 = Quiz.builder().title("B").build();
        assertEquals("B", quiz2.getTitle());

        // Question
        Question question = new Question();
        question.setQuestionId(UUID.randomUUID());
        question.setText("Q");
        question.setMarks(10);
        assertEquals("Q", question.getText());
        assertEquals(10, question.getMarks());
        assertNotNull(question.toString());

        // Attempt
        Attempt attempt = new Attempt();
        attempt.setScore(100.0);
        assertEquals(100.0, attempt.getScore());
        assertNotNull(attempt.toString());

        // DTOs
        QuizRequest quizRequest = new QuizRequest();
        quizRequest.setTitle("Req");
        assertEquals("Req", quizRequest.getTitle());

        QuizResponse quizResponse = QuizResponse.builder().title("Resp").questions(List.of()).build();
        assertEquals("Resp", quizResponse.getTitle());
        assertEquals(0, quizResponse.getQuestions().size());

        QuestionRequest qReq = new QuestionRequest();
        qReq.setText("Text");
        assertEquals("Text", qReq.getText());

        QuestionResponse qResp = QuestionResponse.builder().text("Text").build();
        assertEquals("Text", qResp.getText());

        AttemptRequest aReq = new AttemptRequest();
        aReq.setStudentId(UUID.randomUUID());
        assertNotNull(aReq.getStudentId());

        AttemptResponse aResp = AttemptResponse.builder().score(50.0).build();
        assertEquals(50.0, aResp.getScore());

        AnswerRequest answerRequest = new AnswerRequest();
        answerRequest.setAnswer("A");
        assertEquals("A", answerRequest.getAnswer());

        QuizPublishRequest pubReq = new QuizPublishRequest();
        pubReq.setPublished(true);
        assertTrue(pubReq.getPublished());

        // Event
        NotificationEvent event = new NotificationEvent();
        event.setEventType("EV");
        assertEquals("EV", event.getEventType());
        
        NotificationEvent event2 = NotificationEvent.builder().eventType("EV2").build();
        assertEquals("EV2", event2.getEventType());
        assertNotNull(event2.toString());
    }
}
