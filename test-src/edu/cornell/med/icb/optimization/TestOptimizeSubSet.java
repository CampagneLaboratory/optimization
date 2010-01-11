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

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import junit.framework.TestCase;
import org.jgap.InvalidConfigurationException;

/**
 * @author Fabien Campagne
 *         Date: Oct 10, 2007
 *         Time: 3:15:19 PM
 */
public class TestOptimizeSubSet extends TestCase {
    private SubSetFitnessFunction largestCriterion = new AbstractSubSetFitnessFunction() {
        @Override
        public double evaluate(final IntSet subset) {
            int sum = 0;
            for (final int element : subset) {
                sum += element;
            }
            maxFitnessValue = Math.max(maxFitnessValue, sum);

            //System.out.println("fitness evaluated: "+sum+" max: "+maxFitnessValue);
            return sum;
        }
    };

    private SubSetFitnessFunction smallestCriterion = new AbstractSubSetFitnessFunction() {
        @Override
        public double evaluate(final IntSet subset) {
            int sum = 1000;
            for (final int element : subset) {
                sum -= element;
            }

            return sum;
        }
    };


    public synchronized void testSubSetLargest() throws InvalidConfigurationException {
        final IntSet set = new IntArraySet();
        final int[] elements = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        for (final int element : elements) {
            set.add(element);
        }

        final OptimizeSubSet optimizer = new OptimizeSubSet(set, 3, largestCriterion);
        optimizer.setRandomSeed(1);

        final boolean converged = optimizer.optimize(10000, 0.001);
        assertTrue(converged);

        final IntSet optimalsubSet = optimizer.getFitestSubset();
        printSubset(optimalsubSet);
        assertTrue(optimalsubSet.contains(7));
        assertTrue(optimalsubSet.contains(8));
        assertTrue(optimalsubSet.contains(9));


    }


    public synchronized void testSubSetLargestWithGaps() throws InvalidConfigurationException {
        final IntSet set = new IntArraySet();
        final int[] elements = {1, 74, 87, 900, 2, 61};
        for (final int element : elements) {
            set.add(element);
        }

        final OptimizeSubSet optimizer = new OptimizeSubSet(set, 3, largestCriterion);
        optimizer.setRandomSeed(1);

        final boolean converged = optimizer.optimize(10000, 0.001);
        assertTrue(converged);

        final IntSet optimalsubSet = optimizer.getFitestSubset();
        printSubset(optimalsubSet);
        assertTrue(optimalsubSet.contains(74));
        assertTrue(optimalsubSet.contains(87));
        assertTrue(optimalsubSet.contains(900));
    }

    private double maxFitnessValue;
    private SubSetFitnessFunction largestCriterionWithAlphaBeta = new AbstractSubSetFitnessFunction() {
        @Override
        public double evaluate(final IntSet subset, final double... paramValues) {
            assert paramValues.length == 2 : "evaluate must be called with two parameter values";
            int sum = 100;
            for (final int element : subset) {
                sum += (element * paramValues[0]) - paramValues[1];
            }
            maxFitnessValue = Math.max(maxFitnessValue, sum);
            return sum;
        }
    };

    public synchronized void testSubSetLargestWithGapsAndParams() throws InvalidConfigurationException {
        final IntSet set = new IntArraySet();
        final int[] elements = {1, 74, 87, 900, 2, 61};
        for (final int element : elements) {
            set.add(element);
        }

        // fitness function=sum of elements in subset multiplied by alpha, minus beta.
        final String param1 = "alpha=1,2,3,4,0.5,0.1";    //optimal alpha=4
        final String param2 = "beta=1,2,0,3,4,0.5";       //optimal beta=0
        final OptimizeSubSet optimizer = new OptimizeSubSet(set, 3, largestCriterionWithAlphaBeta, param1, param2);
        optimizer.setRandomSeed(1);

        final boolean converged = optimizer.optimize(10000, 0.001);
        assertTrue(converged);

        final IntSet optimalsubSet = optimizer.getFitestSubset();
        printSubset(optimalsubSet);
        assertTrue(optimalsubSet.contains(74));
        assertTrue(optimalsubSet.contains(87));
        assertTrue(optimalsubSet.contains(900));
        final double[] optimalParameterValues = optimizer.getFitestParameterValues();
        assertEquals(4d, optimalParameterValues[0]);
        assertEquals(0d, optimalParameterValues[1]);
    }

