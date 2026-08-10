package net.critical.orientation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a member as generated/boilerplate so JaCoCo's built-in {@code GeneratedFilter} excludes it
 * from coverage counting. JaCoCo recognizes ANY annotation whose simple name is exactly
 * {@code Generated} (see JaCoCo's {@code org.jacoco.core.internal.analysis.filter.GeneratedFilter}) -
 * it does not care about the annotation's package, so this project-local annotation works exactly
 * like {@code javax.annotation.Generated} would, without pulling in a dependency that modern JDKs
 * (9+) removed from the default module path.
 * <p>
 * Retention is {@code CLASS} rather than {@code RUNTIME} because JaCoCo reads annotations directly
 * from the compiled bytecode during instrumentation/analysis, not via reflection at test run time.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.TYPE})
public @interface Generated {
}
