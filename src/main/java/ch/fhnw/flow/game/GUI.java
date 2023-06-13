package ch.fhnw.flow.game;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class GUI extends Application {
    private Menu menu;

    private final int WINDOW_WIDTH = 1024;
    private final int WINDOW_HEIGHT = 600;
    private final String SCENE_HOME = "home.fxml";     // contains start button and highscore button
    private final String SCENE_LEVEL_SELECT = "level_select.fxml";
    private final String SCENE_INTENSITY_SELECT = "capacity_select.fxml";
    private final String SCENE_PLAY = "play.fxml";     // contains buttons for help, cancellation and finishing the game as well as errors of the player
    private final String SCENE_SCORE = "score.fxml";   // contains the scoreboard and a button to go back to the home screen
    private final String STYLESHEET = "style.css";
    private Stage stage;
    @FXML
    public Label issueMsg;
    public static Scene scene;
    public static Thread gameLoopThread;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        GameLogic gl = new GameLogic();
        Menu menu = new Menu(gl);
        stage.setUserData(menu);

        FXMLLoader loader = new FXMLLoader(GUI.class.getResource(SCENE_HOME));
        GUI.scene = new Scene(loader.load(), this.WINDOW_WIDTH, this.WINDOW_HEIGHT);
        scene.setCursor(Cursor.NONE);
        this.stage = stage;
        this.stage.setTitle("Flow GUI");
        this.stage.setScene(scene);
        this.stage.setFullScreen(true);
        this.stage.setFullScreenExitHint(null);
        this.stage.show();
    }

    public void setIssueMsg(String msg) {
        Platform.runLater( () -> {
            Label l = (Label) this.stage.getScene().lookup("#issueMsg");
            if (l != null) {
                l.setText(msg);
                l.getStyleClass().add("issue_border");
            }
            this.unsetIssueMsgAfter(3000);
        });
    }

    private void unsetIssueMsg() {
        Label l = (Label) this.stage.getScene().lookup("#issueMsg");
        if(l != null) {
            l.setText("");
            l.getStyleClass().removeIf(style -> style.equals("issue_border"));
        }
    }

    private void unsetIssueMsgAfter(int millisecs) {
        Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Thread.sleep(millisecs);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
                return null;
            }
        };
        sleeper.setOnSucceeded(event -> {
            unsetIssueMsg();
        });
        new Thread(sleeper).start();
    }


    private Pane getScene(String name) {
        try {
            return FXMLLoader.load(getClass().getResource( name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void switchScene(ActionEvent event, String name) {
        final Pane p = this.getScene(name);
        GUI.scene.setRoot(p);
    }

    private boolean enableSubmit() {
        Button b = (Button) stage.getScene().lookup("#submit_button");
        if (b == null) {
            return false;
        }
        b.getStyleClass().removeIf(style -> style.equals("button_deactivated"));
        return true;
    }

    public void setIntensityScene() {
        switchScene(new ActionEvent(), SCENE_INTENSITY_SELECT);
    }

    public void switchToHome(ActionEvent event) {
        this.setMenu(event);
        this.menu.onHome();
        switchScene(event, SCENE_HOME);
    }

    public void switchToLevelSelect(ActionEvent event) {
        setMenu(event);
        switchScene(event, SCENE_LEVEL_SELECT);
    }

    public void switchToPlay(ActionEvent event) {
        setMenu(event);
        switchScene(event, SCENE_PLAY);
        this.runGameEndLoop(event);
    }

    public void switchToScore(ActionEvent event) {
        setMenu(event);
        switchScene(event, SCENE_SCORE);
        ListView lv = (ListView) stage.getScene().lookup("#scoreboard");
        ObservableList<Menu.PlayerScore> scores = FXCollections.observableArrayList(this.menu.getPlayerScores());
        lv.setItems(scores);
    }

    public void startEasyLevel(ActionEvent event) {
        setMenu(event);
        this.menu.setEasyLevel();
        this.switchToPlay(event);
    }

    public void startMidLevel(ActionEvent event) {
        setMenu(event);
        this.menu.setMidLevel();
        this.switchToPlay(event);
    }

    public void startHardLevel(ActionEvent event) {
        setMenu(event);
        this.menu.setHardLevel();
        this.switchToPlay(event);
    }

    public void setIntensityCancel(ActionEvent event) {
        setMenu(event);
        String issueMsg = this.menu.onIntensityCancel();
        this.switchToPlay(event);
        if (!issueMsg.equals("")) {
            this.setIssueMsg(issueMsg);
        }
    }

    public void setIntensity0(ActionEvent event) {
        setMenu(event);
        String issueMsg = this.menu.onIntensity0();
        this.switchToPlay(event);
        if (!issueMsg.equals("")) {
            this.setIssueMsg(issueMsg);
        }
    }

    public void setIntensity1(ActionEvent event) {
        setMenu(event);
        String issueMsg = this.menu.onIntensity1();
        this.switchToPlay(event);
        if (!issueMsg.equals("")) {
            this.setIssueMsg(issueMsg);
        }
    }

    public void setIntensity2(ActionEvent event) {
        setMenu(event);
        String issueMsg = this.menu.onIntensity2();
        this.switchToPlay(event);
        if (!issueMsg.equals("")) {
            this.setIssueMsg(issueMsg);
        }
    }

    public void setIntensity3(ActionEvent event) {
        setMenu(event);
        String issueMsg = this.menu.onIntensity3();
        this.switchToPlay(event);
        if (!issueMsg.equals("")) {
            this.setIssueMsg(issueMsg);
        }
    }

    public void onSubmit(ActionEvent event) {
        setMenu(event);
        boolean isValidEnd = this.menu.tryEndLevel();
        if (isValidEnd) {
            this.menu.endGame();
            this.switchToScore(event);
        } else {
            setIssueMsg("Beende erst das Level");
        }
    }

    private void runGameEndLoop(ActionEvent event) {
        if(gameLoopThread != null && gameLoopThread.isAlive()) {
            return;
        }
        Menu.lastPressedInt = null;
        Task<Void> task = new Task<>() {
            public Void call() {
                AtomicBoolean endable = new AtomicBoolean(false);
                while (menu.isBeingPlayed()) {
                    gameLoop(endable);
                }
                return null;
            }
        };
        gameLoopThread = new Thread(task);
        gameLoopThread.start();
    }

    public void gameLoop(AtomicBoolean endable) {
        try {
            Thread.sleep(300);
            if (Menu.lastPressedInt != null && !Menu.isChoosingIntensity) {
                String issueMsg = menu.edgeSelectByIndex(Menu.lastPressedInt.getKey(), Menu.lastPressedInt.getValue());
                if (issueMsg.equals("")) {
                    setIntensityScene();
                    Menu.isChoosingIntensity = true;
                } else {
                    setIssueMsg(issueMsg);
                }
            } else if(!menu.getHwC().isListening()) {
                menu.getHwC().activateListeners();
            }
            if (menu.isEndable()) {
                endable.set(enableSubmit());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void setMenu(ActionEvent event) {
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        menu = (Menu) stage.getUserData();
    }

    public void cancelGame(ActionEvent actionEvent) {
        this.setMenu(actionEvent);
        this.menu.endGame();
        switchToHome(actionEvent);
    }
}
