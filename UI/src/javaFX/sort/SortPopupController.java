package javaFX.sort;

import dto.api.SheetDTO;
import engine.api.Engine;
import javaFX.main.UIModel;
import javaFX.readOnlyPopup.ReadOnlyPopupController;
import javaFX.sheet.SheetController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller for the sort popup, allowing users to sort data within a range.
 */
public class SortPopupController {

    @FXML
    private TextField rangeTextField;  // TextField for range input
    @FXML
    private TextField columnsTextField;  // TextField for columns input
    @FXML
    private Button sortButton;

    private SheetController sheetController;
    private Engine engine;
    private UIModel uiModel;

    public void setSheetController(SheetController sheetController) {
        this.sheetController = sheetController;
        this.engine = sheetController.getEngine();
        this.uiModel = sheetController.getUiModel();
    }

    @FXML
    private void handleSort() {
        String range = rangeTextField.getText().toUpperCase();
        String columns = columnsTextField.getText().toUpperCase();

        if (range != null && !range.isEmpty() && columns != null && !columns.isEmpty()) {
            String[] columnArray = columns.split(",");

            try {
                SheetDTO sortedSheetDTO = engine.sortSheetRangeByColumns(range, columnArray);

                // Open the ReadOnlyPopup to display the sorted sheet
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/javaFX/readOnlyPopup/readOnlyPopup.fxml"));
                VBox root = loader.load();

                ReadOnlyPopupController popupController = loader.getController();
                popupController.setEngine(engine);
                popupController.setUiModel(uiModel);
                popupController.setSheetToDisplay(sortedSheetDTO);
                popupController.displaySheet();

                Stage stage = new Stage();
                stage.setTitle("Sorted Sheet - View Only");
                stage.setScene(new Scene(root, 800, 600));  // Set desired size
                stage.show();

                // Close the sort popup
                Stage currentStage = (Stage) sortButton.getScene().getWindow();
                currentStage.close();

            } catch (Exception e) {
                showError("Error sorting sheet: " + e.getMessage());
            }
        } else {
            // Handle invalid input
            showError("Please enter valid range and columns.");
        }
    }

    // Utility method to show error alerts
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Sort Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
