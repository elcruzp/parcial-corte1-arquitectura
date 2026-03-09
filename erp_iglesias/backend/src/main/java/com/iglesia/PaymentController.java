package com.iglesia;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final OfferingRepository offeringRepository;

    public PaymentController(PaymentRepository paymentRepository,
                             EnrollmentRepository enrollmentRepository,
                             OfferingRepository offeringRepository) {
        this.paymentRepository = paymentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.offeringRepository = offeringRepository;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @GetMapping
    public List<PaymentResponse> list(@RequestParam(name = "status", required = false) PaymentStatus status) {
        List<Payment> payments = status == null ? paymentRepository.findAll() : paymentRepository.findAllByStatus(status);
        return payments.stream().map(PaymentResponse::from).toList();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @PostMapping("/{id}/confirm")
    public PaymentResponse confirm(@PathVariable Long id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado"));

        payment.setStatus(PaymentStatus.CONFIRMADO);
        paymentRepository.save(payment);

        if (payment.getType() == PaymentType.INSCRIPCION_CURSO) {
            Enrollment enrollment = enrollmentRepository.findById(payment.getReferenceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscripción no encontrada"));
            enrollment.setStatus(EnrollmentStatus.PAGADA);
            enrollmentRepository.save(enrollment);
        } else if (payment.getType() == PaymentType.OFRENDA) {
            Offering offering = offeringRepository.findById(payment.getReferenceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ofrenda no encontrada"));
            offering.setStatus(OfferingStatus.REGISTRADA);
            offeringRepository.save(offering);
        }

        return PaymentResponse.from(payment);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @PostMapping("/{id}/fail")
    public PaymentResponse fail(@PathVariable Long id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado"));

        if (payment.getStatus() == PaymentStatus.CONFIRMADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pago ya fue confirmado");
        }

        payment.setAttempts(payment.getAttempts() + 1);
        payment.setStatus(PaymentStatus.FALLIDO);
        paymentRepository.save(payment);

        return PaymentResponse.from(payment);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @PostMapping("/{id}/retry")
    public PaymentResponse retry(@PathVariable Long id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado"));

        if (payment.getStatus() != PaymentStatus.FALLIDO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se reintenta un pago fallido");
        }

        if (payment.getAttempts() >= 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se superó el máximo de reintentos");
        }

        payment.setStatus(PaymentStatus.INICIADO);
        paymentRepository.save(payment);
        return PaymentResponse.from(payment);
    }

    public record PaymentResponse(
        Long id,
        String type,
        String status,
        String amount,
        int attempts,
        Long referenceId
    ) {
        public static PaymentResponse from(Payment payment) {
            return new PaymentResponse(
                payment.getId(),
                payment.getType().name(),
                payment.getStatus().name(),
                payment.getAmount().toPlainString(),
                payment.getAttempts(),
                payment.getReferenceId()
            );
        }
    }
}
