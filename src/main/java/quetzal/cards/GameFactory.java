package quetzal.cards;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.IrremovableComponent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class GameFactory implements EntityFactory {

    @Spawns("Background")
    public Entity spawnBackground(SpawnData data) {
        return FXGL.entityBuilder(data)
                .view(new Rectangle(data.<Integer>get("width"), data.<Integer>get("height"), Color.GREEN))
                .with(new IrremovableComponent())
                .zIndex(-100)
                .build();
    }

    @Spawns("HandRank")
    public Entity spawnHandRank(SpawnData data) {
        return FXGL.entityBuilder(data)
                .view(new Text("wassup"))
                .zIndex(0)
                .build();
    }

    @Spawns("Card")
    public Entity spawnCard(SpawnData data) {
        Card card = data.get("card");
        Entity cardEntity = FXGL.entityBuilder(data)
                .type(EntityType.CARD)
                .with(new CardComponent(card))  // Pass the Card object
                .with(new CardAnimationComponent(data.get("hand")))
                .zIndex(data.get("z-index"))
                .build();
        card.setEntity(cardEntity);

        return cardEntity;
    }

}
