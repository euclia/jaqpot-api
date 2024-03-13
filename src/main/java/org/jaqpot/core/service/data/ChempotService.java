/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.data;

import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.factory.TaskFactory;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;

/**
 *
 * @author pantelispanka
 */

@Stateless
public class ChempotService {
    
    @EJB
    TaskHandler taskHandler;

    @Resource(lookup = "java:jboss/exported/jms/topic/chempot")
    private Topic chempotQueue;

    @Inject
    private JMSContext jmsContext;

    public Task initiatePrediction(Map<String, Object> options, String userName) throws JaqpotDocumentSizeExceededException {
        Task task = TaskFactory.queuedTask("Preparation on file: " + options.get("filename"),
                "A chempot procedure will return a Dataset if completed successfully."
                        + "It may also initiate other procedures if desired.",
                userName);
        task.setType(Task.Type.PREDICTION);
        options.put("taskId", task.getId());
        task.setVisible(Boolean.TRUE);
        taskHandler.create(task);
        jmsContext.createProducer().setDeliveryDelay(1000).send(chempotQueue, options);
        return task;
    }
}
