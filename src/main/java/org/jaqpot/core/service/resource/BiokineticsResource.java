package org.jaqpot.core.service.resource;

//import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.validator.routines.UrlValidator;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.AlgorithmHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.facades.UserFacade;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.authentication.AAService;
import org.jaqpot.core.service.data.TrainingService;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;
import org.jaqpot.core.service.exceptions.QuotaExceededException;
import org.jaqpot.core.service.exceptions.parameter.*;
import org.jaqpot.core.service.validator.ParameterValidator;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.service.annotations.TokenSecured;
import org.jaqpot.core.service.authentication.RoleEnum;
import org.jaqpot.core.service.data.PredictionService;
import xyz.euclia.euclia.accounts.client.models.User;

/**
 * Created by Angelos Valsamis on 23/10/2017.
 */
@Path("/biokinetics")
//@Api(value = "/biokinetics", description = "Biokinetics API")
@Produces({"application/json", "text/uri-list"})
@Authorize
@Tag(name = "biokinetics")
@SecurityScheme(name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER,
        scheme = "bearer",
        description = "add the token retreived from oidc. Example:  Bearer <API_KEY>"
)
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class BiokineticsResource {

    @EJB
    AAService aaService;

    @Context
    SecurityContext securityContext;

    @EJB
    AlgorithmHandler algorithmHandler;

    @EJB
    UserHandler userHandler;

    @EJB
    ModelHandler modelHandler;

    @Inject
    ParameterValidator parameterValidator;

    @EJB
    DatasetHandler datasetHandler;

    @EJB
    PredictionService predictionService;

    @EJB
    TrainingService trainingService;

    @Context
    UriInfo uriInfo;

    @Inject
    @Jackson
    JSONSerializer serializer;

    private static final Logger LOG = Logger.getLogger(BiokineticsResource.class.getName());

    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/pksim/createmodel")

    @Operation(summary = "Creates Biokinetics model with PkSim",
            description = "Creates a biokinetics model given a pksim .xml file and demographic data",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Task.class)))
            })
    @org.jaqpot.core.service.annotations.Task
    public Response trainBiokineticsModel(
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(type = "string")) @HeaderParam("Authorization") String api_key,
            @Parameter(name = "file", description = "xml[m,x] file", required = true, schema = @Schema(type = "string", format = "binary")) @FormParam("file") String file,
            @Parameter(name = "dataset-uri", description = "Dataset uri to be trained upon", required = true, schema = @Schema(type = "string")) @FormParam("dataset-uri") String datasetUri,
            @Parameter(name = "title", description = "Title of model", required = true, schema = @Schema(type = "string")) @FormParam("title") String title,
            @Parameter(name = "description", description = "Description of model", required = true, schema = @Schema(type = "string")) @FormParam("description") String description,
            @Parameter(name = "algorithm-uri", description = "Algorithm URI", required = true, schema = @Schema(type = "string")) @FormParam("algorithm-uri") String algorithmURI,
            @Parameter(name = "parameters", description = "Parameters for algorithm", required = false, schema = @Schema(type = "string")) @FormParam("parameters") String parameters,
            @Parameter(description = "multipartFormData input", hidden = true) MultipartFormDataInput input)
            throws ParameterIsNullException, ParameterInvalidURIException, QuotaExceededException, IOException, ParameterScopeException, ParameterRangeException, ParameterTypeException, JaqpotDocumentSizeExceededException {

        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];

        UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);

        byte[] bytes;
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("file");
        title = uploadForm.get("title").get(0).getBody(String.class, null);
        //String title = uploadForm.get("title").get(0).getBody(String.class, null);
        description = uploadForm.get("description").get(0).getBody(String.class, null);
        //String description = uploadForm.get("description").get(0).getBody(String.class, null);
        algorithmURI = uploadForm.get("algorithm-uri").get(0).getBody(String.class, null);
        //String algorithmURI = uploadForm.get("algorithm-uri").get(0).getBody(String.class, null);
        datasetUri = uploadForm.get("dataset-uri").get(0).getBody(String.class, null);
        //String datasetUri = uploadForm.get("dataset-uri").get(0).getBody(String.class, null);

        User user = userHandler.find(securityContext.getUserPrincipal().getName(), apiKey);
        long modelCount = modelHandler.countAllOfCreator(user.get_id());
        parameters = null;
        //String parameters = null;
        if (uploadForm.get("parameters") != null) {
            parameters = uploadForm.get("parameters").get(0).getBody(String.class, null);
        }

        if (algorithmURI == null) {
            throw new ParameterIsNullException("algorithmURI");
        }

        if (!urlValidator.isValid(algorithmURI)) {
            throw new ParameterInvalidURIException("Not valid Algorithm URI.");
        }
        String algorithmId = algorithmURI.split("algorithm/")[1];

        Algorithm algorithm = algorithmHandler.find(algorithmId);
        if (algorithm == null) {
            throw new NotFoundException("Could not find Algorithm with id:" + algorithmId);
        }

        parameterValidator.validate(parameters, algorithm.getParameters());

        String encodedString = "";
        for (InputPart inputPart : inputParts) {
            try {
                //Convert the uploaded file to inputstream
                InputStream inputStream = inputPart.getBody(InputStream.class, null);
                bytes = getBytesFromInputStream(inputStream);

                //Base64 encode
                byte[] encoded = java.util.Base64.getEncoder().encode(bytes);
                encodedString = new String(encoded);
                System.out.println(encodedString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Append XML file (Base64 string) in parameters
        parameters = parameters.substring(0, parameters.length() - 1);
        parameters += ",\"xml_file\":[\"" + encodedString + "\"]}";

        Map<String, Object> options = new HashMap<>();
        options.put("title", title);
        options.put("description", description);
        options.put("api_key", apiKey);
        options.put("parameters", parameters);
        options.put("base_uri", uriInfo.getBaseUri().toString());
        options.put("dataset_uri", datasetUri);
        options.put("algorithmId", algorithmId);
        options.put("creator", securityContext.getUserPrincipal().getName());
        Task task = trainingService.initiateTraining(options, securityContext.getUserPrincipal().getName());
        return Response.ok(task).build();
    }

    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Path("httk/createmodel")
    @Operation(summary = "Creates an httk biocinetics Model",
            description = "Creates an httk biocinetics Model",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Task.class)),
                        description = "The process has successfully been started. A task URI is returned."),
                @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Bad request. More info can be found in details of Error Report."),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Wrong, missing or insufficient credentials. Error report is produced."),
                @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Algorithm was not found."),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    @org.jaqpot.core.service.annotations.Task
    public Response trainHttk(
            @Parameter(name = "title", required = true, schema = @Schema(implementation = String.class)) @FormParam("title") String title,
            @Parameter(name = "description", required = true, schema = @Schema(implementation = String.class)) @FormParam("description") String description,
            @Parameter(name = "parameters", schema = @Schema(implementation = String.class)) @FormParam("parameters") String parameters,
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key) throws QuotaExceededException, ParameterIsNullException, ParameterInvalidURIException, ParameterTypeException, ParameterRangeException, ParameterScopeException, JaqpotDocumentSizeExceededException {

        UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);

        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];

        String algorithmId = "httk";
        Algorithm algorithm = algorithmHandler.find(algorithmId);
        if (algorithm == null) {
            throw new NotFoundException("Could not find Algorithm with id:" + algorithmId);
        }

        if (title == null) {
            throw new ParameterIsNullException("title");
        }
        if (description == null) {
            throw new ParameterIsNullException("description");
        }

        User user = userHandler.find(securityContext.getUserPrincipal().getName(), apiKey);

        Map<String, Object> options = new HashMap<>();
        options.put("title", title);
        options.put("description", description);
        options.put("dataset_uri", null);
        options.put("prediction_feature", null);
        options.put("api_key", apiKey);
        options.put("algorithmId", algorithmId);
        options.put("parameters", parameters);
        options.put("base_uri", uriInfo.getBaseUri().toString());
        options.put("creator", securityContext.getUserPrincipal().getName());

        Map<String, String> transformationAlgorithms = new LinkedHashMap<>();

        if (!transformationAlgorithms.isEmpty()) {
            String transformationAlgorithmsString = serializer.write(transformationAlgorithms);
            LOG.log(Level.INFO, "Transformations:{0}", transformationAlgorithmsString);
            options.put("transformations", transformationAlgorithmsString);
        }

        parameterValidator.validate(parameters, algorithm.getParameters());

        Task task = trainingService.initiateTraining(options, securityContext.getUserPrincipal().getName());

        return Response.ok(task).build();
    }

    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("httk/model/{id}")
    @Operation(summary = "Creates prediction with httk model",
            description = "Creates prediction with Httk model",
            responses = {
                @ApiResponse(content = @Content(schema = @Schema(implementation = Task.class))),},
            extensions = {
                @Extension(properties = {
                    @ExtensionProperty(name = "orn-@type", value = "x-orn:JaqpotPredictionTaskId"),}
                ),
                @Extension(name = "orn:expects", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AcessToken"),
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:JaqpotModelId")
                }),
                @Extension(name = "orn:returns", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:JaqpotHttkPredictionTaskId")
                })
            })

    @org.jaqpot.core.service.annotations.Task
    public Response makeHttkPrediction(
            @FormParam("visible") Boolean visible,
            @PathParam("id") String id,
            @Parameter(description = "Authorization token") @HeaderParam("Authorization") String api_key) throws GeneralSecurityException, QuotaExceededException, ParameterIsNullException, ParameterInvalidURIException, JaqpotDocumentSizeExceededException {

        if (id == null) {
            throw new ParameterIsNullException("id");
        }

        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];

        UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);

        User user = userHandler.find(securityContext.getUserPrincipal().getName(), apiKey);
