package com.ksol.mes.domain.developer.controller;

import java.sql.SQLException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ksol.mes.domain.common.dto.response.CommonResponseDto;
import com.ksol.mes.domain.developer.dto.request.DeveloperCommitRequestDto;
import com.ksol.mes.domain.developer.dto.request.DeveloperDataRequestDto;
import com.ksol.mes.domain.developer.dto.request.DeveloperQueryRequestDto;
import com.ksol.mes.domain.developer.dto.response.DeveloperCommitResponseDto;
import com.ksol.mes.domain.developer.dto.response.DeveloperDataResponseDto;
import com.ksol.mes.domain.developer.dto.response.DeveloperQueryResponseDto;
import com.ksol.mes.domain.developer.service.DeveloperService;
import com.ksol.mes.global.error.exception.InvalidValueException;
import com.ksol.mes.global.util.jdbc.SQLErrorResponseDto;
import com.ksol.mes.global.util.jdbc.Table;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/mes/developer")
@RequiredArgsConstructor
@Slf4j
public class DeveloperController {

	private final DeveloperService developerService;

	@PostMapping("/data/{page}")
	// @PostMapping("/data")
	public ResponseEntity<CommonResponseDto<?>> getData(
		@PathVariable(required = true) Integer page,
		@RequestBody @Validated DeveloperDataRequestDto developerDataRequestDto, BindingResult bindingResult) {
		checkValidates(bindingResult);
		log.info("{}", developerDataRequestDto.getQuery());
		DeveloperDataResponseDto developerDataResponseDto;

		try {
			Table table = developerService.getTable(developerDataRequestDto.getQuery(), page,
				developerDataRequestDto.getQueryList());
			// Table table = developerService.getTable(developerDataRequestDto.getQuery(),
			// 	developerDataRequestDto.getQueryList());
			developerDataResponseDto = new DeveloperDataResponseDto(table);
		} catch (SQLException e) {
			log.error(e.getMessage());
			return new ResponseEntity<>(CommonResponseDto.success(new SQLErrorResponseDto(e.getMessage())),
				HttpStatus.ACCEPTED);
		}
		return new ResponseEntity<>(CommonResponseDto.success(developerDataResponseDto), HttpStatus.OK);
	}

	@PostMapping("/query")
	public ResponseEntity<CommonResponseDto<?>> executeQuery(
		@RequestBody @Validated DeveloperQueryRequestDto developerQueryRequestDto, BindingResult bindingResult) {
		checkValidates(bindingResult);
		DeveloperQueryResponseDto developerQueryResponseDto = new DeveloperQueryResponseDto();
		log.info("{}", developerQueryRequestDto.getQuery());
		try {
			String query = developerQueryRequestDto.getQuery();
			List<String> queryList = developerQueryRequestDto.getQueryList();
			Integer modifiedCount = developerService.executeQuery(query, queryList);
			String method = "추가";
			if (query.startsWith("update")) {
				method = "수정";
			} else if (query.startsWith("delete")) {
				method = "삭제";
			}
			developerQueryResponseDto.setResult(true);
			developerQueryResponseDto.setContent(modifiedCount + "개의 행이 " + method + "되었습니다.");
		} catch (SQLException e) {
			log.error(e.getMessage());
			developerQueryResponseDto.setResult(false);
			developerQueryResponseDto.setContent(e.getMessage());
		}
		return new ResponseEntity<>(CommonResponseDto.success(developerQueryResponseDto), HttpStatus.OK);
	}

	@PostMapping("/query/rollback/{page}")
	// @PostMapping("/query/rollback")
	public ResponseEntity<CommonResponseDto<?>> executeQueryWithRollback(
		@PathVariable(required = true) Integer page,
		@RequestBody @Validated DeveloperQueryRequestDto developerQueryRequestDto, BindingResult bindingResult) {
		checkValidates(bindingResult);
		DeveloperDataResponseDto developerDataResponseDto;
		log.info("{}", developerQueryRequestDto.getQuery());
		try {
			String query = developerQueryRequestDto.getQuery();
			List<String> queryList = developerQueryRequestDto.getQueryList();
			// Table table = developerService.executeQueryWithRollback(query, queryList);
			Table table = developerService.executeQueryWithRollback(query, page, queryList);
			developerDataResponseDto = new DeveloperDataResponseDto(table);
		} catch (SQLException e) {
			log.error(e.getMessage());
			return new ResponseEntity<>(CommonResponseDto.success(new SQLErrorResponseDto(e.getMessage())),
				HttpStatus.ACCEPTED);
		}
		return new ResponseEntity<>(CommonResponseDto.success(developerDataResponseDto), HttpStatus.OK);
	}

	// //커밋 수행
	@PostMapping("/commit")
	public ResponseEntity<CommonResponseDto<?>> confirmCommit(
		@RequestBody @Validated DeveloperCommitRequestDto developerCommitRequestDto, BindingResult bindingResult) {
		DeveloperCommitResponseDto developerCommitResponseDto = new DeveloperCommitResponseDto();
		try {
			developerService.executeQueryList(developerCommitRequestDto.getQueryList());
			developerCommitResponseDto.setResult(true);
		} catch (SQLException e) {
			log.error(e.getMessage());
			developerCommitResponseDto.setResult(false);
			developerCommitResponseDto.setContent(e.getMessage());
		}
		return new ResponseEntity<>(CommonResponseDto.success(developerCommitResponseDto), HttpStatus.OK);
	}

	private static void checkValidates(BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			throw new InvalidValueException(bindingResult.getFieldError().getDefaultMessage());
		}
	}
}
