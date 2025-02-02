package me.teux.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubCommand {
    String name();
    String[] aliases() default {};
    String permission() default "";
    int cooldown() default -1; // -1 herda do comando principal
    boolean onlyPlayer() default false;
}
