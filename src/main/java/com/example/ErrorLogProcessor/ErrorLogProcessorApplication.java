package com.example.ErrorLogProcessor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.ErrorLogProcessor.Service.ErrorLogService;

@SpringBootApplication
public class ErrorLogProcessorApplication {

    @Value("${log.file.path}")
    private String logFilePath;
    
    private final ErrorLogService errorLogService;
    
    public ErrorLogProcessorApplication(ErrorLogService errorLogService) {
        this.errorLogService = errorLogService;
    }
    
    public static void main(String[] args) {
        SpringApplication.run(ErrorLogProcessorApplication.class, args);
    }
    
    @Bean
    public CommandLineRunner loadDataAtStartup() {
        return args -> {
            System.out.println("Starting to process log file from: " + logFilePath);
            Path path = Paths.get(logFilePath);
            
            try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    errorLogService.parseAndSaveErrorLog(line);
                }
                System.out.println("Log file processing completed.");
            } catch (IOException e) {
                System.err.println("Error reading log file: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}