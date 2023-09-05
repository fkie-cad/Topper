package com.topper.dex.seeker;

import com.topper.dex.pipeline.Stage;

/**
 * Abstract description of a seeker, whose task is to
 * find offsets in a given buffer, and forward these
 * offsets to the next stage.
 * 
 * Its default implementation is the {@link PivotSeeker}.
 * 
 * @author Pascal Kühnemann
 * @since 15.08.2023
 * */
public abstract class Seeker implements Stage {

}