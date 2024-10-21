package com.progIII.ms_security.Repositories;

import com.progIII.ms_security.Models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface UserRepository extends MongoRepository<User,String> {
    //Consulta para obtener un usuario desde un email
    @Query("{'email': ?0}")
    public User getUserByEmail(String userId);

    @Query("{'_id': ?0}")
    public User getUserById(String id);
}
