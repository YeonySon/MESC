package com.ksol.mesc.worker.controller;

import com.ksol.mesc.util.jdbc.Table;
import com.ksol.mesc.worker.dto.request.WorkerDataRequestDto;
import com.ksol.mesc.worker.dto.request.WorkerQueryRequestDto;
import com.ksol.mesc.worker.dto.response.WorkerDataResponseDto;
import com.ksol.mesc.worker.dto.response.WorkerQueryResponseDto;
import com.ksol.mesc.worker.service.WorkerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/worker")
@RequiredArgsConstructor
@Slf4j
public class WorkerController {

    private final WorkerService workerService;

    @PostMapping("/query/{actionId}")
    public ResponseEntity<WorkerQueryResponseDto> getQuery(
            @PathVariable(required = true) Integer actionId,
            @RequestBody @Validated WorkerQueryRequestDto workerQueryRequestDto) {
        String query = workerService.getQuery(actionId, workerQueryRequestDto.getConditions());
        WorkerQueryResponseDto responseDto = new WorkerQueryResponseDto(query);
        if (query != null) {
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        }
        return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
    }

    @PostMapping("/data/{actionId}")
    public ResponseEntity<WorkerDataResponseDto> getData(
            @PathVariable(required = true) Integer actionId,
            @RequestBody @Validated WorkerDataRequestDto workerDataRequestDto) {
        WorkerDataResponseDto workerDataResponseDto = null;
        try {
            Table table = workerService.getTable(actionId, workerDataRequestDto.getConditions());
            workerDataResponseDto = new WorkerDataResponseDto(table);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return new ResponseEntity<>(workerDataResponseDto, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(workerDataResponseDto, HttpStatus.OK);
    }
}
