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
 * The problem contains some basic properties of the coco_problem_t structure that can be accessed
 * through its getter functions.
 */
public class CocoProblem {

	private final long pointer; // Pointer to the coco_problem_t object
	
	private final int dimension;
	private final int number_of_objectives;
	private final int number_of_constraints;
	
	private final double[] lower_bounds;
	private final double[] upper_bounds;
	private final int number_of_integer_variables;
	
	private final String id;
	private final String name;
	
	private final long index;

	/**
	 * Constructs the problem from the pointer.
	 * @param pointer pointer to the coco_problem_t object
	 * @throws Exception
	 */
	public CocoProblem(long pointer) throws Exception {

		super();
		try {		
			this.dimension = CocoJNI.cocoProblemGetDimension(pointer);
			this.number_of_objectives = CocoJNI.cocoProblemGetNumberOfObjectives(pointer);
			this.number_of_constraints = CocoJNI.cocoProblemGetNumberOfConstraints(pointer);
			
			this.lower_bounds = CocoJNI.cocoProblemGetSmallestValuesOfInterest(pointer);
			this.upper_bounds = CocoJNI.cocoProblemGetLargestValuesOfInterest(pointer);
			this.number_of_integer_variables = CocoJNI.cocoProblemGetNumberOfIntegerVariables(pointer);
			
			this.id = CocoJNI.cocoProblemGetId(pointer);
			this.name = CocoJNI.cocoProblemGetName(pointer);

			this.index = CocoJNI.cocoProblemGetIndex(pointer);
			
			this.pointer = pointer;
		} catch (Exception e) {
			throw new Exception("Problem constructor failed.\n" + e.toString());
		}
	}
	
	/**
	 * Evaluates the function in point x and returns the result as an array of doubles. 
	 * @return the result of the function evaluation in point x
	 */
	public double[] evaluateFunction(double[] x) {
		return CocoJNI.cocoEvaluateFunction(this.pointer, x);
	}

	/**
	 * Evaluates the constraint in point x and returns the result as an array of doubles. 
	 * @return the result of the constraint evaluation in point x
	 */
	public double[] evaluateConstraint(double[] x) {
		return CocoJNI.cocoEvaluateConstraint(this.pointer, x);
	}

	// Getters
	public long getPointer() {
		return this.pointer;
	}
	
	public int getDimension() {
		return this.dimension;
	}
	
	public int getNumberOfObjectives() {
		return this.number_of_objectives;
	}
	
	public int getNumberOfConstraints() {
		return this.number_of_constraints;
	}
	
	public double[] getSmallestValuesOfInterest() {
		return this.lower_bounds;
	}
	
	public double getSmallestValueOfInterest(int index) {
		return this.lower_bounds[index];
	}

	public double[] getLargestValuesOfInterest() {
		return this.upper_bounds;
	}
	
	public double getLargestValueOfInterest(int index) {
		return this.upper_bounds[index];
	}
	
	public int getNumberOfIntegerVariabls() {
		return this.number_of_integer_variables;
	}

	public double[] getLargestFValuesOfInterest() {
		return CocoJNI.cocoProblemGetLargestFValuesOfInterest(pointer);
	}
	
	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}
	
	public long getEvaluations() {
		return CocoJNI.cocoProblemGetEvaluations(pointer);
	}
	
	public long getEvaluationsConstraints() {
		return CocoJNI.cocoProblemGetEvaluationsConstraints(pointer);
	}
	
	public long getIndex() {
		return this.index;
	}
	
	public boolean isFinalTargetHit() {
		return (CocoJNI.cocoProblemIsFinalTargetHit(pointer) == 1);
	}
	
	/* toString method */
	@Override
	public String toString() {		
		return this.getId();
	}
}
