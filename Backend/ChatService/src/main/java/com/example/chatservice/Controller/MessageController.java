package com.example.chatservice.Controller;

import com.example.chatservice.Client.UserServiceClient;
import com.example.chatservice.DTO.User;
import com.example.chatservice.Model.Message;
import com.example.chatservice.Service.MessageService;
import com.sun.security.auth.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/messages")
@Slf4j
public class MessageController {

    private final UserServiceClient userServiceClient;
    private final MessageService messageService;

    public MessageController(UserServiceClient userServiceClient, MessageService messageService) {
        this.userServiceClient = userServiceClient;
        this.messageService = messageService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getUserData() {
        try {
            // log.info("Received request to get all users...");
            List<User> users = userServiceClient.getAll().getBody();
            // log.info("Returning all users...");
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error retrieving doctor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getId")
    public ResponseEntity<String> getId(@CookieValue(value = "jwt") String jwt) {
        try {
            log.info("Received request to get id...");
            String id = userServiceClient.getId(jwt).getBody();
            log.info("Returning id...");
            return ResponseEntity.ok(id);
        } catch (Exception e) {
            log.error("Error retrieving id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<Message>> getMessage(@PathVariable("id") String id,
                                                    @CookieValue(value = "jwt") String jwt) {
        try {
            // log.info("Received request to get all messages...");
            User recievedUser = userServiceClient.getUserById(id).getBody();
            String senderId = userServiceClient.getId(jwt).getBody();
            if (recievedUser == null || senderId == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            List<Message> messages = messageService.getMessagesBetweenUsers(senderId, recievedUser.getUserId());
            // log.info("Returning all messages...");
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error retrieving messages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/send/{userId}")
    public ResponseEntity<Message> sendMessage(
            @RequestParam(value = "text", required = false) String text,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @PathVariable("userId") String userId,
            @CookieValue(value = "jwt") String jwt) {
        try {
            log.info("Received request to send message...");
            User receivedUser = userServiceClient.getUserById(userId).getBody();
            String senderId = userServiceClient.getId(jwt).getBody();
            if (receivedUser == null || senderId == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Message newMessage = messageService.sendMessage(senderId, receivedUser.getUserId(), text, image);
            log.info("Returning message...");
            return ResponseEntity.ok(newMessage);
        } catch (Exception e) {
            log.error("Error sending message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}