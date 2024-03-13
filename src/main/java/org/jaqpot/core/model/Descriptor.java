/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licensed by GPL v3 as specified hereafter. Additional components may ship
 * with some other licence as will be specified therein.
 *
 * Copyright (C) 2014-2015 KinkyDesign (Charalampos Chomenidis, Pantelis Sopasakis)
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
 *
 * Source code:
 * The source code of JAQPOT Quattro is available on github at:
 * https://github.com/KinkyDesign/JaqpotQuattro
 * All source files of JAQPOT Quattro that are stored on github are licensed
 * with the aforementioned licence.
 */
package org.jaqpot.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Descriptor extends JaqpotEntity {

    /**
     * Descriptor's parameters.
     */
    private Set<Parameter> parameters;
    /**
     * Users' implementations are ranked by other users.
     */
    private int ranking;
    /**
     * BibTeX reference were one can find more info about the algorithm.
     */
    private Set<BibTeX> bibtex;

    private String descriptorService;


    public Descriptor() {
    }

    public Descriptor(String id) {
        super(id);
    }

    public Descriptor(Descriptor other) {
        super(other);
        this.bibtex = other.bibtex != null ? new HashSet<>(other.bibtex) : null;
        this.parameters = other.parameters != null ? new HashSet<>(other.parameters) : null;
        this.ranking = other.ranking;
        this.descriptorService = other.descriptorService;
    }

    public Set<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(Set<Parameter> parameters) {
        this.parameters = parameters;
    }

    public Integer getRanking() {
        return ranking;
    }

    public void setRanking(Integer ranking) {
        this.ranking = ranking;
    }

    public Set<BibTeX> getBibtex() {
        return bibtex;
    }

    public void setBibtex(Set<BibTeX> bibtex) {
        this.bibtex = bibtex;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public String getDescriptorService() {
        return descriptorService;
    }

    public void setDescriptorService(String descriptorService) {
        this.descriptorService = descriptorService;
    }
}
