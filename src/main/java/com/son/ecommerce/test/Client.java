package com.son.ecommerce.test;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Client {
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext cap = new ClassPathXmlApplicationContext("/spring.xml");

        cap.close();
    }
}
