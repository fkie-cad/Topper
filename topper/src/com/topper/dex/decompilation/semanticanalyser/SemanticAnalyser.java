package com.topper.dex.decompilation.semanticanalyser;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.dex.decompilation.pipeline.Stage;
import com.topper.dex.decompilation.pipeline.StageInfo;

public interface SemanticAnalyser<@NonNull T extends Map<@NonNull String, @NonNull StageInfo>> extends Stage<T> {

}