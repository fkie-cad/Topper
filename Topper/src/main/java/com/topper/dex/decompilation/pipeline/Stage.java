package com.topper.dex.decompilation.pipeline;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.StageException;

/**
 * Single step in a {@link Pipeline}. It performs
 * some computations on a given {@link PipelineContext}
 * and optionally adds information to the <code>PipelineContext</code>.
 * 
 * @author Pascal Kühnemann
 * @since 16.08.2023
 * */
public interface Stage {
	
	/**
	 * Performs computations on the given {@link PipelineContext} and
	 * may store its output in that <code>context</code>.
	 * 
	 * @param context <code>PipelineContext</code>, under which execution takes place.
	 * @throws StageException If any error occurs.
	 * */
	void execute(@NonNull final PipelineContext context) throws StageException;
}