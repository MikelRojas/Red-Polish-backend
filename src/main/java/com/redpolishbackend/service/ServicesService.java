package com.redpolishbackend.service;

import com.redpolishbackend.dto.ServiceDTO;

import java.util.List;

public interface ServicesService {
    ServiceDTO createService(ServiceDTO service);
    ServiceDTO updateService(Long id, ServiceDTO service);
    void deleteService(Long id); // Return message
    ServiceDTO getServiceById(Long id);
    List<ServiceDTO> getServices();
}
