package com.topper.dex.decompilation.pipeline;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.StageException;

public interface Stage {
	void execute(@NonNull final PipelineContext context) throws StageException;
}