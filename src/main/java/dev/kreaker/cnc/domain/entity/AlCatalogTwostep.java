package dev.kreaker.cnc.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "AL_CATALOG_TWOSTEP", schema = "REPORTUSER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AlCatalogTwostep {

	@EmbeddedId
	private AlCatalogTwostepId id;

	@Column(name = "DOMAIN", length = 20)
	private String domain;

	@Column(name = "STATUS")
	private Integer status;

	@CreatedDate
	@Column(name = "CREATED_AT", updatable = false)
	private LocalDateTime createdAt;

	@CreatedBy
	@Column(name = "CREATED_BY", updatable = false, length = 50)
	private String createdBy;

	@LastModifiedDate
	@Column(name = "MODIFIED_AT")
	private LocalDateTime modifiedAt;

	@LastModifiedBy
	@Column(name = "MODIFIED_BY", length = 50)
	private String modifiedBy;
}
