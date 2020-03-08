package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.renderengine.entity.Entity;
import com.robotzero.gamefx.renderengine.entity.EntityService;
import com.robotzero.gamefx.renderengine.entity.EntityType;
import com.robotzero.gamefx.world.GameMemory;
import com.robotzero.gamefx.world.World;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PlayerService {
    private final World world;
    private final GameMemory gameMemory;
    public static final float PlayerHeight = 1.4f;
    public static final float PlayerWidth = 1.0f;

    public PlayerService(World world, GameMemory gameMemory) {
        this.world = world;
        this.gameMemory = gameMemory;
    }

//    public Map<EntityType, List<Map.Entry<EntityType, Matrix4f>>> getModelMatrix() {
//        return IntStream.range(1, gameMemory.HighEntityCount).mapToObj(HighEntityIndex -> {
//            Entity.HighEntity highEntity = gameMemory.HighEntities[HighEntityIndex];
//            Entity.LowEntity lowEntity = gameMemory.LowEntities[highEntity.LowEntityIndex];
//
//            //@TODO
//            Entity Entity = new Entity();
//            Entity.LowIndex = highEntity.LowEntityIndex;
//            Entity.Low = lowEntity;
//            Entity.High = highEntity;
//            highEntity.P = new Vector2f(highEntity.P);
//            final Matrix4f v = new Matrix4f();
//            float EntityGroundPointX = World.ScreenCenterX + World.MetersToPixels * highEntity.P.x();
//            float EntityGroundPointY = World.ScreenCenterY - World.MetersToPixels * highEntity.P.y();
//            float PlayerLeft = EntityGroundPointX - 0.5f * World.MetersToPixels * lowEntity.Width;
//            float PlayerTop = EntityGroundPointY - 0.5f * World.MetersToPixels * lowEntity.Height;
//
//            return Map.of(lowEntity.Type, v.identity().translate(new Vector3f(PlayerLeft, PlayerTop, 0f)));
//        }).flatMap(matrixes -> matrixes.entrySet().stream()).collect(Collectors.groupingBy(a -> {
//            return a.getKey();
//        }));
//    }
}
