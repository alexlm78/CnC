/* (c) 2026 Alejandro Lopez Monzon <alejandro@kreaker.dev> for Kreaker Developments */
package dev.kreaker.cnc.domain.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "AL_CATALOG_TARGETS", schema = "REPORTUSER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AlCatalogTargets {

   @EmbeddedId
   private AlCatalogTwostepId id;

   @Column(name = "SRCTABLE", length = 35)
   private String srcTable;

   @Column(name = "SRCFIELD", length = 35)
   private String srcField;

   @CreatedDate
   @Column(name = "CREATED_AT", updatable = false)
   private LocalDateTime createdAt;

   @LastModifiedDate
   @Column(name = "MODIFIED_AT")
   private LocalDateTime modifiedAt;
}
