/*
 * Copyright (C) 2008-2010 Institute for Computational Biomedicine,
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

import org.jgap.BulkFitnessFunction;
import org.jgap.FitnessFunction;
import org.jgap.IChromosome;
import org.jgap.Population;

import java.util.Iterator;

/**
 * This class implements BulkFitnessFunction, but does not remove a population offset from
 * the fitness value.
 * @author Fabien Campagne
 *         Date: Apr 5, 2008
 *         Time: 4:44:49 PM
 */
public class BulkFitnessNoOffset extends BulkFitnessFunction {
    private final FitnessFunction fitnessFunction;

    public BulkFitnessNoOffset(final FitnessFunction fitnessFunction) {
        this.fitnessFunction = fitnessFunction;
    }

    @Override
    public void evaluate(final Population a_chromosomes) {
         double curFitness;
        final Iterator itChromosomes = a_chromosomes.iterator();
        IChromosome chromosome;
        while (itChromosomes.hasNext()) {
            chromosome = (IChromosome) itChromosomes.next();

            curFitness = chromosome.getFitnessValueDirectly();
            if (curFitness < 0) {
                // fitness was not evaluated for this chromosome yet.

                curFitness = fitnessFunction.getFitnessValue(chromosome);
                // And store it to avoid evaluation of the same Chromosome again:
                chromosome.setFitnessValue(curFitness);
            } else {
                chromosome.setFitnessValue(curFitness);
            }

        }
    }
}
