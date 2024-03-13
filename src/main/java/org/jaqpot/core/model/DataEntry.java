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
import org.jaqpot.core.model.dto.dataset.EntryId;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataEntry extends JaqpotEntity {


    private String datasetId;

    private EntryId entryId;

    public DataEntry(){}

    public DataEntry(String id) {
        super(id);
    }

    TreeMap<String, Object> values;

    public EntryId getEntryId() {
        return entryId;
    }

    public void setEntryId(EntryId entryId) {
        this.entryId = entryId;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(TreeMap<String, Object> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "DataEntry{" + "datasetId=" + datasetId + ", values=" + values + '}';
    }

}
