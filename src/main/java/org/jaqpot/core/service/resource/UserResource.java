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
package org.jaqpot.core.service.resource;

//import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.properties.PropertyManager;
import org.jaqpot.core.service.data.QuotaService;
import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.jaqpot.core.service.annotations.TokenSecured;
import org.jaqpot.core.service.authentication.RoleEnum;
import xyz.euclia.euclia.accounts.client.models.User;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Path("/user")
//@Api(value = "/user", description = "Users API", position = 1)
@Produces({"application/json", "text/uri-list"})
@Tag(name = "user")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class UserResource {

    @EJB
    UserHandler userHandler;

    @Inject
    PropertyManager propertyManager;

    @Context
    SecurityContext securityContext;

//    @GET
//    @TokenSecured({RoleEnum.ADMNISTRATOR})
//    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
//    @Operation(summary = "Lists all Users (admins only)",
//            description = "Lists all Users of Jaqpot Quattro. This operation can only be performed by the system administrators.",
//            responses = {
//                @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class))), description = "Users found and are listed in the response body"),
//                @ApiResponse(responseCode = "401", description = "You are not authorized to access this user"),
//                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)"),
//                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
//            })
//    public Response listUsers(
//            @Parameter(name = "Authorization", description = "Clients need to authenticate in order to access models", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
//            @Parameter(name = "start", description = "start", schema = @Schema(implementation = Integer.class, defaultValue = "0")) @QueryParam("start") Integer start,
//            @Parameter(name = "max", description = "max", schema = @Schema(implementation = Integer.class, defaultValue = "10")) @QueryParam("max") Integer max
//    ) throws JaqpotNotAuthorizedException {
//        // This resource can be accessed only by the system administrators
//        String admins = propertyManager.getProperty(PropertyManager.PropertyType.JAQPOT_ADMINISTRATORS);
//        List<String> adminsList = Arrays.asList(admins.split("\\s*,\\s*"));
//        String currentUserID = securityContext.getUserPrincipal().getName();
//        if (!adminsList.contains(currentUserID)) {
//            throw new JaqpotNotAuthorizedException("User " + currentUserID + " is not a system administrator, "
//                    + "therefore is not authorized to access this resource.", "AdministratorsOnly");
//        }
//
//        List<User> users = userHandler.listMeta(start, max);
//        return Response
//                .ok(users)
//                .build();
//    }
    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/{id}")
    @Operation(summary = "Finds User by Id",
            description = "Finds specified user",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class))), description = "User is found")
                ,
                @ApiResponse(responseCode = "401", description = "You are not authorized to access this user")
                ,
                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)")
                ,
                @ApiResponse(responseCode = "404", description = "This user was not found.")
                ,
                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
            })
    public Response getUser(
            @PathParam("id") String id,
            @Parameter(name = "Authorization", description = "Clients need to authenticate in order to access this resource", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key) throws JaqpotNotAuthorizedException {
        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
//        String currentUserID = securityContext.getUserPrincipal().getName();
        User user = userHandler.find(id, apiKey);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find User with id:" + id).build();
        }
        return Response.ok(user).build();
    }

//    @GET
//    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
//    @TokenSecured({RoleEnum.DEFAULT_USER})
//    @Path("/search/and/found")
//    @Operation(summary = "Finds User from partial given username",
//            description = "Finds all users queried",
//            responses = {
//                @ApiResponse(responseCode = "200", description = "Users found")
//                ,
//                @ApiResponse(responseCode = "401", description = "You are not authorized to access this user")
//                ,
//                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)")
//                ,
//                @ApiResponse(responseCode = "404", description = "No user was not found.")
//                ,
//                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
//            })
//    public Response getAllUser(
//            @Parameter(name = "name", schema = @Schema(implementation = String.class)) @QueryParam("name") String name,
//            @Parameter(name = "mail", schema = @Schema(implementation = String.class)) @QueryParam("mail") String mail,
//            @Parameter(name = "Authorization", description = "Clients need to authenticate in order to access this resource", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key) throws JaqpotNotAuthorizedException {
//
//        String currentUserID = securityContext.getUserPrincipal().getName();
//        Map<String, Object> search = new HashMap();
//        if (name != null) {
//            search.put("name", name.toLowerCase());
//        }
//        if (mail != null) {
//            search.put("mail", mail.toLowerCase());
//        }
//
//        List<User> users = userHandler.findAllWithPattern(search);
//
//        return Response.ok(users).build();
//    }

