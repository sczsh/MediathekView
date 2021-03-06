package mediathek.gui.actions.export;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import mediathek.config.Konstanten;
import mediathek.javafx.tool.FXProgressPane;
import mediathek.mainwindow.MediathekGui;
import org.controlsfx.control.StatusBar;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * Exports the current film list to JSON file.
 */
public class FilmListExportAction extends AbstractAction {
    private final static String HEADER = "Export der Filmliste";
    private final MediathekGui gui;

    public FilmListExportAction(MediathekGui gui) {
        super();
        this.gui = gui;

        putValue(NAME, "Filmliste...");
    }

    private void export(File selectedFile) {
        StatusBar bar = gui.getStatusBarController().getStatusBar();
        FXProgressPane hb = new FXProgressPane();

        FilmListExportWorkerTask task = new FilmListExportWorkerTask(selectedFile);
        task.setOnSucceeded(e -> {
            bar.getRightItems().remove(hb);
            showSuccess();
        });
        task.setOnFailed(e -> {
            bar.getRightItems().remove(hb);
            showError();
        });

        bar.getRightItems().add(hb);
        hb.prog.progressProperty().bind(task.progressProperty());

        CompletableFuture.runAsync(task);
    }

    private void showError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(Konstanten.PROGRAMMNAME);
        alert.setHeaderText(HEADER);
        alert.setContentText("Es gab einen Fehler beim Export der Filmliste.");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    private void showSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(Konstanten.PROGRAMMNAME);
        alert.setHeaderText(HEADER);
        alert.setContentText("Der Export wurde erfolgreich beendet.");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setEnabled(false);

        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Filmliste sichern");
            fileChooser.setInitialFileName("filme");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Unkomprimiert", "*.json"),
                    new FileChooser.ExtensionFilter("XZ Komprimiert (Standard)", "*.xz")
            );
            File selectedFile = fileChooser.showSaveDialog(null);
            if (selectedFile != null) {
                export(selectedFile);
            }
        });

        setEnabled(true);
    }

}
