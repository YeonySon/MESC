package com.ksol.mesc.domain.component.type.button.entity;

import com.ksol.mesc.domain.common.EntityState;
import com.ksol.mesc.domain.component.entity.LinkType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "BUTTON")
// @DiscriminatorValue("BU")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Button {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "BUTTON_ID")
	private Integer id;
	@Column(name = "NAME")
	private String name;
	@Column(name = "LINK_TYPE")
	@Enumerated(EnumType.STRING)
	@Builder.Default
	private LinkType linkType = LinkType.B;
	@Column(name = "LINK")
	private Integer link;
	@Column(name = "ACTION_ID")
	private Integer actionId;
	@Column(name = "ICON_ID")
	private Integer iconId;
	@Column(name = "RESPONSE")
	private String response;
	@Column(name = "RESPONSE_TYPE")
	private String responseType;
	@Enumerated(EnumType.STRING)
	@Builder.Default
	private EntityState state = EntityState.ACTIVE;

}
