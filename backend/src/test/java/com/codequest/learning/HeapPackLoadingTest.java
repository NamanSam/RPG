package com.codequest.learning;

import com.codequest.content.PackService;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest @ActiveProfiles("test")
class HeapPackLoadingTest {
 @Autowired PackService packs;
 @Test void heapResourceLoadsFiveTrailsAndFiftyQuestions() throws Exception {
  try(var input=new ClassPathResource("content/dsa/heaps/v1.json").getInputStream()) {
   var pack=packs.validate(new String(input.readAllBytes(),StandardCharsets.UTF_8));
   assertThat(pack.topic()).isEqualTo("heaps");
   assertThat(pack.trails()).hasSize(5);
   assertThat(pack.questions()).hasSize(50);
   for(int trail=1;trail<=5;trail++) { final int number=trail; assertThat(pack.questions().stream().filter(q->q.trail()==number).count()).isEqualTo(10); }
  }
 }
}
