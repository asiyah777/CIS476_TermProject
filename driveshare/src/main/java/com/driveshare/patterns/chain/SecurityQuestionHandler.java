package com.driveshare.patterns.chain;

import com.driveshare.model.User;
import java.util.List;

public class SecurityQuestionHandler extends Handler {

    private String correctAnswer;
    private int index;

    public SecurityQuestionHandler(String correctAnswer, int index) {
        this.correctAnswer = correctAnswer;
        this.index = index;
    }

    @Override
    public boolean handle(User user, List<String> answers) {
        // Validate that we have enough answers in the list
        if (answers == null || answers.size() <= index) {
            System.out.println("Question " + (index + 1) + " failed: insufficient answers provided");
            return false;
        }

        String userAnswer = answers.get(index);
        if (userAnswer == null || !correctAnswer.equalsIgnoreCase(userAnswer.trim())) {
            System.out.println("Question " + (index + 1) + " failed");
            return false;
        }

        System.out.println("Question " + (index + 1) + " correct");

        if (next != null) {
            return next.handle(user, answers);
        }

        return true;
    }
}