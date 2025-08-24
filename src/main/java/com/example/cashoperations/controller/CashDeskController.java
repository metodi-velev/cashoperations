package com.example.cashoperations.controller;

import com.example.cashoperations.dto.CashOperationRequest;
import com.example.cashoperations.service.CashDeskService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Transactional
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "fibAuth")
@RequestMapping("/api/v1")
public class CashDeskController {

    @Autowired
    private final CashDeskService cashService;

    @PostMapping("/cash-operation")
    public ResponseEntity<String> performOperation(@Valid @RequestBody CashOperationRequest request) {
        cashService.performOperation(request);
        return ResponseEntity.ok("Operation successful");
    }
}