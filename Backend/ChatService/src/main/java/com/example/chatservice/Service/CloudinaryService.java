package com.example.chatservice.Service;

import org.springframework.web.multipart.MultipartFile;


public interface CloudinaryService {
    String uploadFile(MultipartFile file, String folderName);
}