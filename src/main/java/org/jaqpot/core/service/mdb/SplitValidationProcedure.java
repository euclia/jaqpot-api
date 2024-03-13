package org.jaqpot.core.service.mdb;

import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.*;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.data.wrappers.DatasetLegacyWrapper;
import org.jaqpot.core.model.*;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.jpdi.TrainingRequest;
import org.jaqpot.core.model.factory.DatasetFactory;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.properties.PropertyManager;
import org.jaqpot.core.service.annotations.Secure;
import org.jaqpot.core.service.client.jpdi.JPDIClient;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Angelos Valsamis
 * @author Charalampos Chomenidis
 * @author Georgios Drakakis
 * @author Pantelis Sopasakis
 *
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
            propertyValue = "java:jboss/exported/jms/topic/validationSplit")
    ,
    @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "javax.jms.Topic")
})
public class SplitValidationProcedure extends AbstractJaqpotProcedure {

    private static final Logger LOG = Logger.getLogger(SplitValidationProcedure.class.getName());

    @EJB
    AlgorithmHandler algorithmHandler;

    @EJB
    ReportHandler reportHandler;

    @EJB
    DatasetLegacyWrapper datasetLegacyWrapper;
    
    @Inject
    @Jackson
    JSONSerializer serializer;

    @Inject
    JPDIClient jpdiClient;

    @Inject
    PropertyManager propertyManager;

    @Inject
    @Secure
    Client client;

    public SplitValidationProcedure() {
        super(null);
//        throw new IllegalStateException("Cannot use empty constructor, instantiate with TaskHandler");
    }

    @Inject
    public SplitValidationProcedure(TaskHandler taskHandler) {
        super(taskHandler);
    }

    @Override
    public void onMessage(Message msg) {
        Map<String, Object> messageBody;
        try {
            messageBody = msg.getBody(Map.class);
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, "JMS message could not be read", ex);
            return;
        }

        String taskId = (String) messageBody.get("taskId");
        String apiKey = (String) messageBody.get("api_key");
        String algorithmURI = (String) messageBody.get("algorithm_uri");
        String datasetURI = (String) messageBody.get("dataset_uri");
        String predictionFeature = (String) messageBody.get("prediction_feature");
        String algorithmParams = (String) messageBody.get("algorithm_params");
        String trans = (String) messageBody.get("transformations");
        String creator = (String) messageBody.get("creator");

        String stratify = (String) messageBody.get("stratify");
        Integer seed = (Integer) messageBody.get("seed");
        Double splitRatio = (Double) messageBody.get("split_ratio");
        try {
            init(taskId);
            checkCancelled();
            start(Task.Type.VALIDATION);

            progress(1f, "Split validation procedure initiated.");
            Algorithm algorithm = null;

            try {
                algorithm = client.target(algorithmURI)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + apiKey)
                        .get(Algorithm.class);
            } catch (NotFoundException e) {
                String[] algoUriS = algorithmURI.split("/");
                algorithm = algorithmHandler.find(algoUriS[algoUriS.length - 1]);
            }

            progress(5f, "Algorithm retrieved successfully.");
            checkCancelled();

            Dataset dataset = null;
            try{
                dataset = client.target(datasetURI)
                    .queryParam("stratify", stratify)
                    .queryParam("splitRatio", splitRatio)
                    .queryParam("seed", seed)
                    .queryParam("dataEntries", true)
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .get(Dataset.class);
            }catch(NotFoundException e){
                String[] datasetSlit = datasetURI.split("/");
                dataset = datasetLegacyWrapper.find(datasetSlit[datasetSlit.length - 1]);
                //dataset = datasetHandler.find(datasetSlit[datasetSlit.length - 1]);

            }
            progress(10f, "Dataset retrieved successfully.");
            checkCancelled();

            LinkedHashMap<String, String> transformations = new LinkedHashMap<>();
            List<Algorithm> transformationAlgorithms = new ArrayList<>();
            if (trans != null && !trans.isEmpty()) {
                progress("--", "Processing transformations...");

                transformations.putAll(serializer.parse(trans, LinkedHashMap.class));
                LinkedHashMap<String, String> newTransformations = new LinkedHashMap<>();
                transformations.keySet().stream().forEach((algUri) -> {
                    String algId = algUri.split("algorithm/")[1];
                    Algorithm transAlgorithm = algorithmHandler.find(algId);
                    if (transAlgorithm == null) {
                        errNotFound("Algorithm with id:" + algId + " was not found.");
                        return;
                    }
                    newTransformations.put(transAlgorithm.getId(), transformations.get(algUri));
                    if (transAlgorithm.getOntologicalClasses().contains("ot:Transformation")) {
                        transformationAlgorithms.add(transAlgorithm);
                    }
                });
                transformations.putAll(newTransformations);
                for (Algorithm transAlgorithm : transformationAlgorithms) {
                    checkCancelled();
                    progress("-", "Starting transforming on algorithm:" + transAlgorithm.getId());

                    Map<String, Object> parameterMap = null;
                    String transParameters = transformations.get(transAlgorithm.getId());
                    if (transParameters != null && !transParameters.isEmpty()) {
                        parameterMap = serializer.parse(transParameters, new HashMap<String, Object>().getClass());
                    }
                    dataset = jpdiClient.transform(dataset, transAlgorithm, parameterMap, predictionFeature, dataset.getMeta(), taskId, null).get();
                    addProgress(10f, "Done");
                }
                progress(30f, "Done processing transformations.", "--");
            }
            checkCancelled();

