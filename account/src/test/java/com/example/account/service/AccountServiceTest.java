package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
//@WebMvcTest(AccountController.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;


    @Test
    void createAccountSuccess() {

        AccountUser accountUser = AccountUser.builder()
                //.id(12L)
                .name("Pobi").build();

        accountUser.setId(12L);

        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                        .accountNumber("1000000012").build()));

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(accountUser)
                        .accountNumber("1000000015").build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000013", captor.getValue().getAccountNumber());
    }


    @Test
    void createFirstAccount() {

        AccountUser accountUser = AccountUser.builder()
                //.id(15L)
                .name("Pobi").build();

        accountUser.setId(15L);

        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(accountUser)
                        .accountNumber("1000000015").build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(15L, accountDto.getUserId());
        assertEquals("1000000000", captor.getValue().getAccountNumber());
    }


    @Test
    @DisplayName("해당 유저 없음 - 계좌 생성 실패")
    void createAccount_UserNotFound() {

        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("유저 당 최대 계좌는 10개")
    void createAccount_maxAccountIs10() {

        //given
        AccountUser user = AccountUser.builder()
                //.id(15L)
                .name("Pobi")
                .build();

        user.setId(15L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        //then
        assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10, exception.getErrorCode());
    }


    @Test
    void deleteAccountSuccess() {

        AccountUser accountUser = AccountUser.builder()
                //.id(12L)
                .name("Pobi").build();

        accountUser.setId(12L);

        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                                .accountUser(accountUser)
                                .balance(0L)
                                .accountNumber("1000000012").build()));
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountDto accountDto = accountService.deleteAccount(1L, "1234567890");

        //then
        verify(accountRepository, times(0)).save(any());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000012", captor.getValue().getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, captor.getValue().getAccountStatus());
    }


    @Test
    @DisplayName("해당 유저 없음 - 계좌 해지 실패")
    void deleteAccount_UserNotFound() {

        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }



    @Test
    @DisplayName("해당 계좌 없음 - 계좌 해지 실패")
    void deleteAccount_AccountNotFound() {

        AccountUser accountUser = AccountUser.builder()
                //.id(12L)
                .name("Pobi").build();

        accountUser.setId(12L);

        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }



    @Test
    @DisplayName("계좌 소유주 다름")
    void deleteAccountFailed_UserUnMatch() {

        AccountUser pobi = AccountUser.builder()
                //.id(12L)
                .name("pobi").build();
        pobi.setId(12L);

        AccountUser harry = AccountUser.builder()
                //.id(13L)
                .name("harry").build();
        harry.setId(13L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(harry)
                        .balance(0L)
                        .accountNumber("1000000012").build()));

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }


    @Test
    @DisplayName("해지 계좌는 잔액이 없어야 한다.")
    void deleteAccountFailed_balanceNotEmpty() {

        AccountUser pobi = AccountUser.builder()
                //.id(12L)
                .name("pobi").build();
        pobi.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(pobi)
                        .balance(100L)
                        .accountNumber("1000000012").build()));

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertEquals(ErrorCode.BALANCE_NOT_EMPTY, exception.getErrorCode());
    }

    @Test
    @DisplayName("해지 계좌는 해자할 수 없다.")
    void deleteAccountFailed_alreadyUnregistered() {

        AccountUser pobi = AccountUser.builder()
                //.id(12L)
                .name("pobi").build();
        pobi.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(pobi)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .balance(0L)
                        .accountNumber("1000000012").build()));

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }


    @Test
    void successGetAccountsByUserId() {

        //given
        AccountUser pobi = AccountUser.builder()
                //.id(12L)
                .name("pobi").build();
        pobi.setId(12L);

        List<Account> accounts = Arrays.asList(
                Account.builder()
                        .accountUser(pobi)
                        .accountNumber("1111111111")
                        .balance(1000L)
                        .build(),
                Account.builder()
                        .accountUser(pobi)
                        .accountNumber("2222222222")
                        .balance(2000L)
                        .build(),
                Account.builder()
                        .accountUser(pobi)
                        .accountNumber("3333333333")
                        .balance(3000L)
                        .build()
                );

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));
        given(accountRepository.findByAccountUser(any()))
                .willReturn(accounts);

        //when
        List<AccountDto> accountDtos = accountService.getAccountsByUserId(1L);

        //then
        assertEquals(3, accountDtos.size());

        assertEquals("1111111111", accountDtos.get(0).getAccountNumber());
        assertEquals(1000, accountDtos.get(0).getBalance());

        assertEquals("2222222222", accountDtos.get(1).getAccountNumber());
        assertEquals(2000, accountDtos.get(1).getBalance());

        assertEquals("3333333333", accountDtos.get(2).getAccountNumber());
        assertEquals(3000, accountDtos.get(2).getBalance());
    }

    @Test
    void failedToGetAccounts() {

        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.getAccountsByUserId(1L));

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }


    //-----------------------------------------------------------------------------
//    @MockBean
//    private AccountService accountService;
//
//    @MockBean
//    private LockService redisTestService;
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;

//    @Test
//    void successCreateAccount() throws Exception {
//        //given
//        given(accountService.createAccount(anyLong(), anyLong()))
//                .willReturn(AccountDto.builder()
//                        .userId(1L)
//                        .accountNumber("1234567890")
//                        .registeredAt(LocalDateTime.now())
//                        .unRegisteredAt(LocalDateTime.now())
//                        .build());
//        //when
//
//        //then
//        mockMvc.perform(post("/account")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(
//                        new CreateAccount.Request(3333L, 1111L)
//                )))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.userId").value(1))
//                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
//                .andDo(print());
//    }


    //----------------------------------------------------
//    @Mock
//    private AccountRepository accountRepository;
//
//    @InjectMocks
//    private AccountService accountService;


//    @Test
//    @DisplayName("계좌 조회 성공")
//    void testXXX() {
//
//        //given
//        given(accountRepository.findById(anyLong()))
//                .willReturn(Optional.of(Account.builder()
//                        .accountStatus(AccountStatus.UNREGISTERED)
//                        .accountNumber("65789").build()));
//
//        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
//
//        //when
//        Account account = accountService.getAccount(4555L);
//
//        //then
//        verify(accountRepository, times(1)).findById(captor.capture());
//        verify(accountRepository, times(1)).save(any());
//
//        assertEquals(4555L, captor.getValue());
//        assertNotEquals(4555L, captor.getValue());
//
//        assertEquals("65789", account.getAccountNumber());
//        assertEquals(AccountStatus.UNREGISTERED, account.getAccountStatus());
//    }
//
//    @Test
//    @DisplayName("계좌 조회 실패 - 음수로 조회")
//    void testFailedToSearchAccount() {
//
//        //given
//        //when
//        RuntimeException exception = assertThrows(RuntimeException.class,
//                () -> accountService.getAccount(-10L));
//
//        //then
//        assertEquals("Minus", exception.getMessage());
//    }
//
//
//
//    @Test
//    void testGetAccount() {
//
//        Account account = accountService.getAccount(1L);
//
//        assertEquals("40000", account.getAccountNumber());
//        assertEquals(AccountStatus.IN_USE, account.getAccountStatus());
//    }
//
//
//    @Test
//    void testGetAccount2() {
//        Account account = accountService.getAccount(2L);
//
//        assertEquals("40001", account.getAccountNumber());
//        assertEquals(AccountStatus.IN_USE, account.getAccountStatus());
//    }

}