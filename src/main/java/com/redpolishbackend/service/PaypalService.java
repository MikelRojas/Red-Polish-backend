package com.redpolishbackend.service;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import com.redpolishbackend.dto.CitaDto;
import com.redpolishbackend.dto.CompraDto;
import com.redpolishbackend.entity.*;
import com.redpolishbackend.exception.ResourceNotFoundException;
import com.redpolishbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.redpolishbackend.mapper.CompraMapper.mapToCompra;

@Service
@RequiredArgsConstructor
public class PaypalService {

    private final UserRepository userRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final CompraRepository compraRepository;
    private final CitaRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final NotificacionService notificacionService;
    private final ShoppingCartService shoppingCartService;
    private final ProductRepository productRepository;


    @Autowired
    private APIContext apiContext;

    public PaymentPivot createPaymentProducts(CompraDto compraDto) throws PayPalRESTException {

        User existingUser = userRepository.findByEmail(compraDto.getUsuarioEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con email " + compraDto.getUsuarioEmail() + " no encontrado"));
        if (compraRepository.existsByClienteIdAndEstadoPago(existingUser.getId(), "PENDIENTE")) {
            throw new IllegalStateException("Ya existe una compra pendiente. Por favor finalícela antes de realizar otra.");
        }
        List<ShoppingCart> productsList = shoppingCartRepository.findByUser(existingUser);

        if (productsList.isEmpty()) {
            throw new IllegalStateException("El carrito de compras está vacío");
        }

        List<Item> items = new ArrayList<>();

        double totalPrice = productsList.stream()
                .mapToDouble(ci -> {
                    double price = ci.getProduct().getPrice();
                    Promotion promotion = ci.getProduct().getPromotion();
                    if(promotion != null){
                        LocalDate today = LocalDate.now();
                        LocalDate start = promotion.getStart_date().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        LocalDate end = promotion.getEnd_date().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        if ((today.isEqual(start) || today.isAfter(start)) &&
                                (today.isEqual(end) || today.isBefore(end))) {
                            price *= (1 - promotion.getPorcentage() / 100.0);
                        }
                    }
                    Item item = new Item();
                    item.setName(ci.getProduct().getName());
                    item.setCurrency("USD");
                    item.setPrice(String.format(Locale.US, "%.2f", price));
                    item.setQuantity(String.valueOf(ci.getQuantity()));
                    item.setSku("Producto -" + ci.getProduct().getId());
                    items.add(item);
                    return price * ci.getQuantity();
                }).sum();

        // 4. Revisar si ya existe una compra con los mismos datos
        Optional<Compra> existingCompraOpt = compraRepository.findByFechaCompraAndClienteIdAndPrecioCompraAndEstadoPago(
                compraDto.getFechaCompra(), existingUser.getId(), totalPrice,"PENDIENTE");

        Compra compra = existingCompraOpt.orElseGet(Compra::new);
        compra.setDescripcion(compraDto.getDescripcion());
        compra.setFechaCompra(compraDto.getFechaCompra()); // Asegúrate de que esté seteado correctamente
        compra.setCliente(existingUser);
        compra.setPrecioCompra(totalPrice);
        compra.setEstadoPago("PENDIENTE");

        Compra savedCompra = compraRepository.save(compra);

        Amount amount = new Amount();
        amount.setCurrency("USD");
        amount.setTotal(String.format(Locale.US, "%.2f", totalPrice));

        ItemList itemList = new ItemList();
        itemList.setItems(items);

        //Transaction
        Transaction transaction =new Transaction();
        transaction.setDescription(compraDto.getDescripcion());
        transaction.setAmount(amount);
        transaction.setItemList(itemList);
        List<Transaction> transactions = new ArrayList<Transaction>();
        transactions.add(transaction);

        //Payer
        Payer payer = new Payer();
        payer.setPaymentMethod("PAYPAL");

        Payment payment  = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl("http://www.redpolishcr.com/payment-failure");
        redirectUrls.setReturnUrl("http://www.redpolishcr.com/payment-success");
        payment.setRedirectUrls(redirectUrls);

        Payment createdPayment = payment.create(apiContext);

        return PaymentPivot.builder()
                .payment(createdPayment)
                .compra(savedCompra)
                .build();
    }

    public AppointmentPivot createPaymentAppointment(CitaDto citaDto) throws PayPalRESTException {

        // 1. Validar usuario
        User user = userRepository.findById(citaDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + citaDto.getUserId() + " no encontrado"));

        if (appointmentRepository.existsByUserIdAndState(user.getId(), "PENDIENTE")) {
            throw new IllegalStateException("Ya existe una cita pendiente. Debe confirmarla o cancelarla antes de crear otra.");
        }

        // 2. Validar servicio
        Services service = serviceRepository.findById(citaDto.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio con ID " + citaDto.getServiceId() + " no encontrado"));

        // 3. Calcular precio total con promoción (si aplica)
        double totalPrice = service.getPrice();
        Promotion promo = service.getPromotion();

        if (promo != null) {
            LocalDate today = LocalDate.now();
            LocalDate start = promo.getStart_date().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            LocalDate end = promo.getEnd_date().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            if ((today.isEqual(start) || today.isAfter(start)) &&
                    (today.isEqual(end) || today.isBefore(end))) {
                totalPrice -= (service.getPrice() * promo.getPorcentage()) / 100.0;
            }
        }

        // 4. Crear y guardar la cita en estado PENDIENTE
        Optional<Cita> existingCitaOpt = appointmentRepository
                .findByDateAndHourAndUserIdAndServiceIdAndState(
                        citaDto.getDate(),
                        citaDto.getHour(),
                        citaDto.getUserId(),
                        citaDto.getServiceId(),
                        "PENDIENTE"
                );

        Cita cita = existingCitaOpt.orElse(new Cita());
        cita.setDate(citaDto.getDate());
        cita.setHour(citaDto.getHour());
        cita.setState("PENDIENTE");
        cita.setUser(user);
        cita.setService(service);

        Cita savedCita = appointmentRepository.save(cita);

        // 5. Crear ítem PayPal
        Item item = new Item();
        item.setName(service.getName());
        item.setCurrency("USD");
        item.setPrice(String.format(Locale.US, "%.2f", totalPrice));
        item.setQuantity("1");
        item.setSku("Servicio-" + service.getId());

        ItemList itemList = new ItemList();
        itemList.setItems(List.of(item));

        // 6. Crear objeto Amount
        Amount amount = new Amount();
        amount.setCurrency("USD");
        amount.setTotal(String.format(Locale.US, "%.2f", totalPrice));

        // 7. Crear transacción
        Transaction transaction = new Transaction();
        transaction.setDescription("Pago de cita para servicio: " + service.getName());
        transaction.setAmount(amount);
        transaction.setItemList(itemList);

        // 8. Configurar pago
        Payer payer = new Payer();
        payer.setPaymentMethod("PAYPAL");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(List.of(transaction));

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl("http://www.redpolishcr.com/payment-failure"); // Ajustar a tu frontend
        redirectUrls.setReturnUrl("http://www.redpolishcr.com/payment-success"); // Ajustar a tu frontend
        payment.setRedirectUrls(redirectUrls);

        // 9. Ejecutar pago
        Payment createdPayment = payment.create(apiContext);

        // 10. Retornar cita y pago juntos
        return AppointmentPivot.builder()
                .payment(createdPayment)
                .cita(savedCita)
                .build();
    }



    //execute payment

    public Payment executeAppointment(String paymentId , String payerId, Long appointmentId) throws PayPalRESTException {
        try {
            Payment payment = new Payment();
            payment.setId(paymentId);
            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(payerId);

            Cita appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new IllegalArgumentException("No existe la cita con ID = " + appointmentId));
            appointment.setState("CONFIRMADA");
            appointmentRepository.save(appointment);

            // Validar promoción por fecha
            double precio = appointment.getService().getPrice();
            Promotion promo = appointment.getService().getPromotion();
            if (promo != null) {
                LocalDate today = LocalDate.now();
                LocalDate start = promo.getStart_date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate end = promo.getEnd_date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                if ((today.isEqual(start) || today.isAfter(start)) &&
                        (today.isEqual(end) || today.isBefore(end))) {
                    precio -= (precio * promo.getPorcentage()) / 100.0;
                }
            }

            String cuerpo = String.format("""
        Estimado/a %s,

        Gracias por agendar su cita con RedPolish. A continuación encontrará los detalles de su pago y confirmación de cita:

        🧾 Factura de Pago
        - Servicio: %s
        - Fecha: %s
        - Hora: %s
        - Precio: $%.2f
        - Estado: CONFIRMADA

        📍 Dirección del local: RedPolish, Cedral, Ciudad Quesada, Costa Rica  
        📞 Contacto: +506 8358-2929   

        Gracias por confiar en nosotros. Le esperamos en su cita.

        Atentamente,  
        Equipo RedPolish
        """,
                    appointment.getUser().getName(),
                    appointment.getService().getName(),
                    appointment.getDate().toString(),
                    appointment.getHour(),
                    precio
            );

            notificacionService.enviarCorreo(appointment.getUser().getEmail(), "Factura de pago y confirmacion de cita", cuerpo);
            return payment.execute(apiContext, paymentExecution);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public Payment executeProducts(String paymentId , String payerId, Long buyId) throws PayPalRESTException {
        try {
            Payment payment = new Payment();
            payment.setId(paymentId);
            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(payerId);

            Compra buy = compraRepository.findById(buyId)
                    .orElseThrow(() -> new IllegalArgumentException("No existe la compra con ID = " + buyId));
            buy.setEstadoPago("CONFIRMADA");
            compraRepository.save(buy);

            User user = buy.getCliente();
            List<ShoppingCart> productosComprados = shoppingCartRepository.findByUser(user);

            if (productosComprados.isEmpty()) {
                throw new IllegalStateException("No se encontraron productos para el usuario en el carrito.");
            }
            shoppingCartService.reducirStockDeProductos(productosComprados);
            StringBuilder detalleProductos = new StringBuilder();
            double total = 0;

            for (ShoppingCart item : productosComprados) {
                double precio = item.getProduct().getPrice();
                Promotion promo = item.getProduct().getPromotion();

                if (promo != null) {
                    LocalDate today = LocalDate.now();
                    LocalDate start = promo.getStart_date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate end = promo.getEnd_date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                    if ((today.isEqual(start) || today.isAfter(start)) &&
                            (today.isEqual(end) || today.isBefore(end))) {
                        precio -= (precio * promo.getPorcentage()) / 100.0;
                    }
                }

                double subtotal = precio * item.getQuantity();
                total += subtotal;

                detalleProductos.append(String.format("- %s x%d - $%.2f\n", item.getProduct().getName(), item.getQuantity(), subtotal));
            }

            String cuerpo = String.format("""
        Estimado/a %s,

        Gracias por su compra en RedPolish. A continuación encontrará los detalles de su factura:

        🧾 Detalles de la Compra
        %s
        Total pagado: $%.2f
        Estado del pago: CONFIRMADO

        📍 Dirección del local: RedPolish, Cedral, Ciudad Quesada, Costa Rica  
        📞 Contacto: +506 8358-2929  

        Esperamos que disfrute sus productos. ¡Gracias por confiar en nosotros!

        Atentamente,  
        Equipo RedPolish
        """,
                    user.getName(),
                    detalleProductos.toString(),
                    total
            );

            notificacionService.enviarCorreo(user.getEmail(), "Factura de compra y confirmación de pago", cuerpo);
            shoppingCartService.clearCart(user.getId());

            return payment.execute(apiContext, paymentExecution);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}