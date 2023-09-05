package com.topper.dex.pipeline;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.pipeline.DuplicateInfoIdException;
import com.topper.exceptions.pipeline.MissingStageInfoException;

/**
 * Execution context of the {@link Pipeline} that abstracts away managing id -
 * based {@link Stage} parameters.
 * 
 * It is guaranteed that a <code>PipelineContext</code> at least contains an
 * instance of {@link PipelineArgs}.
 * 
 * @author Pascal Kühnemann
 * @since 15.08.2023
 */
public final class PipelineContext {

	/**
	 * Unique identifier for {@link PipelineArgs}.
	 */
	@NonNull
	private static final String KEY_PIPELINE_ARGS = "PipelineArgs";

	/**
	 * Mapping of string identifiers to <code>StageInfo</code> implementations.
	 */
	@NonNull
	private final Map<@NonNull String, @NonNull StageInfo> results;

	/**
	 * Create execution context by providing initial {@link Pipeline} arguments. It
	 * stores <code>args</code> under a unique identifier, which results in
	 * {@link PipelineContext#getArgs}.
	 * 
	 * @param args <code>PipelineArgs</code> used in pipeline execution.
	 */
	public PipelineContext(@NonNull final PipelineArgs args) {

		this.results = new TreeMap<@NonNull String, @NonNull StageInfo>();
		this.results.put(KEY_PIPELINE_ARGS, args);
	}

	/**
	 * Retrieves {@link StageInfo} based on the given <code>key</code>.
	 * 
	 * As it is not possible to check that the generic return type matches the
	 * retrieved <code>StageInfo</code> subclass, the caller must take into account
	 * {@link ClassCastException}s.
	 * 
	 * @param key <code>String</code> identifier for the <code>StageInfo</code> to
	 *            retrieve.
	 * @return A instance of a subclass of <code>StageInfo</code> mapped to
	 *         <code>key</code>.
	 * @throws MissingStageInfoException If <code>key</code> does not exist in the
	 *                                   mapping.
	 */
	@NonNull
	public final <T extends StageInfo> T getInfo(@NonNull final String key) throws MissingStageInfoException {

		if (!this.results.containsKey(key)) {
			throw new MissingStageInfoException("Key " + key + " does not exist.");
		}

		return (T) this.results.get(key);
	}

	/**
	 * Stores an instance of {@link StageInfo} under a unique identifier
	 * <code>key</code>. If <code>key</code> already exists in the underlying map,
	 * then this operation will fail.
	 * 
	 * @param key  Unique identifier, under which to store <code>info</code>.
	 * @param info <code>StageInfo</code> to store under identifier
	 *             <code>key</code>.
	 * @throws DuplicateInfoIdException If <code>key</code> is already part of the
	 *                                  underlying map, i.e. <code>key</code> would
	 *                                  not be unique.
	 */
	public final void putInfo(@NonNull final String key, @NonNull final StageInfo info)
			throws DuplicateInfoIdException {

		if (this.results.containsKey(key)) {
			throw new DuplicateInfoIdException("Key " + key + " is already part of the map.");
		}

		this.results.put(key, info);
	}

	/**
	 * Retrieves {@link SeekerInfo} based on the given <code>key</code>.
	 * 
	 * @param key <code>String</code> identifier for the <code>SeekerInfo</code> to
	 *            retrieve.
	 * @return A instance of a subclass of <code>SeekerInfo</code> mapped to
	 *         <code>key</code>.
	 * @throws MissingStageInfoException If <code>key</code> does not exist in the
	 *                                   mapping, or the resulting instance cannot
	 *                                   be casted to a <code>SeekerInfo</code>.
	 */
	@NonNull
	public final SeekerInfo getSeekerInfo(@NonNull final String key) throws MissingStageInfoException {

		try {
			return (SeekerInfo) this.getInfo(key);
		} catch (final ClassCastException e) {
			throw new MissingStageInfoException("Key " + key + " does not refer to SeekerInfo.", e);
		}
	}

	/**
	 * Retrieves {@link SweeperInfo} based on the given <code>key</code>.
	 * 
	 * @param key <code>String</code> identifier for the <code>SweeperInfo</code> to
	 *            retrieve.
	 * @return A instance of a subclass of <code>SweeperInfo</code> mapped to
	 *         <code>key</code>.
	 * @throws MissingStageInfoException If <code>key</code> does not exist in the
	 *                                   mapping, or the resulting instance cannot
	 *                                   be casted to a <code>SweeperInfo</code>.
	 */
	@NonNull
	public final SweeperInfo getSweeperInfo(@NonNull final String key) throws MissingStageInfoException {

		try {
			return (SweeperInfo) this.getInfo(key);
		} catch (final ClassCastException e) {
			throw new MissingStageInfoException("Key " + key + " does not refer to SweeperInfo.", e);
		}
	}

	/**
	 * Retrieves {@link StaticInfo} based on the given <code>key</code>.
	 * 
	 * @param key <code>String</code> identifier for the <code>StaticInfo</code> to
	 *            retrieve.
	 * @return A instance of a subclass of <code>StaticInfo</code> mapped to
	 *         <code>key</code>.
	 * @throws MissingStageInfoException If <code>key</code> does not exist in the
	 *                                   mapping, or the resulting instance cannot
	 *                                   be casted to a <code>StaticInfo</code>.
	 */
	@NonNull
	public final StaticInfo getStaticInfo(@NonNull final String key) throws MissingStageInfoException {

		try {
			return (StaticInfo) this.getInfo(key);
		} catch (final ClassCastException e) {
			throw new MissingStageInfoException("Key " + key + " does not refer to StaticInfo.", e);
		}
	}

	/**
	 * Retrieves the {@link PipelineArgs} from the underlying map initially provided
	 * to {@link PipelineContext#PipelineContext(PipelineArgs)}.
	 * 
	 * @return Unique instance of <code>PipelineArgs</code>.
	 */
	@NonNull
	public final PipelineArgs getArgs() {
		return (PipelineArgs) this.results.get(KEY_PIPELINE_ARGS);
	}
}