package com.ksol.mesc.domain.group.entity;

import com.ksol.mesc.domain.common.EntityState;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Table(name = "organization")
@ToString
public class Group {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "GROUP_ID")
	Integer id;

	@JoinColumn(name = "USER_ID")
	Integer userId;

	@Column(name = "GROUP_NAME")
	String groupName;
	Integer sequence;
	@Enumerated(EnumType.STRING)
	EntityState state;
}
