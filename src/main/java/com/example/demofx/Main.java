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
            gc.beginPath();
            gc.moveTo(e.getX(), e.getY());
            gc.stroke();
            if(isEraserMode){
                gc.setLineWidth(10.0);
            } else {
                gc.setLineWidth(1.0);
            }
            sendDrawingData(e.getX(), e.getY(), "pressed", isEraserMode ? Color.WHITE : currentColor);
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastDrawTime >= drawDelay) {
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
                if(isEraserMode){
                    gc.setLineWidth(10.0);
                } else {
                    gc.setLineWidth(1.0);
                }
                sendDrawingData(e.getX(), e.getY(), "dragged", isEraserMode ? Color.WHITE : currentColor);
                lastDrawTime = currentTime;
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

        // Pane to hold the canvas and color picker
        Pane root = new Pane();
        root.getChildren().addAll(canvas, colorPicker);
        root.getChildren().add(eraserButton);
        eraserButton.setLayoutX(700);
        eraserButton.setLayoutY(20);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Collaborative Whiteboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void sendDrawingData(double x, double y, String action, Color color) {
        int red = (int) (color.getRed() * 255);
        int green = (int) (color.getGreen() * 255);
        int blue = (int) (color.getBlue() * 255);
        String data = action + "," + x + "," + y + "," + red + "," + green + "," + blue;
        client.sendDrawingData(data);
    }

    public static void receiveDrawingData(String data) {
        String[] parts = data.split(",");
        // Check if there are enough parts in the data to proceed
        if (parts.length < 6) {
            System.out.println("Received invalid data: " + data);
            return;  // Exit early if data is not valid
        }

        String action = parts[0];
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        int red = Integer.parseInt(parts[3]);
        int green = Integer.parseInt(parts[4]);
        int blue = Integer.parseInt(parts[5]);

        if (x < 0 || y < 0 || x > 800 || y > 600) {
            System.out.println("Invalid coordinates received: " + x + ", " + y);
            return;
        }

        Color color = Color.rgb(red, green, blue);
        gc.setStroke(color);

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
