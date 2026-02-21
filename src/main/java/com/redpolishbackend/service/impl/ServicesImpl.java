package com.redpolishbackend.service.impl;

import com.redpolishbackend.dto.ServiceDTO;
import com.redpolishbackend.entity.Category;
import com.redpolishbackend.entity.Promotion;
import com.redpolishbackend.exception.ResourceNotFoundException;
import com.redpolishbackend.mapper.ServicesMapper;
import com.redpolishbackend.repository.CategoryRepository;
import com.redpolishbackend.repository.PromotionRepository;
import com.redpolishbackend.repository.ServiceRepository;
import com.redpolishbackend.service.ServicesService;
import com.redpolishbackend.entity.Services;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ServicesImpl implements ServicesService {

    private final ServiceRepository servicesRepository;
    private final CategoryRepository categoryRepository;
    private final PromotionRepository promotionRepository;

    @Override
    public ServiceDTO createService(ServiceDTO serviceDTO) {
        Category category = categoryRepository.findById(serviceDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Promotion promotion = null;
        if (serviceDTO.getPromotionId() != null) {
            promotion = promotionRepository.findById(serviceDTO.getPromotionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
        }

        Services service = ServicesMapper.mapToService(serviceDTO, promotion, category);
        Services savedService = servicesRepository.save(service);
        return ServicesMapper.mapToServiceDTO(savedService);
    }

    @Override
    public List<ServiceDTO> getServices() {
        List<Services> services = servicesRepository.findAll();
        return services.stream()
                .map(ServicesMapper::mapToServiceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ServiceDTO getServiceById(Long id) {
        Services exampleService = new Services();
        exampleService.setId(id);

        Example<Services> example = Example.of(exampleService);
        Services service = servicesRepository.findBy(example, query -> query.first().orElse(null));

        if (service == null) {
            throw new ResourceNotFoundException("Service with ID " + id + " not found");
        }

        return ServicesMapper.mapToServiceDTO(service);
    }

    @Override
    public void deleteService(Long id) {
        System.out.println("Intentando eliminar servicio con ID: " + id);
        try {
            Services service = servicesRepository.findByServiceId(id);
            System.out.println("Servicio encontrado: " + service.getName());
            servicesRepository.delete(service);
            System.out.println("Servicio eliminado exitosamente.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public ServiceDTO updateService(Long id, ServiceDTO serviceDTO) {
        try {
            Services existingService = servicesRepository.findByServiceId(id);
            Category category = categoryRepository.findById(serviceDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

            Promotion promotion = null;
            if (serviceDTO.getPromotionId() != null) {
                promotion = promotionRepository.findById(serviceDTO.getPromotionId())
                        .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
            }

            existingService.setName(serviceDTO.getName());
            existingService.setDescription(serviceDTO.getDescription());
            existingService.setDuration(serviceDTO.getDuration());
            existingService.setPrice(serviceDTO.getPrice());
            existingService.setImageUrl(serviceDTO.getImageUrl());
            existingService.setCategory(category);
            existingService.setPromotion(promotion);

            Services updatedService = servicesRepository.save(existingService);
            return ServicesMapper.mapToServiceDTO(updatedService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}