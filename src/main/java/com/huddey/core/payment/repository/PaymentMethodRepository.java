package com.huddey.core.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.huddey.core.payment.data.entity.PaymentMethod;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

  Page<PaymentMethod> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  Optional<PaymentMethod> findByUserIdAndIsDefaultTrue(Long userId);

  Optional<PaymentMethod> findByUserIdAndIsBackupTrue(Long userId);

  Optional<PaymentMethod> findByUserIdAndId(Long userId, Long id);

  @Modifying
  @Query("UPDATE PaymentMethod pm SET pm.isDefault = false WHERE pm.userId = :userId")
  void clearDefaultForUser(@Param("userId") Long userId);

  @Modifying
  @Query("UPDATE PaymentMethod pm SET pm.isBackup = false WHERE pm.userId = :userId")
  void clearBackupForUser(@Param("userId") Long userId);

  @Query(
      "SELECT pm FROM PaymentMethod pm WHERE pm.expYear < :currentYear OR (pm.expYear = :currentYear AND pm.expMonth < :currentMonth)")
  List<PaymentMethod> findExpiredPaymentMethods(
      @Param("currentYear") int currentYear, @Param("currentMonth") int currentMonth);

  Optional<PaymentMethod> findByStripePaymentMethodId(String id);
}
