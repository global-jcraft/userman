package com.huddey.core.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.huddey.core.payment.repository.UserSubscriptionRepository;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Price;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.SubscriptionItemCollection;
import com.stripe.model.checkout.Session;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

  @Mock private UserSubscriptionRepository subscriptionRepository;
  @InjectMocks private WebhookService webhookService;

  private Event mockEvent;
  private EventDataObjectDeserializer mockDeserializer;
  private UserSubscription mockUserSubscription;

  @BeforeEach
  void setUp() {
    mockEvent = mock(Event.class);
    mockDeserializer = mock(EventDataObjectDeserializer.class);
    lenient().when(mockEvent.getDataObjectDeserializer()).thenReturn(mockDeserializer);

    mockUserSubscription = new UserSubscription();
    mockUserSubscription.setUserId(1L);
    mockUserSubscription.setStripeSubscriptionId("sub_123");
    mockUserSubscription.setStatus(SubscriptionStatus.ACTIVE);
  }

  @Test
  void handleCheckoutSessionCompleted_shouldCreateNewSubscription() {
    Session mockSession = mock(Session.class);
    Map<String, String> metadata = new HashMap<>();
    metadata.put("userId", "1");
    metadata.put("plan", "PRO_MONTHLY");

    when(mockDeserializer.getObject()).thenReturn(Optional.of(mockSession));
    when(mockSession.getMode()).thenReturn("subscription");
    when(mockSession.getMetadata()).thenReturn(metadata);
    when(mockSession.getCustomer()).thenReturn("cus_123");
    when(mockSession.getSubscription()).thenReturn("sub_123");
    when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());

    Subscription mockStripeSubscription = mock(Subscription.class);
    SubscriptionItemCollection mockItems = mock(SubscriptionItemCollection.class);
    SubscriptionItem mockItem = mock(SubscriptionItem.class);

    when(mockStripeSubscription.getItems()).thenReturn(mockItems);
    when(mockItems.getData()).thenReturn(List.of(mockItem));
    when(mockItem.getCurrentPeriodStart()).thenReturn(1640995200L);
    when(mockItem.getCurrentPeriodEnd()).thenReturn(1672531200L);

    try (MockedStatic<Subscription> mockedSubscription = mockStatic(Subscription.class)) {
      mockedSubscription
          .when(() -> Subscription.retrieve("sub_123"))
          .thenReturn(mockStripeSubscription);

      webhookService.handleCheckoutSessionCompleted(mockEvent);

      verify(subscriptionRepository).save(any(UserSubscription.class));
    }
  }

  @Test
  void handleSubscriptionUpdated_shouldUpdateExistingSubscription() {
    Subscription mockSubscription = mock(Subscription.class);
    SubscriptionItemCollection mockItems = mock(SubscriptionItemCollection.class);
    SubscriptionItem mockItem = mock(SubscriptionItem.class);
    Price mockPrice = mock(Price.class);

    when(mockDeserializer.getObject()).thenReturn(Optional.of(mockSubscription));
    when(mockSubscription.getId()).thenReturn("sub_123");
    when(mockSubscription.getStatus()).thenReturn("active");
    when(mockSubscription.getCancelAtPeriodEnd()).thenReturn(false);
    when(mockSubscription.getItems()).thenReturn(mockItems);
    when(mockItems.getData()).thenReturn(List.of(mockItem));
    when(mockItem.getCurrentPeriodStart()).thenReturn(1640995200L);
    when(mockItem.getCurrentPeriodEnd()).thenReturn(1672531200L);
    when(mockItem.getPrice()).thenReturn(mockPrice);
    when(mockPrice.getId()).thenReturn("price_pro_monthly");

    when(subscriptionRepository.findByStripeSubscriptionId("sub_123"))
        .thenReturn(Optional.of(mockUserSubscription));

    try (MockedStatic<SubscriptionPlan> mockedPlan = mockStatic(SubscriptionPlan.class)) {
      mockedPlan
          .when(() -> SubscriptionPlan.fromStripePriceId("price_pro_monthly"))
          .thenReturn(SubscriptionPlan.PRO_MONTHLY);

      webhookService.handleSubscriptionUpdated(mockEvent);

      verify(subscriptionRepository).save(mockUserSubscription);
      assertEquals(SubscriptionStatus.ACTIVE, mockUserSubscription.getStatus());
      assertEquals(SubscriptionPlan.PRO_MONTHLY, mockUserSubscription.getPlan());
    }
  }

  @Test
  void handleSubscriptionDeleted_shouldCancelSubscription() {
    Subscription mockSubscription = mock(Subscription.class);

    when(mockDeserializer.getObject()).thenReturn(Optional.of(mockSubscription));
    when(mockSubscription.getId()).thenReturn("sub_123");
    when(subscriptionRepository.findByStripeSubscriptionId("sub_123"))
        .thenReturn(Optional.of(mockUserSubscription));

    webhookService.handleSubscriptionDeleted(mockEvent);

    verify(subscriptionRepository).save(mockUserSubscription);
    assertEquals(SubscriptionStatus.CANCELED, mockUserSubscription.getStatus());
  }

  @Test
  void handleSubscriptionCreated_shouldCreateSubscription() {
    Subscription mockSubscription = mock(Subscription.class);
    SubscriptionItemCollection mockItems = mock(SubscriptionItemCollection.class);
    SubscriptionItem mockItem = mock(SubscriptionItem.class);
    Price mockPrice = mock(Price.class);

    when(mockDeserializer.getObject()).thenReturn(Optional.of(mockSubscription));
    when(mockSubscription.getId()).thenReturn("sub_123");
    when(mockSubscription.getStatus()).thenReturn("active");
    when(mockSubscription.getCancelAtPeriodEnd()).thenReturn(false);
    when(mockSubscription.getItems()).thenReturn(mockItems);
    when(mockItems.getData()).thenReturn(List.of(mockItem));
    when(mockItem.getCurrentPeriodStart()).thenReturn(1640995200L);
    when(mockItem.getCurrentPeriodEnd()).thenReturn(1672531200L);
    when(mockItem.getPrice()).thenReturn(mockPrice);
    when(mockPrice.getId()).thenReturn("price_pro_monthly");

    when(subscriptionRepository.findByStripeSubscriptionId("sub_123"))
        .thenReturn(Optional.of(mockUserSubscription));

    try (MockedStatic<SubscriptionPlan> mockedPlan = mockStatic(SubscriptionPlan.class)) {
      mockedPlan
          .when(() -> SubscriptionPlan.fromStripePriceId("price_pro_monthly"))
          .thenReturn(SubscriptionPlan.PRO_MONTHLY);

      webhookService.handleSubscriptionCreated(mockEvent);

      verify(subscriptionRepository).save(mockUserSubscription);
    }
  }

  @Test
  void updateSubscriptionInDatabase_shouldUpdateAllFields() {
    Subscription mockSubscription = mock(Subscription.class);
    SubscriptionItemCollection mockItems = mock(SubscriptionItemCollection.class);
    SubscriptionItem mockItem = mock(SubscriptionItem.class);
    Price mockPrice = mock(Price.class);

    when(mockSubscription.getId()).thenReturn("sub_123");
    when(mockSubscription.getStatus()).thenReturn("past_due");
    when(mockSubscription.getCancelAtPeriodEnd()).thenReturn(true);
    when(mockSubscription.getItems()).thenReturn(mockItems);
    when(mockItems.getData()).thenReturn(List.of(mockItem));
    when(mockItem.getCurrentPeriodStart()).thenReturn(1640995200L);
    when(mockItem.getCurrentPeriodEnd()).thenReturn(1672531200L);
    when(mockItem.getPrice()).thenReturn(mockPrice);
    when(mockPrice.getId()).thenReturn("price_elite_yearly");

    when(subscriptionRepository.findByStripeSubscriptionId("sub_123"))
        .thenReturn(Optional.of(mockUserSubscription));

    try (MockedStatic<SubscriptionPlan> mockedPlan = mockStatic(SubscriptionPlan.class)) {
      mockedPlan
          .when(() -> SubscriptionPlan.fromStripePriceId("price_elite_yearly"))
          .thenReturn(SubscriptionPlan.ELITE_YEARLY);

      webhookService.updateSubscriptionInDatabase(mockSubscription);

      verify(subscriptionRepository).save(mockUserSubscription);
      assertEquals(SubscriptionStatus.PAST_DUE, mockUserSubscription.getStatus());
      assertEquals(SubscriptionPlan.ELITE_YEARLY, mockUserSubscription.getPlan());
      assertTrue(mockUserSubscription.isCancelAtPeriodEnd());
      assertNotNull(mockUserSubscription.getCurrentPeriodStart());
      assertNotNull(mockUserSubscription.getCurrentPeriodEnd());
    }
  }

  @Test
  void updateSubscriptionInDatabase_shouldHandleUnknownPriceId() {
    Subscription mockSubscription = mock(Subscription.class);
    SubscriptionItemCollection mockItems = mock(SubscriptionItemCollection.class);
    SubscriptionItem mockItem = mock(SubscriptionItem.class);
    Price mockPrice = mock(Price.class);

    when(mockSubscription.getId()).thenReturn("sub_123");
    when(mockSubscription.getStatus()).thenReturn("active");
    when(mockSubscription.getCancelAtPeriodEnd()).thenReturn(false);
    when(mockSubscription.getItems()).thenReturn(mockItems);
    when(mockItems.getData()).thenReturn(List.of(mockItem));
    when(mockItem.getCurrentPeriodStart()).thenReturn(1640995200L);
    when(mockItem.getCurrentPeriodEnd()).thenReturn(1672531200L);
    when(mockItem.getPrice()).thenReturn(mockPrice);
    when(mockPrice.getId()).thenReturn("unknown_price_id");

    when(subscriptionRepository.findByStripeSubscriptionId("sub_123"))
        .thenReturn(Optional.of(mockUserSubscription));

    try (MockedStatic<SubscriptionPlan> mockedPlan = mockStatic(SubscriptionPlan.class)) {
      mockedPlan
          .when(() -> SubscriptionPlan.fromStripePriceId("unknown_price_id"))
          .thenThrow(new IllegalArgumentException("Unknown price ID: unknown_price_id"));

      assertThrows(
          IllegalArgumentException.class,
          () -> {
            webhookService.updateSubscriptionInDatabase(mockSubscription);
          });
    }
  }

  @Test
  void handleSubscriptionUpdated_shouldApplyScheduledPlanChangeWhenPeriodEnds() {
    // Given: User had yearly plan that was scheduled to change to monthly
    mockUserSubscription.setPlan(SubscriptionPlan.PRO_YEARLY);
    mockUserSubscription.setCurrentPeriodEnd(OffsetDateTime.now().minusDays(1)); // Period ended

    Subscription mockSubscription = mock(Subscription.class);
    SubscriptionItemCollection mockItems = mock(SubscriptionItemCollection.class);
    SubscriptionItem mockItem = mock(SubscriptionItem.class);
    Price mockPrice = mock(Price.class);

    when(mockDeserializer.getObject()).thenReturn(Optional.of(mockSubscription));
    when(mockSubscription.getId()).thenReturn("sub_123");
    when(mockSubscription.getStatus()).thenReturn("active");
    when(mockSubscription.getCancelAtPeriodEnd()).thenReturn(false);
    when(mockSubscription.getItems()).thenReturn(mockItems);
    when(mockItems.getData()).thenReturn(List.of(mockItem));
    // New monthly period dates
    when(mockItem.getCurrentPeriodStart()).thenReturn(1672531200L); // Start of new period
    when(mockItem.getCurrentPeriodEnd()).thenReturn(1675209600L); // End of monthly period
    when(mockItem.getPrice()).thenReturn(mockPrice);
    when(mockPrice.getId()).thenReturn("price_pro_monthly");

    when(subscriptionRepository.findByStripeSubscriptionId("sub_123"))
        .thenReturn(Optional.of(mockUserSubscription));

    try (MockedStatic<SubscriptionPlan> mockedPlan = mockStatic(SubscriptionPlan.class)) {
      mockedPlan
          .when(() -> SubscriptionPlan.fromStripePriceId("price_pro_monthly"))
          .thenReturn(SubscriptionPlan.PRO_MONTHLY);

      // When: Webhook is triggered at period end
      webhookService.handleSubscriptionUpdated(mockEvent);

      // Then: Plan is updated to monthly with new period dates
      verify(subscriptionRepository).save(mockUserSubscription);
      assertEquals(SubscriptionPlan.PRO_MONTHLY, mockUserSubscription.getPlan());
      assertEquals(SubscriptionStatus.ACTIVE, mockUserSubscription.getStatus());
      assertNotNull(mockUserSubscription.getCurrentPeriodStart());
      assertNotNull(mockUserSubscription.getCurrentPeriodEnd());
    }
  }

  @Test
  void handleSubscriptionUpdated_shouldRenewMonthlySubscriptionWithNewPeriodDates() {
    // Given: User has monthly subscription that just renewed
    mockUserSubscription.setPlan(SubscriptionPlan.PRO_MONTHLY);
    mockUserSubscription.setCurrentPeriodStart(OffsetDateTime.now().minusMonths(1));
    mockUserSubscription.setCurrentPeriodEnd(
        OffsetDateTime.now().minusDays(1)); // Previous period ended

    Subscription mockSubscription = mock(Subscription.class);
    SubscriptionItemCollection mockItems = mock(SubscriptionItemCollection.class);
    SubscriptionItem mockItem = mock(SubscriptionItem.class);
    Price mockPrice = mock(Price.class);

    when(mockDeserializer.getObject()).thenReturn(Optional.of(mockSubscription));
    when(mockSubscription.getId()).thenReturn("sub_123");
    when(mockSubscription.getStatus()).thenReturn("active");
    when(mockSubscription.getCancelAtPeriodEnd()).thenReturn(false);
    when(mockSubscription.getItems()).thenReturn(mockItems);
    when(mockItems.getData()).thenReturn(List.of(mockItem));
    // New monthly period dates after renewal
    when(mockItem.getCurrentPeriodStart()).thenReturn(1675209600L); // Start of new monthly period
    when(mockItem.getCurrentPeriodEnd()).thenReturn(1677628800L); // End of new monthly period
    when(mockItem.getPrice()).thenReturn(mockPrice);
    when(mockPrice.getId()).thenReturn("price_pro_monthly");

    when(subscriptionRepository.findByStripeSubscriptionId("sub_123"))
        .thenReturn(Optional.of(mockUserSubscription));

    try (MockedStatic<SubscriptionPlan> mockedPlan = mockStatic(SubscriptionPlan.class)) {
      mockedPlan
          .when(() -> SubscriptionPlan.fromStripePriceId("price_pro_monthly"))
          .thenReturn(SubscriptionPlan.PRO_MONTHLY);

      // When: Webhook is triggered after monthly renewal
      webhookService.handleSubscriptionUpdated(mockEvent);

      // Then: Subscription is renewed with new monthly period dates
      verify(subscriptionRepository).save(mockUserSubscription);
      assertEquals(SubscriptionPlan.PRO_MONTHLY, mockUserSubscription.getPlan());
      assertEquals(SubscriptionStatus.ACTIVE, mockUserSubscription.getStatus());
      assertNotNull(mockUserSubscription.getCurrentPeriodStart());
      assertNotNull(mockUserSubscription.getCurrentPeriodEnd());
    }
  }
}
