package com.example.demofx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

public class Main extends Application {
    private WhiteboardClient client;
    private static GraphicsContext gc;
    private static Color currentColor = Color.BLACK;
    private boolean isEraserMode = false;
    private boolean isHighlightMode = false;
    long lastDrawTime = 0;
    long drawDelay = 10;

    @Override
    public void start(Stage primaryStage) {
        // Create the initial login UI for the PIN
        VBox loginLayout = new VBox(10);
        Label pinLabel = new Label("Enter PIN:");
        TextField pinField = new TextField();
        Button joinButton = new Button("Join");

        loginLayout.getChildren().addAll(pinLabel, pinField, joinButton);
        Scene loginScene = new Scene(loginLayout, 300, 150);
        primaryStage.setTitle("Collaborative Whiteboard - Login");
        primaryStage.setScene(loginScene);
        primaryStage.show();

        joinButton.setOnAction(e -> {
            String pin = pinField.getText();
            try {
                client = new WhiteboardClient("127.0.0.1", pin); // Pass the PIN to the client
                setupWhiteboard(primaryStage); // Set up the whiteboard if joining is successful
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void setupWhiteboard(Stage primaryStage) {
        // Set up drawing canvas
        Canvas canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setStroke(currentColor);

        ColorPicker colorPicker = new ColorPicker(currentColor);


        // Add event listener for color changes
        colorPicker.setOnAction(e -> {
            currentColor = colorPicker.getValue();
            gc.setStroke(currentColor);
        });

        // Add event listeners for drawing
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            double lineWidth = isEraserMode ? 10.0 : 1.0;  // Set line width based on mode
            String mode = isHighlightMode ? "highlight" : (isEraserMode ? "erase" : "normal");

            if (isHighlightMode) {
                drawHighlight(e.getX(), e.getY());
            } else if (isEraserMode){
                gc.beginPath();
                gc.moveTo(e.getX(), e.getY());
                gc.stroke();
                gc.setLineWidth(lineWidth);
                sendDrawingData(e.getX(), e.getY(), "pressed", Color.WHITE, lineWidth, mode);
            } else {
                gc.beginPath();
                gc.moveTo(e.getX(), e.getY());
                gc.stroke();
                gc.setLineWidth(lineWidth);
                sendDrawingData(e.getX(), e.getY(), "pressed", currentColor, lineWidth, mode);
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            double lineWidth = isEraserMode ? 10.0 : 1.0;  // Set line width based on mode
            String mode = isHighlightMode ? "highlight" : (isEraserMode ? "erase" : "normal");

            if (isHighlightMode) {
                drawHighlight(e.getX(), e.getY());
            } else if (isEraserMode) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastDrawTime >= drawDelay) {
                    gc.lineTo(e.getX(), e.getY());
                    gc.stroke();
                    gc.setLineWidth(lineWidth);
                    sendDrawingData(e.getX(), e.getY(), "dragged", Color.WHITE, lineWidth, mode);
                    lastDrawTime = currentTime;
                }
            } else {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastDrawTime >= drawDelay) {
                    gc.lineTo(e.getX(), e.getY());
                    gc.stroke();
                    gc.setLineWidth(lineWidth);
                    sendDrawingData(e.getX(), e.getY(), "dragged", currentColor, lineWidth, mode);
                    lastDrawTime = currentTime;
                }
            }
        });


        ToggleButton eraserButton = new ToggleButton("Eraser");
        eraserButton.setOnAction(e -> {
            isEraserMode = eraserButton.isSelected();
            if (isEraserMode) {
                gc.setStroke(Color.WHITE);
            } else {
                gc.setStroke(currentColor);
            }
        });

        ToggleButton highlightButton = new ToggleButton("Highlight");
        highlightButton.setOnAction(e -> {
            isHighlightMode = highlightButton.isSelected();
        });

        // Pane to hold the canvas and color picker
        Pane root = new Pane();
        root.getChildren().addAll(canvas, colorPicker);
        root.getChildren().add(eraserButton);
        eraserButton.setLayoutX(700);
        eraserButton.setLayoutY(20);

        highlightButton.setLayoutX(600);
        highlightButton.setLayoutY(20);
        root.getChildren().add(highlightButton);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Collaborative Whiteboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void drawHighlight(double x, double y) {
        Color highlightColor = Color.YELLOW; // Change this to your desired highlight color
        gc.setFill(highlightColor.deriveColor(0, 1, 1, 0.05)); // Semi-transparent color
        gc.fillRect(x - 10, y - 10, 20, 20); // Draw a rectangle as the highlight

        // Prepare the data string in the required format
        int red = (int) (highlightColor.getRed() * 255);
        int green = (int) (highlightColor.getGreen() * 255);
        int blue = (int) (highlightColor.getBlue() * 255);
        int lineWidth = 0; // Set to your desired line width or modify as needed
        String mode = "highlight"; // Mode for the highlight

        // Create the data string
        String data = mode + "," + x + "," + y + "," + red + "," + green + "," + blue + "," + lineWidth + "," + mode;

        // Send drawing data to the server
        client.sendDrawingData(data);
    }

    private void sendDrawingData(double x, double y, String action, Color color, double lineWidth, String mode) {
        int red = (int) (color.getRed() * 255);
        int green = (int) (color.getGreen() * 255);
        int blue = (int) (color.getBlue() * 255);
        String data = action + "," + x + "," + y + "," + red + "," + green + "," + blue + "," + lineWidth + "," + mode;
        client.sendDrawingData(data);
    }


    public static void receiveDrawingData(String data) {
        String[] parts = data.split(",");
        // Ensure we have enough parts in the data to proceed
        if (parts.length < 8) {
            System.out.println("Received invalid data: " + data);
            return;  // Exit early if data is not valid
        }

        String action = parts[0];
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        int red = Integer.parseInt(parts[3]);
        int green = Integer.parseInt(parts[4]);
        int blue = Integer.parseInt(parts[5]);
        double lineWidth = Double.parseDouble(parts[6]);
        String mode = parts[7];  // Get the mode

        if (x < 0 || y < 0 || x > 800 || y > 600) {
            System.out.println("Invalid coordinates received: " + x + ", " + y);
            return;
        }

        Color color = Color.rgb(red, green, blue);
        gc.setStroke(color);
        gc.setLineWidth(lineWidth);  // Set line width

        if (mode.equals("highlight")) {
            Color highlightColor = Color.YELLOW; // Change this to your desired highlight color
            gc.setFill(highlightColor.deriveColor(0, 1, 1, 0.05)); // Semi-transparent color
            gc.fillRect(x - 10, y - 10, 20, 20); // Draw a rectangle as the highlight
        } else if (mode.equals("erase")) {
            gc.setStroke(Color.WHITE);  // Use white for erasing
        } else {
            gc.setStroke(color);
        }

        if (action.equals("pressed")) {
            gc.beginPath();
            gc.moveTo(x, y);
            gc.stroke();
        } else if (action.equals("dragged")) {
            gc.lineTo(x, y);
            gc.stroke();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
