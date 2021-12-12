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

public class Observer {
	
	private long pointer; // Pointer to the coco_observer_t object
	private String name;

	/** 
	 * Constructs the observer from observerName and observerOptions.
	 * See http://numbbo.github.io/coco-doc/C/#observer-parameters for more information on 
	 * valid observer parameters.
	 * @param observerName
	 * @param observerOptions
	 * @throws Exception
	 */
	public Observer(String observerName, String observerOptions) throws Exception {

		super();
		try {
			this.pointer = CocoJNI.cocoGetObserver(observerName, observerOptions);
			this.name = observerName;
		} catch (Exception e) {
			throw new Exception("Observer constructor failed.\n" + e.toString());
		}
	}

	/**
	 * Finalizes the observer.
	 * @throws Exception 
	 */
	public void finalizeObserver() throws Exception {
		try {
			CocoJNI.cocoFinalizeObserver(this.pointer);
		} catch (Exception e) {
			throw new Exception("Observer finalization failed.\n" + e.toString());
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