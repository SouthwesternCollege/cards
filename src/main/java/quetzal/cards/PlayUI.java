package quetzal.cards;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javax.imageio.IIOException;
import java.io.IOException;

public class PlayUI extends GameApplication {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setFullScreenAllowed(true);  // Allow full-screen mode
        gameSettings.setFullScreenFromStart(true);
        gameSettings.setWidth(1920);
        gameSettings.setHeight(1080);
    }

    @Override
    protected void initGame() {


    }

    @Override
    protected void initUI() {
        FXMLLoader loader = new FXMLLoader(PlayUI.class.getResource("play-scene.fxml"));

        try {
            FXGL.getGameScene().addUINode(loader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

}
