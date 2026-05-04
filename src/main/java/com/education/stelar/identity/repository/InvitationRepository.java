package com.education.stelar.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.education.stelar.identity.entity.Invitation;
import com.education.stelar.identity.entity.InvitationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    Optional<Invitation> findByToken(String token);

    List<Invitation> findAllByTenantId(UUID tenantId);

    List<Invitation> findAllByTenantIdAndStatus(UUID tenantId, InvitationStatus status);

    Optional<Invitation> findByInvitedEmailAndTenantIdAndStatus(
            String invitedEmail, UUID tenantId, InvitationStatus status);
}
