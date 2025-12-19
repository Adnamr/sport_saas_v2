package com.sportsaas.dashboard.infra;

import com.sportsaas.auth.domain.UserRepository;
import com.sportsaas.auth.domain.UserRole;
import com.sportsaas.billing.domain.InvoiceRepository;
import com.sportsaas.catalog.domain.ProductRepository;
import com.sportsaas.dashboard.api.dto.*;
import com.sportsaas.dashboard.domain.*;
import com.sportsaas.inventory.domain.StockItem;
import com.sportsaas.inventory.domain.StockItemRepository;
import com.sportsaas.order.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Implementation du service Dashboard.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final Set<String> VALID_PERIODS = Set.of("last_7_days", "last_30_days", "last_12_months");
    private static final int DEFAULT_LOW_STOCK_LIMIT = 20;

    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final StockItemRepository stockItemRepository;
    private final ProductRepository productRepository;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;

    @Override
    @Cacheable(value = "dashboardStats", key = "'stats'", unless = "#result == null")
    public DashboardStatsResponse getStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfMonth.minusNanos(1);

        // Revenus - utilise les queries SUM optimisees
        BigDecimal totalRevenue = invoiceRepository.sumTotalPaid();
        BigDecimal monthlyRevenue = invoiceRepository.sumTotalPaidBetween(startOfMonth, now);
        BigDecimal lastMonthRevenue = invoiceRepository.sumTotalPaidBetween(startOfLastMonth, endOfLastMonth);
        BigDecimal revenueGrowth = calculateGrowth(monthlyRevenue, lastMonthRevenue);

        // Commandes - utilise la query COUNT optimisee
        long totalOrders = orderRepository.count();
        long monthlyOrders = orderRepository.countByCreatedAtBetween(startOfMonth, now);
        long pendingOrders = countOrdersByStatuses(List.of(
            OrderStatus.PENDING_PAYMENT, OrderStatus.PAID, OrderStatus.PROCESSING, OrderStatus.READY
        ));

        // Produits et stock
        long totalProducts = productRepository.count();
        List<StockItem> lowStockItems = stockItemRepository.findLowStock();
        long lowStockProducts = lowStockItems.stream().filter(s -> !s.isOutOfStock()).count();
        long outOfStockProducts = lowStockItems.stream().filter(StockItem::isOutOfStock).count();

        // Clients (role CUSTOMER) - utilise la nouvelle query
        long totalCustomers = userRepository.countByRole(UserRole.CUSTOMER);
        long newCustomersThisMonth = userRepository.countByRoleAndCreatedAtBetween(
            UserRole.CUSTOMER, startOfMonth, now
        );

        // Locations actives
        long activeRentals = rentalRepository.findByStatusOrderByCreatedAtDesc(
            RentalStatus.ACTIVE, PageRequest.of(0, 1)
        ).getTotalElements();

        return new DashboardStatsResponse(
            totalRevenue,
            monthlyRevenue,
            revenueGrowth,
            totalOrders,
            monthlyOrders,
            pendingOrders,
            totalProducts,
            lowStockProducts,
            outOfStockProducts,
            totalCustomers,
            newCustomersThisMonth,
            activeRentals
        );
    }

    @Override
    @Cacheable(value = "revenueChart", key = "#period", unless = "#result == null")
    public RevenueChartResponse getRevenueChart(String period) {
        // Validation du parametre period
        String validPeriod = VALID_PERIODS.contains(period) ? period : "last_12_months";

        LocalDateTime now = LocalDateTime.now();
        List<String> labels = new ArrayList<>();
        List<BigDecimal> revenues = new ArrayList<>();

        switch (validPeriod) {
            case "last_7_days" -> {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
                for (int i = 6; i >= 0; i--) {
                    LocalDate date = now.toLocalDate().minusDays(i);
                    labels.add(date.format(formatter));
                    revenues.add(invoiceRepository.sumTotalPaidByDate(date));
                }
            }
            case "last_30_days" -> {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
                for (int i = 29; i >= 0; i--) {
                    LocalDate date = now.toLocalDate().minusDays(i);
                    labels.add(date.format(formatter));
                    revenues.add(invoiceRepository.sumTotalPaidByDate(date));
                }
            }
            default -> { // last_12_months
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yy");
                for (int i = 11; i >= 0; i--) {
                    YearMonth month = YearMonth.now().minusMonths(i);
                    labels.add(month.format(formatter));
                    revenues.add(invoiceRepository.sumTotalPaidByDateRange(
                        month.atDay(1), month.atEndOfMonth()
                    ));
                }
            }
        }

        BigDecimal total = revenues.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = revenues.isEmpty() ? BigDecimal.ZERO :
            total.divide(BigDecimal.valueOf(revenues.size()), 2, RoundingMode.HALF_UP);

        return new RevenueChartResponse(validPeriod, labels, revenues, total, average);
    }

    @Override
    @Cacheable(value = "ordersByStatus", unless = "#result == null")
    public OrdersByStatusResponse getOrdersByStatus() {
        long total = orderRepository.count();
        Map<String, Long> byStatus = new LinkedHashMap<>();

        // Initialise tous les statuts a 0
        for (OrderStatus status : OrderStatus.values()) {
            byStatus.put(status.name(), 0L);
        }

        // Utilise la query GROUP BY optimisee
        List<Object[]> counts = orderRepository.countByStatus();
        for (Object[] row : counts) {
            OrderStatus status = (OrderStatus) row[0];
            Long count = (Long) row[1];
            byStatus.put(status.name(), count);
        }

        return new OrdersByStatusResponse(total, byStatus);
    }

    @Override
    public List<RecentOrderResponse> getRecentOrders(int limit) {
        Page<Order> orders = orderRepository.findAll(
            PageRequest.of(0, Math.min(limit, 50), Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return orders.getContent().stream()
            .map(order -> new RecentOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerName(),
                order.getStatus().name(),
                order.getType().name(),
                order.getTotal(),
                order.getCurrency(),
                order.getCreatedAt()
            ))
            .toList();
    }

    @Override
    public List<LowStockItemResponse> getLowStockItems(int limit) {
        List<StockItem> items = stockItemRepository.findLowStockWithProduct();

        return items.stream()
            .limit(limit > 0 ? limit : DEFAULT_LOW_STOCK_LIMIT)
            .map(item -> new LowStockItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getSku(),
                item.getAvailableQuantity(),
                item.getLowStockThreshold(),
                item.getLocation(),
                item.isOutOfStock()
            ))
            .toList();
    }

    @Override
    public Page<ActivityResponse> getRecentActivities(Pageable pageable) {
        Page<Activity> activities = activityRepository.findRecentActivities(pageable);

        return activities.map(activity -> new ActivityResponse(
            activity.getId(),
            activity.getType().name(),
            activity.getDescription(),
            activity.getUserId(),
            activity.getUserName(),
            activity.getEntityType(),
            activity.getEntityId(),
            activity.getCreatedAt()
        ));
    }

    @Override
    @Transactional
    public Activity logActivity(Activity activity) {
        return activityRepository.save(activity);
    }

    // === Helper methods ===

    private BigDecimal calculateGrowth(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        return current.subtract(previous)
            .divide(previous, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);
    }

    private long countOrdersByStatuses(List<OrderStatus> statuses) {
        return orderRepository.findByStatusIn(statuses, Pageable.unpaged()).getTotalElements();
    }
}
