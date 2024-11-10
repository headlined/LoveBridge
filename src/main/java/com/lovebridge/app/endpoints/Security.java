package com.lovebridge.app.endpoints;

import com.lovebridge.app.classes.Password;
import com.lovebridge.app.classes.User;
import com.lovebridge.app.interfaces.PasswordRepository;
import com.lovebridge.app.interfaces.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/")
public class Security {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordRepository passwordRepository;

    final public String localUsername = "A1EC3106057E2982CECE78522BDE740C3C9F2EF6D1E8293"; // User & Pass are AES-198 encrypted strings (Corrupted)
    final public String localPassword = "BAE60B0C316919239898AAB6F3D57DABBED6755644D3726"; // Login information for API

    private boolean unauthenticated(String username, String password) {
        return !this.localUsername.equals(username) || !this.localPassword.equals(password);
    }

    @PostMapping("/findPass")
    public ResponseEntity<String> findPass(@RequestHeader("Username") String u,
                                           @RequestHeader("Password") String p,
                                           @RequestBody Map<String, String> request) {

        if (unauthenticated(u, p)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized, check login information");
        }

//      Useful for getting information, that you don't want the public to see.
//      Types: "GET"  |  Returns the current password for the current user.
//             "REM"  |  Removes the current password from the database.
//             "DEL"  |  Inserts the new password, or replaces.

        String type = request.get("Type");                  // Specify the type of the request to run specific stuff.
        String username = request.get("User");              // Has to be "Username" so it doesn't override.
        String newPassword = request.get("NewPassword");    // The new password to replace the old one with.

        User user = userRepository.findByUserIgnoreCase(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        switch (type.toUpperCase()) {
            case "GET":
                Password storedPassword = passwordRepository.findByUserIgnoreCase(username);

                if (storedPassword != null) {
                    return ResponseEntity.ok(storedPassword.getPassword());
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Password not found");
                }

            case "REM":
                passwordRepository.deleteByUserIgnoreCase(username);

//              Success response! Lets the Discord App know everything
//              went well.

                return ResponseEntity.ok("Password removed for " + username);

            case "DEL":
                if (newPassword == null || newPassword.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("New password cannot be empty");
                }

                Password passwordRecord = passwordRepository.findByUserIgnoreCase(username);

                if (passwordRecord == null) {
                    passwordRecord = new Password();
                    passwordRecord.setUser(username);
                }

                passwordRecord.setPassword(newPassword);
                passwordRepository.save(passwordRecord);

                return ResponseEntity.ok("Password set for " + username);

            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OP type, review source for OP types :3");
        }
    }
}
