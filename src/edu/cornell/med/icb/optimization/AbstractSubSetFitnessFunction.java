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

import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * @author Fabien Campagne
 *         Date: Apr 5, 2008
 *         Time: 11:29:40 AM
 */
public abstract class AbstractSubSetFitnessFunction implements SubSetFitnessFunction {

    public double evaluate(final IntSet subset, final double... paramValues) {
        assert paramValues.length == 0 : " paramValues are not supported by this implementation.";
        return evaluate(subset);
    }

    public double evaluate(final IntSet subset) {
        assert false : "This method must be overriden with a functional implementation";
        return 0;
    }
}
