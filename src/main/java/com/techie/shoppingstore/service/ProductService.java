package com.techie.shoppingstore.service;

import com.techie.shoppingstore.dto.ProductAvailability;
import com.techie.shoppingstore.dto.ProductDto;
import com.techie.shoppingstore.model.Category;
import com.techie.shoppingstore.model.Product;
import com.techie.shoppingstore.repository.CategoryRepository;
import com.techie.shoppingstore.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Cacheable(value = "products")
    public List<ProductDto> findAll() {
        List<Product> all = productRepository.findAll();
        log.info("Found {} products..", all.size());
        return all
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ProductDto mapToDto(Product product) {
        ProductAvailability productAvailability = product.getQuantity() > 0 ? inStock() : outOfStock();
        return new ProductDto(product.getName(), product.getImageUrl(), product.getPrice(), product.getDescription(), product.getManufacturer(), productAvailability, product.getProductAttributeList());
    }

    private ProductAvailability outOfStock() {
        return new ProductAvailability("Out of Stock", "red");
    }

    private ProductAvailability inStock() {
        return new ProductAvailability("Out of Stock", "forestgreen");
    }

    @Cacheable(value = "singleProduct", key = "#productName")
    public ProductDto readOneProduct(String productName) {
        log.info("Reading Product with productName - {}", productName);
        Optional<Product> optionalProduct = productRepository.findByName(productName);
        Product product = optionalProduct.orElseThrow(IllegalArgumentException::new);

        return mapToDto(product);
    }

    @Cacheable(value = "productsByCategory")
    public List<ProductDto> findByCategoryName(String categoryName) {
        log.info("Reading Products belonging to category- {}", categoryName);
        Optional<Category> categoryOptional = categoryRepository.findByName(categoryName);
        Category category = categoryOptional.orElseThrow(() -> new IllegalArgumentException("Category Not Found"));

        List<Product> products = productRepository.findByCategory(category);
        log.info("Found {} categories", products.size());
        return products.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public void save(ProductDto productDto) {
        Product product = new Product();
        product.setName(productDto.getProductName());
    }
}
