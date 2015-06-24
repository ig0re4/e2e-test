package com.e2e.robot;

import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface ThrowingBiConsumer<T, V>{
	default void accept(final T t, final V v) {
		try {
			acceptThrows(t, v);
		} catch (final Exception e) {
			/* Do whatever here ... */
			LoggerFactory.getLogger(ThrowingHelper.class).error(
					"handling an exception...");
			throw new RuntimeException(e);
		}
	}

	void acceptThrows(T t, V v) throws Exception;
}