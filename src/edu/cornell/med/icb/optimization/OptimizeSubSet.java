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

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.logging.ProgressLogger;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.RandomGenerator;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.IntegerGene;
import org.jgap.supergenes.AbstractSupergene;

import java.text.DecimalFormat;
import java.text.Format;

/**
 * Optimize a subset of elements according to some convergenceCriterion. Given a set S of N
 * elements, determines the subset Sk of S with k elements which maximizes a
 * configurable convergenceCriterion.
 *
 * @author Fabien Campagne Date: Oct 10, 2007 Time: 12:38:14 PM
 */
public class OptimizeSubSet {
    private int[] allElements;
    private final SubSetFitnessFunction convergenceCriterion;
    private int k;

    private FitnessFunction fitnessFunction;
    Gene[] subsetGenes;
    final int numElementsInSet;
    IChromosome subsetChromosome;
    int numParams;
    private static final int DEFAULT_POPULATION_SIZE = 1000;
    private Configuration configuration;
    private int numberOfIterationsPerformed;
    RandomGenerator randomGenerator;


    double randomSeed;
    private IntSet set;
    private boolean configured;
    private int numberOfParameters;
    private double fitestFunctionValue;

    public void setModuloProgressReport(final int moduloProgressReport) {
        this.moduloProgressReport = moduloProgressReport;
    }

    private int moduloProgressReport = 100;

    public OptimizeSubSet(final IntSet set, final int k, final SubSetFitnessFunction convergenceCriterion) throws InvalidConfigurationException {
        this(set, k, convergenceCriterion, DEFAULT_POPULATION_SIZE);
    }

    public OptimizeSubSet(final IntSet set, final int k, final SubSetFitnessFunction convergenceCriterion, final String... params) throws InvalidConfigurationException {
        this(set, k, convergenceCriterion, DEFAULT_POPULATION_SIZE, params);
    }

    public OptimizeSubSet(final IntSet set, final int k, final SubSetFitnessFunction convergenceCriterion, final int populationSize,
                          final String... params) throws InvalidConfigurationException {
        this.numParams = params.length;
        parseParameterDefinitions(params);
        this.set = set;
        this.k = k;
        int i = 0;
        this.allElements = new int[set.size()];
        for (final int element : set) {
            allElements[i++] = element;
        }

        this.convergenceCriterion = convergenceCriterion;
        this.k = k;
        numElementsInSet = set.size();
        // This is odd: JGAP seems to maintain properties associated with each thread, so creating a new
        // Configuration instance is not sufficient to indicate that we want a fresh configuration. We have
        // to call the static reset method on Configuration..
        Configuration.reset();

        randomGenerator = new TwisterGenerator();
        this.populationSize = populationSize;
        configure();
    }

    private String[] parameterNames;
    private double[][] allPossibleParameterValues;

    private void parseParameterDefinitions(final String[] paramDefinitions) {
        int paramIndex = 0;
        parameterNames = new String[paramDefinitions.length];
        allPossibleParameterValues = new double[paramDefinitions.length][];
        for (final String parameterDefinition : paramDefinitions) {
            final String[] tokens = parameterDefinition.split("[=,]");
            assert tokens.length > 3 : "parameter definition must be of the form name=value1,value2,...";
            parameterNames[paramIndex] = tokens[0];
            final DoubleList paramValues = new DoubleArrayList();
            for (int valueIndex = 1; valueIndex < tokens.length; valueIndex++) {
                paramValues.add(Double.parseDouble(tokens[valueIndex]));
            }
            allPossibleParameterValues[paramIndex] = paramValues.toDoubleArray();
            paramIndex++;
        }
    }

    /**
     * Set the number of individuals in the population used by the genetic algorithm.
     *
     * @param populationSize
     */
    public void setPopulationSize(final int populationSize) {
        this.populationSize = populationSize;
    }

    public void setRandomSeed(final int randomSeed) {
        this.randomSeed = randomSeed;
        this.randomGenerator = new TwisterGenerator(randomSeed);
    }

    private int populationSize;


    public void configure() throws InvalidConfigurationException {


        configuration = new DefaultConfiguration();
        configuration.setPreservFittestIndividual(true);
        configuration.setKeepPopulationSizeConstant(true);

        configuration.setRandomGenerator(randomGenerator);
        fitnessFunction = new FitnessFunctionAdapter(set, k, convergenceCriterion, allPossibleParameterValues);

      configuration.setBulkFitnessFunction(new BulkFitnessNoOffset(fitnessFunction));

        subsetChromosome = setupChromosome(allElements);
        configuration.setSampleChromosome(subsetChromosome);
        configuration.setPopulationSize(populationSize);
        progressLogger = new ProgressLogger(log);
        fitestSubset = new IntArraySet();
        configured = true;
    }

