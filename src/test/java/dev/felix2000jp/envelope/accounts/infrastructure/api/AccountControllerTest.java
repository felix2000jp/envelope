package dev.felix2000jp.envelope.accounts.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.felix2000jp.envelope.accounts.application.AccountService;
import dev.felix2000jp.envelope.accounts.application.dtos.*;
import dev.felix2000jp.envelope.accounts.domain.exceptions.AccountNotFoundException;
import dev.felix2000jp.envelope.accounts.domain.exceptions.TransactionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class, controllers = AccountController.class)
class AccountControllerTest {

    @MockitoBean
    private AccountService accountService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private AccountDto accountDto;

    @BeforeEach
    void setUp() {
        accountDto = new AccountDto(UUID.randomUUID(), "Checking", new BigDecimal("100.00"), false);
    }

    @Test
    void get_then_return_200_and_accounts() throws Exception {
        var accountListDto = new AccountListDto(1, List.of(accountDto));

        var expectedResponse = objectMapper.writeValueAsString(accountListDto);

        when(accountService.get()).thenReturn(accountListDto);

        var request = get("/api/accounts");
        mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse));
    }

    @ParameterizedTest
    @MethodSource
    void create_then_return_201_and_created_account(String name, BigDecimal balance) throws Exception {
        var createAccountDto = new CreateAccountDto(name, balance);
        var newAccountDto = new AccountDto(UUID.randomUUID(), name, balance, false);

        var requestBody = objectMapper.writeValueAsString(createAccountDto);
        var expectedResponse = objectMapper.writeValueAsString(newAccountDto);

        when(accountService.create(name, balance)).thenReturn(newAccountDto);

        var request = post("/api/accounts")
                .contentType(APPLICATION_JSON)
                .content(requestBody);
        mockMvc
                .perform(request)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/accounts/" + newAccountDto.id()))
                .andExpect(content().json(expectedResponse));
    }

    static Stream<Arguments> create_then_return_201_and_created_account() {
        return Stream.of(
                Arguments.of("New Account", new BigDecimal("500.00")),
                Arguments.of("Zero Account", BigDecimal.ZERO),
                Arguments.of("High Balance Account", new BigDecimal("10000.99")),
                Arguments.of("Savings", new BigDecimal("1.50"))
        );
    }

    @ParameterizedTest
    @MethodSource
    void create_given_invalid_request_then_return_400(String invalidRequest) throws Exception {
        var request = post("/api/accounts")
                .contentType(APPLICATION_JSON)
                .content(invalidRequest);
        mockMvc
                .perform(request)
                .andExpect(status().isBadRequest());
    }

    static Stream<Arguments> create_given_invalid_request_then_return_400() {
        return Stream.of(
                Arguments.of("{ \"name\": \"\", \"initialBalance\": -100 }"),
                Arguments.of("{ \"initialBalance\": 100 }"),
                Arguments.of("{ \"name\": \"Test Account\" }"),
                Arguments.of("{ \"name\": null, \"initialBalance\": 100 }"),
                Arguments.of("{ \"name\": \"Test\", \"initialBalance\": null }"),
                Arguments.of("{ \"name\": \"Test\", \"initialBalance\": -0.01 }")
        );
    }

    @ParameterizedTest
    @MethodSource
    void update_then_return_200_and_updated_account(String name, BigDecimal balance) throws Exception {
        var updateAccountDto = new UpdateAccountDto(name, balance);
        var updatedAccountDto = new AccountDto(accountDto.id(), name, accountDto.balance().add(balance), false);

        var expectedResponse = objectMapper.writeValueAsString(updatedAccountDto);
        var requestBody = objectMapper.writeValueAsString(updateAccountDto);

        when(accountService.update(accountDto.id(), updateAccountDto)).thenReturn(updatedAccountDto);

        var request = put("/api/accounts/{id}", accountDto.id())
                .contentType(APPLICATION_JSON)
                .content(requestBody);
        mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse));
    }

    static Stream<Arguments> update_then_return_200_and_updated_account() {
        return Stream.of(
                Arguments.of("Updated Account", new BigDecimal("750.00")),
                Arguments.of("New Name", BigDecimal.ZERO),
                Arguments.of("Savings Account", new BigDecimal("1500.99")),
                Arguments.of("Checking", new BigDecimal("2.50"))
        );
    }

    @Test
    void update_given_account_not_found_then_return_404() throws Exception {
        var accountId = UUID.randomUUID();
        var updateAccountDto = new UpdateAccountDto("Updated Account", new BigDecimal("750.00"));
        var exception = new AccountNotFoundException();
        var requestBody = objectMapper.writeValueAsString(updateAccountDto);

        when(accountService.update(accountId, updateAccountDto)).thenThrow(exception);

        var request = put("/api/accounts/{id}", accountId)
                .contentType(APPLICATION_JSON)
                .content(requestBody);
        mockMvc
                .perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

    @ParameterizedTest
    @MethodSource
    void update_given_invalid_request_then_return_400(String invalidRequest) throws Exception {
        var accountId = UUID.randomUUID();
        var request = put("/api/accounts/{id}", accountId)
                .contentType(APPLICATION_JSON)
                .content(invalidRequest);
        mockMvc
                .perform(request)
                .andExpect(status().isBadRequest());
    }

    static Stream<Arguments> update_given_invalid_request_then_return_400() {
        return Stream.of(
                Arguments.of("{ \"name\": \"\", \"balance\": -100 }"),
                Arguments.of("{ \"balance\": 100 }"),
                Arguments.of("{ \"name\": \"Test Account\" }"),
                Arguments.of("{ \"name\": null, \"balance\": 100 }"),
                Arguments.of("{ \"name\": \"Test\", \"balance\": null }"),
                Arguments.of("{ \"name\": \"Test\", \"balance\": -0.01 }")
        );
    }

    @Test
    void getAccountById_then_return_200_and_account() throws Exception {
        var expectedResponse = objectMapper.writeValueAsString(accountDto);

        when(accountService.getAccountById(accountDto.id())).thenReturn(accountDto);

        var request = get("/api/accounts/{id}", accountDto.id());
        mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    void getAccountById_given_not_found_then_return_404() throws Exception {
        var accountId = UUID.randomUUID();
        var exception = new AccountNotFoundException();

        when(accountService.getAccountById(accountId)).thenThrow(exception);

        var request = get("/api/accounts/{id}", accountId);
        mockMvc
                .perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void closeAccount_then_return_200_and_closed_account() throws Exception {
        var request = patch("/api/accounts/{id}/close", accountDto.id());
        mockMvc
                .perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    void closeAccount_given_account_not_found_then_return_404() throws Exception {
        var accountId = UUID.randomUUID();
        var exception = new AccountNotFoundException();

        doThrow(exception).when(accountService).closeAccount(accountId);

        var request = patch("/api/accounts/{id}/close", accountId);
        mockMvc
                .perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void openAccount_then_return_200_and_opened_account() throws Exception {
        var request = patch("/api/accounts/{id}/open", accountDto.id());
        mockMvc
                .perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    void openAccount_given_account_not_found_then_return_404() throws Exception {
        var accountId = UUID.randomUUID();
        var exception = new AccountNotFoundException();

        doThrow(exception).when(accountService).openAccount(accountId);

        var request = patch("/api/accounts/{id}/open", accountId);
        mockMvc
                .perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getAccountTransactions_then_return_200_and_transaction_list() throws Exception {
        var transactionDto1 = new TransactionDto(
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                LocalDate.of(2024, 1, 1),
                "Initial balance",
                true
        );
        var transactionDto2 = new TransactionDto(
                UUID.randomUUID(),
                new BigDecimal("50.00"),
                LocalDate.of(2024, 1, 15),
                "Deposit",
                true
        );
        var transactionDto3 = new TransactionDto(
                UUID.randomUUID(),
                new BigDecimal("-25.00"),
                LocalDate.of(2024, 2, 10),
                "Withdrawal",
                false
        );
        var transactionListDto = new TransactionListDto(3, List.of(transactionDto1, transactionDto2, transactionDto3));

        var expectedResponse = objectMapper.writeValueAsString(transactionListDto);

        when(accountService.getAccountTransactions(accountDto.id())).thenReturn(transactionListDto);

        var request = get("/api/accounts/{id}/transactions", accountDto.id());
        mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    void getAccountTransactions_given_account_not_found_then_return_404() throws Exception {
        var accountId = UUID.randomUUID();
        var exception = new AccountNotFoundException();

        when(accountService.getAccountTransactions(accountId)).thenThrow(exception);

        var request = get("/api/accounts/{id}/transactions", accountId);
        mockMvc
                .perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void addTransaction_then_return_201_and_updated_account() throws Exception {
        var addTransactionDto = new AddTransactionDto(
                new BigDecimal("50.00"),
                LocalDate.of(2024, 2, 15),
                "Test transaction",
                true
        );
        var newTransactionDto = new TransactionDto(
                UUID.randomUUID(),
                new BigDecimal("50.00"),
                LocalDate.of(2024, 2, 15),
                "Test transaction",
                true
        );

        var requestBody = objectMapper.writeValueAsString(addTransactionDto);
        var expectedResponse = objectMapper.writeValueAsString(newTransactionDto);

        when(accountService.addTransaction(accountDto.id(), addTransactionDto)).thenReturn(newTransactionDto);

        var request = post("/api/accounts/{id}/transactions", accountDto.id())
                .contentType(APPLICATION_JSON)
                .content(requestBody);
        mockMvc
                .perform(request)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/accounts/" + accountDto.id() + "/transactions"))
                .andExpect(content().json(expectedResponse));
    }

    @Test
    void addTransaction_given_account_not_found_then_return_404() throws Exception {
        var accountId = UUID.randomUUID();
        var addTransactionDto = new AddTransactionDto(
                new BigDecimal("50.00"),
                null,
                null,
                true
        );
        var exception = new AccountNotFoundException();

        var requestBody = objectMapper.writeValueAsString(addTransactionDto);

        when(accountService.addTransaction(accountId, addTransactionDto)).thenThrow(exception);

        var request = post("/api/accounts/{id}/transactions", accountId)
                .contentType(APPLICATION_JSON)
                .content(requestBody);
        mockMvc
                .perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

    @ParameterizedTest
    @MethodSource
    void addTransaction_given_invalid_request_then_return_400(AddTransactionDto invalidDto) throws Exception {
        var requestBody = objectMapper.writeValueAsString(invalidDto);

        var request = post("/api/accounts/{id}/transactions", accountDto.id())
                .contentType(APPLICATION_JSON)
                .content(requestBody);
        mockMvc
                .perform(request)
                .andExpect(status().isBadRequest());
    }

    static Stream<Arguments> addTransaction_given_invalid_request_then_return_400() {
        return Stream.of(
                Arguments.of(new AddTransactionDto(null, LocalDate.now(), "Test", true)),
                Arguments.of(new AddTransactionDto(new BigDecimal("100.00"), LocalDate.now(), "A".repeat(300), true))
        );
    }

    @Test
    void updateTransaction_then_return_200_and_updated_account() throws Exception {
        var transactionId = UUID.randomUUID();
        var updateTransactionDto = new UpdateTransactionDto(
                new BigDecimal("75.00"),
                LocalDate.of(2024, 3, 20),
                "Updated memo"
        );
        var updatedTransactionDto = new TransactionDto(
                UUID.randomUUID(),
                new BigDecimal("75.00"),
                LocalDate.of(2024, 3, 20),
                "Updated memo",
                false
        );

        var requestBody = objectMapper.writeValueAsString(updateTransactionDto);
        var expectedResponse = objectMapper.writeValueAsString(updatedTransactionDto);

        when(accountService.updateTransaction(accountDto.id(), transactionId, updateTransactionDto)).thenReturn(updatedTransactionDto);

        var request = put("/api/accounts/{accountId}/transactions/{transactionId}", accountDto.id(), transactionId)
                .contentType(APPLICATION_JSON)
                .content(requestBody);
        mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    void updateTransaction_given_account_not_found_then_return_404() throws Exception {
        var accountId = UUID.randomUUID();
        var transactionId = UUID.randomUUID();
        var updateTransactionDto = new UpdateTransactionDto(
                new BigDecimal("75.00"),
                LocalDate.of(2024, 3, 20),
                "Updated memo"
        );
        var exception = new AccountNotFoundException();

        var requestBody = objectMapper.writeValueAsString(updateTransactionDto);

        when(accountService.updateTransaction(accountId, transactionId, updateTransactionDto)).thenThrow(exception);

        var request = put("/api/accounts/{accountId}/transactions/{transactionId}", accountId, transactionId)
                .contentType(APPLICATION_JSON)
                .content(requestBody);
        mockMvc
                .perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateTransaction_given_transaction_not_found_then_return_404() throws Exception {
        var transactionId = UUID.randomUUID();
        var updateTransactionDto = new UpdateTransactionDto(
                new BigDecimal("75.00"),
                LocalDate.of(2024, 3, 20),
                "Updated memo"
        );
        var exception = new TransactionNotFoundException();

        var requestBody = objectMapper.writeValueAsString(updateTransactionDto);

        when(accountService.updateTransaction(accountDto.id(), transactionId, updateTransactionDto)).thenThrow(exception);

        var request = put("/api/accounts/{accountId}/transactions/{transactionId}", accountDto.id(), transactionId)
                .contentType(APPLICATION_JSON)
                .content(requestBody);
        mockMvc
                .perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

    @ParameterizedTest
    @MethodSource
    void updateTransaction_given_invalid_request_then_return_400(UpdateTransactionDto invalidDto) throws Exception {
        var transactionId = UUID.randomUUID();
        var requestBody = objectMapper.writeValueAsString(invalidDto);

        var request = put("/api/accounts/{accountId}/transactions/{transactionId}", accountDto.id(), transactionId)
                .contentType(APPLICATION_JSON)
                .content(requestBody);
        mockMvc
                .perform(request)
                .andExpect(status().isBadRequest());
    }

    static Stream<Arguments> updateTransaction_given_invalid_request_then_return_400() {
        return Stream.of(
                Arguments.of(new UpdateTransactionDto(null, null, "A".repeat(300)))
        );
    }

    @Test
    void removeTransaction_then_return_204() throws Exception {
        var accountId = UUID.randomUUID();
        var transactionId = UUID.randomUUID();

        doNothing().when(accountService).removeTransaction(accountId, transactionId);

        var request = delete("/api/accounts/{accountId}/transactions/{transactionId}", accountId, transactionId);
        mockMvc
                .perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    void removeTransaction_given_account_not_found_then_return_404() throws Exception {
        var accountId = UUID.randomUUID();
        var transactionId = UUID.randomUUID();
        var exception = new AccountNotFoundException();

        doThrow(exception).when(accountService).removeTransaction(accountId, transactionId);

        var request = delete("/api/accounts/{accountId}/transactions/{transactionId}", accountId, transactionId);
        mockMvc
                .perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void removeTransaction_given_transaction_not_found_then_return_404() throws Exception {
        var accountId = UUID.randomUUID();
        var transactionId = UUID.randomUUID();
        var exception = new TransactionNotFoundException();

        doThrow(exception).when(accountService).removeTransaction(accountId, transactionId);

        var request = delete("/api/accounts/{accountId}/transactions/{transactionId}", accountId, transactionId);
        mockMvc
                .perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void clearTransaction_then_return_204() throws Exception {
        var accountId = UUID.randomUUID();
        var transactionId = UUID.randomUUID();

        doNothing().when(accountService).clearTransaction(accountId, transactionId);

        var request = patch("/api/accounts/{accountId}/transactions/{transactionId}/clear", accountId, transactionId);
        mockMvc
                .perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    void clearTransaction_given_account_not_found_then_return_404() throws Exception {
        var accountId = UUID.randomUUID();
        var transactionId = UUID.randomUUID();
        var exception = new AccountNotFoundException();

        doThrow(exception).when(accountService).clearTransaction(accountId, transactionId);

        var request = patch("/api/accounts/{accountId}/transactions/{transactionId}/clear", accountId, transactionId);
        mockMvc
                .perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void clearTransaction_given_transaction_not_found_then_return_404() throws Exception {
        var accountId = UUID.randomUUID();
        var transactionId = UUID.randomUUID();
        var exception = new TransactionNotFoundException();

        doThrow(exception).when(accountService).clearTransaction(accountId, transactionId);

        var request = patch("/api/accounts/{accountId}/transactions/{transactionId}/clear", accountId, transactionId);
        mockMvc
                .perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void unclearTransaction_then_return_204() throws Exception {
        var accountId = UUID.randomUUID();
        var transactionId = UUID.randomUUID();

        doNothing().when(accountService).unclearTransaction(accountId, transactionId);

        var request = patch("/api/accounts/{accountId}/transactions/{transactionId}/unclear", accountId, transactionId);
        mockMvc
                .perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    void unclearTransaction_given_account_not_found_then_return_404() throws Exception {
        var accountId = UUID.randomUUID();
        var transactionId = UUID.randomUUID();
        var exception = new AccountNotFoundException();

        doThrow(exception).when(accountService).unclearTransaction(accountId, transactionId);

        var request = patch("/api/accounts/{accountId}/transactions/{transactionId}/unclear", accountId, transactionId);
        mockMvc
                .perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void unclearTransaction_given_transaction_not_found_then_return_404() throws Exception {
        var accountId = UUID.randomUUID();
        var transactionId = UUID.randomUUID();
        var exception = new TransactionNotFoundException();

        doThrow(exception).when(accountService).unclearTransaction(accountId, transactionId);

        var request = patch("/api/accounts/{accountId}/transactions/{transactionId}/unclear", accountId, transactionId);
        mockMvc
                .perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.status").value(404));
    }

}