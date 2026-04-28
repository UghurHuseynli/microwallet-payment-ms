package az.abb.payment.service;

import az.abb.payment.dto.request.AccountRequest;
import az.abb.payment.dto.response.AccountResponse;
import az.abb.payment.entity.Account;
import az.abb.payment.enums.AccountStatus;
import az.abb.payment.enums.Currency;
import az.abb.payment.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private AccountRequest accountRequest;

    @BeforeEach
    void setUp() {
        accountRequest = new AccountRequest(BigDecimal.valueOf(1000), Currency.USD);
    }

    @Test
    void createAccount_ShouldReturnCreated_WhenAccountIsSavedSuccessfully() {
        Long userId = 1L;
        Long cifValue = 100L;

        Account savedAccount = Account.builder()
                .id(1L)
                .userId(userId)
                .balance(accountRequest.balance())
                .currency(accountRequest.currency())
                .cif(cifValue)
                .build();

        given(accountRepository.getNextCifValue()).willReturn(cifValue);
        given(accountRepository.save(any(Account.class))).willReturn(savedAccount);

        AccountResponse response = accountService.createAccount(userId, accountRequest);

        assertThat(response.getStatus()).isEqualTo(AccountStatus.CREATED);

        verify(accountRepository).getNextCifValue();
        verify(accountRepository).save(argThat(account ->
                account.getUserId().equals(userId) &&
                        account.getBalance().equals(accountRequest.balance()) &&
                        account.getCurrency().equals(accountRequest.currency()) &&
                        account.getCif().equals(cifValue)
        ));
    }

    @Test
    void createAccount_ShouldReturnFailed_WhenSavedAccountHasNoId() {
        Long userId = 1L;
        Long cifValue = 100L;

        Account savedAccount = Account.builder()
                .id(null)
                .build();

        given(accountRepository.getNextCifValue()).willReturn(cifValue);
        given(accountRepository.save(any(Account.class))).willReturn(savedAccount);

        AccountResponse response = accountService.createAccount(userId, accountRequest);

        assertThat(response.getStatus()).isEqualTo(AccountStatus.FAILED);
    }

    @Test
    void createAccount_ShouldPropagateException_WhenRepositoryThrows() {
        given(accountRepository.getNextCifValue()).willThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> accountService.createAccount(1L, accountRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");
    }
}