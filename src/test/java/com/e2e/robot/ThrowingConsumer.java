package com.e2e.robot;

import java.util.function.Consumer;

import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface ThrowingConsumer<T> extends Consumer<T>{

	default void accept(final T elem) {
		try {
			acceptThrows(elem);
		} catch (final Exception e) {
			/* Do whatever here ... */
			LoggerFactory.getLogger(ThrowingHelper.class).error(
					"handling an exception...");
			throw new RuntimeException(e);
		}
	}

	void acceptThrows(T elem) throws Exception;
}