//    @PUT
//    @Produces({MediaType.APPLICATION_JSON})
//    @Consumes({MediaType.APPLICATION_JSON})
//    @TokenSecured({RoleEnum.DEFAULT_USER})
//    @Path("/{id}")
//    @Operation(summary = "Updates User by Id",
//            description = "Updates specified user",
//            responses = {
//                @ApiResponse(responseCode = "200", description = "Users is found")
//                ,
//                @ApiResponse(responseCode = "401", description = "You are not authorized to access this user")
//                ,
//                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)")
//                ,
//                @ApiResponse(responseCode = "404", description = "This user was not found.")
//                ,
//                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
//            })
//    public Response updateUser(
//            @PathParam("id") String id,
//            @Parameter(name = "Authorization", description = "Clients need to authenticate in order to access this resource", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
//            User userForUpadate) throws JaqpotNotAuthorizedException {
//
//        String[] apiA = api_key.split("\\s+");
//        String apiKey = apiA[1];
//        String currentUserID = securityContext.getUserPrincipal().getName();
//
//        User userById = userHandler.find(currentUserID, apiKey);
//
//        if (!userForUpadate.get_id().equals(userById.get_id())) {
//            throw new JaqpotNotAuthorizedException("Only the actual user can update its resources");
//        }
//        if (userById == null) {
//            return Response.status(Response.Status.NOT_FOUND).entity("Could not find User with id:" + id).build();
//        } else {
//            // Hide the hashed password!
//            userById.setHashedPass(null);
//        }
//        userHandler.edit(userForUpadate);
//        User userUpdated = userHandler.find(currentUserID);
//        return Response.ok(userUpdated).build();
//    }

//    @GET
//    @TokenSecured({RoleEnum.DEFAULT_USER})
//    @Produces({MediaType.APPLICATION_JSON})
//    @Path("/{id}/quota")
//    @Operation(summary = "Retrieves user's quota",
//            description = "Returns user's quota given the user's ID. Authenicated users can access only their own quota. ",
//            responses = {
//                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserQuota.class)), description = "User is found and quota are retrieved")
//                ,
//                @ApiResponse(responseCode = "401", description = "You are not authorized to access this user's quota")
//                ,
//                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)")
//                ,
//                @ApiResponse(responseCode = "404", description = "This user was not found.")
//                ,
//                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
//            })
//    public Response getUserQuota(
//            @PathParam("id") String id,
//            @Parameter(name = "Authorization", description = "Clients need to authenticate in order to access this resource", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key) throws JaqpotNotAuthorizedException {
//
//        String currentUserID = securityContext.getUserPrincipal().getName();
//        String admins = propertyManager.getProperty(PropertyManager.PropertyType.JAQPOT_ADMINISTRATORS);
//        List<String> adminsList = Arrays.asList(admins.split("\\s*,\\s*"));
//        if (!adminsList.contains(currentUserID) && !id.equals(currentUserID)) {
//            throw new JaqpotNotAuthorizedException("User " + currentUserID + "is not authorized access "
//                    + "this resource (/user/" + id + ")", "Unauthorized");
//        }
//
//        UserQuota userQuota = quotaService.getUserQuota(currentUserID);
//
//        return Response.ok(userQuota).build();
//    }

