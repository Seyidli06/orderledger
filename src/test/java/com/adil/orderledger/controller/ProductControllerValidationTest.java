package com.adil.orderledger.controller;

import com.adil.orderledger.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void createProduct_zeroPrice_shouldReturnBadRequest() throws Exception {
        String requestBody = """
                {
                  "name": "Invalid Product",
                  "unitPrice": 0,
                  "stockQuantity": 10
                }
                """;

        mockMvc.perform(
                        post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_blankName_shouldReturnBadRequest() throws Exception {
        String requestBody = """
                {
                  "name": "",
                  "unitPrice": 100,
                  "stockQuantity": 10
                }
                """;

        mockMvc.perform(
                        post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_negativeStock_shouldReturnBadRequest() throws Exception {
        String requestBody = """
                {
                  "name": "Invalid Product",
                  "unitPrice": 100,
                  "stockQuantity": -1
                }
                """;

        mockMvc.perform(
                        post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }
}