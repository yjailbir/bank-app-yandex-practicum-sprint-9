package ru.yjailbir.accountsservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCrypt;
import ru.yjailbir.accountsservice.entity.AccountEntity;
import ru.yjailbir.accountsservice.entity.UserEntity;
import ru.yjailbir.accountsservice.repository.AccountsRepository;
import ru.yjailbir.accountsservice.repository.UserRepository;
import ru.yjailbir.accountsservice.security.JwtUtil;
import ru.yjailbir.accountsservice.service.UserService;
import ru.yjailbir.commonslib.dto.request.*;
import ru.yjailbir.commonslib.dto.response.UserAccountsResponseDto;

import java.time.LocalDate;
import java.util.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveNewUser_whenLoginTaken_shouldThrow() {
        RegisterRequestDto dto = new RegisterRequestDto("user1", "pass", "Surname", "Name", LocalDate.of(2000,1,1));
        when(userRepository.findByLogin(dto.login())).thenReturn(Optional.of(new UserEntity()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.saveNewUser(dto));
        assertEquals("Имя пользователя занято!", ex.getMessage());
    }

    @Test
    void saveNewUser_whenUnderage_shouldThrow() {
        RegisterRequestDto dto = new RegisterRequestDto("user2", "pass", "Surname", "Name", LocalDate.now().minusYears(17));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.saveNewUser(dto));
        assertEquals("Возраст должен быть не меньше 18 лет!", ex.getMessage());
    }

    @Test
    void loginUser_wrongPassword_shouldThrow() {
        String hashed = BCrypt.hashpw("pass", BCrypt.gensalt());
        UserEntity user = new UserEntity("user", hashed, "Surname", "Name", LocalDate.of(2000,1,1));
        when(userRepository.findByLogin("user")).thenReturn(Optional.of(user));

        LoginRequestDto dto = new LoginRequestDto("user", "wrongpass");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.loginUser(dto));
        assertEquals("Неверный пароль!", ex.getMessage());
    }

    @Test
    void loginUser_correctPassword_shouldReturnToken() {
        String hashed = BCrypt.hashpw("pass", BCrypt.gensalt());
        UserEntity user = new UserEntity("user", hashed, "Surname", "Name", LocalDate.of(2000,1,1));
        when(userRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(jwtUtil.generateJwtToken(user)).thenReturn("token");

        LoginRequestDto dto = new LoginRequestDto("user", "pass");
        String token = userService.loginUser(dto);
        assertEquals("token", token);
    }

    @Test
    void doCashOperation_putAndGet_shouldChangeBalance() {
        UserEntity user = new UserEntity("user", "pass", "Surname", "Name", LocalDate.of(2000,1,1));
        AccountEntity account = new AccountEntity("USD", "Dollar", user);
        account.setBalance(100.0);
        account.setActive(true);
        user.getAccounts().add(account);

        when(jwtUtil.getLoginFromJwtToken("token")).thenReturn("user");
        when(userRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(accountsRepository.findByCurrencyAndUser_Id("USD", 1L)).thenReturn(account);

        user.setId(1L);

        CashRequestDtoWithToken deposit = new CashRequestDtoWithToken("USD", 50.0, "PUT", "token");
        userService.doCashOperation(deposit);
        assertEquals(150.0, account.getBalance());

        CashRequestDtoWithToken withdraw = new CashRequestDtoWithToken("USD", 70.0, "GET", "token");
        userService.doCashOperation(withdraw);
        assertEquals(80.0, account.getBalance());
    }

    @Test
    void doTransfer_intraUser_shouldTransferFunds() {
        UserEntity user = new UserEntity("user", "pass", "Surname", "Name", LocalDate.of(2000,1,1));
        AccountEntity accFrom = new AccountEntity("USD", "Dollar", user);
        AccountEntity accTo = new AccountEntity("RUB", "Ruble", user);
        accFrom.setBalance(100.0); accFrom.setActive(true);
        accTo.setBalance(50.0); accTo.setActive(true);
        user.getAccounts().addAll(List.of(accFrom, accTo));
        user.setId(1L);

        when(jwtUtil.getLoginFromJwtToken("token")).thenReturn("user");
        when(userRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(accountsRepository.findByCurrencyAndUser_Id("USD", 1L)).thenReturn(accFrom);
        when(accountsRepository.findByCurrencyAndUser_Id("RUB", 1L)).thenReturn(accTo);

        ExchangedTransferDtoWithToken dto = new ExchangedTransferDtoWithToken( "USD", "RUB", 20.0, 150.0, "user", "token");
        userService.doTransfer(dto);

        assertEquals(80.0, accFrom.getBalance());
        assertEquals(200.0, accTo.getBalance());
    }

    @Test
    void updateUser_shouldUpdateNameSurnameAndAccounts() {
        UserEntity user = new UserEntity("user", "pass", "OldSurname", "OldName", LocalDate.of(2000,1,1));
        AccountEntity acc1 = new AccountEntity("USD", "Dollar", user);
        AccountEntity acc2 = new AccountEntity("RUB", "Ruble", user);
        acc1.setActive(false);
        acc2.setActive(false);
        user.getAccounts().addAll(List.of(acc1, acc2));
        user.setId(1L);

        when(jwtUtil.getLoginFromJwtToken("token")).thenReturn("user");
        when(userRepository.findByLogin("user")).thenReturn(Optional.of(user));

        UserEditRequestDtoWithToken dto = new UserEditRequestDtoWithToken(
                "NewName", "NewSurname", List.of("USD"), "token"
        );

        userService.updateUser(dto);

        assertEquals("NewName", user.getName());
        assertEquals("NewSurname", user.getSurname());
        assertTrue(acc1.isActive());
        assertFalse(acc2.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void getUserActiveAccounts_shouldReturnOnlyActiveAccounts() {
        UserEntity user = new UserEntity("user", "pass", "Surname", "Name", LocalDate.of(2000,1,1));
        AccountEntity acc1 = new AccountEntity("USD", "Dollar", user);
        AccountEntity acc2 = new AccountEntity("RUB", "Ruble", user);
        acc1.setActive(true);
        acc2.setActive(false);
        user.getAccounts().addAll(List.of(acc1, acc2));

        when(jwtUtil.getLoginFromJwtToken("token")).thenReturn("user");
        when(userRepository.findByLogin("user")).thenReturn(Optional.of(user));

        UserAccountsResponseDto response = userService.getUserActiveAccounts("token");

        assertEquals(1, response.accounts.size());
        assertEquals("USD", response.accounts.getFirst().currency());
        assertEquals("ok", response.status);
    }

    @Test
    void doCashOperation_negativeValue_shouldThrow() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        when(jwtUtil.getLoginFromJwtToken("token")).thenReturn("user");
        when(userRepository.findByLogin("user")).thenReturn(Optional.of(user));
        AccountEntity account = new AccountEntity("USD", "Dollar", user);
        account.setActive(true);
        when(accountsRepository.findByCurrencyAndUser_Id("USD", 1L)).thenReturn(account);

        CashRequestDtoWithToken dto = new CashRequestDtoWithToken("USD", -10.0, "PUT", "token");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.doCashOperation(dto));
        assertEquals("Число должно быть неотрицательным!", ex.getMessage());
    }

    @Test
    void doCashOperation_inactiveAccount_shouldThrow() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        when(jwtUtil.getLoginFromJwtToken("token")).thenReturn("user");
        when(userRepository.findByLogin("user")).thenReturn(Optional.of(user));
        AccountEntity account = new AccountEntity("USD", "Dollar", user);
        account.setActive(false);
        when(accountsRepository.findByCurrencyAndUser_Id("USD", 1L)).thenReturn(account);

        CashRequestDtoWithToken dto = new CashRequestDtoWithToken( "USD", 10.0, "PUT", "token");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.doCashOperation(dto));
        assertEquals("Пользователь не имеет счёта в выбранной валюте!", ex.getMessage());
    }

    @Test
    void doCashOperation_withdrawInsufficientFunds_shouldThrow() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        when(jwtUtil.getLoginFromJwtToken("token")).thenReturn("user");
        when(userRepository.findByLogin("user")).thenReturn(Optional.of(user));
        AccountEntity account = new AccountEntity("USD", "Dollar", user);
        account.setActive(true);
        account.setBalance(50.0);
        when(accountsRepository.findByCurrencyAndUser_Id("USD", 1L)).thenReturn(account);

        CashRequestDtoWithToken dto = new CashRequestDtoWithToken("USD", 100.0, "GET", "token");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.doCashOperation(dto));
        assertEquals("Недостаточно средств!", ex.getMessage());
    }

    @Test
    void doCashOperation_invalidAction_shouldThrow() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        when(jwtUtil.getLoginFromJwtToken("token")).thenReturn("user");
        when(userRepository.findByLogin("user")).thenReturn(Optional.of(user));
        AccountEntity account = new AccountEntity("USD", "Dollar", user);
        account.setActive(true);
        when(accountsRepository.findByCurrencyAndUser_Id("USD", 1L)).thenReturn(account);

        CashRequestDtoWithToken dto = new CashRequestDtoWithToken( "USD", 10.0, "INVALID", "token");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.doCashOperation(dto));
        assertEquals("Неверное действие!", ex.getMessage());
    }

    // -----------------------
    // doTransfer edge cases
    // -----------------------
    @Test
    void doTransfer_toSelfInsufficientFunds_shouldThrow() {
        UserEntity user = new UserEntity("user", "pass", "Surname", "Name", LocalDate.of(2000,1,1));
        user.setId(1L);
        AccountEntity accFrom = new AccountEntity("USD", "Dollar", user);
        accFrom.setBalance(50.0); accFrom.setActive(true);
        AccountEntity accTo = new AccountEntity("RUB", "Ruble", user);
        accTo.setBalance(100.0); accTo.setActive(true);
        user.getAccounts().addAll(List.of(accFrom, accTo));

        when(jwtUtil.getLoginFromJwtToken("token")).thenReturn("user");
        when(userRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(accountsRepository.findByCurrencyAndUser_Id("USD", 1L)).thenReturn(accFrom);
        when(accountsRepository.findByCurrencyAndUser_Id("RUB", 1L)).thenReturn(accTo);

        ExchangedTransferDtoWithToken dto = new ExchangedTransferDtoWithToken("USD", "RUB", 100.0, 150.0, "user", "token");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.doTransfer(dto));
        assertEquals("Недостаточно средств!", ex.getMessage());
    }

    @Test
    void doTransfer_toAnotherUser_insufficientFunds_shouldThrow() {
        UserEntity userFrom = new UserEntity("userFrom", "pass", "Surname", "Name", LocalDate.of(2000,1,1));
        userFrom.setId(1L);
        UserEntity userTo = new UserEntity("userTo", "pass", "Surname", "Name", LocalDate.of(2000,1,1));
        userTo.setId(2L);

        AccountEntity accFrom = new AccountEntity("USD", "Dollar", userFrom);
        accFrom.setBalance(50.0); accFrom.setActive(true);
        AccountEntity accTo = new AccountEntity("RUB", "Ruble", userTo);
        accTo.setBalance(100.0); accTo.setActive(true);

        userFrom.getAccounts().add(accFrom);
        userTo.getAccounts().add(accTo);

        when(jwtUtil.getLoginFromJwtToken("token")).thenReturn("userFrom");
        when(userRepository.findByLogin("userFrom")).thenReturn(Optional.of(userFrom));
        when(userRepository.findByLogin("userTo")).thenReturn(Optional.of(userTo));
        when(accountsRepository.findByCurrencyAndUser_Id("USD", 1L)).thenReturn(accFrom);
        when(accountsRepository.findByCurrencyAndUser_Id("RUB", 2L)).thenReturn(accTo);

        ExchangedTransferDtoWithToken dto = new ExchangedTransferDtoWithToken("USD",  "RUB", 100.0, 150.0, "userTo", "token");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.doTransfer(dto));
        assertEquals("Недостаточно средств!", ex.getMessage());
    }

    @Test
    void doTransfer_toAnotherUser_shouldTransferFunds() {
        UserEntity userFrom = new UserEntity("userFrom", "pass", "Surname", "Name", LocalDate.of(2000,1,1));
        userFrom.setId(1L);
        UserEntity userTo = new UserEntity("userTo", "pass", "Surname", "Name", LocalDate.of(2000,1,1));
        userTo.setId(2L);

        AccountEntity accFrom = new AccountEntity("USD", "Dollar", userFrom);
        accFrom.setBalance(100.0); accFrom.setActive(true);
        AccountEntity accTo = new AccountEntity("RUB", "Ruble", userTo);
        accTo.setBalance(50.0); accTo.setActive(true);

        userFrom.getAccounts().add(accFrom);
        userTo.getAccounts().add(accTo);

        when(jwtUtil.getLoginFromJwtToken("token")).thenReturn("userFrom");
        when(userRepository.findByLogin("userFrom")).thenReturn(Optional.of(userFrom));
        when(userRepository.findByLogin("userTo")).thenReturn(Optional.of(userTo));
        when(accountsRepository.findByCurrencyAndUser_Id("USD", 1L)).thenReturn(accFrom);
        when(accountsRepository.findByCurrencyAndUser_Id("RUB", 2L)).thenReturn(accTo);

        ExchangedTransferDtoWithToken dto = new ExchangedTransferDtoWithToken("USD",  "RUB", 20.0,150.0, "userTo", "token");

        userService.doTransfer(dto);

        assertEquals(80.0, accFrom.getBalance());
        assertEquals(200.0, accTo.getBalance());
    }

    @Test
    void validateToken_shouldReturnValidation() {
        when(jwtUtil.validateJwtToken("token")).thenReturn("ok");
        assertEquals("ok", userService.validateToken("token"));
    }

    @Test
    void getLoginFromToken_shouldReturnLogin() {
        when(jwtUtil.getLoginFromJwtToken("token")).thenReturn("user");
        assertEquals("user", userService.getLoginFromToken("token"));
    }
}

