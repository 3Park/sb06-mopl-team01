package org.example.mopl.content.s3;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class ContentS3ClientTest {

  @Autowired
  private ContentS3Client contentS3Client;

  @BeforeEach
  void setUp() {
  }
}