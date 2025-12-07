package com.fsk.transaction.selfinvocation.service;

import com.fsk.transaction.selfinvocation.entity.Product;
import com.fsk.transaction.selfinvocation.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ÇÖZÜM 3: Ayrı service'e taşımak
 * Bu en temiz çözümdür - Single Responsibility Principle
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductInnerService {
    
    private final ProductRepository productRepository;
    
    @Transactional
    public void saveProduct(String name, Double price) {
        log.info("ProductInnerService.saveProduct - Transaction VAR (Ayrı service)");
        log.info("Current transaction: {}", 
            org.springframework.transaction.support.TransactionSynchronizationManager.getCurrentTransactionName());
        
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setStock(100);
        
        productRepository.save(product);
    }
}


