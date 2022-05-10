package com.example.banksystem;

import com.example.banksystem.models.Account;
import com.example.banksystem.models.Bank;
import com.example.banksystem.models.Client;
import com.example.banksystem.repositories.AccountRepository;
import com.example.banksystem.repositories.BankRepository;
import com.example.banksystem.repositories.ClientRepository;
import com.example.banksystem.repositories.TransactionRepository;
import com.example.banksystem.securityconfig.CustomAuthenticationEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
public class AccountTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ClientRepository clientRepository;

    @MockBean
    AccountRepository accountRepository;

    @MockBean
    BankRepository bankRepository;

    @MockBean
    TransactionRepository transactionRepository;

    @MockBean
    CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void accounts() throws Exception {
        Client client = new Client(3, "user", "sur", "add", "pass");
        Bank bank = new Bank(1, "bank", 10);

        Account account1 = new Account(1, new BigDecimal(1000), "Account1", bank, client);
        client.addAccounts(account1);
        Account account2 = new Account(2, new BigDecimal(2000), "Account2", bank, client);
        client.addAccounts(account2);

        Mockito.when(clientRepository.findByName(Mockito.any())).thenReturn(client);
        Mockito.when(accountRepository.findAll()).thenReturn(client.getAccounts());

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.get("/accounts");

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(model().attribute("accounts", client.getAccounts()))
                .andExpect(model().attribute("user", client));
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void redirect() throws Exception {
        Client client = new Client(3,"user", "sur", "add", "pass");

        Mockito.when(clientRepository.findByName(Mockito.any())).thenReturn(client);
        Mockito.when(accountRepository.findAll()).thenReturn(client.getAccounts());

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .get("/accounts/add/form")
                .param("type", "TestAccount")
                .param("bankId", "3");

        mockMvc.perform(mockRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accounts"));
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void typeIsEmpty() throws Exception {
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .get("/accounts/add/form")
                .param("bankId", "3");

        mockMvc.perform(mockRequest)
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void bankIdIsEmpty() throws Exception {
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .get("/accounts/add/form")
                .param("type", "TestAccount");

        mockMvc.perform(mockRequest)
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void accountAdd() throws Exception {
        Client client = new Client(3,"user", "sur", "add", "pass");

        Mockito.when(clientRepository.findByName(Mockito.any())).thenReturn(client);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.get("/accounts/add");

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(model().attribute("title", "Account form"))
                .andExpect(model().attribute("user", client));
    }
}