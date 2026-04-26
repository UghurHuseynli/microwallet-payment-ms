package az.abb.payment.controller;

import az.abb.payment.dto.request.AccountRequest;
import az.abb.payment.dto.response.AccountResponse;
import az.abb.payment.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @RequestHeader("X-Account-Id") Long userId,
            @RequestBody AccountRequest accountRequest
    ) {
        AccountResponse response = accountService.createAccount(userId, accountRequest);
        return ResponseEntity.ok(response);
    }

}
