package com.codequest.content;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.context.ConfigurableApplicationContext;

@Component
public class ContentLoader implements ApplicationRunner {
    private final PackService packs;
    private final ConfigurableApplicationContext context;
    public ContentLoader(PackService packs, ConfigurableApplicationContext context) { this.packs=packs; this.context=context; }
    public void run(ApplicationArguments args) throws Exception {
        if(args.containsOption("pack-file") || args.containsOption("pack-import")) {
            String file=args.containsOption("pack-file") ? args.getOptionValues("pack-file").getFirst() : System.getenv("CODEQUEST_PACK_FILE");
            var source=Files.readString(Path.of(file));
            if(args.containsOption("validate-only")) packs.validate(source); else packs.publish(source);
            System.out.println("Question pack validation/import succeeded.");
            context.close();
        } else {
            try(var input=new ClassPathResource("content/dsa/arrays/v1.json").getInputStream()) {
                packs.publish(new String(input.readAllBytes(),StandardCharsets.UTF_8));
            }
        }
    }
}
