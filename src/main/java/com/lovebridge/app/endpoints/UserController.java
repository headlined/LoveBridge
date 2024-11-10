package com.lovebridge.app.endpoints;

import com.lovebridge.app.classes.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/")
public class UserController {
    @Autowired
    private com.lovebridge.app.interfaces.UserRepository userRepository;

    final public String username = "A1EC3106057E2982CECE78522BDE740C3C9F2EF6D1E8293"; // User & Pass are AES-198 encrypted strings (Corrupted)
    final public String password = "BAE60B0C316919239898AAB6F3D57DABBED6755644D3726"; // Login information for API

    private boolean Unauthenticated(String username, String password) {
        return !this.username.equals(username) || !this.password.equals(password);
    }


//  Account methods, for logging in, out, etc.
//  Maps: /create  |  Adds an account into database.        (User, Password, Username: String)
//        /remove  |  Removes account from both databases.  (User, Password, ID:       String)

    @PostMapping("/create")
    public String createUser(@RequestHeader("Username") String u,
                             @RequestHeader("Password") String p,
                             @RequestBody User newUser) {

        if (Unauthenticated(u, p)) {
            return "Unauthorized, check login information";
        }

        Optional<User> existingUser = Optional.ofNullable(userRepository.findByUserIgnoreCase(newUser.getUser()));

        if (existingUser.isPresent()) {
            return "Duplicate username";
        }

        List<User> similarUsers = userRepository.findAllByDiscordId(newUser.getDiscordId());
        int linkedIndex = similarUsers.size() + 1;

        newUser.setLinked(linkedIndex);
        newUser.setId(null);
        userRepository.save(newUser);

        return "User linked successfully!";
    }

    @PostMapping("/remove")
    public String removeUser(@RequestHeader("Username") String u,
                             @RequestHeader("Password") String p,
                             @RequestParam String id) {

        if (Unauthenticated(u, p)) {
            return "Unauthorized, check login information";
        }

        User user = userRepository.findByDiscordId(id);

        if (user == null) {
            return "Account not found";
        }

        return "Unlinked account for Discord ID: " + id;
    }
}
