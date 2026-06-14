package com.huddey.core.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.huddey.core.payment.data.entity.Dispute;
import com.huddey.core.payment.data.entity.Refund;
import com.huddey.core.payment.repository.DisputeRepository;
import com.huddey.core.payment.repository.RefundRepository;

@ExtendWith(MockitoExtension.class)
class RefundDisputeRepositoryTest {

  @Mock private RefundRepository refundRepository;
  @Mock private DisputeRepository disputeRepository;

  private Refund testRefund;
  private Dispute testDispute;

  @BeforeEach
  void setUp() {
    testRefund = new Refund();
    testRefund.setId(100L); // Set ID since we're mocking save
    testRefund.setStripeRefundId("re_test123");
    testRefund.setStripeChargeId("ch_test123");
    testRefund.setUserId(1L);
    testRefund.setAmount(2000L);
    testRefund.setCurrency("USD");
    testRefund.setReason("requested_by_customer");
    testRefund.setStatus("succeeded");

    testDispute = new Dispute();
    testDispute.setId(200L); // Set ID since we're mocking save
    testDispute.setStripeDisputeId("dp_test123");
    testDispute.setStripeChargeId("ch_test123");
    testDispute.setUserId(1L);
    testDispute.setAmount(2000L);
    testDispute.setCurrency("USD");
    testDispute.setReason("fraudulent");
    testDispute.setStatus("needs_response");
    testDispute.setEvidenceDueBy(OffsetDateTime.now().plusDays(7));
  }

  @Test
  void refundRepository_SaveAndFind_WorksCorrectly() {
    when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);

    Refund savedRefund = refundRepository.save(testRefund);

    assertNotNull(savedRefund.getId());
    assertEquals("re_test123", savedRefund.getStripeRefundId());
    assertEquals(1L, savedRefund.getUserId());
    assertEquals(2000L, savedRefund.getAmount());
    verify(refundRepository).save(testRefund);
  }

  @Test
  void refundRepository_FindByStripeRefundId_ReturnsCorrectRefund() {
    when(refundRepository.findByStripeRefundId("re_test123")).thenReturn(Optional.of(testRefund));

    Optional<Refund> found = refundRepository.findByStripeRefundId("re_test123");

    assertTrue(found.isPresent());
    assertEquals("re_test123", found.get().getStripeRefundId());
    assertEquals("ch_test123", found.get().getStripeChargeId());
    verify(refundRepository).findByStripeRefundId("re_test123");
  }

  @Test
  void refundRepository_FindByUserId_ReturnsUserRefunds() {
    Refund secondRefund = new Refund();
    secondRefund.setId(101L);
    secondRefund.setStripeRefundId("re_test456");
    secondRefund.setStripeChargeId("ch_test456");
    secondRefund.setUserId(1L);
    secondRefund.setAmount(1000L);
    secondRefund.setCurrency("USD");
    secondRefund.setReason("duplicate");
    secondRefund.setStatus("succeeded");

    when(refundRepository.findByUserId(1L)).thenReturn(List.of(testRefund, secondRefund));

    List<Refund> userRefunds = refundRepository.findByUserId(1L);

    assertEquals(2, userRefunds.size());
    assertTrue(userRefunds.stream().allMatch(r -> r.getUserId().equals(1L)));
    verify(refundRepository).findByUserId(1L);
  }

  @Test
  void disputeRepository_SaveAndFind_WorksCorrectly() {
    when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

    Dispute savedDispute = disputeRepository.save(testDispute);

    assertNotNull(savedDispute.getId());
    assertEquals("dp_test123", savedDispute.getStripeDisputeId());
    assertEquals(1L, savedDispute.getUserId());
    assertEquals(2000L, savedDispute.getAmount());
    assertNotNull(savedDispute.getEvidenceDueBy());
    verify(disputeRepository).save(testDispute);
  }

  @Test
  void disputeRepository_FindByStripeDisputeId_ReturnsCorrectDispute() {
    when(disputeRepository.findByStripeDisputeId("dp_test123"))
        .thenReturn(Optional.of(testDispute));

    Optional<Dispute> found = disputeRepository.findByStripeDisputeId("dp_test123");

    assertTrue(found.isPresent());
    assertEquals("dp_test123", found.get().getStripeDisputeId());
    assertEquals("fraudulent", found.get().getReason());
    verify(disputeRepository).findByStripeDisputeId("dp_test123");
  }

  @Test
  void disputeRepository_FindByStatus_ReturnsDisputesWithStatus() {
    Dispute closedDispute = new Dispute();
    closedDispute.setId(201L);
    closedDispute.setStripeDisputeId("dp_closed123");
    closedDispute.setStripeChargeId("ch_closed123");
    closedDispute.setUserId(2L);
    closedDispute.setAmount(1500L);
    closedDispute.setCurrency("USD");
    closedDispute.setReason("fraudulent");
    closedDispute.setStatus("lost");

    when(disputeRepository.findByStatus("needs_response")).thenReturn(List.of(testDispute));
    when(disputeRepository.findByStatus("lost")).thenReturn(List.of(closedDispute));

    List<Dispute> needsResponseDisputes = disputeRepository.findByStatus("needs_response");
    List<Dispute> lostDisputes = disputeRepository.findByStatus("lost");

    assertEquals(1, needsResponseDisputes.size());
    assertEquals("needs_response", needsResponseDisputes.get(0).getStatus());

    assertEquals(1, lostDisputes.size());
    assertEquals("lost", lostDisputes.get(0).getStatus());

    verify(disputeRepository).findByStatus("needs_response");
    verify(disputeRepository).findByStatus("lost");
  }
}
