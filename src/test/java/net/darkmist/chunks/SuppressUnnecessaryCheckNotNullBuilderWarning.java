package net.darkmist.chunks;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.immutables.annotate.InjectAnnotation;
import org.immutables.annotate.InjectAnnotation.Where;

/**
 * Annotation that tells Immutables to add {@link SuppressWarnings SuppressWarnings("UnnecessaryCheckNotNull")} annotation to the builder.
 */
@InjectAnnotation(
	type=SuppressWarnings.class,
	target = {
		Where.BUILDER_TYPE
	},
	code="(\"UnnecessaryCheckNotNull\")"
)
@Retention(RetentionPolicy.SOURCE)
@interface SuppressUnnecessaryCheckNotNullBuilderWarning
{
	// annotation
}

