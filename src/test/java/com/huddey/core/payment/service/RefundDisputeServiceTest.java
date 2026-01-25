package com.huddey.core.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.huddey.core.payment.data.entity.Dispute;
import com.huddey.core.payment.data.entity.Refund;
import com.huddey.core.payment.data.entity.UserSubscription;
import com.huddey.core.payment.data.enums.SubscriptionStatus;
import com.huddey.core.payment.repository.DisputeRepository;
import com.huddey.core.payment.repository.RefundRepository;
import com.huddey.core.payment.repository.UserSubscriptionRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RefundDisputeServiceTest {

  @Mock private RefundRepository refundRepository;
  @Mock private DisputeRepository disputeRepository;
  @Mock private UserSubscriptionRepository subscriptionRepository;

  @InjectMocks private RefundDisputeService refundDisputeService;

  private com.stripe.model.Refund mockStripeRefund;
  private com.stripe.model.Dispute mockStripeDispute;
  private UserSubscription mockSubscription;
  private Charge mockCharge;

  @BeforeEach
  void setUp() {
    mockStripeRefund = mock(com.stripe.model.Refund.class);
    mockStripeDispute = mock(com.stripe.model.Dispute.class);
    mockSubscription = new UserSubscription();
    mockCharge = mock(Charge.class);

    // Setup default mock behaviors
    when(mockStripeRefund.getId()).thenReturn("re_test123");
    when(mockStripeRefund.getCharge()).thenReturn("ch_test123");
    when(mockStripeRefund.getAmount()).thenReturn(2000L);
    when(mockStripeRefund.getCurrency()).thenReturn("usd");
    when(mockStripeRefund.getReason()).thenReturn("requested_by_customer");
    when(mockStripeRefund.getStatus()).thenReturn("succeeded");

    when(mockStripeDispute.getId()).thenReturn("dp_test123");
    when(mockStripeDispute.getCharge()).thenReturn("ch_test123");
    when(mockStripeDispute.getAmount()).thenReturn(2000L);
    when(mockStripeDispute.getCurrency()).thenReturn("usd");
    when(mockStripeDispute.getReason()).thenReturn("fraudulent");
    when(mockStripeDispute.getStatus()).thenReturn("warning_needs_response");

    mockSubscription.setId(1L);
    mockSubscription.setUserId(100L);
    mockSubscription.setStripeCustomerId("cus_test123");
    mockSubscription.setStripeSubscriptionId("sub_test123");
    mockSubscription.setPlanKey("huddey_pro");
    mockSubscription.setStatus(SubscriptionStatus.ACTIVE);
    mockSubscription.setCurrency("USD");
  }

  @Test
  void handleRefund_NewRefund_SavesRefundToDatabase() {
    // Arrange
    when(refundRepository.findByStripeRefundId("re_test123")).thenReturn(Optional.empty());
    when(mockCharge.getCustomer()).thenReturn("cus_test123");
    when(subscriptionRepository.findByStripeCustomerId("cus_test123"))
        .thenReturn(Optional.of(mockSubscription));

    try (var chargeStatic = mockStatic(Charge.class)) {
      chargeStatic.when(() -> Charge.retrieve("ch_test123")).thenReturn(mockCharge);

      // Act
      refundDisputeService.handleRefund(mockStripeRefund);

      // Assert
      verify(refundRepository)
          .save(
              argThat(
                  refund ->
                      refund.getStripeRefundId().equals("re_test123")
                          && refund.getUserId().equals(100L)
                          && refund.getAmount().equals(2000L)
                          && refund.getCurrency().equals("USD")
                          && refund.getStatus().equals("succeeded")));
    }
  }

  @Test
  void handleRefund_ExistingRefund_UpdatesStatus() {
    // Arrange
    Refund existingRefund = new Refund();
    existingRefund.setStripeRefundId("re_test123");
    existingRefund.setStatus("pending");

    when(refundRepository.findByStripeRefundId("re_test123"))
        .thenReturn(Optional.of(existingRefund));

    // Act
    refundDisputeService.handleRefund(mockStripeRefund);

    // Assert
    verify(refundRepository)
        .save(
            argThat(
                refund ->
                    refund.getStatus().equals("succeeded")
                        && refund.getReason().equals("requested_by_customer")));
  }

  @Test
  void handleRefund_FullRefund_DowngradesToFreePlan() {
    // Arrange
    when(refundRepository.findByStripeRefundId("re_test123")).thenReturn(Optional.empty());
    when(mockCharge.getCustomer()).thenReturn("cus_test123");
    when(mockCharge.getAmount()).thenReturn(2000L); // Same as refund amount
    when(subscriptionRepository.findByStripeCustomerId("cus_test123"))
        .thenReturn(Optional.of(mockSubscription));
    when(subscriptionRepository.findByUserId(100L)).thenReturn(Optional.of(mockSubscription));

    try (var chargeStatic = mockStatic(Charge.class)) {
      chargeStatic.when(() -> Charge.retrieve("ch_test123")).thenReturn(mockCharge);

      // Act
      refundDisputeService.handleRefund(mockStripeRefund);

      // Assert
      verify(subscriptionRepository)
          .save(
              argThat(
                  sub ->
                      sub.getPlanKey().equals("huddey_free")
                          && sub.getStripeSubscriptionId() == null
                          && sub.getSeatCount().equals(1L)
                          && sub.getStatus() == SubscriptionStatus.ACTIVE));
    }
  }

  @Test
  void handleRefund_PartialRefund_NoDowngrade() {
    // Arrange
    when(refundRepository.findByStripeRefundId("re_test123")).thenReturn(Optional.empty());
    when(mockCharge.getCustomer()).thenReturn("cus_test123");
    when(mockCharge.getAmount()).thenReturn(5000L); // More than refund amount
    when(subscriptionRepository.findByStripeCustomerId("cus_test123"))
        .thenReturn(Optional.of(mockSubscription));
    when(subscriptionRepository.findByUserId(100L)).thenReturn(Optional.of(mockSubscription));

    try (var chargeStatic = mockStatic(Charge.class)) {
      chargeStatic.when(() -> Charge.retrieve("ch_test123")).thenReturn(mockCharge);

      // Act
      refundDisputeService.handleRefund(mockStripeRefund);

      // Assert
      verify(subscriptionRepository, never()).save(any(UserSubscription.class));
    }
  }

  @Test
  void handleDisputeCreated_NewDispute_SavesDisputeToDatabase() {
    // Arrange
    when(disputeRepository.findByStripeDisputeId("dp_test123")).thenReturn(Optional.empty());
    when(mockCharge.getCustomer()).thenReturn("cus_test123");
    when(subscriptionRepository.findByStripeCustomerId("cus_test123"))
        .thenReturn(Optional.of(mockSubscription));

    try (var chargeStatic = mockStatic(Charge.class)) {
      chargeStatic.when(() -> Charge.retrieve("ch_test123")).thenReturn(mockCharge);

      // Act
      refundDisputeService.handleDisputeCreated(mockStripeDispute);

      // Assert
      verify(disputeRepository)
          .save(
              argThat(
                  dispute ->
                      dispute.getStripeDisputeId().equals("dp_test123")
                          && dispute.getUserId().equals(100L)
                          && dispute.getAmount().equals(2000L)
                          && dispute.getCurrency().equals("USD")
                          && dispute.getStatus().equals("warning_needs_response")));
    }
  }

  @Test
  void handleDisputeUpdated_ExistingDispute_UpdatesStatus() {
    // Arrange
    Dispute existingDispute = new Dispute();
    existingDispute.setStripeDisputeId("dp_test123");
    existingDispute.setStatus("warning_needs_response");

    when(disputeRepository.findByStripeDisputeId("dp_test123"))
        .thenReturn(Optional.of(existingDispute));
    when(mockStripeDispute.getStatus()).thenReturn("warning_under_review");

    // Act
    refundDisputeService.handleDisputeUpdated(mockStripeDispute);

    // Assert
    verify(disputeRepository)
        .save(argThat(dispute -> dispute.getStatus().equals("warning_under_review")));
  }

  @Test
  void handleDisputeClosed_DisputeLost_DowngradesToFreePlan() {
    // Arrange
    Dispute existingDispute = new Dispute();
    existingDispute.setStripeDisputeId("dp_test123");
    existingDispute.setUserId(100L);

    when(disputeRepository.findByStripeDisputeId("dp_test123"))
        .thenReturn(Optional.of(existingDispute));
    when(mockStripeDispute.getStatus()).thenReturn("lost");
    when(subscriptionRepository.findByUserId(100L)).thenReturn(Optional.of(mockSubscription));

    // Act
    refundDisputeService.handleDisputeClosed(mockStripeDispute);

    // Assert
    verify(subscriptionRepository)
        .save(
            argThat(
                sub ->
                    sub.getPlanKey().equals("huddey_free")
                        && sub.getStripeSubscriptionId() == null));
  }

  @Test
  void handleDisputeClosed_DisputeWon_KeepsSubscriptionActive() {
    // Arrange
    Dispute existingDispute = new Dispute();
    existingDispute.setStripeDisputeId("dp_test123");
    existingDispute.setUserId(100L);

    when(disputeRepository.findByStripeDisputeId("dp_test123"))
        .thenReturn(Optional.of(existingDispute));
    when(mockStripeDispute.getStatus()).thenReturn("won");
    when(subscriptionRepository.findByUserId(100L)).thenReturn(Optional.of(mockSubscription));

    // Act
    refundDisputeService.handleDisputeClosed(mockStripeDispute);

    // Assert
    verify(subscriptionRepository, never()).save(any(UserSubscription.class));
    verify(disputeRepository).save(argThat(dispute -> dispute.getStatus().equals("won")));
  }

  @Test
  void handleRefund_StripeException_HandlesGracefully() {
    // Arrange
    when(refundRepository.findByStripeRefundId("re_test123")).thenReturn(Optional.empty());

    try (var chargeStatic = mockStatic(Charge.class)) {
      chargeStatic
          .when(() -> Charge.retrieve("ch_test123"))
          .thenThrow(new StripeException("API Error", "request_id", "code", 400) {});

      // Act & Assert - Should not throw exception
      assertDoesNotThrow(() -> refundDisputeService.handleRefund(mockStripeRefund));

      // Should still save refund even if charge retrieval fails
      verify(refundRepository, never()).save(any(Refund.class));
    }
  }

  @Test
  void handleRefund_NoUserFound_DoesNotSaveRefund() {
    // Arrange
    when(refundRepository.findByStripeRefundId("re_test123")).thenReturn(Optional.empty());
    when(mockCharge.getCustomer()).thenReturn("cus_test123");
    when(subscriptionRepository.findByStripeCustomerId("cus_test123")).thenReturn(Optional.empty());

    try (var chargeStatic = mockStatic(Charge.class)) {
      chargeStatic.when(() -> Charge.retrieve("ch_test123")).thenReturn(mockCharge);

      // Act
      refundDisputeService.handleRefund(mockStripeRefund);

      // Assert
      verify(refundRepository, never()).save(any(Refund.class));
    }
  }
}
