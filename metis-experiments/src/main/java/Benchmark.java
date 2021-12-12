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
 * The benchmark contains a suite and an observer and is able to return the next problem. 
 */
public class Benchmark {
	
	private final Suite suite;
	private final Observer observer;
	
	/** 
	 * Constructor 
	 */
	public Benchmark(Suite suite, Observer observer) {
		this.suite = suite;
		this.observer = observer;
	}
	
	/**
	 * Function that returns the next problem in the suite. When it comes to the end of the suite, 
	 * it returns null.
	 * @return the next problem in the suite or null when there is no next problem
	 */
	public CocoProblem getNextProblem() throws Exception {
		
		try {		
			long problemPointer = CocoJNI.cocoSuiteGetNextProblem(suite.getPointer(), observer.getPointer());
			
			if (problemPointer == 0)
				return null;
			
			return new CocoProblem(problemPointer);
		} catch (Exception e) {
			throw new Exception("Fetching of next problem failed.\n" + e.toString());
		}
	}
	
	/**
	 * Finalizes the observer and suite. This method needs to be explicitly called in order to log 
	 * the last results.
	 */
	public void finalizeBenchmark() throws Exception {
		
		try {		
			observer.finalizeObserver();
			suite.finalizeSuite();
		} catch (Exception e) {
			throw new Exception("Benchmark finalization failed.\n" + e.toString());
		}
	}
}