    /**
     * Creates a chromosome that encodes the optimization problem.
     *
     * @param set The set of values from which solutions will be chosen.
     * @return A valid JGAP chromosome.
     * @throws InvalidConfigurationException If any error occurs creating the chromosome.
     */
    private IChromosome setupChromosome(final int[] set) throws InvalidConfigurationException {
        subsetGenes = new Gene[k];
        int minElementValue = Integer.MAX_VALUE;
        int maxElementValue = Integer.MIN_VALUE;
        for (final int element : set) {
            minElementValue = Math.min(element, minElementValue);
            maxElementValue = Math.max(element, maxElementValue);
        }

        for (int g = 0; g < k; g++) {

            subsetGenes[g] = new IntegerGene(configuration, 0, numElementsInSet - 1);
        }
        final Gene[] genes = new Gene[1 + getNumberOfParameters()];
        final AbstractSupergene constrainedGenes = new SubsetSuperGene(configuration, subsetGenes, this.allElements);
        genes[0] = constrainedGenes;
        for (int paramIndex = 0; paramIndex < getNumberOfParameters(); paramIndex++) {
            genes[1 + paramIndex] = new IntegerGene(configuration, 0, this.allPossibleParameterValues[paramIndex].length - 1);
        }
        subsetChromosome = new Chromosome(configuration, genes);
        return subsetChromosome;
    }

    ProgressLogger progressLogger;
    Logger log = Logger.getLogger(OptimizeSubSet.class);

    public void setLog(final Logger log) {
        this.log = log;
        progressLogger = new ProgressLogger(log);
    }

    public int getNumberOfIterationsPerformed() {
        return numberOfIterationsPerformed;
    }

    /**
     * Find an optimal subset, given the convergenceCriterion. Optimization will proceed for at most numSteps. If convergence of
     * the convergenceCriterion cost function occurs becore maxSteps, the optimization terminates. Formally, the optimization
     * terminates if cost(step i+1) - cost(step i) < abs(convergenceDelta) for more than deltaNumSteps contiguous steps.
     *
     * @param numSteps         Maximum number of steps before stopping.
     * @param convergenceDelta convergence cost function convergenceCriterion
     * @return True when the optimization has converged, False otherwise.
     * @throws org.jgap.InvalidConfigurationException
     *          If an error occurs configuring the optimization solver.
     */
    public boolean optimize(final int numSteps, final double convergenceDelta) throws InvalidConfigurationException {
        if (!configured) {
            configure();
        }
        final int deltaNumSteps = numSteps/10;
        final Genotype population = Genotype.randomInitialGenotype(configuration);
        final boolean converged;
        int timesFitnessStable = 0;
        double previousFitness = -1;
        progressLogger.expectedUpdates = numSteps;
        progressLogger.start("optimization");
        IChromosome fitestChromosome = null;
        double fitness = -2;
        for (int i = 0; i < numSteps; i++) {
             //   writeCurrentPopulation(population);
            fitestChromosome = population.getFittestChromosome();

            fitness = fitnessFunction.getFitnessValue(fitestChromosome);
            fitestFunctionValue = fitness;
            if (i % moduloProgressReport == 1) {
                log.info("Current solution has a fitness value of " +
                        formatDouble(Math.log(fitestChromosome.getFitnessValue())) +
                        " absolute: " + formatDouble(Math.log(fitness)) +
                        "or raw: " + fitness);
                log.debug(fitestChromosome.getGene(0).toString());
            }
            if (fitness >= previousFitness && fitness - previousFitness < Math.abs(convergenceDelta)) {
                timesFitnessStable++;
                log.trace("fitness function value stable " + timesFitnessStable);
            } else {
                timesFitnessStable = 0;
                log.trace("fitness function not stable, old: " + previousFitness + " new fitness: " + fitness);
            }

            previousFitness = fitness;
            if (timesFitnessStable >= deltaNumSteps) {
                break;
            }
            numberOfIterationsPerformed = i;
            if (i != numSteps - 1) {
                // do not evolve if this is the last step.
                population.evolve();
                progressLogger.lightUpdate();
            }

        }
        if (fitness - previousFitness < Math.abs(convergenceDelta)) {
            converged = true;
        } else {
            converged = false;
        }
        progressLogger.stop("optimization");
        convertFittestToSolution(fitestChromosome);

        return converged;

    }

    private IntSet fitestSubset;
    private double[] fitestParams;

    private void convertFittestToSolution(final IChromosome fittestChromosome) {
        if (fittestChromosome == null) {
            fitestSubset = null;
            fitestParams = ArrayUtils.EMPTY_DOUBLE_ARRAY;
        } else {
            fitestSubset = ((SubsetSuperGene) fittestChromosome.getGene(0)).getSubSet();
            fitestParams = FitnessFunctionAdapter.getParameterValues(fittestChromosome, allPossibleParameterValues);

        }

    }

    /**
     * Get the fittest subset of elements. The subset with the elements that maximize the fitness function.
     *
     * @return The subset with the elements that maximize the fitness function.
     */
    public IntSet getFitestSubset() {
        return fitestSubset;
    }

    public double[] getFitestParameterValues() {
        return fitestParams;
    }

    Format formatter = new DecimalFormat("0.00");

    private String formatDouble(final double v) {

        return formatter.format(v);
    }

    public int getNumberOfParameters() {
        return numParams;
    }

    public double getFitestFunctionValue() {
        return fitestFunctionValue;
    }
}
