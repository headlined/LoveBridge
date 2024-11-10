package com.lovebridge.app.endpoints;

import com.lovebridge.app.classes.User;
import com.lovebridge.app.interfaces.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class Accounts {
    @Autowired
    private UserRepository userRepository;

    final public String username = "A1EC3106057E2982CECE78522BDE740C3C9F2EF6D1E8293"; // User & Pass are AES-198 encrypted strings (Corrupted)
    final public String password = "BAE60B0C316919239898AAB6F3D57DABBED6755644D3726"; // Login information for API

    private boolean unauthenticated(String username, String password) {
        return !this.username.equals(username) || !this.password.equals(password);
    }

    @GetMapping("/search/{username}")
    public ResponseEntity<?> findUser(@PathVariable String username) {
        try {
            User information = userRepository.findByUserIgnoreCase(username);

            if (information == null) {
                Map<String, String> error = new HashMap<>();
                error.put("Error", username + " not found");

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Map<String, Object> response = new HashMap<>();

//          All sorts of information :)

            response.put("time", information.getTime());
            response.put("discordId", information.getDiscordId());
            response.put("username", information.getUser());
            response.put("command", information.getCommand());
            response.put("arg", information.getArg());
            response.put("linked", information.getLinked());
            response.put("premium", information.isPremium());

            return ResponseEntity.ok(response);
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }


//  Multi-mapped endpoint,
//  takes in GET and POST requests

    @GetMapping("/all")
    public ResponseEntity<?> allUsers() {
        try {
            List<User> commands = userRepository.findAll();

            return ResponseEntity.ok(commands);
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

//  Different from "/createUser",
//  I couldn't think of a better name

    @PostMapping("/all")
    public ResponseEntity<String> addUser(@RequestHeader("Username") String u,
                                          @RequestHeader("Password") String p,
                                          @RequestBody Map<String, String> request) {

        if (unauthenticated(u, p)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized, check login information");
        }

        String username = request.get("user");
        String passcode = request.get("passcode");
        String id = request.get("discordId");

        if (username == null || passcode == null || id == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing required fields");
        }

        try {
            User user = userRepository.findByUserIgnoreCase(username);

            if (user != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This username already exists");
            }

            User newUser = new User();
            newUser.setUser(username);
            newUser.setDiscordId(id);

            userRepository.save(newUser);

            return ResponseEntity.status(HttpStatus.CREATED).body("Account created for user: " + id);
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
}
