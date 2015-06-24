package com.e2e.robot;

import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface ThrowingHelper<T, V>{

	default T accept(final V v) {
		try {
			return acceptThrows(v);
		} catch (final Exception e) {
			/* Do whatever here ... */
			LoggerFactory.getLogger(ThrowingHelper.class).error(
					"handling an exception...");
			throw new RuntimeException(e);
		}
	}

	T acceptThrows(V v) throws Exception;
}