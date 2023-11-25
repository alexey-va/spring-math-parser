package ru.mfti;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.mfti.controller.SummatorController;
import ru.mfti.model.ExpressionParserModel;
import ru.mfti.model.TokenManager;
import ru.mfti.model.arithmetics.BasicArithmeticProvider;

public class MyTests {


    ExpressionParserModel model;

    @Before
    public void setup(){
        this.model = new ExpressionParserModel(new TokenManager(new BasicArithmeticProvider()));
    }

    @Test
    public void do_some_math(){
        long l1 = System.currentTimeMillis();

        Assert.assertEquals(model.parseExpression("1+2*2/2*2").get(), "5");
        Assert.assertEquals(model.parseExpression("10*2/4*3").get(), "15");
        Assert.assertEquals(model.parseExpression("1+2^3^2-5!").get(), "-55");
        Assert.assertEquals(model.parseExpression("cos(0)").get(), "1");
        Assert.assertEquals(model.parseExpression("2^2^3").get(), "64");
        Assert.assertEquals(model.parseExpression("2sin(pi/6)").get(), "1");
        Assert.assertEquals(model.parseExpression("10*2/4*3+2").get(), "17");
        Assert.assertEquals(model.parseExpression("10*2/4*3+2*3").get(), "21");
        System.out.println(System.currentTimeMillis()-l1);
    }

}
