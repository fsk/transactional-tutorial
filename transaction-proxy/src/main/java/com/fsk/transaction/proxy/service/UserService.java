package com.fsk.transaction.proxy.service;

import com.fsk.transaction.proxy.entity.User;
import com.fsk.transaction.proxy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @Transactional nasıl çalışır? (Proxy mekanizması)
 * 
 * Spring AOP proxy kullanır (JDK proxy / CGLIB)
 * Method çağrısı proxy'den geçerse transaction açılır
 * Transaction yönetimi method body'de değil, method çağrısı öncesi/sonrası çalışır.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * Bu method proxy üzerinden çağrıldığında transaction açılır
     * Method başında: Transaction başlatılır
     * Method sonunda: Transaction commit edilir (exception yoksa)
     */
    @Transactional
    public User createUser(String name, String email, Integer age) {
        log.info("Current transaction: {}", TransactionSynchronizationManager.getCurrentTransactionName());
        log.info("Is Transaction Active: {}", TransactionSynchronizationManager.isActualTransactionActive());
        log.info("Resource Map: {}", TransactionSynchronizationManager.getResourceMap());
        
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setAge(age);
        
        User saved = userRepository.save(user);
        log.info("User saved with ID: {} (NOTE: ID assigned but not yet committed!)", saved.getId());
        
        return saved;
    }
    
    /**
     * @Transactional olmayan method
     * Her save işlemi kendi transaction'ında çalışır (JPA repository default)
     */
    public User createUserWithoutTransaction(String name, String email, Integer age) {
        log.info("Transaction NOT present - Proxy bypass");
        
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setAge(age);
        
        // Repository method'u kendi transaction'ını açar (Spring Data JPA default)
        return userRepository.save(user);
    }
}


