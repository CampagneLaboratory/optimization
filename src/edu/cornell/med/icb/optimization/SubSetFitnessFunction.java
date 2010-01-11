/*
 * Copyright (C) 2007-2010 Institute for Computational Biomedicine,
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

/**
 * @author Fabien Campagne
 *         Date: Oct 10, 2007
 *         Time: 1:20:33 PM
 */
public interface SubSetFitnessFunction {
    /**
     * Evaluate the fitness function given a subset. Larger fitness values are
     * more fit and will be favored during optimization.
     * @param subset  Subset of elements from the initial set.
     * @param paramValues value of each define parameters (in the order that the parameters
     * were provided to the Constructor of {@link edu.cornell.med.icb.optimization.OptimizeSubSet})
     * @return Value of the fitness function for the given subset.
     */
    double evaluate(IntSet subset, double ... paramValues);
        /**
     * Evaluate the fitness function given a subset. Larger fitness values are
     * more fit and will be favored during optimization.
     * @param subset  Subset of elements from the initial set.
     * @return Value of the fitness function for the given subset.
     */
    double evaluate(IntSet subset);
}
