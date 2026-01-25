package com.huddey.core.payment.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.huddey.core.payment.data.entity.Dispute;
import com.huddey.core.payment.data.entity.Refund;
import com.huddey.core.payment.data.entity.UserSubscription;
import com.huddey.core.payment.data.enums.SubscriptionStatus;
import com.huddey.core.payment.repository.DisputeRepository;
import com.huddey.core.payment.repository.RefundRepository;
import com.huddey.core.payment.repository.UserSubscriptionRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RefundDisputeService {

  private final RefundRepository refundRepository;
  private final DisputeRepository disputeRepository;
  private final UserSubscriptionRepository subscriptionRepository;

  public RefundDisputeService(
      RefundRepository refundRepository,
      DisputeRepository disputeRepository,
      UserSubscriptionRepository subscriptionRepository) {
    this.refundRepository = refundRepository;
    this.disputeRepository = disputeRepository;
    this.subscriptionRepository = subscriptionRepository;
  }

  @Transactional
  public void handleRefund(com.stripe.model.Refund stripeRefund) {
    log.info("Processing refund: {}", stripeRefund.getId());

    Optional<Refund> existingRefund = refundRepository.findByStripeRefundId(stripeRefund.getId());
    if (existingRefund.isPresent()) {
      log.debug("Refund {} already exists, updating status", stripeRefund.getId());
      Refund refund = existingRefund.get();
      refund.setStatus(stripeRefund.getStatus());
      refund.setReason(stripeRefund.getReason());
      refundRepository.save(refund);
      return;
    }

    Long userId = getUserIdFromCharge(stripeRefund.getCharge());
    if (userId == null) {
      log.warn("Cannot find user for refund {}", stripeRefund.getId());
      return;
    }

    Refund refund = new Refund();
    refund.setStripeRefundId(stripeRefund.getId());
    refund.setStripeChargeId(stripeRefund.getCharge());
    refund.setUserId(userId);
    refund.setAmount(stripeRefund.getAmount());
    refund.setCurrency(stripeRefund.getCurrency().toUpperCase());
    refund.setReason(stripeRefund.getReason());
    refund.setStatus(stripeRefund.getStatus());

    refundRepository.save(refund);
    log.info("Saved refund {} for user {}", stripeRefund.getId(), userId);

    handleRefundBusinessLogic(userId, stripeRefund);
  }

  // @Transactional
  public void handleDisputeCreated(com.stripe.model.Dispute stripeDispute) {
    log.info("Processing dispute created: {}", stripeDispute.getId());

    Optional<Dispute> existingDispute =
        disputeRepository.findByStripeDisputeId(stripeDispute.getId());
    if (existingDispute.isPresent()) {
      log.debug("Dispute {} already exists", stripeDispute.getId());
      return;
    }

    Long userId = getUserIdFromCharge(stripeDispute.getCharge());
    if (userId == null) {
      log.warn("Cannot find user for dispute {}", stripeDispute.getId());
      return;
    }

    Dispute dispute = new Dispute();
    dispute.setStripeDisputeId(stripeDispute.getId());
    dispute.setStripeChargeId(stripeDispute.getCharge());
    dispute.setUserId(userId);
    dispute.setAmount(stripeDispute.getAmount());
    dispute.setCurrency(stripeDispute.getCurrency().toUpperCase());
    dispute.setReason(stripeDispute.getReason());
    dispute.setStatus(stripeDispute.getStatus());

    if (stripeDispute.getEvidenceDetails() != null
        && stripeDispute.getEvidenceDetails().getDueBy() != null) {
      dispute.setEvidenceDueBy(
          Instant.ofEpochSecond(stripeDispute.getEvidenceDetails().getDueBy())
              .atZone(ZoneId.systemDefault())
              .toOffsetDateTime());
    }

    disputeRepository.save(dispute);
    log.info("Saved dispute {} for user {}", stripeDispute.getId(), userId);

    handleDisputeCreatedBusinessLogic(userId, stripeDispute);
  }

  @Transactional
  public void handleDisputeUpdated(com.stripe.model.Dispute stripeDispute) {
    log.info("Processing dispute updated: {}", stripeDispute.getId());

    Optional<Dispute> existingDispute =
        disputeRepository.findByStripeDisputeId(stripeDispute.getId());
    if (existingDispute.isEmpty()) {
      log.warn("Dispute {} not found, creating new", stripeDispute.getId());
      this.handleDisputeCreated(stripeDispute);
      return;
    }

    Dispute dispute = existingDispute.get();
    dispute.setStatus(stripeDispute.getStatus());
    dispute.setReason(stripeDispute.getReason());

    if (stripeDispute.getEvidenceDetails() != null
        && stripeDispute.getEvidenceDetails().getDueBy() != null) {
      dispute.setEvidenceDueBy(
          Instant.ofEpochSecond(stripeDispute.getEvidenceDetails().getDueBy())
              .atZone(ZoneId.systemDefault())
              .toOffsetDateTime());
    }

    disputeRepository.save(dispute);
    log.info("Updated dispute {} with status {}", stripeDispute.getId(), stripeDispute.getStatus());
  }

  @Transactional
  public void handleDisputeClosed(com.stripe.model.Dispute stripeDispute) {
    log.info("Processing dispute closed: {}", stripeDispute.getId());

    Optional<Dispute> existingDispute =
        disputeRepository.findByStripeDisputeId(stripeDispute.getId());
    if (existingDispute.isEmpty()) {
      log.warn("Dispute {} not found", stripeDispute.getId());
      return;
    }

    Dispute dispute = existingDispute.get();
    dispute.setStatus(stripeDispute.getStatus());
    disputeRepository.save(dispute);

    handleDisputeClosedBusinessLogic(dispute.getUserId(), stripeDispute);
    log.info("Closed dispute {} with status {}", stripeDispute.getId(), stripeDispute.getStatus());
  }

  private void handleRefundBusinessLogic(Long userId, com.stripe.model.Refund stripeRefund) {
    if (!"succeeded".equals(stripeRefund.getStatus())) {
      return;
    }

    Optional<UserSubscription> subOpt = subscriptionRepository.findByUserId(userId);
    if (subOpt.isEmpty()) {
      log.debug("No subscription found for user {} during refund", userId);
      return;
    }

    UserSubscription subscription = subOpt.get();

    try {
      Charge charge = Charge.retrieve(stripeRefund.getCharge());
      Long chargeAmount = charge.getAmount();
      Long refundAmount = stripeRefund.getAmount();

      boolean isFullRefund = refundAmount.equals(chargeAmount);

      if (isFullRefund && subscription.getStripeSubscriptionId() != null) {
        log.info("Full refund detected for user {}, downgrading to free plan", userId);
        downgradeToFreePlan(subscription);
      } else {
        log.info("Partial refund of {} for user {}, no action taken", refundAmount, userId);
      }
    } catch (StripeException e) {
      log.error("Failed to retrieve charge {} for refund analysis", stripeRefund.getCharge(), e);
    }
  }

  private void handleDisputeCreatedBusinessLogic(
      Long userId, com.stripe.model.Dispute stripeDispute) {
    Optional<UserSubscription> subOpt = subscriptionRepository.findByUserId(userId);
    if (subOpt.isEmpty()) {
      return;
    }

    UserSubscription subscription = subOpt.get();
    if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
      log.warn(
          "Dispute created for user {}, subscription remains active pending resolution", userId);
    }
  }

  private void handleDisputeClosedBusinessLogic(
      Long userId, com.stripe.model.Dispute stripeDispute) {
    Optional<UserSubscription> subOpt = subscriptionRepository.findByUserId(userId);
    if (subOpt.isEmpty()) {
      return;
    }

    UserSubscription subscription = subOpt.get();

    if ("lost".equals(stripeDispute.getStatus())) {
      log.warn("Dispute lost for user {}, consider downgrading subscription", userId);
      if (subscription.getStripeSubscriptionId() != null) {
        downgradeToFreePlan(subscription);
      }
    } else if ("won".equals(stripeDispute.getStatus())) {
      log.info("Dispute won for user {}, subscription remains active", userId);
    }
  }

  private void downgradeToFreePlan(UserSubscription subscription) {
    String currency = subscription.getCurrency() != null ? subscription.getCurrency() : "EUR";
    subscription.setStripeSubscriptionId(null);
    subscription.setPlanKey("huddey_free");
    subscription.setBillingInterval("month");
    subscription.setCurrency(currency);
    subscription.setSeatCount(1L);
    subscription.setStatus(SubscriptionStatus.ACTIVE);
    subscription.setCancelAtPeriodEnd(false);
    subscription.setCurrentPeriodStart(OffsetDateTime.now());
    subscription.setCurrentPeriodEnd(OffsetDateTime.now().plusYears(100));
    subscriptionRepository.save(subscription);
    log.info("Downgraded user {} to free plan", subscription.getUserId());
  }

  private Long getUserIdFromCharge(String chargeId) {
    try {
      Charge charge = Charge.retrieve(chargeId);
      String customerId = charge.getCustomer();
      if (customerId != null) {
        Optional<UserSubscription> sub = subscriptionRepository.findByStripeCustomerId(customerId);
        return sub.map(UserSubscription::getUserId).orElse(null);
      }
    } catch (StripeException e) {
      log.error("Failed to retrieve charge {} to find user", chargeId, e);
    }
    return null;
  }
}
