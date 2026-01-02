package com.huddey.core.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.huddey.core.payment.config.PlanKey;
import com.huddey.core.payment.data.entity.UserSubscription;
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
    mockUserSubscription.setPlanKey(PlanKey.PRO.getKey());
    mockUserSubscription.setStatus(SubscriptionStatus.ACTIVE);
    mockUserSubscription.setStripeSubscriptionId("sub_123");
    mockUserSubscription.setCurrentPeriodStart(OffsetDateTime.now().minusMonths(2));
    mockUserSubscription.setCurrentPeriodEnd(OffsetDateTime.now().plusMonths(10));
  }

  private Subscription createMockStripeSubscription() {
    Subscription mockSubscription = mock(Subscription.class);
    SubscriptionItemCollection mockItems = mock(SubscriptionItemCollection.class);
    SubscriptionItem mockItem = mock(SubscriptionItem.class);
    com.stripe.model.Price mockPrice = mock(com.stripe.model.Price.class);

    lenient().when(mockSubscription.getItems()).thenReturn(mockItems);
    lenient().when(mockItems.getData()).thenReturn(java.util.List.of(mockItem));
    lenient().when(mockItem.getId()).thenReturn("si_123");
    lenient().when(mockItem.getPrice()).thenReturn(mockPrice);
    lenient().when(mockPrice.getUnitAmount()).thenReturn(3900L);

    return mockSubscription;
  }

  @Test
  void switchPlan_shouldPreservePeriodDates() throws StripeException {
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(mockUserSubscription));

    try (MockedStatic<Subscription> mockedSubscription = mockStatic(Subscription.class);
        MockedStatic<com.stripe.model.Price> mockedPrice =
            mockStatic(com.stripe.model.Price.class)) {

      Subscription mockStripeSubscription = createMockStripeSubscription();
      mockedSubscription
          .when(() -> Subscription.retrieve("sub_123"))
          .thenReturn(mockStripeSubscription);

      com.stripe.model.PriceCollection mockPriceCollection =
          mock(com.stripe.model.PriceCollection.class);
      com.stripe.model.Price mockPrice = mock(com.stripe.model.Price.class);
      when(mockPriceCollection.getData()).thenReturn(java.util.List.of(mockPrice));
      when(mockPrice.getId()).thenReturn("price_123");

      mockedPrice.when(() -> com.stripe.model.Price.list(anyMap())).thenReturn(mockPriceCollection);
      mockedPrice
          .when(() -> com.stripe.model.Price.list(any(com.stripe.param.PriceListParams.class)))
          .thenReturn(mockPriceCollection);
      mockedPrice.when(() -> com.stripe.model.Price.retrieve("price_123")).thenReturn(mockPrice);
      when(mockPrice.getUnitAmount()).thenReturn(1500L);

      OffsetDateTime originalPeriodEnd = mockUserSubscription.getCurrentPeriodEnd();
      subscriptionService.switchPlan(1L, PlanKey.STARTER.getKey(), "month", "EUR");

      verify(subscriptionRepository).save(mockUserSubscription);
      assertEquals(originalPeriodEnd, mockUserSubscription.getCurrentPeriodEnd());
      assertEquals(PlanKey.PRO.getKey(), mockUserSubscription.getPlanKey());
    }
  }

  @Test
  void switchPlan_shouldThrowExceptionWhenSubscriptionNotFound() {
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());

    assertThrows(
        SubscriptionNotFoundException.class,
        () -> subscriptionService.switchPlan(1L, PlanKey.STARTER.getKey(), "month", "EUR"));
  }

  @Test
  void switchPlan_shouldThrowExceptionWhenSubscriptionInactive() {
    mockUserSubscription.setStatus(SubscriptionStatus.CANCELED);
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(mockUserSubscription));

    assertThrows(
        StripeServiceException.class,
        () -> subscriptionService.switchPlan(1L, PlanKey.STARTER.getKey(), "month", "EUR"));
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
  void createFreeLocalSubscription_shouldCreateFreeSubscription() {
    when(subscriptionRepository.save(any(UserSubscription.class)))
        .thenAnswer(i -> i.getArgument(0));

    UserSubscription result =
        subscriptionService.createFreeLocalSubscription(
            1L, "cus_123", PlanKey.FREE.getKey(), "month", "EUR", 1L);

    assertEquals(1L, result.getUserId());
    assertEquals(PlanKey.FREE.getKey(), result.getPlanKey());
    assertEquals(SubscriptionStatus.ACTIVE, result.getStatus());
    assertFalse(result.isCancelAtPeriodEnd());
    verify(subscriptionRepository).save(any(UserSubscription.class));
  }

  @Test
  void changeSeatCount_shouldUpdateSeats() throws StripeException {
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(mockUserSubscription));

    try (MockedStatic<Subscription> mockedSubscription = mockStatic(Subscription.class)) {
      Subscription mockStripeSubscription = createMockStripeSubscription();
      mockedSubscription
          .when(() -> Subscription.retrieve("sub_123"))
          .thenReturn(mockStripeSubscription);

      subscriptionService.changeSeatCount(
          1L, 5L, com.stripe.param.SubscriptionUpdateParams.ProrationBehavior.CREATE_PRORATIONS);

      verify(subscriptionRepository).save(mockUserSubscription);
    }
  }
}
