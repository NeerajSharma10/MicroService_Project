package com.njcoder.productservice;

import com.njcoder.productservice.dto.ProductRequest;
import com.njcoder.productservice.dto.ProductResponse;
import com.njcoder.productservice.repository.ProductRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

	@DynamicPropertySource
	static void setProperty(DynamicPropertyRegistry dynamicPropertyRegistry) {
		dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ProductRepository productRepository;

	ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@DisplayName(value = "Checking the post request for product controller")
	public void shouldCreateProduct() throws Exception {
		ProductRequest productRequest = ProductRequest.builder()
				.name("Moto 5g")
				.description("SmartPhone")
				.price(new BigDecimal(13000))
				.build();
		String productRequestString = objectMapper.writeValueAsString(productRequest);
		mockMvc.perform(post("/api/product")
				.contentType("application/json")
				.content(productRequestString)
		).andExpect(status().isCreated());

	}

	@Test
	@SneakyThrows
	@DisplayName(value = "test case to check the get request")
	public void checkGetRequest() {
		for(int i=0;i < 3; i++) {
			ProductRequest productRequest = createProductRequest(i+1);
			String productRequestString = objectMapper.writeValueAsString(productRequest);
			mockMvc.perform(post("/api/product")
					.contentType("application/json")
					.content(productRequestString)
			).andExpect(status().isCreated());
		}

		MvcResult result = mockMvc.perform(get("/api/product"))
				.andExpect(status().isOk())
				.andReturn();

		String responseBody = result.getResponse().getContentAsString();
		List<ProductResponse> productResponses = objectMapper.readValue(responseBody, new TypeReference<List<ProductResponse>> () {});
		Assertions.assertEquals(3, productResponses.size());
	}

	private ProductRequest createProductRequest(Integer hashCode) {
		return ProductRequest.builder()
				.name("Test Phone " + hashCode)
				.description("SmartPhone")
				.price(new BigDecimal(13000))
				.build();
	}

}
