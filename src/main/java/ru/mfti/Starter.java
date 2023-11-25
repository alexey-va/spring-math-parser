package ru.mfti;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.mfti.model.ExpressionParserModel;

@SpringBootApplication(scanBasePackages = "ru.mfti")
public class Starter {

    @Autowired
    ExpressionParserModel expressionParserModel;

    public static void main(String[] args) {
        SpringApplication.run(Starter.class, args);
    }
    
    
}
