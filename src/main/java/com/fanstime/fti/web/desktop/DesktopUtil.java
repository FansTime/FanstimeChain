package com.fanstime.fti.web.desktop;

import org.springframework.beans.BeansException;
import org.springframework.boot.context.embedded.EmbeddedServletContainerException;
import org.springframework.context.ApplicationContextException;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Bynum Williams on 07.03.18.
 */
public class DesktopUtil {

    /**
     * Find initial exception by visiting
     */
    public static Throwable findCauseFromSpringException(Throwable e) {
        final List<Class<? extends Exception>> skipList = Arrays.asList(BeansException.class, EmbeddedServletContainerException.class, ApplicationContextException.class);

        for (int i = 0; i < 50; i++) {
            final Throwable inner = e;
            final boolean isSkipped = skipList.stream().anyMatch(c -> c.isAssignableFrom(inner.getClass()));
            if (isSkipped) {
                e = e.getCause();
            } else {
                return e;
            }
        }
        return e;
    }
}
