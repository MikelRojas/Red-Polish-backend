package com.redpolishbackend.service;
import com.redpolishbackend.dto.ProductDto;
import java.util.List;

public interface ProductService {
    ProductDto createProduct(ProductDto product);
    List<ProductDto> getProducts();
    ProductDto getProductById(Long id);
    void deleteProduct(Long id);
    ProductDto updateProduct(Long id, ProductDto productDto);
}
