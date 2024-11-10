package com.lovebridge.app.endpoints;

import com.lovebridge.app.classes.Password;
import com.lovebridge.app.classes.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/")
public class UserController {
    @Autowired
    private com.lovebridge.app.interfaces.UserRepository userRepository;

    @Autowired
    private com.lovebridge.app.interfaces.PasswordRepository passwordRepository;

    final public String username = "A1EC3106057E2982CECE78522BDE740C3C9F2EF6D1E8293"; // User & Pass are AES-198 encrypted strings (Corrupted)
    final public String password = "BAE60B0C316919239898AAB6F3D57DABBED6755644D3726"; // Login information for API

    private boolean Unauthenticated(String username, String password) {
        return !this.username.equals(username) || !this.password.equals(password);
    }


//  Account methods, for logging in, out, etc.
//  Maps: /create  |  Adds an account into database.        (User, Password, Username: String)
//        /remove  |  Removes account from both databases.  (User, Password, ID:       String)

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createUser(@RequestHeader("Username") String u,
                                                          @RequestHeader("Password") String p,
                                                          @RequestBody User newUser) {

        if (Unauthenticated(u, p)) {
            Map<String, Object> response = new HashMap<>();

            response.put("status", "error");
            response.put("message", "Unauthorized, check login information");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Optional<User> existingUser = Optional.ofNullable(userRepository.findByUserIgnoreCase(newUser.getUser()));

        if (existingUser.isPresent()) {
            Map<String, Object> response = new HashMap<>();

            response.put("status", "error");
            response.put("message", "Duplicate username");

            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        List<User> similarUsers = userRepository.findAllByDiscordId(newUser.getId());
        int linkedIndex = similarUsers.size() + 1;

        newUser.setLinked(linkedIndex);
        newUser.setId(null);

        userRepository.save(newUser);

        Map<String, Object> response = new HashMap<>();

        response.put("status", "success");
        response.put("message", "User linked successfully");

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> removeUser(@RequestHeader("Username") String u,
                                                          @RequestHeader("Password") String p,
                                                          @RequestBody String id) {

        Map<String, Object> response = new HashMap<>();

        if (Unauthenticated(u, p)) {
            response.put("status", "error");
            response.put("message", "Unauthorized, check login information");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        User user = userRepository.findByDiscordId(id);

        if (user == null) {
            response.put("status", "error");
            response.put("message", "Account not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        userRepository.delete(user);

        response.put("status", "success");
        response.put("message", "Unlinked account for ID: " + id);

        return ResponseEntity.ok(response);
    }
}
