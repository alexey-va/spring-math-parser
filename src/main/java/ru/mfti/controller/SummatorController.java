package ru.mfti.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mfti.model.ExpressionParserModel;

import java.util.Optional;


//REST API
@RestController
public class SummatorController {

    @Autowired
    ExpressionParserModel expressionParserModel;

    @CrossOrigin(originPatterns = "*")
    @GetMapping("/make")
    public ResponseEntity<String> arithmeticExpression(@RequestParam String expression) {

        System.out.println(expression);


        return fun(expression);
    }
	
	//логику писать сюда
    public ResponseEntity fun(String str){
        Optional<String> op = expressionParserModel.parseExpression(str);
        System.out.println("Result: "+op);
        if(op.isEmpty()) return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Ошибка!");
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.TEXT_PLAIN)
                .body(op.get());
    }
}
