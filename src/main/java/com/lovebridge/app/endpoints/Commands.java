package com.lovebridge.app.endpoints;

import com.lovebridge.app.classes.User;
import com.lovebridge.app.interfaces.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/account/command")
public class Commands {
    @Autowired
    private UserRepository userRepository;

    final public String username = "A1EC3106057E2982CECE78522BDE740C3C9F2EF6D1E8293"; // User & Pass are AES-198 encrypted strings (Corrupted)
    final public String password = "BAE60B0C316919239898AAB6F3D57DABBED6755644D3726"; // Login information for API

    private boolean unauthenticated(String username, String password) {
        return !this.username.equals(username) || !this.password.equals(password);
    }

    @PostMapping
    public ResponseEntity<String> updateCommand(@RequestHeader("Username") String u,
                                                @RequestHeader("Password") String p,
                                                @RequestBody Map<String, String> request) {

        if (unauthenticated(u, p)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized, check login information");
        }

        String id = request.get("discordId");
        String command = request.get("command");
        String argument = request.get("arg");

        if (id == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Discord ID is required");
        }

        try {
            List<User> users = userRepository.findAllByDiscordId(id);

            if (users == null || users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account(s) not found");
            }

            int editedAmount = 0;

            for (User user : users) {
                user.setTime(String.valueOf(System.currentTimeMillis() / 1000));
                user.setCommand(command);
                user.setArg(argument);

                userRepository.save(user);
                editedAmount++;
            }

            return ResponseEntity.ok(String.valueOf(editedAmount));
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + error);
        }
    }

    @PostMapping("/clear")
    public ResponseEntity<String> clearCommand(@RequestHeader("Username") String u,
                                               @RequestHeader("Password") String p,
                                               @RequestBody Map<String, String> request) {

        if (unauthenticated(u, p)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized, check login information");
        }

        String id = request.get("ID");

        if (id == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Discord ID is required");
        }

        try {
            User user = userRepository.findByDiscordId(id);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
            }

            user.setTime(null);
            user.setCommand(null);
            user.setArg(null);

            userRepository.save(user);

            return ResponseEntity.ok("Command cleared successfully");
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + error);
        }
    }
}
