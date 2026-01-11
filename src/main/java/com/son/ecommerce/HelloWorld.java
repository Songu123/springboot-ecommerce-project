package com.son.ecommerce;

//import org.springframework.beans.factory.DisposableBean;
//import org.springframework.beans.factory.InitializingBean;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

public class HelloWorld {
    @PostConstruct
    public void init() throws Exception
    {
        System.out.println("Bean HelloWorld has been "	+ "instantiated and I'm the " + "init() method");
    }

    @PreDestroy
    public void destroy() throws Exception
    {
        System.out.println("Container has been closed "+ "and I'm the destroy() method");
    }
}
