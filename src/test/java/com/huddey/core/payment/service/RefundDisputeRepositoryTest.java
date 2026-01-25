package com.huddey.core.payment.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.huddey.core.config.TestConfig;
import com.huddey.core.payment.data.entity.Dispute;
import com.huddey.core.payment.data.entity.Refund;
import com.huddey.core.payment.repository.DisputeRepository;
import com.huddey.core.payment.repository.RefundRepository;

@DataJpaTest
@Import(TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class RefundDisputeRepositoryTest {

  @Autowired private RefundRepository refundRepository;
  @Autowired private DisputeRepository disputeRepository;

  private Refund testRefund;
  private Dispute testDispute;

  @BeforeEach
  void setUp() {
    testRefund = new Refund();
    testRefund.setStripeRefundId("re_test123");
    testRefund.setStripeChargeId("ch_test123");
    testRefund.setUserId(1L);
    testRefund.setAmount(2000L);
    testRefund.setCurrency("USD");
    testRefund.setReason("requested_by_customer");
    testRefund.setStatus("succeeded");

    testDispute = new Dispute();
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
    Refund savedRefund = refundRepository.save(testRefund);

    assertNotNull(savedRefund.getId());
    assertEquals("re_test123", savedRefund.getStripeRefundId());
    assertEquals(1L, savedRefund.getUserId());
    assertEquals(2000L, savedRefund.getAmount());
  }

  @Test
  void refundRepository_FindByStripeRefundId_ReturnsCorrectRefund() {
    refundRepository.save(testRefund);

    Optional<Refund> found = refundRepository.findByStripeRefundId("re_test123");

    assertTrue(found.isPresent());
    assertEquals("re_test123", found.get().getStripeRefundId());
    assertEquals("ch_test123", found.get().getStripeChargeId());
  }

  @Test
  void refundRepository_FindByUserId_ReturnsUserRefunds() {
    Refund secondRefund = new Refund();
    secondRefund.setStripeRefundId("re_test456");
    secondRefund.setStripeChargeId("ch_test456");
    secondRefund.setUserId(1L);
    secondRefund.setAmount(1000L);
    secondRefund.setCurrency("USD");
    secondRefund.setReason("duplicate");
    secondRefund.setStatus("succeeded");

    refundRepository.save(testRefund);
    refundRepository.save(secondRefund);

    List<Refund> userRefunds = refundRepository.findByUserId(1L);

    assertEquals(2, userRefunds.size());
    assertTrue(userRefunds.stream().allMatch(r -> r.getUserId().equals(1L)));
  }

  @Test
  void disputeRepository_SaveAndFind_WorksCorrectly() {
    Dispute savedDispute = disputeRepository.save(testDispute);

    assertNotNull(savedDispute.getId());
    assertEquals("dp_test123", savedDispute.getStripeDisputeId());
    assertEquals(1L, savedDispute.getUserId());
    assertEquals(2000L, savedDispute.getAmount());
    assertNotNull(savedDispute.getEvidenceDueBy());
  }

  @Test
  void disputeRepository_FindByStripeDisputeId_ReturnsCorrectDispute() {
    disputeRepository.save(testDispute);

    Optional<Dispute> found = disputeRepository.findByStripeDisputeId("dp_test123");

    assertTrue(found.isPresent());
    assertEquals("dp_test123", found.get().getStripeDisputeId());
    assertEquals("fraudulent", found.get().getReason());
  }

  @Test
  void disputeRepository_FindByStatus_ReturnsDisputesWithStatus() {
    Dispute closedDispute = new Dispute();
    closedDispute.setStripeDisputeId("dp_closed123");
    closedDispute.setStripeChargeId("ch_closed123");
    closedDispute.setUserId(2L);
    closedDispute.setAmount(1500L);
    closedDispute.setCurrency("USD");
    closedDispute.setReason("fraudulent");
    closedDispute.setStatus("lost");

    disputeRepository.save(testDispute);
    disputeRepository.save(closedDispute);

    List<Dispute> needsResponseDisputes = disputeRepository.findByStatus("needs_response");
    List<Dispute> lostDisputes = disputeRepository.findByStatus("lost");

    assertEquals(1, needsResponseDisputes.size());
    assertEquals("needs_response", needsResponseDisputes.getFirst().getStatus());

    assertEquals(1, lostDisputes.size());
    assertEquals("lost", lostDisputes.getFirst().getStatus());
  }
}
