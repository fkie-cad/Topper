package com.topper.dex.decompilation.pipeline;

import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.MissingStageInfoException;

public final class PipelineContext {

	private static final String METHOD_NAME_GET_RESULT = "getResult";

	@NonNull
	private static final String KEY_PIPELINE_ARGS = "PipelineArgs";

	@NonNull
	private final Map<@NonNull String, @NonNull StageInfo> results;

	public PipelineContext(@NonNull final PipelineArgs args) {

		this.results = new TreeMap<@NonNull String, @NonNull StageInfo>();
		this.results.put(KEY_PIPELINE_ARGS, args);
	}

	@NonNull
	public final <T extends StageInfo> T getResult(@NonNull final String key) throws MissingStageInfoException {

		if (!this.results.containsKey(key)) {
			throw new MissingStageInfoException("Key " + key + " does not exist.");
		}

		final StageInfo info = this.results.get(key);
		try {
			// TODO: Fix this somehow.
			final Class<T> type = (Class<T>) this.getClass()
					.getDeclaredMethod("getResult", String.class).getTypeParameters()[0].getGenericDeclaration().getGenericReturnType().getClass();
			System.out.println("Type: " + type.getSimpleName());
			if (!type.isAssignableFrom(info.getClass())) {
				throw new MissingStageInfoException("Key " + key + " does not refer to info that matches given type.");
			}
			return (T) this.results.get(key);
		} catch (final ClassCastException | NoSuchMethodException | SecurityException e) {
			throw new MissingStageInfoException("Resolving requested type with key " + key + " failed.", e);
		}
	}

	public final void putResult(@NonNull final String key, @NonNull final StageInfo info) {

		if (this.results.containsKey(key)) {
			throw new IllegalArgumentException("Key " + key + " is already part of the map.");
		}

		this.results.put(key, info);
	}

	@NonNull
	public final PipelineArgs getArgs() {
		return (PipelineArgs) this.results.get(KEY_PIPELINE_ARGS);
	}
}