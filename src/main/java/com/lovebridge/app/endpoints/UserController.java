package com.lovebridge.app.endpoints;

import com.lovebridge.app.classes.Password;
import com.lovebridge.app.classes.User;
import com.lovebridge.app.interfaces.PasswordRepository;
import com.lovebridge.app.interfaces.UserRepository;
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
    private UserRepository userRepository;

    @Autowired
    private PasswordRepository passwordRepository;

    final public String username = "A1EC3106057E2982CECE78522BDE740C3C9F2EF6D1E8293"; // User & Pass are AES-198 encrypted strings (Corrupted)
    final public String password = "BAE60B0C316919239898AAB6F3D57DABBED6755644D3726"; // Login information for API

    private boolean Unauthenticated(String username, String password) {
        return !this.username.equals(username) || !this.password.equals(password);
    }


//  Account methods, for logging in, out, etc.
//  Maps: /create  |  Adds an account into database.        (User, Password, Username: String)
//        /remove  |  Removes account from both databases.  (User, Password, ID:       String)

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestHeader("Username") String u,
                                                          @RequestHeader("Password") String p,
                                                          @RequestBody User newUser,
                                                          @RequestParam String passcode) {

        if (Unauthenticated(u, p)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized, check login information");
        }

        Optional<User> existingUser = Optional.ofNullable(userRepository.findByUserIgnoreCase(newUser.getUser()));

        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate username");
        }

        List<User> similarUsers = userRepository.findAllByDiscordId(newUser.getId());
        int linkedIndex = similarUsers.size() + 1;

        newUser.setLinked(linkedIndex);
        newUser.setId(null);

        userRepository.save(newUser);

        Password passwordEntry = new Password();
        passwordEntry.setId(newUser.getId());
        passwordEntry.setPassword(passcode);

        passwordRepository.save(passwordEntry);

        Map<String, Object> response = new HashMap<>();

        response.put("id", newUser.getId());
        response.put("user", newUser.getUser());
        response.put("discordId", newUser.getDiscordId());
        response.put("linked", newUser.getLinked());

        return ResponseEntity.ok(newUser);
    }

    public ResponseEntity<?> removeUser(@RequestHeader("Username") String u,
                                                          @RequestHeader("Password") String p,
                                                          @RequestBody String user,
                                                          @RequestParam String passcode) {

        if (Unauthenticated(u, p)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized, check login information");
        }

        User userRecord = userRepository.findByDiscordId(user);
        Password passwordRecord = passwordRepository.findByUserIgnoreCase(user);

        if (userRecord == null || passwordRecord == null || !passwordRecord.getPassword().equals(passcode)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
        }

        userRepository.delete(userRecord);
        passwordRepository.deleteByUserIgnoreCase(user);
        
        return ResponseEntity.ok("Unlinked account for user: " + user);
    }
}
