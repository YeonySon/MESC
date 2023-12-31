package com.ksol.mesc.domain.log.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ksol.mesc.domain.common.dto.response.CommonResponseDto;
import com.ksol.mesc.domain.log.dto.request.LogRequestDto;
import com.ksol.mesc.domain.log.dto.response.LogResponseDto;
import com.ksol.mesc.domain.log.service.LogSerivce;
import com.ksol.mesc.global.error.exception.InvalidValueException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
@Slf4j
public class LogController {

	private final LogSerivce logSerivce;

	@PostMapping
	public ResponseEntity<CommonResponseDto<LogResponseDto>> getData(
		@RequestBody @Validated LogRequestDto logRequestDto, BindingResult bindingResult) {
		checkValidates(bindingResult);
		String keyword = logRequestDto.getKeyword();
		String date = logRequestDto.getDate();
		List<String> levelList = logRequestDto.getLevelList();
		String command = logSerivce.getCommand(keyword, date, levelList);

		LogResponseDto logResponseDto = new LogResponseDto(logSerivce.getLogs(command));
		return new ResponseEntity<>(CommonResponseDto.success(logResponseDto), HttpStatus.OK);
	}

	private static void checkValidates(BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			throw new InvalidValueException(bindingResult.getFieldError().getDefaultMessage());
		}
	}
}