//        long datasetCount = datasetHandler.countAllOfCreator(user.getId());
//        int maxAllowedDatasets = new UserFacade(user).getMaxDatasets();
//
//        if (datasetCount > maxAllowedDatasets) {
//            LOG.info(String.format("User %s has %d datasets while maximum is %d",
//                    user.getId(), datasetCount, maxAllowedDatasets));
//            throw new QuotaExceededException("Dear " + user.getId()
//                    + ", your quota has been exceeded; you already have " + datasetCount + " datasets. "
//                    + "No more than " + maxAllowedDatasets + " are allowed with your subscription.");
//        }

        Model model = modelHandler.find(id);
        if (model == null) {
            throw new NotFoundException("Model not found.");
        }
        if (!model.getAlgorithm().getId().equals("httk")) {
            throw new NotFoundException("Model is not created from httk");
        }

        List<String> requiredFeatures = retrieveRequiredFeatures(model);

        Map<String, Object> options = new HashMap<>();
        options.put("dataset_uri", null);
        options.put("api_key", apiKey);
        options.put("modelId", id);
        options.put("creator", securityContext.getUserPrincipal().getName());
        options.put("base_uri", uriInfo.getBaseUri().toString());
        Task task = predictionService.initiatePrediction(options);
        return Response.ok(task).build();
    }

    private List<String> retrieveRequiredFeatures(Model model) {
        if (model.getTransformationModels() != null && !model.getTransformationModels().isEmpty()) {
            String transModelId = model.getTransformationModels().get(0).split("model/")[1];
            Model transformationModel = modelHandler.findModelIndependentFeatures(transModelId);
            if (transformationModel != null && transformationModel.getIndependentFeatures() != null) {
                return transformationModel.getIndependentFeatures();
            }
        }
        return model.getIndependentFeatures();
    }

    private static byte[] getBytesFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        for (int len; (len = is.read(buffer)) != -1;) {
            os.write(buffer, 0, len);
        }
        os.flush();
        return os.toByteArray();
    }
}
