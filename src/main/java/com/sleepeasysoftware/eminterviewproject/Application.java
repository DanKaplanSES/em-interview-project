package com.sleepeasysoftware.eminterviewproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public Connection getConnect() throws SQLException {
        return DriverManager.
                getConnection("jdbc:h2:mem:~/test", "sa", "");
    }
}
