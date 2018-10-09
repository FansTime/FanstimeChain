package com.fanstime.fti.web;

import com.fanstime.fti.config.FtiChainConfig;
import com.fanstime.fti.Start;
import com.fanstime.fti.config.SystemProperties;
import com.fanstime.fti.facade.Fti;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

@SpringBootApplication
@EnableScheduling
@Import({FtiChainConfig.class})
public class Application {

    /**
     * Does one of:
     * - start blockchain peer;
     * - perform action and exit on completion.
     */
    public static void main(String[] args) throws Exception {
        // Overriding mine.start to get control of its startup
        // in {@link com.blockchain.fanstime.service.PrivateMinerService}
        SystemProperties.getDefault().overrideParams("mine.start", "false");
        final List<String> actions = asList("importBlocks");

        final Optional<String> foundAction = asList(args).stream()
                .filter(arg -> actions.contains(arg))
                .findFirst();

        if (foundAction.isPresent()) {
            foundAction.ifPresent(action -> System.out.println("Performing action: " + action));
            Start.main(args);
            // system is expected to exit after action performed
        } else {
            if (!SystemProperties.getDefault().blocksLoader().equals("")) {
                SystemProperties.getDefault().setSyncEnabled(false);
            }

            ConfigurableApplicationContext context = SpringApplication.run(new Object[]{Application.class}, args);

            Fti fti = context.getBean(Fti.class);

            if (!SystemProperties.getDefault().blocksLoader().equals("")) {
                fti.getBlockLoader().loadBlocks();
            }
        }
    }
}
