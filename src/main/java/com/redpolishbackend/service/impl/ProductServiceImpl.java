package com.redpolishbackend.service.impl;

import com.redpolishbackend.dto.ProductDto;
import com.redpolishbackend.entity.Category;
import com.redpolishbackend.entity.Promotion;
import com.redpolishbackend.repository.CategoryRepository;
import com.redpolishbackend.repository.PromotionRepository;
import com.redpolishbackend.service.ProductService;
import com.redpolishbackend.entity.Product;
import com.redpolishbackend.exception.ResourceNotFoundException;
import com.redpolishbackend.mapper.ProductMapper;
import com.redpolishbackend.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;



@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PromotionRepository promotionRepository;

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        Category category = categoryRepository.findById(productDto.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category not found"));;
        Product product = ProductMapper.mapToProduct(productDto,null, category);
        Product savedProduct = productRepository.save(product);
        return ProductMapper.mapToProductDto(savedProduct);
    }

    @Override
    public List<ProductDto> getProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(ProductMapper::mapToProductDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDto getProductById(Long id) {
        Product productExample = new Product();
        productExample.setId(id);

        Example<Product> example = Example.of(productExample);

        Product product = productRepository.findBy(example, query -> query.first().orElse(null));
        if (product == null) {
            throw new ResourceNotFoundException("Product with ID " + id + " not found");
        }

        return ProductMapper.mapToProductDto(product);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + id + " no encontrado"));
        productRepository.delete(product);
    }

    @Override
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + id + " no encontrado"));

        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        Promotion promotion = null;
        if (productDto.getPromotionId() != null) {
            promotion = promotionRepository.findById(productDto.getPromotionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada"));
        }

        existingProduct.setName(productDto.getName());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setPrice(productDto.getPrice());
        existingProduct.setStock(productDto.getStock());
        existingProduct.setImage(productDto.getImage());
        existingProduct.setCategory(category);
        existingProduct.setPromotion(promotion);


        Product updatedProduct = productRepository.save(existingProduct);
        return ProductMapper.mapToProductDto(updatedProduct);
    }
}
