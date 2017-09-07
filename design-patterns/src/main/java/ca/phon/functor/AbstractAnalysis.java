/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.functor;

import java.util.*;

public abstract class AbstractAnalysis<R, T> implements Analysis<R, T> {
	
	protected final List<AnalysisStep<?, ?>> functors = 
			Collections.synchronizedList(new ArrayList<AnalysisStep<?, ?>>());

	public AbstractAnalysis() {
	}
	
	public void addStep(AnalysisStep<?, ?> step) {
		functors.add(step);
	}
	
	@Override
	public AnalysisStep<?, ?>[] getAnalysis() {
		return functors.toArray(new AnalysisStep<?, ?>[0]);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public R performAnalysis(T val) {
		checkAnalysis();
		Object prevReturn = val;
		for(AnalysisStep step:getAnalysis()) {
			prevReturn = step.op(prevReturn);
		}
		return (R)getReturnType().cast( prevReturn );
	}

	public abstract Class<R> getReturnType();
	
	public abstract Class<T> getParameterType();

	// ensure sanity
	private void checkAnalysis() {
		if(getAnalysis().length == 0)
			throw new IllegalStateException("Empty analysis");
		
		Class<?> paramType = getParameterType();
		for(AnalysisStep<?, ?> step:getAnalysis()) {
			// ensure param type match
			if(!paramType.isAssignableFrom(step.getParameterType())) {
				throw new IllegalStateException("Analysis chain does not link");
			}
			paramType = step.getReturnType();
		}
		
		final AnalysisStep<?, ?> lastStep = getAnalysis()[(getAnalysis().length-1)];
		if(!getReturnType().isAssignableFrom(lastStep.getReturnType())) {
			throw new IllegalStateException("Analysis chain is incomplete");
		}
	}

}
