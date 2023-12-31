package com.ksol.mesc.domain.api.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ksol.mesc.domain.api.dto.request.DeveloperCommitRequestDto;
import com.ksol.mesc.domain.api.dto.request.DeveloperDataRequestDto;
import com.ksol.mesc.domain.api.dto.request.DeveloperQueryRequestDto;
import com.ksol.mesc.domain.api.dto.request.WorkerDataRequestDto;
import com.ksol.mesc.domain.api.dto.request.WorkerQueryRequestDto;
import com.ksol.mesc.domain.api.dto.response.MessageResponseDto;
import com.ksol.mesc.domain.api.service.ApiService;
import com.ksol.mesc.domain.common.dto.response.CommonResponseDto;
import com.ksol.mesc.domain.user.service.UserService;
import com.ksol.mesc.global.error.exception.InvalidValueException;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ApiController {
	private final ApiService apiService;
	private final UserService userService;

	@GetMapping("/start-message")
	public ResponseEntity<CommonResponseDto<MessageResponseDto>> getStartMessage(Authentication authentication) {
		Integer userId = Integer.parseInt(authentication.getName());
		String name = userService.findById(userId).getName();
		String message = apiService.getStartMessage(name);
		MessageResponseDto messageDto = new MessageResponseDto(message);
		return new ResponseEntity<>(CommonResponseDto.success(messageDto), HttpStatus.OK);
	}

	@Operation(summary = "쿼리 데이터 조회 API", description = "쿼리를 통해 데이터를 조회한다.")
	@PostMapping("/developer/data/{page}")
	// @PostMapping("/developer/data")
	public ResponseEntity<?> getDeveloperData(
		@PathVariable @Valid Integer page,
		@RequestBody @Validated DeveloperDataRequestDto developerDataRequestDto,
		BindingResult bindingResult) {
		checkValidates(bindingResult);
		String query = developerDataRequestDto.getQuery();
		List<String> queryList = developerDataRequestDto.getQueryList();
		LinkedHashMap<String, Object> tableByQuery = apiService.getTableByQuery(query, page, queryList);
		// LinkedHashMap<String, Object> tableByQuery = apiService.getTableByQuery(query, queryList);
		System.out.println(tableByQuery.keySet().stream().collect(Collectors.toList()));
		if (tableByQuery.containsKey("message")) {
			return new ResponseEntity<>(
				CommonResponseDto.error(HttpStatus.NOT_ACCEPTABLE.value(), (String)tableByQuery.get("message")),
				HttpStatus.NOT_ACCEPTABLE);
		}
		return ResponseEntity.ok(CommonResponseDto.success(tableByQuery));
	}

	@Operation(summary = "쿼리 데이터 삽입, 수정, 삭제 API", description = "쿼리를 통해 데이터를 삽입, 수정, 삭제한다.")
	@PostMapping("/developer/query")
	public ResponseEntity<CommonResponseDto> getDeveloperQuery(
		@RequestBody @Validated DeveloperQueryRequestDto DeveloperQueryRequestDto, BindingResult bindingResult) {
		checkValidates(bindingResult);
		String query = DeveloperQueryRequestDto.getQuery();
		LinkedHashMap<String, Object> countsByQuery = apiService.getCountsByQuery(query);
		if (countsByQuery.containsKey("message")) {
			return new ResponseEntity<>(
				CommonResponseDto.error(HttpStatus.NOT_ACCEPTABLE.value(), (String)countsByQuery.get("message")),
				HttpStatus.NOT_ACCEPTABLE);
		}
		return ResponseEntity.ok(CommonResponseDto.success(countsByQuery));
	}

	@Operation(summary = "액션ID 데이터 조회 API", description = "액션ID를 통해 데이터를 조회한다.")
	@PostMapping("/worker/data/{actionId}/{page}")
	// @PostMapping("/worker/data/{actionId}")
	public ResponseEntity<?> getWorkerData(
		@PathVariable(required = true) Integer page,
		@PathVariable(required = true) Integer actionId,
		@RequestBody @Validated WorkerDataRequestDto workerDataRequestDto,
		BindingResult bindingResult) {
		checkValidates(bindingResult);
		String conditions = workerDataRequestDto.getConditions();
		List<String> queryList = workerDataRequestDto.getQueryList();
		LinkedHashMap<String, Object> tableByActionId = apiService.getTableByActionId(actionId, conditions, page,
			queryList);
		// LinkedHashMap<String, Object> tableByActionId = apiService.getTableByActionId(actionId, conditions, queryList);
		if (tableByActionId.containsKey("message")) {
			return new ResponseEntity<>(
				CommonResponseDto.error(HttpStatus.NOT_ACCEPTABLE.value(), (String)tableByActionId.get("message")),
				HttpStatus.NOT_ACCEPTABLE);
		}
		return ResponseEntity.ok(CommonResponseDto.success(tableByActionId));
	}

	@Operation(summary = "액션ID 데이터 추가, 수정, 삭제 API", description = "액션ID를 통해 데이터를 추가, 수정, 삭제한다.")
	@PostMapping("/worker/query/{actionId}")
	public ResponseEntity<?> getWorkerQuery(@PathVariable(required = true) Integer actionId,
		@RequestBody @Validated WorkerQueryRequestDto workerQueryRequestDto,
		BindingResult bindingResult) {
		checkValidates(bindingResult);
		String conditions = workerQueryRequestDto.getConditions();
		LinkedHashMap<String, Object> countsByActionId = apiService.getCountsByActionId(actionId, conditions);
		if (countsByActionId.containsKey("message")) {
			return new ResponseEntity<>(
				CommonResponseDto.error(HttpStatus.NOT_ACCEPTABLE.value(), (String)countsByActionId.get("message")),
				HttpStatus.NOT_ACCEPTABLE);
		}
		return ResponseEntity.ok(CommonResponseDto.success(countsByActionId));
	}

	@Operation(summary = "commit API", description = "commit을 수행한다.")
	@PostMapping("/developer/commit")
	public ResponseEntity<?> commit(@RequestBody @Validated DeveloperCommitRequestDto developerCommitRequestDto,
		BindingResult bindingResult) {
		checkValidates(bindingResult);
		List<String> queryList = developerCommitRequestDto.getQueryList();
		LinkedHashMap<String, Object> result = apiService.commit(queryList);
		if ((Boolean)result.get("result"))
			result.put("blockId", 15);
		else
			result.put("blockId", 16);

		return ResponseEntity.ok(CommonResponseDto.success(result));
	}

	private static void checkValidates(BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			throw new InvalidValueException(bindingResult.getFieldError().getDefaultMessage());
		}
	}
}
