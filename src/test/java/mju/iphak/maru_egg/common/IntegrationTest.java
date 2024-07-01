package mju.iphak.maru_egg.common;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest()
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class IntegrationTest {
	@Autowired
	protected MockMvc mvc;
	@Autowired
	protected ObjectMapper objectMapper;
}