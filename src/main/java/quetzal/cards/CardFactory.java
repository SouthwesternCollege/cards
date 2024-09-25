package quetzal.cards;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.IrremovableComponent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class CardFactory implements EntityFactory {
    @Spawns("Background")
    public Entity spawnBackground(SpawnData data) {
        return FXGL.entityBuilder(data)
                .view(new Rectangle(data.<Integer>get("width"), data.<Integer>get("height"), Color.GREEN))
                .with(new IrremovableComponent())
                .zIndex(-100)
                .build();
    }
    @Spawns("Card")
    public Entity spawnCard(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.CARD)
                .with(new CardComponent(data.<Integer>get("card-index")))
                .with(new CardAnimationComponent(data.get("hand"), data.get("selected")))
                .zIndex(data.get("zIndex"))
                .build();
    }
}
