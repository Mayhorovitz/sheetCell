
import cell.api.Cell;
import cell.api.EffectiveValue;
import coordinate.Coordinate;
import coordinate.CoordinateImpl;
import engine.api.Engine;
import engine.exceptions.*;
import engine.impl.EngineImpl;
import sheet.api.Sheet;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static coordinate.CoordinateFactory.createCoordinate;

public class ConsoleUI {
    private Engine engine;
    private Scanner scanner;

    public ConsoleUI() {
        this.engine = new EngineImpl();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        while (true) {
            displayMenu();
            String command = scanner.nextLine();
            processCommand(command);
        }
    }

    public void displayMenu() {
        System.out.println("1. Load File");
        System.out.println("2. Display Spreadsheet");
        System.out.println("3. Display Cell");
        System.out.println("4. Update Cell");
        System.out.println("5. Display Versions");
        System.out.println("6. Exit");
    }

    public void processCommand(String command) {
        try {
        switch (command) {
            case "1":
                handleLoadFile();
                break;
            case "2":
                displaySpreadSheet();
                break;
            case "3":
                handleDisplayCell();
                break;
            case "4":
                handleUpdateCell();
                break;
            case "5":
                handleDisplayVersions();
                break;
            case "6":
                engine.exit();
                System.exit(0);
                break;
            default:
                System.out.println("Invalid command");
        }
    } catch (NoFileLoadedException e) {
        System.out.println("Error: " + e.getMessage());
            handleLoadFile();
        } catch (Exception e) {
        System.out.println("An unexpected error occurred: " + e.getMessage());
    }
    }

    public void handleLoadFile() {
        System.out.println("Enter file path:");
        String filePath = scanner.nextLine();
        try {
            engine.loadFile(filePath);
            System.out.println("File loaded successfully.");
        } catch (InvalidFileFormatException | InvalidSheetLayoutException e) {
            System.out.println("Error loading file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    public void displaySpreadSheet() {
        printSpreadSheet(engine.getCurrentSpreadSheet());
    }

    public void handleDisplayCell() {
        System.out.println("Enter cell identifier (e.g., A1):");
        String cellId = scanner.nextLine();
        try {
            Cell currentCell = engine.getCellInfo(cellId);
            System.out.println("Cell ID: " + cellId);
            System.out.println("Original Value: " + currentCell.getOriginalValue());
            System.out.println("Effective Value: " + currentCell.getEffectiveValue().toString());
            System.out.println("The last modified version of the cell is: " + currentCell.getVersion());

            System.out.print("Dependents on: ");
            List<Cell> dependents = currentCell.getDependsOn();
            String dependentsOutput = (dependents != null && !dependents.isEmpty())
                    ? dependents.stream()
                    .map(cell -> cell.getCoordinate().toString())
                    .collect(Collectors.joining(", "))
                    : "None";
            System.out.println(dependentsOutput);

            System.out.print("Influencing on: ");
            List<Cell> references = currentCell.getInfluencingOn();
            String referencesOutput = (references != null && !references.isEmpty())
                    ? references.stream()
                    .map(cell -> cell.getCoordinate().toString())
                    .collect(Collectors.joining(", "))
                    : "None";
            System.out.println(referencesOutput);

        } catch (Exception e) {
            System.out.println("Error retrieving cell: " + e.getMessage());
        }
    }

    private void printSpreadSheet(Sheet sheet) {
        System.out.println("Spreadsheet version is: " + sheet.getVersion());
        System.out.println("Spreadsheet name is: " + sheet.getName());
        int numRows = sheet.getRows();
        int numCols = sheet.getCols();
        int widthCol = sheet.getColWidth();
        int heightRow = sheet.getRowHeight();

        // Print column headers with centered letters
        System.out.print("  "); // Initial space for row numbers
        System.out.print("|");
        for (int col = 0; col < numCols; col++) {
            String header = String.valueOf((char) ('A' + col));
            int padding = (widthCol - header.length()) / 2; // Calculate padding for centering
            System.out.print(" ".repeat(Math.max(0, padding))); // Add left padding
            System.out.print(header); // Print the column letter
            System.out.print(" ".repeat(Math.max(0, widthCol - padding - header.length()))); // Add right padding
            if (col < numCols - 1) {
                System.out.print("|");
            }
        }
        System.out.println("|"); // Add separator after last column

        // Print each row
        for (int row = 1; row <= numRows; row++) {
            System.out.printf("%02d", row); // Row number
            System.out.print("|");

            for (int col = 0; col < numCols; col++) {
                Coordinate cellID = new CoordinateImpl(row, col + 1);
                Cell currentCell = sheet.getActiveCells().get(cellID);
                if (currentCell == null) {
                    System.out.printf("%-" + widthCol + "s", ' ');
                } else {
                    EffectiveValue cellContent = currentCell.getEffectiveValue(); // Retrieve cell content
                    System.out.printf("%-" + widthCol + "s", cellContent.getValue());
                }

                if (col < numCols - 1) {
                    System.out.print("|");
                }
            }
            System.out.println("|"); // Add separator after last column

            // Print additional empty lines for row height
            for (int h = 1; h < heightRow; h++) {
                System.out.print("  "); // Initial space for row numbers
                System.out.print("|");
                for (int col = 0; col < numCols; col++) {
                    System.out.print(" ".repeat(widthCol));
                    if (col < numCols - 1) {
                        System.out.print("|");
                    }
                }
                System.out.println("|"); // Add separator after last column
            }
        }
    }

    private void handleUpdateCell() {
        System.out.println("Enter cell identifier (e.g., A1):");
        String cellId = scanner.nextLine();
        Cell currentCell = engine.getCellInfo(cellId);
        if (currentCell != null) {
            System.out.println("Cell ID: " + cellId);
            System.out.println("Original Value: " + currentCell.getOriginalValue());
            System.out.println("Effective Value: " + currentCell.getEffectiveValue().toString());
        }
        System.out.print("Enter new value: ");
        String newValue = scanner.nextLine().trim();

        try {
            engine.updateCell(cellId, newValue);
            System.out.println("Cell updated successfully.");
        } catch (Exception e) {
            System.out.println("Error updating cell: " + e.getMessage());
        }
    }

    private void handleDisplayVersions() {
        try {
            System.out.println("Version | Changed Cells Count");
            System.out.println("----------------------------");
            for (int i = 1; i <= engine.getCurrentSheetVersion(); i++) {
                System.out.printf("%7d | %17d%n", i, engine.getSheetByVersion(i).getCellsThatHaveChanged().size());
            }
            while (true) {
                System.out.println("Enter the version number to view, or 'q' to quit:");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("q")) {
                    break;
                }

                try {
                    int versionNumber = Integer.parseInt(input);
                    if (versionNumber > 0 && versionNumber <= engine.getCurrentSheetVersion()) {
                        printSpreadSheet(engine.getSheetByVersion(versionNumber));
                    } else {
                        System.out.println("Invalid version number. Please try again.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a valid version number or 'q' to quit.");
                }
            }
        } catch (InvalidVersionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
