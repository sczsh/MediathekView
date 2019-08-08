package mediathek.gui.actions.import_actions;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import mediathek.config.Konstanten;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ImportOldAbosAction extends AbstractAction {
    public ImportOldAbosAction() {
        putValue(Action.NAME, "Alte Abos...");
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        //putValue(Action.SMALL_ICON, IconFontSwing.buildIcon(FontAwesome.COGS, 16));
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(Konstanten.PROGRAMMNAME);
            alert.setHeaderText("Abos importieren");
            alert.setContentText("Diese Funktion ist noch nicht implementiert");
            alert.showAndWait();
        });
    }
}
