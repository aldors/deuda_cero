package com.aldo.deuda_cero.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aldo.deuda_cero.dto.balance.BalanceMiembroResponse;
import com.aldo.deuda_cero.service.interfaces.BalanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/balances")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;
    
    @GetMapping("/{grupoId}/balance")
    public ResponseEntity<List<BalanceMiembroResponse>> obtenerBalance(@PathVariable Long grupoId){
        return ResponseEntity.ok(balanceService.obtenerBalance(grupoId));
    }
}
