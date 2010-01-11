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

import cern.jet.random.engine.MersenneTwister;
import org.jgap.RandomGenerator;

import java.util.Date;

/**
 * An adapter to use the Colt MersenneTwister random engine with JGAP.
 *  @author Fabien Campagne
 *         Date: Oct 10, 2007
 *         Time: 6:50:30 PM
 */
public class TwisterGenerator extends MersenneTwister implements RandomGenerator {
    public TwisterGenerator(final Date date) {
        super(date);
    }

    public TwisterGenerator() {
        super();
    }

    public TwisterGenerator(final int seed) {
        super(seed);
    }

    public int nextInt(final int ceiling) {
        return (int) Math.round(nextDouble() * (ceiling - 1));
    }

    public boolean nextBoolean() {
        return nextDouble() < 0.5d;
    }
}
