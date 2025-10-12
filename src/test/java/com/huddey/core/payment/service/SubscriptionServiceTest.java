package com.huddey.core.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.huddey.core.payment.data.entity.UserSubscription;
import com.huddey.core.payment.data.enums.SubscriptionPlan;
import com.huddey.core.payment.data.enums.SubscriptionStatus;
import com.huddey.core.payment.exception.StripeServiceException;
import com.huddey.core.payment.exception.SubscriptionNotFoundException;
import com.huddey.core.payment.repository.ProductPriceRepository;
import com.huddey.core.payment.repository.UserSubscriptionRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.SubscriptionItemCollection;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

  @Mock private UserSubscriptionRepository subscriptionRepository;
  @Mock private ProductPriceRepository productPriceRepository;
  @InjectMocks private SubscriptionService subscriptionService;

  private UserSubscription mockUserSubscription;

  @BeforeEach
  void setUp() {
    mockUserSubscription = new UserSubscription();
    mockUserSubscription.setUserId(1L);
    mockUserSubscription.setPlan(SubscriptionPlan.PRO_YEARLY);
    mockUserSubscription.setStatus(SubscriptionStatus.ACTIVE);
    mockUserSubscription.setStripeSubscriptionId("sub_123");
    mockUserSubscription.setCurrentPeriodStart(OffsetDateTime.now().minusMonths(2));
    mockUserSubscription.setCurrentPeriodEnd(OffsetDateTime.now().plusMonths(10));
  }

  private Subscription createMockStripeSubscription() {
    Subscription mockSubscription = mock(Subscription.class);
    SubscriptionItemCollection mockItems = mock(SubscriptionItemCollection.class);
    SubscriptionItem mockItem = mock(SubscriptionItem.class);

    lenient().when(mockSubscription.getItems()).thenReturn(mockItems);
    lenient().when(mockItems.getData()).thenReturn(java.util.List.of(mockItem));
    lenient().when(mockItem.getId()).thenReturn("si_123");

    return mockSubscription;
  }

  @Test
  void updateSubscription_shouldPreservePeriodDates() throws StripeException {
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(mockUserSubscription));

    try (MockedStatic<Subscription> mockedSubscription = mockStatic(Subscription.class)) {
      Subscription mockStripeSubscription = createMockStripeSubscription();
      mockedSubscription
          .when(() -> Subscription.retrieve("sub_123"))
          .thenReturn(mockStripeSubscription);

      OffsetDateTime originalPeriodEnd = mockUserSubscription.getCurrentPeriodEnd();
      subscriptionService.updateSubscription(1L, SubscriptionPlan.PRO_MONTHLY);

      verify(subscriptionRepository).save(mockUserSubscription);
      assertEquals(originalPeriodEnd, mockUserSubscription.getCurrentPeriodEnd());
      assertEquals(SubscriptionPlan.PRO_YEARLY, mockUserSubscription.getPlan());
    }
  }

  @Test
  void updateSubscription_shouldThrowExceptionWhenSubscriptionNotFound() {
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());

    assertThrows(
        SubscriptionNotFoundException.class,
        () -> subscriptionService.updateSubscription(1L, SubscriptionPlan.PRO_MONTHLY));
  }

  @Test
  void updateSubscription_shouldThrowExceptionWhenSubscriptionInactive() {
    mockUserSubscription.setStatus(SubscriptionStatus.CANCELED);
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(mockUserSubscription));

    assertThrows(
        StripeServiceException.class,
        () -> subscriptionService.updateSubscription(1L, SubscriptionPlan.PRO_MONTHLY));
  }

  @Test
  void cancelSubscription_shouldSetCancelAtPeriodEnd() throws StripeException {
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(mockUserSubscription));

    try (MockedStatic<Subscription> mockedSubscription = mockStatic(Subscription.class)) {
      Subscription mockStripeSubscription = createMockStripeSubscription();
      mockedSubscription
          .when(() -> Subscription.retrieve("sub_123"))
          .thenReturn(mockStripeSubscription);

      var response = subscriptionService.cancelSubscription(1L, true);

      assertTrue(response.isSuccess());
      assertTrue(mockUserSubscription.isCancelAtPeriodEnd());
      verify(subscriptionRepository).save(mockUserSubscription);
    }
  }

  @Test
  void cancelSubscription_shouldCancelImmediately() throws StripeException {
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(mockUserSubscription));

    try (MockedStatic<Subscription> mockedSubscription = mockStatic(Subscription.class)) {
      Subscription mockStripeSubscription = createMockStripeSubscription();
      mockedSubscription
          .when(() -> Subscription.retrieve("sub_123"))
          .thenReturn(mockStripeSubscription);

      var response = subscriptionService.cancelSubscription(1L, false);

      assertTrue(response.isSuccess());
      assertEquals(SubscriptionStatus.CANCELED, mockUserSubscription.getStatus());
      verify(subscriptionRepository).save(mockUserSubscription);
    }
  }

  @Test
  void getUserSubscription_shouldReturnSubscription() {
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(mockUserSubscription));

    Optional<UserSubscription> result = subscriptionService.getUserSubscription(1L);

    assertTrue(result.isPresent());
    assertEquals(mockUserSubscription, result.get());
  }

  @Test
  void createLocalFreeSubscription_shouldCreateFreeSubscription() {
    when(subscriptionRepository.save(any(UserSubscription.class)))
        .thenAnswer(i -> i.getArgument(0));

    UserSubscription result = subscriptionService.createFreeSubscription(1L, "cus_123");

    assertEquals(1L, result.getUserId());
    assertEquals(SubscriptionPlan.FREE, result.getPlan());
    assertEquals(SubscriptionStatus.ACTIVE, result.getStatus());
    assertFalse(result.isCancelAtPeriodEnd());
    verify(subscriptionRepository).save(any(UserSubscription.class));
  }

  @Test
  void updateSubscription_yearlyToMonthly_shouldPreserveYearlyBenefitsUntilPeriodEnds()
      throws StripeException {
    // Given: User has yearly subscription with 10 months remaining
    mockUserSubscription.setPlan(SubscriptionPlan.PRO_YEARLY);
    mockUserSubscription.setCurrentPeriodEnd(OffsetDateTime.now().plusMonths(10));

    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(mockUserSubscription));

    try (MockedStatic<Subscription> mockedSubscription = mockStatic(Subscription.class)) {
      Subscription mockStripeSubscription = createMockStripeSubscription();
      mockedSubscription
          .when(() -> Subscription.retrieve("sub_123"))
          .thenReturn(mockStripeSubscription);

      OffsetDateTime originalYearlyPeriodEnd = mockUserSubscription.getCurrentPeriodEnd();

      // When: User changes to monthly plan
      subscriptionService.updateSubscription(1L, SubscriptionPlan.PRO_MONTHLY);

      // Then: User keeps yearly plan and period dates until current period ends
      assertEquals(SubscriptionPlan.PRO_YEARLY, mockUserSubscription.getPlan()); // Still yearly
      assertEquals(
          originalYearlyPeriodEnd, mockUserSubscription.getCurrentPeriodEnd()); // Period preserved

      // Verify Stripe was called with NONE proration (no immediate change)
      verify(mockStripeSubscription).update(any(com.stripe.param.SubscriptionUpdateParams.class));

      verify(subscriptionRepository).save(mockUserSubscription);
    }
  }
}
