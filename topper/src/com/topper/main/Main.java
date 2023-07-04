package com.topper.main;

import java.io.IOException;

import com.topper.interactive.InteractiveTopper;

public class Main {

	public static void main(String[] args) throws IOException {

		final InteractiveTopper interactive = new InteractiveTopper(null);
		interactive.mainLoop();
	}

}
