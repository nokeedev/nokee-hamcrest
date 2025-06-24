package dev.nokee.commons.hamcrest.gradle;

import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectCollectionSchema;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.equalTo;

public final class NamedMatcher<T> extends FeatureMatcher<T, String> {
	private NamedMatcher(Matcher<? super String> matcher) {
		super(requireNonNull(matcher), "an object named", "the object's name");
	}

	@Override
	protected String featureValueOf(T actual) {
		try {
			if (actual instanceof Named) {
				return ((Named) actual).getName();
			}
			// Configuration class somewhat behave like a Named class
			else if (actual instanceof Configuration) {
				return ((Configuration) actual).getName();
			}
			// Task class somewhat behave like a Named class
			else if (actual instanceof Task) {
				return ((Task) actual).getName();
			}

			// SourceSet class somewhat behave like a Named class
			else if (actual instanceof SourceSet) {
				return ((SourceSet) actual).getName();
			}

			// NamedDomainObjectProvider class somewhat behave like a Named class
			//   in theory, all default implementation implements Named class
			else if (actual instanceof NamedDomainObjectProvider) {
				return ((NamedDomainObjectProvider<?>) actual).getName();
			}

			// NamedDomainObjectSchema class somewhat behave like a Named class
			else if (actual instanceof NamedDomainObjectCollectionSchema.NamedDomainObjectSchema) {
				return ((NamedDomainObjectCollectionSchema.NamedDomainObjectSchema) actual).getName();
			}
		} catch (NoClassDefFoundError e) {
			// ignore, not in a Gradle context
		}

		// Class with getName() returning String somewhat behave like a Named class
		try {
			final Method getNameMethod = actual.getClass().getMethod("getName");
			if (String.class.isAssignableFrom(getNameMethod.getReturnType())) {
				try {
					return (String) getNameMethod.invoke(actual);
				} catch (
					InvocationTargetException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			} else if (Provider.class.isAssignableFrom(getNameMethod.getReturnType())) {
				try {
					return (String) ((Provider<?>) getNameMethod.invoke(actual)).getOrNull();
				} catch (InvocationTargetException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		} catch (NoSuchMethodException e) {
			// do nothing, no getName() methods
		}

		throw new UnsupportedOperationException(String.format("Object '%s' of type %s is not named-able.", actual, actual.getClass().getCanonicalName()));
	}

	public static <T> NamedMatcher<T> named(String name) {
		return new NamedMatcher<>(equalTo(requireNonNull(name)));
	}

	public static <T> NamedMatcher<T> named(Matcher<? super String> matcher) {
		return new NamedMatcher<>(matcher);
	}
}
