package com.red.team.taskvisionapp.controller;


import com.red.team.taskvisionapp.model.entity.User;
import com.red.team.taskvisionapp.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Update User (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") UUID id, @Valid @RequestBody User userDetails) {
        Optional<User> userOptional = userRepository.findById(id);

        if (!userOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Jika user tidak ditemukan
        }

        User user = userOptional.get();
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setPassword(userDetails.getPassword());
        user.setRole(userDetails.getRole());
        user.setContact(userDetails.getContact());
        user.setKpi(userDetails.getKpi());
        user.setUpdatedAt(userDetails.getUpdatedAt());

        userRepository.save(user); // Simpan user yang sudah diperbarui
        return new ResponseEntity<>(user, HttpStatus.OK); // Kembalikan status OK dengan data user yang diperbarui
    }

    // Delete User (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") UUID id) {
        Optional<User> userOptional = userRepository.findById(id);

        if (!userOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Jika user tidak ditemukan
        }

        userRepository.deleteById(id); // Hapus user berdasarkan id
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Kembalikan status NO_CONTENT (tidak ada konten setelah penghapusan)
    }

}
