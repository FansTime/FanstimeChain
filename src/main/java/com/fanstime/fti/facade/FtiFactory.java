package com.fanstime.fti.facade;

import com.fanstime.fti.config.DefaultConfig;
import com.fanstime.fti.config.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;



@Component
public class FtiFactory {

    private static final Logger logger = LoggerFactory.getLogger("general");

    public static Fti createFti() {
        return createFti((Class) null);
    }

    public static Fti createFti(Class userSpringConfig) {
        return userSpringConfig == null ? createFti(new Class[] {DefaultConfig.class}) :
                createFti(DefaultConfig.class, userSpringConfig);
    }

    /**
     * @deprecated The config parameter is not used anymore. The configuration is passed
     * via 'systemProperties' bean either from the DefaultConfig or from supplied userSpringConfig
     * @param config  Not used
     * @param userSpringConfig   User Spring configuration class
     * @return  Fully initialized Fti instance
     */
    public static Fti createFti(SystemProperties config, Class userSpringConfig) {

        return userSpringConfig == null ? createFti(new Class[] {DefaultConfig.class}) :
                createFti(DefaultConfig.class, userSpringConfig);
    }

    public static Fti createFti(Class ... springConfigs) {
        logger.info("Starting Fti...");
        AbstractApplicationContext context = new AnnotationConfigApplicationContext(springConfigs);
        context.registerShutdownHook();
        return context.getBean(Fti.class);
    }
}
