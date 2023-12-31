package com.ksol.mesc.domain.group.dto.response;

import java.util.List;

import com.ksol.mesc.domain.user.dto.UserReq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GroupMemberResponse {
	private List<UserReq> userList;
}
