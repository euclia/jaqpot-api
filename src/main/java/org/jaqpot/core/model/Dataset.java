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

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dataset extends JaqpotEntity {

    /**
     * List of links to substances.
     */
    private List<String> substances;

    /**
     * An ordered list of Features. These features are standard properties of
     * the substance as opposed to prediction features. These are not calculated
     * or measured properties, but actual values that either measure or computed
     * in-silico.
     */
    private List<String> features;
    /**
     * List of predicted features. This is an ordered list of features for which
     * a QSAR/QSPR/QNAR model is needed to predict them.
     */
    private List<String> predictedFeatures;
    /**
     * User ID of the creator of the dataset.
     */
    private String createdBy;

    private Boolean onTrash;
    
    public Dataset() {
    }

    public Dataset(String id) {
        super(id);
    }

    public Dataset(Dataset other) {
        super(other);
        this.createdBy = other.createdBy;
        this.features = other.features != null ? new ArrayList<>(other.features) : null;
        this.predictedFeatures = other.predictedFeatures != null ? new ArrayList<>(other.predictedFeatures) : null;
        this.substances = other.substances != null ? new ArrayList<>(other.substances) : null;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<String> getSubstances() {
        return substances;
    }

    public void setSubstances(List<String> substances) {
        this.substances = substances;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public List<String> getPredictedFeatures() {
        return predictedFeatures;
    }

    public void setPredictedFeatures(List<String> predictedFeatures) {
        this.predictedFeatures = predictedFeatures;
    }

    public Boolean getOnTrash() {
        return onTrash;
    }

    public void setOnTrash(Boolean onTrash) {
        this.onTrash = onTrash;
    }
    
}
