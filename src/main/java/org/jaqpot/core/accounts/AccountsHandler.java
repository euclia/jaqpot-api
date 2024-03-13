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
package org.jaqpot.core.accounts;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import org.jaqpot.core.properties.PropertyManager;
import xyz.euclia.euclia.accounts.client.EucliaAccounts;
import xyz.euclia.euclia.accounts.client.EucliaAccountsFactory;
import xyz.euclia.jquots.serialize.Serializer;

/**
 *
 * @author pantelispanka
 */
@Startup
@Singleton
@DependsOn("PropertyManager")
public class AccountsHandler {

    private static final Logger LOG = Logger.getLogger(AccountsHandler.class.getName());
    
    @Inject
    PropertyManager propertyManager;
    
    private EucliaAccounts client;

    
    
    @PostConstruct
    public void init() {
        String accountsBase = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.EUCLIA_ACCOUNTS_URL);
        client = EucliaAccountsFactory.createNewClient(accountsBase);
    }
    
    @PreDestroy
    public void destroy() throws IOException{
        try{
            this.client.close();
        }catch(IOException e){
            LOG.log(Level.SEVERE, e.getLocalizedMessage());
        }
        
    }
    
    public EucliaAccounts getClient(){
        return this.client;
    }
    
}