//    @GET
//    @Produces({MediaType.APPLICATION_JSON})
//    @TokenSecured({RoleEnum.DEFAULT_USER})
//    @Path("/{id}/picture")
//    @Operation(summary = "Finds Users profile pic by Id",
//            description = "Finds specified users profile pic",
//            responses = {
//                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)), description = "User is found")
//                ,
//                @ApiResponse(responseCode = "401", description = "You are not authorized to access this user")
//                ,
//                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)")
//                ,
//                @ApiResponse(responseCode = "404", description = "This user was not found.")
//                ,
//                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
//            })
//    public Response getUserPic(
//            @PathParam("id") String id,
//            @Parameter(name = "Authorization", description = "Clients need to authenticate in order to access this resource", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key) throws JaqpotNotAuthorizedException {
//
//        String currentUserID = securityContext.getUserPrincipal().getName();
//        User user = userHandler.getProfPic(id);
//        if (user == null) {
//            return Response.status(Response.Status.NOT_FOUND).entity("Could not find User with id:" + id).build();
//        } else {
//            // Hide the hashed password!
//            user.setHashedPass(null);
//        }
//        return Response.ok(user).build();
//    }
//
//    @GET
//    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
//    @TokenSecured({RoleEnum.DEFAULT_USER})
//    @Path("/{id}/occupation")
//    @Operation(summary = "Finds User occupation by Id",
//            description = "Finds specified users occupation",
//            responses = {
//                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)), description = "User is found")
//                ,
//                @ApiResponse(responseCode = "401", description = "You are not authorized to access this user")
//                ,
//                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)")
//                ,
//                @ApiResponse(responseCode = "404", description = "This user was not found.")
//                ,
//                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
//            })
//    public Response getUserOccupation(
//            @PathParam("id") String id,
//            @Parameter(name = "Authorization", description = "Clients need to authenticate in order to access this resource", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key) throws JaqpotNotAuthorizedException {
//
//        String currentUserID = securityContext.getUserPrincipal().getName();
//        User user = userHandler.getOccupation(id);
//        if (user == null) {
//            return Response.status(Response.Status.NOT_FOUND).entity("Could not find User with id:" + id).build();
//        } else {
//            // Hide the hashed password!
//            user.setHashedPass(null);
//        }
//        return Response.ok(user).build();
//    }
//
//    @GET
//    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
//    @TokenSecured({RoleEnum.DEFAULT_USER})
//    @Path("/{id}/occupationat")
//    /*@ApiOperation(value = "Finds User occupation place by Id",
//     notes = "Finds specified users occupation organization",
//     response = User.class)
//     @ApiResponses(value = {
//     @ApiResponse(code = 200, message = "User is found")
//     ,
//     @ApiResponse(code = 401, message = "You are not authorized to access this user")
//     ,
//     @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)")
//     ,
//     @ApiResponse(code = 404, message = "This user was not found.")
//     ,
//     @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
//     })*/
//
//    @Operation(summary = "Finds User occupation place by Id",
//            description = "Finds specified users occupation organization",
//            responses = {
//                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)), description = "User is found")
//                ,
//                @ApiResponse(responseCode = "401", description = "You are not authorized to access this user")
//                ,
//                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)")
//                ,
//                @ApiResponse(responseCode = "404", description = "This user was not found.")
//                ,
//                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
//            })
//    public Response getUserOccupationAt(
//            @PathParam("id") String id,
//            @Parameter(name = "Authorization", description = "Clients need to authenticate in order to access this resource", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key) throws JaqpotNotAuthorizedException {
//
//        String currentUserID = securityContext.getUserPrincipal().getName();
//        User user = userHandler.getOccupationAt(id);
//        if (user == null) {
//            return Response.status(Response.Status.NOT_FOUND).entity("Could not find User with id:" + id).build();
//        } else {
//            // Hide the hashed password!
//            user.setHashedPass(null);
//        }
//        return Response.ok(user).build();
//    }
//
//    @GET
//    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
//    @TokenSecured({RoleEnum.DEFAULT_USER})
//    @Path("/{id}/name")
//    @Operation(summary = "Finds User occupation place by Id",
//            description = "Finds specified users occupation organization",
//            responses = {
//                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)), description = "User is found")
//                ,
//                @ApiResponse(responseCode = "401", description = "You are not authorized to access this user")
//                ,
//                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)")
//                ,
//                @ApiResponse(responseCode = "404", description = "This user was not found.")
//                ,
//                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
//            })
//
//    public Response getUserName(
//            @PathParam("id") String id,
//            @Parameter(name = "Authorization", description = "Clients need to authenticate in order to access this resource", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key) throws JaqpotNotAuthorizedException {
//
//        String currentUserID = securityContext.getUserPrincipal().getName();
//        User user = userHandler.getName(id);
//        if (user == null) {
//            return Response.status(Response.Status.NOT_FOUND).entity("Could not find User with id:" + id).build();
//        } else {
//            // Hide the hashed password!
//            user.setHashedPass(null);
//        }
//        return Response.ok(user).build();
//    }
//
//    @GET
//    @Produces({MediaType.APPLICATION_JSON})
//    @TokenSecured({RoleEnum.DEFAULT_USER})
//    @Path("/{id}/organizations")
//    @Operation(summary = "Finds User's Organizations by user Id",
//            description = "Finds specified users organization",
//            responses = {
//                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)), description = "User is found")
//                ,
//                @ApiResponse(responseCode = "401", description = "You are not authorized to access this user")
//                ,
//                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)")
//                ,
//                @ApiResponse(responseCode = "404", description = "This user was not found.")
//                ,
//                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
//            })
//    public Response getUserOrganizations(
//            @PathParam("id") String id,
//            @Parameter(name = "Authorization", description = "Clients need to authenticate in order to access this resource", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key) throws JaqpotNotAuthorizedException {
//
//        String currentUserID = securityContext.getUserPrincipal().getName();
//        User user = userHandler.getOrganizations(id);
//        return Response.ok(user).build();
//    }

}
