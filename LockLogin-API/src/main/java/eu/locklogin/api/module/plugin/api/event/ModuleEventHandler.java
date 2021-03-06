package eu.locklogin.api.module.plugin.api.event;

/*
 * GNU LESSER GENERAL PUBLIC LICENSE
 * Version 2.1, February 1999
 * <p>
 * Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * <p>
 * [This is the first released version of the Lesser GPL.  It also counts
 * as the successor of the GNU Library Public License, version 2, hence
 * the version number 2.1.]
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation must be used over a event
 * method to make the plugin know it's a event
 * handler method
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleEventHandler {

    Priority priority() default Priority.NORMAL;

    String after() default "";

    boolean ignoreHandled() default true;

    /**
     * Event listener priority
     */
    enum Priority {
        /**
         * Run the event as first as possible
         */
        FIRST,

        /**
         * Run the event as the plugin would
         * run it normally, with no priorities
         */
        NORMAL,

        /**
         * Run the event the latest
         */
        LAST,

        /**
         * Run the event after the specified
         * module ( after="" )
         */
        AFTER
    }
}
