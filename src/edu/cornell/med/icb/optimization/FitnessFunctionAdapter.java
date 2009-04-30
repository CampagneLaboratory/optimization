/*
 * Copyright (C) 2007-2009 Institute for Computational Biomedicine,
 *                         Weill Medical College of Cornell University
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.cornell.med.icb.optimization;

import it.unimi.dsi.fastutil.ints.IntSet;
import org.jgap.FitnessFunction;
import org.jgap.IChromosome;
import org.jgap.impl.IntegerGene;

/**
 * @author Fabien Campagne
 *         Date: Oct 10, 2007
 *         Time: 1:33:20 PM
 */
public class FitnessFunctionAdapter extends FitnessFunction {
    private int k;
    private IntSet set;
    SubSetFitnessFunction delegate;
    private double[][] paramValues;

    public FitnessFunctionAdapter(final IntSet set, final int k, final SubSetFitnessFunction convergenceCriterion, final double[][] paramValues) {
        this.k = k;
        this.set = set;
        this.delegate = convergenceCriterion;
        this.paramValues = paramValues;
    }

    @Override
    protected double evaluate(final IChromosome potentialSolution) {
        final IntSet subset;
        final SubsetSuperGene subsetSuperGene = (SubsetSuperGene) potentialSolution.getGene(0);
        if (!subsetSuperGene.isValid()) {
            return 0;
        }
        subset = subsetSuperGene.getSubSet();
        final double[] evalParamValues = getParameterValues(potentialSolution, paramValues);

        return delegate.evaluate(subset, evalParamValues);

    }

    protected static double[] getParameterValues(final IChromosome potentialSolution,
                                                 final double[][] parameterValues) {
        final int numberOfParameters = potentialSolution.getGenes().length - 1;
        final double[] evalParamValues = new double[numberOfParameters];

        for (int paramIndex = 0; paramIndex < numberOfParameters; paramIndex++) {
            final IntegerGene paramGene = (IntegerGene) potentialSolution.getGene(1 + paramIndex);
            final int paramValueIndex = paramGene.intValue();
            final double paramValue = parameterValues[paramIndex][paramValueIndex];
            evalParamValues[paramIndex] = paramValue;
        }
        return evalParamValues;
    }


}
