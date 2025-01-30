package me.teux.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CommandInfo {
    String name();

    String[] aliases() default {};

    String description() default "";

    String permission() default "";

    int cooldown() default 0;

    boolean onlyPlayer() default false;

    SubCommand[] subCommands() default {};
}
