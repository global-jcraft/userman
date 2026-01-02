package com.huddey.core.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.huddey.core.payment.config.PlanKey;
import com.huddey.core.payment.data.entity.UserSubscription;
import com.huddey.core.payment.data.enums.SubscriptionStatus;
import com.huddey.core.payment.repository.UserSubscriptionRepository;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

  @Mock private UserSubscriptionRepository subscriptionRepository;
  @Mock private PaymentMethodService paymentMethodService;
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
    metadata.put("planKey", PlanKey.PRO.getKey());
    metadata.put("interval", "month");
    metadata.put("currency", "EUR");
    metadata.put("seats", "1");

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
    when(mockItem.getQuantity()).thenReturn(1L);

    try (var mockedSubscription = mockStatic(Subscription.class)) {
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
    when(mockPrice.getLookupKey()).thenReturn(PlanKey.PRO.lookup("month", "EUR"));
    when(mockItem.getQuantity()).thenReturn(1L);

    when(subscriptionRepository.findByStripeSubscriptionId("sub_123"))
        .thenReturn(Optional.of(mockUserSubscription));

    webhookService.handleSubscriptionUpdated(mockEvent);

    verify(subscriptionRepository).save(mockUserSubscription);
    assertEquals(SubscriptionStatus.ACTIVE, mockUserSubscription.getStatus());
    assertEquals(PlanKey.PRO.getKey(), mockUserSubscription.getPlanKey());
  }

  @Test
  void handleSubscriptionDeleted_shouldDowngradeToFree() {
    Subscription mockSubscription = mock(Subscription.class);

    when(mockDeserializer.getObject()).thenReturn(Optional.of(mockSubscription));
    when(mockSubscription.getId()).thenReturn("sub_123");
    when(mockSubscription.getCustomer()).thenReturn("cus_123");
    when(subscriptionRepository.findByStripeSubscriptionId("sub_123"))
        .thenReturn(Optional.of(mockUserSubscription));

    webhookService.handleSubscriptionDeleted(mockEvent);

    verify(subscriptionRepository).save(mockUserSubscription);
    assertEquals(PlanKey.FREE.getKey(), mockUserSubscription.getPlanKey());
    assertEquals(SubscriptionStatus.ACTIVE, mockUserSubscription.getStatus());
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
    when(mockPrice.getLookupKey()).thenReturn(PlanKey.PRO.lookup("month", "EUR"));
    when(mockItem.getQuantity()).thenReturn(1L);

    when(subscriptionRepository.findByStripeSubscriptionId("sub_123"))
        .thenReturn(Optional.of(mockUserSubscription));

    webhookService.handleSubscriptionCreated(mockEvent);

    verify(subscriptionRepository).save(mockUserSubscription);
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
    when(mockPrice.getLookupKey()).thenReturn(PlanKey.ELITE.lookup("year", "EUR"));
    when(mockItem.getQuantity()).thenReturn(1L);

    when(subscriptionRepository.findByStripeSubscriptionId("sub_123"))
        .thenReturn(Optional.of(mockUserSubscription));

    webhookService.updateSubscriptionInDatabase(mockSubscription);

    verify(subscriptionRepository).save(mockUserSubscription);
    assertEquals(SubscriptionStatus.PAST_DUE, mockUserSubscription.getStatus());
    assertEquals(PlanKey.ELITE.getKey(), mockUserSubscription.getPlanKey());
    assertTrue(mockUserSubscription.isCancelAtPeriodEnd());
    assertNotNull(mockUserSubscription.getCurrentPeriodStart());
    assertNotNull(mockUserSubscription.getCurrentPeriodEnd());
  }

  @Test
  void updateSubscriptionInDatabase_shouldHandleUnknownLookupKey() {
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
    when(mockPrice.getLookupKey()).thenReturn(null);
    when(mockItem.getQuantity()).thenReturn(1L);

    when(subscriptionRepository.findByStripeSubscriptionId("sub_123"))
        .thenReturn(Optional.of(mockUserSubscription));

    assertDoesNotThrow(() -> webhookService.updateSubscriptionInDatabase(mockSubscription));

    verify(subscriptionRepository).save(mockUserSubscription);
    assertEquals(SubscriptionStatus.ACTIVE, mockUserSubscription.getStatus());
  }
}
