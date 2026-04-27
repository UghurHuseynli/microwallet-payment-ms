package az.abb.payment.service;

import az.abb.payment.dto.request.AccountRequest;
import az.abb.payment.dto.response.AccountResponse;
import az.abb.payment.entity.Account;
import az.abb.payment.enums.AccountStatus;
import az.abb.payment.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse createAccount(Long userId, AccountRequest accountRequest) {
        Long uniqueCif = accountRepository.getNextCifValue();
        Account newAccount = Account.builder()
                .userId(userId)
                .balance(accountRequest.balance())
                .currency(accountRequest.currency())
                .cif(uniqueCif)
                .build();
        Account savedAccount = accountRepository.save(newAccount);
        if (savedAccount.getId() != null) {
            return new AccountResponse(AccountStatus.CREATED);
        } return new AccountResponse(AccountStatus.FAILED);
    }
}
