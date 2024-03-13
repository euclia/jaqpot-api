/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it, in particular:
 * (i)   JaqpotCoreServices
 * (ii)  JaqpotAlgorithmServices
 * (iii) JaqpotDB
 * (iv)  JaqpotDomain
 * (v)   JaqpotEAR
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
package org.jaqpot.core.search.engine;

import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.InternalServerErrorException;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author pantelispanka
 */
@Singleton
@Startup
public class ElasticQueries {

    private String modelIndice;
    private String jaqpotIndex;
    private String jaqpotSearchQuery;
    
    
    @PostConstruct
    void init(){
        String model_indice = this.getFile("elastic/jaqpot_indice.json");
        this.modelIndice = model_indice;
        String model_index = this.getFile("elastic/jaqpot_index.json");
        this.jaqpotIndex = model_index;
        String query = this.getFile("elastic/jaqpot_search_query.json");
        this.jaqpotSearchQuery = query;
    }
    
    public void modelIndice() {
        String result = this.getFile("elastic/jaqpot_indice.json");
        this.modelIndice = result;
    }

    public void modelIndex() {
        String result = this.getFile("elastic/jaqpot_index.json");
        this.jaqpotIndex = result;
    }

    private String getFile(String filename) {
        String result = "";
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream(filename));
        } catch (IOException e) {
            throw new InternalServerErrorException(e);
        }
        return result;
    }

    public String getModelIndice() {
        return this.modelIndice;
    }

    public String getModelIndex() {
        return this.jaqpotIndex;
    }
    
    public String getQuery() {
        return this.jaqpotSearchQuery;
    }

}
