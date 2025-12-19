package com.sportsaas.billing.api;

import com.sportsaas.billing.api.dto.*;
import com.sportsaas.billing.domain.Invoice;
import com.sportsaas.billing.domain.InvoiceItem;
import com.sportsaas.billing.domain.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BillingMapper {

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "rentalId", source = "rental.id")
    @Mapping(target = "overdue", expression = "java(invoice.isOverdue())")
    InvoiceResponse toInvoiceResponse(Invoice invoice);

    List<InvoiceResponse> toInvoiceResponseList(List<Invoice> invoices);

    InvoiceItemResponse toInvoiceItemResponse(InvoiceItem item);

    List<InvoiceItemResponse> toInvoiceItemResponseList(List<InvoiceItem> items);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "invoiceNumber", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "rental", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "issueDate", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "paidAmount", ignore = true)
    @Mapping(target = "balanceDue", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    Invoice toInvoice(CreateInvoiceRequest request);

    @Mapping(target = "invoiceId", source = "invoice.id")
    @Mapping(target = "invoiceNumber", source = "invoice.invoiceNumber")
    PaymentResponse toPaymentResponse(Payment payment);

    List<PaymentResponse> toPaymentResponseList(List<Payment> payments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "paymentReference", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "method", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "externalReference", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "failedAt", ignore = true)
    @Mapping(target = "failureReason", ignore = true)
    @Mapping(target = "refundedAt", ignore = true)
    @Mapping(target = "refundedAmount", ignore = true)
    Payment toPayment(CreatePaymentRequest request);
}
