package com.lovebridge.app.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
                                        @RequestParam String discordId,
                                        @RequestParam String passcode,
                                        @RequestParam boolean premium) {

        if (Unauthenticated(u, p)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized, check login information");
        }

        Optional<User> existingUser = Optional.ofNullable(userRepository.findByUserIgnoreCase(newUser.getUser()));

        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate username");
        }

        newUser.setDiscordId(discordId);
        newUser.setPremium(premium);

        List<User> similarUsers = userRepository.findAllByDiscordId(newUser.getDiscordId());
        int linkedIndex = similarUsers.size() + 1;

        newUser.setLinked(linkedIndex);
        newUser.setCommand("null");
        newUser.setArg("null");
        newUser.setTime("null");

        userRepository.save(newUser);

        Password passwordEntry = new Password();
        passwordEntry.setDiscordId(newUser.getDiscordId());
        passwordEntry.setUser(newUser.getUser());
        passwordEntry.setPassword(passcode);

        passwordRepository.save(passwordEntry);

        Map<String, Object> response = new HashMap<>();

        response.put("id", newUser.getId());
        response.put("user", newUser.getUser());
        response.put("discordId", newUser.getDiscordId());
        response.put("linked", newUser.getLinked());
        response.put("command", newUser.getCommand());
        response.put("arg", newUser.getArg());
        response.put("time", newUser.getTime());
        response.put("premium", newUser.isPremium());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeUser(@RequestHeader("Username") String u,
                                        @RequestHeader("Password") String p,
                                        @RequestBody String user,
                                        @RequestParam String discordId,
                                        @RequestParam String passcode) {

        if (Unauthenticated(u, p)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized, check login information");
        }

        String username;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(user);

            username = json.get("user").asText();
        } catch (JsonProcessingException error) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON format");
        }

        Optional<User> savedUsername = Optional.ofNullable(userRepository.findByUserIgnoreCaseAndDiscordId(username, discordId));
        Optional<Password> savedPassword = Optional.ofNullable(passwordRepository.findByUserIgnoreCase(username));

        if (savedUsername.isEmpty() || savedPassword.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
        }

        Map<String, Object> response = new HashMap<>();

        response.put("success", true);
        response.put("user", savedUsername.get().getUser());
        response.put("discordId", savedUsername.get().getDiscordId());

        if (!savedPassword.get().getPassword().equals(passcode)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
        }

        userRepository.delete(savedUsername.get());
        passwordRepository.deleteByUserIgnoreCase(savedUsername.get().getUser());

        return ResponseEntity.ok(response);
    }
}
