package com.lovebridge.app.interfaces;

import com.lovebridge.app.classes.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepository extends MongoRepository<User, String>{
    User findByUserIgnoreCase(String user);
    User findByDiscordId(String discordId);
    User findByUserIgnoreCaseAndDiscordId(String user, String discordId);

    List<User> findAllByDiscordId(String discordId);
}
