package fr.epita.assistant.jws.presentation.rest;

import fr.epita.assistant.jws.converter.GameEntityToGameDetailResponseDTO;
import fr.epita.assistant.jws.domain.entity.GameEntity;
import fr.epita.assistant.jws.domain.service.GameService;
import fr.epita.assistant.jws.presentation.rest.request.CoordRequestDTO;
import fr.epita.assistant.jws.presentation.rest.request.RequestDTO;
import fr.epita.assistant.jws.presentation.rest.response.GameListResponseDTO;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ApplicationScoped
@Path("/games")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GameRessource {
    @Inject GameService game_service;
    @Inject GameEntityToGameDetailResponseDTO converter;

    @GET
    public Response getAllGames()
    {
        var games = game_service.getAllGames();
        return Response.ok(games.stream()
                .map(game -> new GameListResponseDTO(
                        game.id, game.players.size(), game.state))
                .collect(Collectors.toList())).build();
    }

    @POST
    public Response createNewGame(final RequestDTO req) throws IOException {
        if (req == null || req.name == null)
        {
            return Response.status(400).build();
        }
        var game = game_service.createNewGame(req.name);
        return Response.ok(converter.convert(game)).build();
    }

    @GET
    @Path("/{gameId}")
    public Response getGameInfo(@PathParam("gameId") Long id) throws IOException {
        if (id == null)
        {
            return Response.status(404).build();
        }
        var gameEntity = game_service.getGameById(id);
        if (gameEntity == null)
        {
            return Response.status(404).build();
        }
        return Response.ok(converter.convert(gameEntity)).build();
    }

    @POST
    @Path("/{gameId}")
    public Response joinGame(@PathParam("gameId") Long gameId, final RequestDTO req) throws IOException {
        if (req == null || req.name == null || gameId == null)
        {
            return Response.status(400).build();
        }
        if (game_service.existGame(gameId) == false)
        {
            return Response.status(404).build();
        }
        var gameEntity = game_service.addPlayer(req.name, gameId);
        if (gameEntity == null)
        {
            return Response.status(400).build();
        }
        return Response.ok(converter.convert(gameEntity)).build();
    }


    @PATCH
    @Path("/{gameId}/start")
    public Response startGame(@PathParam("gameId") Long gameId) throws IOException {
        var gameEntity = game_service.start(gameId);
        if (gameEntity == null)
        {
            return Response.status(404).build();
        }
        return Response.ok(converter.convert(gameEntity)).build();
    }


    @POST
    @Path("/{gameId}/players/{playerId}/move")
    public Response movePlayer(@PathParam("gameId") Long gameId, @PathParam("playerId") Long playerId, final CoordRequestDTO req) throws IOException, InterruptedException, ExecutionException {
        if (gameId == null || playerId == null || game_service.existPlayerGame(playerId, gameId) == false)
        {
            return Response.status(404).build();
        }
        if (game_service.canPlayerMove(playerId) == false)
        {
            return Response.status(429).build();
        }
        if (req == null)
        {
            return Response.status(400).build();
        }
        var gameEntity = game_service.movePlayer(gameId, playerId, req.posX, req.posY);
        if (gameEntity == null)
        {
            return Response.status(400).build();
        }

        return Response.ok(converter.convert(gameEntity)).build();
    }


    @POST
    @Path("/{gameId}/players/{playerId}/bomb")
    public Response putBomb(@PathParam("gameId") Long gameId, @PathParam("playerId") Long playerId, final CoordRequestDTO req) throws IOException, ExecutionException, InterruptedException {
        if (gameId == null || playerId == null || game_service.existPlayerGame(playerId, gameId) == false)
        {
            return Response.status(404).build();
        }
        if (game_service.canPlayerPutBomb(playerId) == false)
        {
            return Response.status(429).build();
        }
        if (req == null)
        {
            return Response.status(400).build();
        }
        var gameEntity = game_service.putBomb(gameId, playerId, req.posX, req.posY);
        if (gameEntity == null)
        {
            return Response.status(400).build();
        }

        Thread runner = new Thread(
                () -> {
                    try {
                        game_service.bombExplosion(req.posX, req.posY, gameId, playerId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );
        runner.start();

        return Response.ok(converter.convert(gameEntity)).build();
    }
}
