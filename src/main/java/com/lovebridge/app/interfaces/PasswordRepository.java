package com.lovebridge.app.interfaces;

import com.lovebridge.app.classes.Password;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PasswordRepository extends MongoRepository<Password, String>{
    Password findByUserIgnoreCase(String user);
    void deleteByUserIgnoreCase(String user);
}