            Map<String, Object> parameterMap = null;
            if (algorithmParams != null && !algorithmParams.isEmpty()) {
                parameterMap = serializer.parse(algorithmParams, new HashMap<String, Object>().getClass());
            }

            Integer rows = dataset.getTotalRows();
            Long split = Math.round(rows * splitRatio);

            Dataset trainDataset = DatasetFactory.copy(dataset, 0, split.intValue());
            Dataset testDataset = DatasetFactory.copy(dataset, split.intValue(), rows - split.intValue());

            progress(50f, "Created train and test datasets.");
            checkCancelled();

            progress("Starting train and test with train_dataset:" + trainDataset.getDatasetURI() + " test_dataset:" + testDataset.getDatasetURI());

            String predictedFeature = "";
            Integer indepFeatureSize = 0;

            Model model = jpdiClient.train(trainDataset, algorithm, parameterMap, predictionFeature, trainDataset.getMeta(), taskId).get();
            Dataset predictedDataset = jpdiClient.predict(testDataset, model, testDataset.getMeta(), taskId, null).get();

            addProgress(20f, "Finished train and test with train_dataset:" + trainDataset.getDatasetURI() + " test_dataset:" + testDataset.getDatasetURI());
            checkCancelled();

            Dataset finalDataset = null;
            finalDataset = DatasetFactory.mergeRows(finalDataset, predictedDataset);
            predictedFeature = model.getPredictedFeatures().get(0);
            indepFeatureSize = Math.max(indepFeatureSize, model.getIndependentFeatures().size());

            checkCancelled();

            ValidationType validationType;
            if (algorithm.getOntologicalClasses().contains("ot:Regression")) {
                validationType = ValidationType.REGRESSION;
            } else if (algorithm.getOntologicalClasses().contains("ot:Classification")) {
                validationType = ValidationType.CLASSIFICATION;
            } else {
                throw new IllegalArgumentException("Selected Algorithm is neither Regression nor Classification.");
            }

            progress("Creating report...");

            TrainingRequest reportRequest = new TrainingRequest();

            reportRequest.setDataset(finalDataset);
            reportRequest.setPredictionFeature(predictionFeature);
            Map<String, Object> validationParameters = new HashMap<>();
            validationParameters.put("predictionFeature", predictionFeature);
            validationParameters.put("predictedFeature", predictedFeature);
            validationParameters.put("variables", indepFeatureSize);
            validationParameters.put("type", validationType);
            reportRequest.setParameters(validationParameters);

            Report report = client.target(propertyManager.getProperty(PropertyManager.PropertyType.JAQPOT_BASE_VALIDATION))
                    .request()
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.json(reportRequest), Report.class);

            progress(90f, "Done", "Saving report to database...");
            checkCancelled();

            ROG randomStringGenerator = new ROG(true);
            String reportId = randomStringGenerator.nextString(15);
            report.setId(reportId);
            report.setMeta(MetaInfoBuilder
                    .builder()
                    .addTitles("Split validation report")
                    .addCreators(creator)
                    .addSources(datasetURI, algorithmURI)
                    .addDescriptions(splitRatio + " Split validation on algorithm:" + algorithmURI + " with dataset:" + datasetURI)
                    .build());
            report.setVisible(Boolean.TRUE);
            reportHandler.create(report);
            complete("report/" + report.getId());

        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, "Validation procedure interupted", ex);
            errInternalServerError(ex, "Validation procedure interupted");
        } catch (ExecutionException ex) {
            LOG.log(Level.SEVERE, "Validation procedure execution error", ex.getCause());
            errInternalServerError(ex.getCause(), "JPDI Validation procedure error");
        } catch (CancellationException ex) {
            LOG.log(Level.INFO, "Task with id:{0} was cancelled", taskId);
            cancel();
        } catch (BadRequestException | IllegalArgumentException ex) {
            errBadRequest(ex, null);
        } catch (NotFoundException ex) {
            errNotFound(ex);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "JPDI Validation procedure unknown error", ex);
            errInternalServerError(ex, "JPDI Validation procedure unknown error");
        }
    }
}