    public synchronized void testSubSetSmallest() throws InvalidConfigurationException {
        final IntSet set = new IntArraySet();
        final int[] elements = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        for (final int element : elements) {
            set.add(element);
        }
        final OptimizeSubSet optimizer = new OptimizeSubSet(set, 3, smallestCriterion);
        optimizer.setRandomSeed(1);
        final boolean converged = optimizer.optimize(10000, 0.001);
        assertTrue(converged);

        final IntSet optimalsubSet;
        System.out.println("Number of iterations: " + optimizer.getNumberOfIterationsPerformed());
        optimalsubSet = optimizer.getFitestSubset();
        printSubset(optimalsubSet);
        assertTrue(optimalsubSet.contains(1));
        assertTrue(optimalsubSet.contains(2));
        assertTrue(optimalsubSet.contains(3));
    }

    public synchronized void testSubSetLargest100() throws InvalidConfigurationException {
        final IntSet set = new IntArraySet();
        final int[] elements = new int[100];
        int max = Integer.MIN_VALUE;
        for (int i = 1; i <= elements.length; ++i) {
            set.add(i);
            max = Math.max(i, max);
        }
        System.out.println("max element: " + max);
        final OptimizeSubSet optimizer = new OptimizeSubSet(set, 3, largestCriterion);
        optimizer.setRandomSeed(39);
        optimizer.setPopulationSize(10000);
        maxFitnessValue = 0;
        final boolean converged = optimizer.optimize(10000, 0.001);
        assertEquals("fitest chromosome must have maximum fitness evaluated by fitness function.", maxFitnessValue, optimizer.getFitestFunctionValue());
        assertTrue(converged);
        System.out.println("Number of iterations: " + optimizer.getNumberOfIterationsPerformed());

        final IntSet optimalsubSet;

        optimalsubSet = optimizer.getFitestSubset();
        printSubset(optimalsubSet);
        assertTrue(optimalsubSet.contains(elements.length));
        assertTrue(optimalsubSet.contains(elements.length - 1));
        assertTrue(optimalsubSet.contains(elements.length - 2));
    }

    // The following tests are disabled because they take a long time to run.
    // optimization algorithms make no garantee that the optimal solution will be found, so this result is not too
    // surprising. It is interesting to note that algouth the optimal solution is not found, the solution found is
    // very close in each case.

    public synchronized void testSubSetLargest2000() throws InvalidConfigurationException {
        final IntSet set = new IntArraySet();
        final int[] elements = new int[2000];
        for (int i = 1; i <= elements.length; ++i) {
            set.add(i);
        }
        final double optimal = 2000 + 1999 + 1998;
        final OptimizeSubSet optimizer = new OptimizeSubSet(set, 3, largestCriterion);
        optimizer.setPopulationSize(1000);
        optimizer.setRandomSeed(1);
        maxFitnessValue = 0;
        final boolean converged = optimizer.optimize(20000, 0.001);
        assertEquals("fitest chromosome must have maximum fitness evaluated by fitness function.", maxFitnessValue, optimizer.getFitestFunctionValue());
        assertTrue(converged);

        final IntSet optimalsubSet;
        assertEquals("optimal fitest function must match true optimum", optimal, optimizer.getFitestFunctionValue());
        optimalsubSet = optimizer.getFitestSubset();
        printSubset(optimalsubSet);
        assertTrue(optimalsubSet.contains(elements.length));
        assertTrue(optimalsubSet.contains(elements.length - 1));
        assertTrue(optimalsubSet.contains(elements.length - 2));
    }

    public synchronized void TestSubSetLargest20000() throws InvalidConfigurationException {
        final IntSet set = new IntArraySet();
        final int[] elements = new int[20000];
        for (int i = 1; i <= elements.length; ++i) {
            set.add(i);
        }
        final double optimal = 20000 + 20000-1 + 20000-2;
        final OptimizeSubSet optimizer = new OptimizeSubSet(set, 3, largestCriterion);
        optimizer.setPopulationSize(1000);
        final boolean converged = optimizer.optimize(200000, 0.001);
        assertTrue(converged);
        assertEquals("optimal fitest function must match true optimum", optimal, optimizer.getFitestFunctionValue());

        final IntSet optimalsubSet;

        optimalsubSet = optimizer.getFitestSubset();
        printSubset(optimalsubSet);
        assertTrue(optimalsubSet.contains(elements.length));
        assertTrue(optimalsubSet.contains(elements.length - 1));
        assertTrue(optimalsubSet.contains(elements.length - 2));
    }

    private void printSubset(final IntSet optimalsubSet) {
        for (final int element : optimalsubSet) {
            System.out.println("element " + element);
        }
    }
}
