package sheetView.components.actionLine;

import com.google.gson.Gson;
import dto.impl.CellDTOImpl;
import dto.impl.SheetDTOImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import sheetView.main.SheetViewMainController;
import util.Constants;
import util.http.HttpClientUtil;

import java.io.IOException;



public class ActionLineController {

    @FXML
    private Label selectedCellId;

    @FXML
    private Label originalValueLabel;

    @FXML
    private TextField newValueField;

    @FXML
    private Label lastUpdateCellVersion;

    @FXML
    private Button updateButton;

    @FXML
    private ComboBox<String> versionSelector;

    private SheetViewMainController sheetViewMainController;

    private boolean isReadOnly = false;

    public void setMainController(SheetViewMainController sheetViewMainController) {
        this.sheetViewMainController = sheetViewMainController;
    }

    public void updateActionLine(CellDTOImpl selectedCell, String cellId) {
        if (selectedCell != null) {
            selectedCellId.setText(selectedCell.getIdentity());
            originalValueLabel.setText(selectedCell.getOriginalValue());
            newValueField.setText("");
            lastUpdateCellVersion.setText(String.valueOf(selectedCell.getVersion()) + " changed by: " + selectedCell.getChangedBy());
        } else {
            selectedCellId.setText(cellId);
            originalValueLabel.setText("");
            newValueField.setText("");
            lastUpdateCellVersion.setText("N/A");
        }
    }

    @FXML
    private void handleUpdateCell() {
        if(this.isReadOnly){
            showErrorAlert("You do not have permission to update cells.");
        }
        else {
        String newValue = newValueField.getText();
        String selectedCell = selectedCellId.getText();

        if (sheetViewMainController != null) {
            sheetViewMainController.handleUpdateCell(newValue, selectedCell);
        }
        }
    }

    @FXML
    private void handleVersionSelection() {
        String selectedVersion = versionSelector.getValue();

        if (sheetViewMainController != null && selectedVersion != null) {
            int selectedVersionNumber = Integer.parseInt(selectedVersion);
            int currentVersionNumber = Integer.parseInt(versionSelector.getItems().getLast());

            String finalUrl = HttpUrl
                    .parse(Constants.GET_SHEET_VERSION)
                    .newBuilder()
                    .addQueryParameter("sheetName", sheetViewMainController.getCurrentSheetName())
                    .addQueryParameter("version", selectedVersion)
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> showErrorAlert("Failed to get sheet version: " + e.getMessage()));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String sheetJson = response.body().string();
                        SheetDTOImpl selectedSheetDTO = new Gson().fromJson(sheetJson, SheetDTOImpl.class);

                        Platform.runLater(() -> {
                            if (selectedVersionNumber < currentVersionNumber) {
                                sheetViewMainController.showSheetVersionPopup(selectedSheetDTO);
                            } else if (selectedVersionNumber == currentVersionNumber) {
                                versionSelector.setStyle("-fx-background-color: #ffffff; -fx-border-color: #9bc0c4;");
                               setVersionSelectorItems(selectedSheetDTO.getVersion());
                                sheetViewMainController.updateCurrentSheet(selectedSheetDTO);
                            }
                        });
                    } else {
                        Platform.runLater(() -> showErrorAlert("Failed to get sheet version: " + response.message()));
                    }
                }
            });
        }
    }

    public void setVersionSelectorItems(int currentVersion) {
        versionSelector.getItems().clear();
        for (int i = 1; i <= currentVersion; i++) {
            versionSelector.getItems().add(String.valueOf(i));
        }
    }

    public void setReadOnly(boolean readOnly) {
        this.isReadOnly = readOnly;
        if (updateButton != null) {
            updateButton.setDisable(readOnly);
        }
        if (newValueField != null) {
            newValueField.setEditable(!readOnly);
        }
    }

    @FXML
    private void initialize() {
        // Update button and field based on read-only status
        setReadOnly(isReadOnly);

        // Set listener for version selection changes
        if (versionSelector != null) {
            versionSelector.setOnAction(event -> handleVersionSelection());
        }
    }

    private void showErrorAlert(String message) {
        if (sheetViewMainController != null) {
            sheetViewMainController.showErrorAlert("Error: " + message);
        }
    }
    // Highlight ComboBox and update versions
    public void updateVersionSelector(int latestVersion) {
        // Update the ComboBox with the latest versions
        setVersionSelectorItems(latestVersion);

        // Highlight the ComboBox to indicate a new version is available
        versionSelector.setStyle("-fx-border-color: #00ffcc; -fx-border-width: 2px;");
    }
}
