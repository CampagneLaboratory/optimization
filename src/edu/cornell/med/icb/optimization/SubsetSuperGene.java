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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.lang.MutableString;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.InvalidConfigurationException;
import org.jgap.RandomGenerator;
import org.jgap.supergenes.AbstractSupergene;
import org.jgap.supergenes.Supergene;

import java.util.Collections;
/**
 * @author Fabien Campagne
 *         Date: Oct 10, 2007
 *         Time: 4:34:43 PM
 */
public class SubsetSuperGene extends AbstractSupergene {
    IntSet subset;
    private int[] allElements;

    public SubsetSuperGene() throws InvalidConfigurationException {
        this.subset = new IntArraySet();
    }

    public SubsetSuperGene(final Configuration configuration) throws InvalidConfigurationException {
        super(configuration);
        this.subset = new IntArraySet();
    }

    public SubsetSuperGene(final Configuration configuration, final Gene[] subsetGenes) throws InvalidConfigurationException {
        super(configuration, subsetGenes);
        this.subset = new IntArraySet();
    }

    public SubsetSuperGene(final Configuration configuration, final Gene[] subsetGenes, final int[] allElements) throws InvalidConfigurationException {
        super(configuration, subsetGenes);
        this.subset = new IntArraySet();
        this.allElements = allElements;
    }


    @Override
    public synchronized boolean isValid(final Gene[] genes, final Supergene supergene) {
        subset.clear();
        int previous = -1;
        for (final Gene gene : genes) {
            final int element = (Integer) gene.getAllele();

            if (element <= previous) {
                // force increasing order of element to avoid considering order permutations
                return false;
            }
            subset.add(element);
            previous = element;
        }
        // valid solutions have no duplicate elements.
        return subset.size() == genes.length;
    }

    @Override
    public Gene newGene() {
        final SubsetSuperGene copy = (SubsetSuperGene) super.newGene();
        copy.allElements = allElements;
        return copy;
    }

    public IntSet getSubSet() {
        final IntSet result = new IntArraySet();
        for (final Gene gene : getGenes()) {
            final Integer element = (Integer) gene.getAllele();
            result.add(allElements[element]);

        }
        return result;
    }

    @Override
    public String toString() {
        final MutableString result = new MutableString();
        result.append("{ ");
        for (final Gene gene : getGenes()) {
            result.append(gene.getAllele());
            result.append(' ');
        }
        result.append("} ");
        return result.toString();
    }

    @Override
    public void setToRandomValue(final RandomGenerator a_numberGenerator) {

        final IntList sortedSubSet = new IntArrayList();
        final Gene[] m_genes = getGenes();
        for (int i = 0; i < m_genes.length; i++) {
            int value = -1;
            do {
                m_genes[i].setToRandomValue(a_numberGenerator);
                value = (Integer) m_genes[i].getAllele();
            } while (sortedSubSet.contains(value));
            sortedSubSet.add(value);
        }
        Collections.sort(sortedSubSet);
        // set all to random value first

        for (int i = 0; i < m_genes.length; i++) {
            m_genes[i].setAllele(sortedSubSet.get(i));
        }
        if (!isValid()) {
            throw new InternalError("Supergene content must be compatible with valid method");
        }
    }
}
