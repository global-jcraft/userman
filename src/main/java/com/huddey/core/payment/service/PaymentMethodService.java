package com.huddey.core.payment.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.huddey.core.payment.data.dto.AddPaymentMethodRequest;
import com.huddey.core.payment.data.dto.CreatePaymentMethodCheckoutRequest;
import com.huddey.core.payment.data.dto.PaymentMethodDto;
import com.huddey.core.payment.data.dto.UpdatePaymentMethodRequest;
import com.huddey.core.payment.data.entity.PaymentMethod;
import com.huddey.core.payment.data.entity.UserSubscription;
import com.huddey.core.payment.data.enums.PaymentMethodType;
import com.huddey.core.payment.exception.PaymentMethodNotFoundException;
import com.huddey.core.payment.repository.PaymentMethodRepository;
import com.huddey.core.payment.repository.UserSubscriptionRepository;
import com.stripe.exception.StripeException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentMethodService {

  private final PaymentMethodRepository paymentMethodRepository;
  private final UserSubscriptionRepository subscriptionRepository;

  public Page<PaymentMethodDto> getUserPaymentMethods(Long userId, int page, int size) {
    log.debug("Fetching payment methods from database for user: {}", userId);

    Pageable pageable = PageRequest.of(page, size);
    Page<PaymentMethod> paymentMethods =
        paymentMethodRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

    List<PaymentMethodDto> dtos = paymentMethods.getContent().stream().map(this::toDto).toList();

    return new PageImpl<>(dtos, pageable, paymentMethods.getTotalElements());
  }

  // @Cacheable(value = "paymentMethods", key = "#userId + '_' + #page + '_' + #size")
  public Page<PaymentMethodDto> syncUserPaymentMethodsWithStripe(Long userId, int page, int size)
      throws StripeException {
    log.debug("Fetching payment methods from Stripe for user: {}", userId);

    String customerId = getCustomerIdForUser(userId);

    // Fetch all payment method types from Stripe

    // Fetch cards
    var cards =
        com.stripe.model.PaymentMethod.list(
            com.stripe.param.PaymentMethodListParams.builder()
                .setCustomer(customerId)
                .setType(com.stripe.param.PaymentMethodListParams.Type.CARD)
                .build());
    List<com.stripe.model.PaymentMethod> allPaymentMethods =
        new java.util.ArrayList<>(cards.getData());

    // Fetch bank accounts
    var bankAccounts =
        com.stripe.model.PaymentMethod.list(
            com.stripe.param.PaymentMethodListParams.builder()
                .setCustomer(customerId)
                .setType(com.stripe.param.PaymentMethodListParams.Type.US_BANK_ACCOUNT)
                .build());
    allPaymentMethods.addAll(bankAccounts.getData());

    // Sync with database and return
    List<PaymentMethodDto> dtos =
        allPaymentMethods.stream()
            .map(stripeMethod -> syncAndConvertToDto(userId, stripeMethod))
            .toList();

    Pageable pageable = PageRequest.of(page, size);
    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), dtos.size());
    List<PaymentMethodDto> pageContent = dtos.subList(start, end);

    return new PageImpl<>(pageContent, pageable, dtos.size());
  }

  private PaymentMethodDto syncAndConvertToDto(
      Long userId, com.stripe.model.PaymentMethod stripeMethod) {

    Optional<PaymentMethod> existing =
        paymentMethodRepository.findByStripePaymentMethodId(stripeMethod.getId());

    PaymentMethod paymentMethod;
    if (existing.isPresent()) {
      paymentMethod = existing.get();
    } else {
      paymentMethod = createFromStripe(userId, stripeMethod);
      paymentMethodRepository.save(paymentMethod);
    }

    return toDto(paymentMethod);
  }

  @Transactional
  @CacheEvict(value = "paymentMethods", key = "#userId + '*'")
  @Retry(name = "stripe-api")
  @CircuitBreaker(name = "stripe-api", fallbackMethod = "fallbackAddPaymentMethod")
  public PaymentMethodDto addPaymentMethod(Long userId, AddPaymentMethodRequest request)
      throws StripeException {
    log.debug("Starting addPaymentMethod for user: {}, type: {}", userId, request.getType());

    String customerId = getCustomerIdForUser(userId);
    log.debug("Retrieved Stripe customer: {} for user: {}", customerId, userId);

    // Create Checkout Session for setup mode
    var session =
        com.stripe.model.checkout.Session.create(
            com.stripe.param.checkout.SessionCreateParams.builder()
                .setMode(com.stripe.param.checkout.SessionCreateParams.Mode.SETUP)
                .setCustomer(customerId)
                .addPaymentMethodType(
                    com.stripe.param.checkout.SessionCreateParams.PaymentMethodType.valueOf(
                        request.getType().getStripeType().toUpperCase()))
                .setSuccessUrl("${app.frontend.successUrl}/payment-methods?success=true")
                .setCancelUrl("${app.frontend.cancelUrl}/payment-methods?cancelled=true")
                .putMetadata("userId", userId.toString())
                .putMetadata("setAsDefault", String.valueOf(request.isSetAsDefault()))
                .build());

    log.debug("Created Checkout Session {} for user: {}", session.getId(), userId);

    return createCheckoutSessionDto(session);
  }

  @Transactional
  @CacheEvict(value = "paymentMethods", key = "#userId + '*'")
  public PaymentMethodDto updatePaymentMethod(
      Long userId, Long paymentMethodId, UpdatePaymentMethodRequest request)
      throws StripeException {
    log.debug("Updating payment method {} for user: {}", paymentMethodId, userId);

    PaymentMethod paymentMethod = getPaymentMethodForUser(userId, paymentMethodId);

    if (request.getExpMonth() != null) {
      log.debug(
          "Updating expiration month to {} for payment method {}",
          request.getExpMonth(),
          paymentMethodId);
      paymentMethod.setExpMonth(request.getExpMonth());
    }
    if (request.getExpYear() != null) {
      log.debug(
          "Updating expiration year to {} for payment method {}",
          request.getExpYear(),
          paymentMethodId);
      paymentMethod.setExpYear(request.getExpYear());
    }

    PaymentMethod saved = paymentMethodRepository.save(paymentMethod);
    log.debug("Updated payment method {} for user: {}", paymentMethodId, userId);

    return toDto(saved);
  }

  @Transactional
  @CacheEvict(value = "paymentMethods", key = "#userId + '*'")
  @Retry(name = "stripe-api")
  @CircuitBreaker(name = "stripe-api", fallbackMethod = "fallbackRemovePaymentMethod")
  public boolean removePaymentMethod(Long userId, Long paymentMethodId) throws StripeException {
    log.debug(
        "Starting removePaymentMethod for user: {}, paymentMethodId: {}", userId, paymentMethodId);

    PaymentMethod paymentMethod = getPaymentMethodForUser(userId, paymentMethodId);
    log.debug(
        "Found payment method {} for user: {}", paymentMethod.getStripePaymentMethodId(), userId);

    com.stripe.model.PaymentMethod stripePaymentMethod =
        com.stripe.model.PaymentMethod.retrieve(paymentMethod.getStripePaymentMethodId());
    log.debug("Retrieved Stripe payment method for detachment: {}", stripePaymentMethod.getId());

    stripePaymentMethod.detach();
    log.debug("Detached payment method from Stripe customer for user: {}", userId);

    paymentMethodRepository.delete(paymentMethod);
    log.debug("Deleted payment method {} from database for user: {}", paymentMethodId, userId);

    return true;
  }

  @Transactional
  @CacheEvict(value = "paymentMethods", key = "#userId + '*'")
  @Retry(name = "stripe-api")
  @CircuitBreaker(name = "stripe-api", fallbackMethod = "fallbackSetAsDefault")
  public PaymentMethodDto setAsDefault(Long userId, Long paymentMethodId) throws StripeException {
    log.debug("Setting payment method {} as default for user: {}", paymentMethodId, userId);

    PaymentMethod paymentMethod = getPaymentMethodForUser(userId, paymentMethodId);
    String customerId = getCustomerIdForUser(userId);

    var customer = com.stripe.model.Customer.retrieve(customerId);
    customer.update(
        com.stripe.param.CustomerUpdateParams.builder()
            .setInvoiceSettings(
                com.stripe.param.CustomerUpdateParams.InvoiceSettings.builder()
                    .setDefaultPaymentMethod(paymentMethod.getStripePaymentMethodId())
                    .build())
            .build());
    log.debug("Updated default payment method in Stripe for customer: {}", customerId);

    paymentMethodRepository.clearDefaultForUser(userId);
    paymentMethod.setDefault(true);

    PaymentMethod saved = paymentMethodRepository.save(paymentMethod);
    log.debug("Set payment method {} as default for user: {}", paymentMethodId, userId);

    return toDto(saved);
  }

  @Transactional
  @CacheEvict(value = "paymentMethods", key = "#userId + '*'")
  public PaymentMethodDto setAsBackup(Long userId, Long paymentMethodId) {
    log.debug("Setting payment method {} as backup for user: {}", paymentMethodId, userId);

    PaymentMethod paymentMethod = getPaymentMethodForUser(userId, paymentMethodId);
    log.debug("Clearing existing backup payment methods for user: {}", userId);

    paymentMethodRepository.clearBackupForUser(userId);
    paymentMethod.setBackup(true);

    PaymentMethod saved = paymentMethodRepository.save(paymentMethod);
    log.debug("Set payment method {} as backup for user: {}", paymentMethodId, userId);

    return toDto(saved);
  }

  public List<PaymentMethod> getExpiredPaymentMethods() {
    LocalDate now = LocalDate.now();
    log.debug(
        "Checking for expired payment methods as of {}/{}", now.getMonthValue(), now.getYear());

    List<PaymentMethod> expired =
        paymentMethodRepository.findExpiredPaymentMethods(now.getYear(), now.getMonthValue());
    log.debug("Found {} expired payment methods", expired.size());

    return expired;
  }

  @Transactional
  public String getCustomerIdForUser(Long userId) throws StripeException {
    Optional<UserSubscription> subscription = subscriptionRepository.findByUserId(userId);

    if (subscription.isPresent() && subscription.get().getStripeCustomerId() != null) {
      return subscription.get().getStripeCustomerId();
    }

    // Create Stripe customer if user doesn't have one yet
    log.debug("Creating Stripe customer for user: {}", userId);
    var customer =
        com.stripe.model.Customer.create(
            com.stripe.param.CustomerCreateParams.builder()
                .putMetadata("userId", userId.toString())
                .build());

    log.debug("Created Stripe customer {} for user: {}", customer.getId(), userId);

    // Save customer ID to avoid future duplicates
    UserSubscription userSub = subscription.orElse(new UserSubscription());
    userSub.setUserId(userId);
    userSub.setStripeCustomerId(customer.getId());
    subscriptionRepository.save(userSub);

    return customer.getId();
  }

  private PaymentMethod getPaymentMethodForUser(Long userId, Long paymentMethodId) {
    return paymentMethodRepository
        .findByUserIdAndId(userId, paymentMethodId)
        .orElseThrow(() -> new PaymentMethodNotFoundException("Payment method not found for user"));
  }

  private PaymentMethod createFromStripe(
      Long userId, com.stripe.model.PaymentMethod stripePaymentMethod) {
    PaymentMethod paymentMethod = new PaymentMethod();
    paymentMethod.setUserId(userId);
    paymentMethod.setStripePaymentMethodId(stripePaymentMethod.getId());
    paymentMethod.setType(PaymentMethodType.fromStripeType(stripePaymentMethod.getType()).name());

    if (stripePaymentMethod.getCard() != null) {
      paymentMethod.setLastFour(stripePaymentMethod.getCard().getLast4());
      paymentMethod.setBrand(stripePaymentMethod.getCard().getBrand());
      paymentMethod.setExpMonth(Math.toIntExact(stripePaymentMethod.getCard().getExpMonth()));
      paymentMethod.setExpYear(Math.toIntExact(stripePaymentMethod.getCard().getExpYear()));
    }

    return paymentMethod;
  }

  public boolean fallbackRemovePaymentMethod(Long userId, Long paymentMethodId, Exception ex) {
    log.error(
        "Circuit breaker activated for removePaymentMethod - userId: {}, paymentMethodId: {}, error: {}",
        userId,
        paymentMethodId,
        ex.getMessage());
    return false;
  }

  public PaymentMethodDto fallbackAddPaymentMethod(
      Long userId, AddPaymentMethodRequest request, Exception ex) {
    log.error(
        "Circuit breaker activated for addPaymentMethod - userId: {}, error: {}",
        userId,
        ex.getMessage());
    PaymentMethodDto fallbackDto = new PaymentMethodDto();
    fallbackDto.setId(-1L);
    return fallbackDto;
  }

  public PaymentMethodDto fallbackSetAsDefault(Long userId, Long paymentMethodId, Exception ex) {
    log.error(
        "Circuit breaker activated for setAsDefault - userId: {}, paymentMethodId: {}, error: {}",
        userId,
        paymentMethodId,
        ex.getMessage());
    PaymentMethodDto fallbackDto = new PaymentMethodDto();
    fallbackDto.setId(-1L);
    return fallbackDto;
  }

  private PaymentMethodDto toDto(PaymentMethod paymentMethod) {
    PaymentMethodDto dto = new PaymentMethodDto();
    dto.setId(paymentMethod.getId());
    dto.setType(PaymentMethodType.valueOf(paymentMethod.getType()));
    dto.setLastFour(paymentMethod.getLastFour());
    dto.setBrand(paymentMethod.getBrand());
    dto.setExpMonth(paymentMethod.getExpMonth());
    dto.setExpYear(paymentMethod.getExpYear());
    dto.setDefault(paymentMethod.isDefault());
    dto.setBackup(paymentMethod.isBackup());
    dto.setCreatedAt(paymentMethod.getCreatedAt());

    if (paymentMethod.getExpMonth() != null && paymentMethod.getExpYear() != null) {
      LocalDate expiry = LocalDate.of(paymentMethod.getExpYear(), paymentMethod.getExpMonth(), 1);
      dto.setExpired(expiry.isBefore(LocalDate.now()));
    }

    return dto;
  }

  public PaymentMethodDto createPaymentMethodCheckoutSession(
      Long userId, CreatePaymentMethodCheckoutRequest request) throws StripeException {
    log.debug(
        "Creating checkout session for payment method setup - user: {}, type: {}",
        userId,
        request.getPaymentMethodType());

    String customerId = getCustomerIdForUser(userId);

    var session =
        com.stripe.model.checkout.Session.create(
            com.stripe.param.checkout.SessionCreateParams.builder()
                .setMode(com.stripe.param.checkout.SessionCreateParams.Mode.SETUP)
                .setCustomer(customerId)
                .addPaymentMethodType(
                    com.stripe.param.checkout.SessionCreateParams.PaymentMethodType.valueOf(
                        request.getPaymentMethodType().getStripeType().toUpperCase()))
                .setSuccessUrl(request.getSuccessUrl())
                .setCancelUrl(request.getCancelUrl())
                .putMetadata("userId", userId.toString())
                .putMetadata("setAsDefault", String.valueOf(request.isSetAsDefault()))
                .build());

    log.debug("Created checkout session {} for user: {}", session.getId(), userId);

    return createCheckoutSessionDto(session);
  }

  private PaymentMethodDto createCheckoutSessionDto(com.stripe.model.checkout.Session session) {
    PaymentMethodDto dto = new PaymentMethodDto();
    dto.setSetupIntentId(session.getId());
    dto.setSetupUrl(session.getUrl());
    dto.setStatus(session.getStatus());
    return dto;
  }
}
