package com.ksol.mesc.domain.group.dto.request;

import java.util.List;

import lombok.Data;

@Data
public class GroupMemberReq {
	private List<Integer> userList;
}
