package com.huddey.core.payment.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

import org.springframework.stereotype.Service;

import com.huddey.core.payment.data.dto.CancelSubscriptionResponse;
import com.huddey.core.payment.data.dto.CreateCheckoutSessionRequest;
import com.huddey.core.payment.data.entity.ProductPrice;
import com.huddey.core.payment.data.entity.UserSubscription;
import com.huddey.core.payment.data.enums.SubscriptionStatus;
import com.huddey.core.payment.exception.StripeServiceException;
import com.huddey.core.payment.exception.SubscriptionNotFoundException;
import com.huddey.core.payment.repository.ProductPriceRepository;
import com.huddey.core.payment.repository.UserSubscriptionRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.PriceCollection;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PriceListParams;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SubscriptionService {

  private final UserSubscriptionRepository subscriptionRepository;
  private final ProductPriceRepository productPriceRepository;

  public SubscriptionService(
      UserSubscriptionRepository subscriptionRepository,
      ProductPriceRepository productPriceRepository) {
    this.subscriptionRepository = subscriptionRepository;
    this.productPriceRepository = productPriceRepository;
  }

  /**
   * Creates a Checkout Session for a subscription using lookup-key based pricing. - Seat-based
   * plans use quantity = seats - Individual plans use quantity = 1 - Multi-currency & interval
   * supported via lookup key
   */
  public String createCheckoutSession(Long userId, String email, CreateCheckoutSessionRequest req)
      throws StripeException {

    log.debug(
        "Creating checkout session for user {} planKey={} interval={} currency={} seats={}",
        userId,
        req.getPlanKey(),
        req.getInterval(),
        req.getCurrency(),
        req.getSeats());

    // No blocking - allow free users to upgrade and paid users to change plans via checkout

    Customer customer = getOrCreateStripeCustomer(userId, email);

    String lookupKey = lookupKey(req.getPlanKey(), req.getInterval(), req.getCurrency());
    String priceId = resolvePriceIdByLookupKey(lookupKey);

    long quantity = Math.max(1L, req.getSeats()); // 1 for individuals, N for Teams

    SessionCreateParams params =
        SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .setCustomer(customer.getId())
            .setSuccessUrl(req.getSuccessUrl() + "?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(req.getCancelUrl())
            .setAutomaticTax(SessionCreateParams.AutomaticTax.builder().setEnabled(true).build())
            .setAllowPromotionCodes(true)
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setPrice(priceId) // resolved by lookup key
                    .setQuantity(quantity)
                    .build())
            .putMetadata("userId", userId.toString())
            .putMetadata("planKey", req.getPlanKey())
            .putMetadata("interval", req.getInterval().toLowerCase(Locale.ROOT))
            .putMetadata("currency", req.getCurrency().toUpperCase(Locale.ROOT))
            .putMetadata("seats", Long.toString(quantity))
            .build();

    Session session = Session.create(params);
    log.debug("Created checkout session {} for user {}", session.getId(), userId);

    return session.getUrl();
  }

  /**
   * Upgrade/Downgrade plan by switching the price (keeps quantity for seat-based plans). Uses
   * proration for upgrades, and no proration for downgrades (common SaaS policy).
   */
  public void switchPlan(
      Long userId,
      String newPlanKey, // e.g. "huddey_pro" or "huddey_teams"
      String interval, // "month" | "year"
      String currency // "EUR" | "USD" | "GBP"
      ) throws StripeException {

    UserSubscription userSub =
        subscriptionRepository
            .findByUserId(userId)
            .orElseThrow(
                () ->
                    new SubscriptionNotFoundException(
                        "Subscription not found for user: " + userId));

    if (userSub.getStatus() != SubscriptionStatus.ACTIVE) {
      throw new StripeServiceException("Cannot update inactive subscription", null);
    }

    Subscription subscription = Subscription.retrieve(userSub.getStripeSubscriptionId());
    SubscriptionItem item = firstSubscriptionItem(subscription);

    long currentQty = Optional.ofNullable(item.getQuantity()).orElse(1L);
    long currentAmount = Optional.ofNullable(item.getPrice().getUnitAmount()).orElse(0L);

    String newPriceId = resolvePriceIdByLookupKey(lookupKey(newPlanKey, interval, currency));
    Price newPrice = Price.retrieve(newPriceId);
    long newAmount = Optional.ofNullable(newPrice.getUnitAmount()).orElse(0L);

    boolean isDowngrade = newAmount < currentAmount;

    SubscriptionUpdateParams params =
        SubscriptionUpdateParams.builder()
            .addItem(
                SubscriptionUpdateParams.Item.builder()
                    .setId(item.getId())
                    .setPrice(newPriceId)
                    .setQuantity(currentQty)
                    .build())
            .setProrationBehavior(
                isDowngrade
                    ? SubscriptionUpdateParams.ProrationBehavior.NONE
                    : SubscriptionUpdateParams.ProrationBehavior.CREATE_PRORATIONS)
            .putMetadata("planKey", newPlanKey)
            .putMetadata("interval", interval.toLowerCase(Locale.ROOT))
            .putMetadata("currency", currency.toUpperCase(Locale.ROOT))
            .build();

    subscription.update(params);

    // Persist minimal local changes; detailed fields should be synced via webhook
    if (isDowngrade) {
      userSub.setPendingPlanKey(newPlanKey);
      userSub.setPendingPlanEffectiveDate(userSub.getCurrentPeriodEnd());
    } else {
      userSub.setPlanKey(newPlanKey);
    }
    userSub.setUpdatedAt(OffsetDateTime.now());
    subscriptionRepository.save(userSub);
  }

  /**
   * Seat management: change quantity with a chosen proration behavior. Example: increase from 30 ->
   * 32 seats (call with newSeatCount=32).
   */
  public void changeSeatCount(
      Long userId, long newSeatCount, SubscriptionUpdateParams.ProrationBehavior prorationBehavior)
      throws StripeException {

    if (newSeatCount < 1) {
      throw new IllegalArgumentException("Seat count must be >= 1");
    }

    UserSubscription userSub =
        subscriptionRepository
            .findByUserId(userId)
            .orElseThrow(
                () ->
                    new SubscriptionNotFoundException(
                        "Subscription not found for user: " + userId));

    if (userSub.getStatus() != SubscriptionStatus.ACTIVE) {
      throw new StripeServiceException("Cannot change seats on inactive subscription", null);
    }

    Subscription subscription = Subscription.retrieve(userSub.getStripeSubscriptionId());
    SubscriptionItem item = firstSubscriptionItem(subscription);

    SubscriptionUpdateParams params =
        SubscriptionUpdateParams.builder()
            .addItem(
                SubscriptionUpdateParams.Item.builder()
                    .setId(item.getId())
                    .setQuantity(newSeatCount)
                    .build())
            .setProrationBehavior(prorationBehavior)
            .build();

    subscription.update(params);

    // Store updatedAt; seat count is derived from Stripe; full sync via webhook
    userSub.setUpdatedAt(OffsetDateTime.now());
    subscriptionRepository.save(userSub);
  }

  @Transactional
  public CancelSubscriptionResponse cancelSubscription(Long userId, boolean cancelAtPeriodEnd)
      throws StripeException {

    log.debug("Cancelling subscription for user {}, at period end: {}", userId, cancelAtPeriodEnd);

    UserSubscription userSub =
        subscriptionRepository
            .findByUserId(userId)
            .orElseThrow(
                () ->
                    new SubscriptionNotFoundException(
                        "Subscription not found for user: " + userId));

    Subscription subscription = Subscription.retrieve(userSub.getStripeSubscriptionId());

    if (cancelAtPeriodEnd) {
      subscription.update(SubscriptionUpdateParams.builder().setCancelAtPeriodEnd(true).build());
      userSub.setCancelAtPeriodEnd(true);
    } else {
      subscription.cancel();
      userSub.setStatus(SubscriptionStatus.CANCELED);
    }

    subscriptionRepository.save(userSub);
    log.debug("Successfully cancelled subscription for user {}", userId);
    return new CancelSubscriptionResponse(true, "Subscription cancelled successfully");
  }

  public UserSubscription createFreeLocalSubscription(
      Long userId,
      String stripeCustomerId,
      String planKey, // e.g. "huddey_free"
      String interval, // "month" | "year"
      String currency, // "EUR" | "USD" | "GBP"
      Long seats // usually 1
      ) {

    UserSubscription free = new UserSubscription();
    free.setUserId(userId);
    free.setStripeCustomerId(stripeCustomerId);
    free.setStripeSubscriptionId(null); // no Stripe sub for free tier
    free.setPlanKey(planKey);
    free.setBillingInterval(interval.toLowerCase());
    free.setCurrency(currency.toUpperCase());
    free.setSeatCount(seats);
    free.setStatus(SubscriptionStatus.ACTIVE);
    free.setCancelAtPeriodEnd(false);

    var now = OffsetDateTime.now();
    free.setCreatedAt(now);
    free.setUpdatedAt(now);
    free.setCurrentPeriodStart(now);
    // keep it far in the future; you control downgrades/upgrades by status/planKey
    free.setCurrentPeriodEnd(now.plusYears(100));

    return subscriptionRepository.save(free);
  }

  /** Optional pass-throughs you already had. */
  public Optional<UserSubscription> getUserSubscription(Long userId) {
    return subscriptionRepository.findByUserId(userId);
  }

  public Optional<List<ProductPrice>> getCurrentPlan(String planKey) {
    // If you maintain a price table locally, keep this. Otherwise, you can remove.
    return productPriceRepository.findByPlanId(planKey);
  }

  /* ----------------------- Webhook persistence helpers ----------------------- */

  public void saveSubscriptionFromWebhook(
      String stripeSubscriptionId,
      String stripeCustomerId,
      String planKey,
      SubscriptionStatus status,
      Long currentPeriodStart,
      Long currentPeriodEnd,
      boolean cancelAtPeriodEnd) {

    Optional<UserSubscription> existingSub =
        subscriptionRepository.findByStripeCustomerId(stripeCustomerId);

    if (existingSub.isEmpty()) {
      log.warn("No existing subscription found for customer {}", stripeCustomerId);
      return;
    }

    UserSubscription userSub = existingSub.get();
    userSub.setStripeSubscriptionId(stripeSubscriptionId);
    userSub.setPlanKey(planKey);
    userSub.setStatus(status);
    userSub.setCurrentPeriodStart(
        OffsetDateTime.ofInstant(
            Instant.ofEpochSecond(currentPeriodStart), ZoneId.systemDefault()));
    userSub.setCurrentPeriodEnd(
        OffsetDateTime.ofInstant(Instant.ofEpochSecond(currentPeriodEnd), ZoneId.systemDefault()));
    userSub.setCancelAtPeriodEnd(cancelAtPeriodEnd);
    userSub.setUpdatedAt(OffsetDateTime.now());

    subscriptionRepository.save(userSub);
    log.debug("Updated subscription from webhook for customer {}", stripeCustomerId);
  }

  /* ----------------------- Price & Customer utilities ----------------------- */

  /** Resolve a Stripe Price by our stable lookup key. */
  private String resolvePriceIdByLookupKey(String lookupKey) throws StripeException {
    PriceListParams params =
        PriceListParams.builder().addLookupKey(lookupKey).setActive(true).setLimit(1L).build();
    PriceCollection pc = Price.list(params);
    if (pc.getData().isEmpty()) {
      throw new StripeServiceException("No active price found for lookup_key: " + lookupKey, null);
    }
    return pc.getData().getFirst().getId();
  }

  /** Build our canonical lookup key: planKey:interval:CURRENCY */
  private static String lookupKey(String planKey, String interval, String currency) {
    return planKey
        + ":"
        + interval.toLowerCase(Locale.ROOT)
        + ":"
        + currency.toUpperCase(Locale.ROOT);
  }

  /** First (and only) subscription item helper. */
  private static SubscriptionItem firstSubscriptionItem(Subscription sub) {
    if (sub.getItems() == null || sub.getItems().getData().isEmpty()) {
      throw new IllegalStateException("Subscription has no items");
    }
    return sub.getItems().getData().getFirst();
  }

  /**
   * Get or create Stripe Customer bound to the user.
   *
   * @param userId
   * @param email
   * @return
   * @throws StripeException
   */
  public Customer getOrCreateStripeCustomer(Long userId, String email) throws StripeException {
    log.debug("Getting or creating Stripe customer for user {}", userId);
    Optional<UserSubscription> existing = subscriptionRepository.findByUserId(userId);
    if (existing.isPresent() && existing.get().getStripeCustomerId() != null) {
      return Customer.retrieve(existing.get().getStripeCustomerId());
    }
    CustomerCreateParams params =
        CustomerCreateParams.builder()
            .setEmail(email)
            .putMetadata("userId", userId.toString())
            .build();
    Customer customer = Customer.create(params);
    log.debug("Created Stripe customer {} for user {}", customer.getId(), userId);
    return customer;
  }

  /* ----------------------- Legacy helpers kept for compatibility ----------------------- */

  /** Kept for compatibility; prefer getOrCreateStripeCustomer(..) */
  public Customer getStripeCustomer(Long userId) throws StripeException {
    Optional<UserSubscription> existingSub = subscriptionRepository.findByUserId(userId);
    if (existingSub.isEmpty()) {
      throw new StripeServiceException("No customer found for user: " + userId, null);
    }
    return Customer.retrieve(existingSub.get().getStripeCustomerId());
  }

  /** If you still need direct active prices for a Stripe product id. */
  public List<Price> getActivePricesForProduct(String productId) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("product", productId);
      params.put("active", true);
      return Price.list(params).getData();
    } catch (StripeException e) {
      log.error("Failed to fetch active prices for product {}: {}", productId, e.getMessage());
      return new ArrayList<>();
    }
  }

  /**
   * Convenience: get current active price for currency within a single product id.
   *
   * @param planKey the plan key
   * @param interval the billing interval
   * @param currency the currency
   * @return Optional<ProductPrice>
   */
  public Optional<ProductPrice> getCurrentPrice(String planKey, String interval, String currency) {
    // Fallback-safety on case and nulls
    String i = interval == null ? "month" : interval.toLowerCase();
    String c = currency == null ? "EUR" : currency.toUpperCase();

    // If you have a repository method for this, prefer it.
    // Otherwise fetch by planId and filter in-memory.
    return productPriceRepository
        .findByPlanId(planKey)
        .flatMap(
            list ->
                list.stream()
                    .filter(p -> Boolean.TRUE.equals(p.getActive()))
                    .filter(p -> i.equalsIgnoreCase(p.getRecurringInterval()))
                    .filter(p -> c.equalsIgnoreCase(p.getCurrency()))
                    .findFirst());
  }
}
