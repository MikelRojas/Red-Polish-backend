package com.redpolishbackend.service.impl;

import com.redpolishbackend.dto.PromotionDto;
import com.redpolishbackend.entity.Product;
import com.redpolishbackend.entity.Promotion;
import com.redpolishbackend.entity.Services;
import com.redpolishbackend.entity.User;
import com.redpolishbackend.mapper.PromotionMapper;
import com.redpolishbackend.repository.ProductRepository;
import com.redpolishbackend.repository.PromotionRepository;
import com.redpolishbackend.repository.ServiceRepository;
import com.redpolishbackend.repository.UserRepository;
import com.redpolishbackend.service.NotificacionService;
import com.redpolishbackend.service.PromotionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository repository;
    private final NotificacionService notificacionService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ServiceRepository serviceRepository;

    @Override
    public PromotionDto create(PromotionDto dto) {
        Promotion promotion = PromotionMapper.mapToPromotion(dto);
        return PromotionMapper.mapToPromotionDto(repository.save(promotion));
    }

    @Override
    public List<PromotionDto> getAll() {
        return repository.findAll().stream()
                .map(PromotionMapper::mapToPromotionDto)
                .collect(Collectors.toList());
    }

    @Override
    public PromotionDto update(Long id, PromotionDto dto) {
        Promotion promotion = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        promotion.setTitle(dto.getTitle());
        promotion.setDescription(dto.getDescription());
        promotion.setStart_date(dto.getStart_date());
        promotion.setEnd_date(dto.getEnd_date());
        promotion.setPorcentage(dto.getPorcentage());

        return PromotionMapper.mapToPromotionDto(repository.save(promotion));
    }

    @Override
    public void sendEmailActivePromotions(Long promotionId) {
        Date hoy = new Date();

        Promotion promo = repository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promoción no encontrada con ID: " + promotionId));

        // Verificar que esté activa en la fecha actual
        if (promo.getStart_date().after(hoy) || promo.getEnd_date().before(hoy)) {
            return; // No está activa, no se envía nada
        }

        List<Product> productos = productRepository.findByPromotionId(promo.getId());
        List<Services> servicios = serviceRepository.findByPromotionId(promo.getId());

        if ((productos == null || productos.isEmpty()) && (servicios == null || servicios.isEmpty())) {
            return; // No hay productos ni servicios asociados
        }

        StringBuilder detalles = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#.00");

        for (Product p : productos) {
            double nuevoPrecio = p.getPrice() - (p.getPrice() * promo.getPorcentage() / 100);
            detalles.append(String.format("- Producto: %s\n  Precio original: $%s\n  Descuento: %.0f%%\n  Precio con descuento: $%s\n\n",
                    p.getName(), df.format(p.getPrice()), promo.getPorcentage(), df.format(nuevoPrecio)));
        }

        for (Services s : servicios) {
            double nuevoPrecio = s.getPrice() - (s.getPrice() * promo.getPorcentage() / 100);
            detalles.append(String.format("- Servicio: %s\n  Precio original: $%s\n  Descuento: %.0f%%\n  Precio con descuento: $%s\n\n",
                    s.getName(), df.format(s.getPrice()), promo.getPorcentage(), df.format(nuevoPrecio)));
        }

        String cuerpo = String.format("""
        ¡Promoción activa en RedPolish!

        %s

        %s

        Aprovecha antes del %s

        📍 Visítanos en Cedral, Ciudad Quesada
        📞 Contacto: +506 8358-2929

        ¡Gracias por confiar en nosotros!
        """,
                promo.getTitle(),
                detalles.toString(),
                promo.getEnd_date().toString()
        );

        List<User> usuarios = userRepository.findByRol("Usuario");
        for (User user : usuarios) {
            notificacionService.enviarCorreo(user.getEmail(), "🎉 Promoción activa: " + promo.getTitle(), cuerpo);
        }
    }


    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public PromotionDto getById(Long id) {
        Promotion promotion = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
        return PromotionMapper.mapToPromotionDto(promotion);
    }
}
