package com.overload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
//Backend: 
    //cd backend && mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
    //Backend Server: http://localhost:8080/swagger-ui/index.html
//Frontend:
    //cd frontend && npm run dev
    //Frontend Server: http://localhost:5173/
//DataBase
    //docker compose up
    //Database is changed from backend/src/main/resources/db/migration
//Test
    //cd backend && mvn test