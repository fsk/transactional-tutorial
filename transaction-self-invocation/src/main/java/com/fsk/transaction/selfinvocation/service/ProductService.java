package com.fsk.transaction.selfinvocation.service;

import com.fsk.transaction.selfinvocation.entity.Product;
import com.fsk.transaction.selfinvocation.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Self-invocation problemi ve çözümleri
 * 
 * Problem: Aynı class içindeki bir @Transactional method neden çalışmaz?
 * Çünkü inner() çağrısı proxy üzerinden değil, transaction açılmaz
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ApplicationContext applicationContext;

    @EventListener(ApplicationReadyEvent.class)
    public void inspect() {
        ProductService service = applicationContext.getBean(ProductService.class);

        log.info("======= ProductService PROXY INFO (CTX) =======");
        log.info("Is AOP Proxy: {}", AopUtils.isAopProxy(service));
        log.info("Is JDK Proxy: {}", AopUtils.isJdkDynamicProxy(service));
        log.info("Is CGLIB Proxy: {}", AopUtils.isCglibProxy(service));
        log.info("Actual class: {}", service.getClass().getName());
        log.info("==============================================");
    }

    
    /**
     * PROBLEM: Self-invocation
     * outer() method'u inner()'ı çağırıyor ama inner()'daki @Transactional çalışmaz
     * Çünkü this.inner() çağrısı proxy'yi bypass eder
     */
    public void outerMethod(String name, Double price) {
        log.info("outerMethod called - Transaction NOT present");
        // Bu çağrı proxy üzerinden geçmez, transaction açılmaz!
        innerMethod(name, price);
    }
    
    @Transactional
    public void innerMethod(String name, Double price) {
        log.info("innerMethod called - But transaction did not open because of self-invocation!");
        log.info("Current transaction: {}", TransactionSynchronizationManager.getCurrentTransactionName());
        log.info("Current transaction is Active: {}", TransactionSynchronizationManager.isActualTransactionActive());

        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setStock(100);
        
        productRepository.save(product);
    }
    
    /**
     * ÇÖZÜM 1: @Transactional'ı outer metoda koymak
     */
    @Transactional
    public void outerMethodWithTransaction(String name, Double price) {
        log.info("outerMethodWithTransaction - Transaction present");
        log.info("Current transaction: {}", TransactionSynchronizationManager.getCurrentTransactionName());
        log.info("Current transaction is Active: {}", TransactionSynchronizationManager.isActualTransactionActive());
        
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setStock(100);
        
        productRepository.save(product);
        
        // Artık inner method çağrısı da aynı transaction içinde
        innerMethodInSameTransaction(name + " - Inner", price * 2);
    }
    
    @Transactional
    public void innerMethodInSameTransaction(String name, Double price) {
        log.info("innerMethodInSameTransaction - In the same transaction");
        log.info("Current transaction: {}", TransactionSynchronizationManager.getCurrentTransactionName());
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setStock(50);
        productRepository.save(product);
    }
    
    /**
     * ÇÖZÜM 2: ApplicationContext üzerinden proxy çağrısı
     */
    public void outerMethodWithProxyCall(String name, Double price) {
        log.info("outerMethodWithProxyCall - Call via proxy");
        
        // Proxy'yi al ve onun üzerinden çağır
        ProductService self = applicationContext.getBean(ProductService.class);
        self.innerMethod(name, price);
    }
    
    /**
     * ÇÖZÜM 3: Ayrı service'e taşımak (EN İYİ ÇÖZÜM)
     * Bu çözüm ProductInnerService'de gösterilecek
     */
}


