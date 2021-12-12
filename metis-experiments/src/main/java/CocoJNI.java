/*
 * Copyright (C) Zijie Cong 2021
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * This class contains the declaration of all the CocoJNI functions.
 */
public class CocoJNI {

	/* Load the library */
	static {
		System.loadLibrary("CocoJNI");
	}

	/* Native methods */
	public static native void cocoSetLogLevel(String logLevel);
	
	// Observer
	public static native long cocoGetObserver(String observerName, String observerOptions);
	public static native void cocoFinalizeObserver(long observerPointer);
	public static native long cocoProblemAddObserver(long problemPointer, long observerPointer);
	public static native long cocoProblemRemoveObserver(long problemPointer, long observerPointer);

	// Suite
	public static native long cocoGetSuite(String suiteName, String suiteInstance, String suiteOptions);
	public static native void cocoFinalizeSuite(long suitePointer);

	// Problem
	public static native long cocoSuiteGetNextProblem(long suitePointer, long observerPointer);
	public static native long cocoSuiteGetProblem(long suitePointer, long problemIndex);

	// Functions
	public static native double[] cocoEvaluateFunction(long problemPointer, double[] x);
	public static native double[] cocoEvaluateConstraint(long problemPointer, double[] x);

	// Getters
	public static native int cocoProblemGetDimension(long problemPointer);
	public static native int cocoProblemGetNumberOfObjectives(long problemPointer);
	public static native int cocoProblemGetNumberOfConstraints(long problemPointer);

	public static native double[] cocoProblemGetSmallestValuesOfInterest(long problemPointer);
	public static native double[] cocoProblemGetLargestValuesOfInterest(long problemPointer);
	public static native int cocoProblemGetNumberOfIntegerVariables(long problemPointer);
	
	public static native double[] cocoProblemGetLargestFValuesOfInterest(long problemPointer);

	public static native String cocoProblemGetId(long problemPointer);
	public static native String cocoProblemGetName(long problemPointer);
	
	public static native long cocoProblemGetEvaluations(long problemPointer);
	public static native long cocoProblemGetEvaluationsConstraints(long problemPointer);
	public static native long cocoProblemGetIndex(long problemPointer); 
	
	public static native int cocoProblemIsFinalTargetHit(long problemPointer);
}
