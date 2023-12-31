package com.ksol.mes.domain.developer.dto.request;

import java.util.List;

import com.ksol.mes.global.annotation.InsertUpdateDeleteQuery;

import lombok.Data;

@Data
public class DeveloperQueryRequestDto {
	// 프론트에서 빈문자열을 null로 넘길지 ""로 넘길지 확인해서 그에 맞게 처리해야 함 일단은 빈문자열로 가정
	// insert 문인지 검사해야함
	@InsertUpdateDeleteQuery
	private String query;

	private List<String> queryList;
}
