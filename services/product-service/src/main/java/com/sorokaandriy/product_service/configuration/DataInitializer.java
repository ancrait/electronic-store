package com.sorokaandriy.product_service.configuration;

import com.sorokaandriy.product_service.model.CategoryEntity;
import com.sorokaandriy.product_service.model.ProductEntity;
import com.sorokaandriy.product_service.repository.CategoryRepository;
import com.sorokaandriy.product_service.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner seedCatalog(CategoryRepository categoryRepository, ProductRepository productRepository) {
        return args -> {
            if (categoryRepository.count() > 0) {
                return;
            }

            CategoryEntity laptops = categoryRepository.save(CategoryEntity.builder()
                    .name("Ноутбуки").slug("laptops").description("Ноутбуки та ультрабуки").build());
            CategoryEntity phones = categoryRepository.save(CategoryEntity.builder()
                    .name("Смартфони").slug("phones").description("Смартфони та аксесуари").build());
            CategoryEntity audio = categoryRepository.save(CategoryEntity.builder()
                    .name("Аудіо").slug("audio").description("Навушники та колонки").build());

            productRepository.save(ProductEntity.builder()
                    .name("ThinkPad X1 Carbon Gen 12").brand("Lenovo")
                    .description("14\" 2.8K OLED, Core Ultra 7, 32 ГБ RAM, 1 ТБ SSD")
                    .price(new BigDecimal("74999.00")).stockQuantity(7).category(laptops).build());

            productRepository.save(ProductEntity.builder()
                    .name("MacBook Air 15 M3").brand("Apple")
                    .description("15.3\" Liquid Retina, M3, 16 ГБ RAM, 512 ГБ SSD")
                    .price(new BigDecimal("69999.00")).stockQuantity(4).category(laptops).build());

            productRepository.save(ProductEntity.builder()
                    .name("Galaxy S24 Ultra 512 ГБ").brand("Samsung")
                    .description("6.8\" QHD+ AMOLED, Snapdragon 8 Gen 3, 12 ГБ RAM")
                    .price(new BigDecimal("52999.00")).stockQuantity(11).category(phones).build());

            productRepository.save(ProductEntity.builder()
                    .name("Pixel 8 Pro 256 ГБ").brand("Google")
                    .description("6.7\" LTPO OLED, Tensor G3, 12 ГБ RAM")
                    .price(new BigDecimal("39999.00")).stockQuantity(6).category(phones).build());

            productRepository.save(ProductEntity.builder()
                    .name("WH-1000XM5").brand("Sony")
                    .description("Бездротові навушники з активним шумозаглушенням, 30 год роботи")
                    .price(new BigDecimal("13999.00")).stockQuantity(15).category(audio).build());

            productRepository.save(ProductEntity.builder()
                    .name("AirPods Pro 2 USB-C").brand("Apple")
                    .description("Внутрішньоканальні навушники з ANC та адаптивним звуком")
                    .price(new BigDecimal("9999.00")).stockQuantity(20).category(audio).build());

            log.info("Catalog seeded with demo data");
        };
    }
}
