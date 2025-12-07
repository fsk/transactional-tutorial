package com.fsk.transaction.selfinvocation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class SelfInvocationApplication {

    static void main(String[] args) {
        SpringApplication.run(SelfInvocationApplication.class, args);
        //ApplicationContext context = SpringApplication.run(SelfInvocationApplication.class, args);
        //String[] beanDefinitionNames = context.getBeanDefinitionNames();
        //for (String beanDefinitionName : beanDefinitionNames) {
        //    System.out.println("Bean Name => " + beanDefinitionName);
        //}
    }

}

