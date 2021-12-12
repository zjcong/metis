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

public class Suite {
	
	private final long pointer; // Pointer to the coco_suite_t object
	private final String name;

	/**
	 * Constructs the suite from the given suiteName, suiteInstance and suiteOptions.
	 * See http://numbbo.github.io/coco-doc/C/#suite-parameters for more information on 
	 * valid suite parameters.
	 */
	public Suite(String suiteName, String suiteInstance, String suiteOptions) throws Exception {

		super();
		try {
			this.pointer = CocoJNI.cocoGetSuite(suiteName, suiteInstance, suiteOptions);
			this.name = suiteName;
		} catch (Exception e) {
			throw new Exception("Suite constructor failed.\n" + e);
		}
	}

	/**
	 * Finalizes the suite.
	 */
	public void finalizeSuite() throws Exception {
		try {
			CocoJNI.cocoFinalizeSuite(this.pointer);
		} catch (Exception e) {
			throw new Exception("Suite finalization failed.\n" + e);
		}
	}

	public long getPointer() {
		return this.pointer;
	}
	
	public String getName() {
		return this.name;
	}

	/* toString method */
	@Override
	public String toString() {
		return getName();
	}
}