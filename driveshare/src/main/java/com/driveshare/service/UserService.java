package com.driveshare.service;

import com.driveshare.model.User;
import com.driveshare.patterns.chain.Handler;
import com.driveshare.patterns.chain.SecurityQuestionHandler;
import com.driveshare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Save a new user to the database during registration
    public void registerUser(String username, String email, String password,
                             String answer1, String answer2, String answer3) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setSecurityAnswer1(answer1);
        user.setSecurityAnswer2(answer2);
        user.setSecurityAnswer3(answer3);
        user.setBalance(1000.0); // initial demo balance
        userRepository.save(user);
    }

    public User authenticate(String email, String password) {
        // Try DB lookup first (registered users)
        User user = userRepository.findByEmail(email);
        if (user != null && password.equals(user.getPassword())) {
            return user;
        }
        // Fallback dummy credentials for quick demo without registration
        if ("user@example.com".equals(email) && "password".equals(password)) {
            User dummy = new User();
            dummy.setEmail(email);
            dummy.setPassword(password);
            dummy.setSecurityAnswer1("answer1");
            dummy.setSecurityAnswer2("answer2");
            dummy.setSecurityAnswer3("answer3");
            return dummy;
        }
        return null;
    }

    // Chain of Responsibility — verify three security questions before allowing reset
    public boolean resetPassword(User user, List<String> answers, String newPassword) {

        Handler q1 = new SecurityQuestionHandler(user.getSecurityAnswer1(), 0);
        Handler q2 = new SecurityQuestionHandler(user.getSecurityAnswer2(), 1);
        Handler q3 = new SecurityQuestionHandler(user.getSecurityAnswer3(), 2);

        // Build the validation chain: q1 -> q2 -> q3
        q1.setNext(q2);
        q2.setNext(q3);

        // Validate all security questions first
        boolean allQuestionsCorrect = q1.handle(user, answers);

        // If validation succeeds, perform the actual password reset
        if (allQuestionsCorrect) {
            user.setPassword(newPassword);
            return true;
        }

        return false;
    }
}
