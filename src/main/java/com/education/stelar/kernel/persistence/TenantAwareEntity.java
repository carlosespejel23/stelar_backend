package com.education.stelar.kernel.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;

import java.util.UUID;

import com.education.stelar.kernel.multitenancy.TenantContext;

@MappedSuperclass
@Getter
public abstract class TenantAwareEntity extends AuditableEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @PrePersist
    protected void setTenantOnCreate() {
        if (this.tenantId == null) {
            this.tenantId = TenantContext.getCurrentTenant();
        }
    }
}
