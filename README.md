# Collaborative Whiteboard

This is a collaborative whiteboard application that allows multiple users to draw and interact with each other in real-time. The application is built using JavaFX for the frontend and Java sockets for the backend to manage client-server communication. Users can join a session using a unique PIN, enabling them to share and collaborate on a whiteboard.

![collaborative-whiteboard](https://github.com/user-attachments/assets/c27bc3e0-5296-498a-bf7d-8f75d0123759)

## Features

- **Real-time Collaboration**: Multiple users can join the same whiteboard session using a PIN and draw together in real-time.
- **Drawing Tools**:
  - Pencil for normal drawing.
  - Eraser to clear parts of the drawing.
  - Highlighter to emphasize certain areas with transparent color.
- **Color Picker**: Users can change the drawing color via a color picker.
- **PIN-based Sessions**: Users can join different whiteboard sessions using a unique PIN, ensuring group collaboration.

## Project Structure

The project consists of the following components:

- **Client** (`WhiteboardClient`): Connects to the server and sends/receives drawing data to/from other clients in the same session.
- **Server** (`WhiteboardServer`): Manages multiple client connections, grouping them by a PIN, and handles broadcasting of drawing data between clients in the same session.
- **Frontend** (`Main`): JavaFX-based UI where users can interact with the whiteboard, including drawing, erasing, highlighting, and picking colors.

## Getting Started

### Prerequisites

To run this project locally, you will need the following:

- Java 8 or higher
- JavaFX 11 or higher
- Any IDE that supports Java (e.g., IntelliJ, Eclipse)

### Installation

1. Clone this repository:

   ```bash
   git clone https://github.com/gitgunawardhana/collaborative-whitebroard.git

2. Navigate to the project directory:

    ```bash
    cd collaborative-whitebroard

3. Set up JavaFX in your IDE. For IntelliJ IDEA, add the JavaFX SDK to your project libraries.

4. Build the project and ensure all dependencies are resolved.

## Running the Application

### Starting the Server

1. Run the `WhiteboardServer` class to start the server:

   ```bash
   java com.example.demofx.WhiteboardServer
   ```
   The server will listen on port `12345` for incoming connections.

2. Valid PINs for sessions are hardcoded in the server. You can modify or add valid PINs in the `WhiteboardServer` class:

    ```bash
    validPins.add("1234");  // Example PIN
    validPins.add("5678");

### Running the Client

1. Run the `Main` class to start the client.

2. On the login screen, enter one of the valid PINs (e.g., `1234`) and click "Join".

3. Once the whiteboard is loaded, you can start drawing.

## Drawing on the Whiteboard

- Pencil: Click and drag on the whiteboard to draw.
- Eraser: Toggle the "Eraser" button to enter eraser mode and erase parts of the drawing.
- Highlight: Toggle the "Highlight" button to add semi-transparent highlights.
- Color Picker: Choose a drawing color from the color picker.

### Real-time Collaboration

All drawing events (mouse press, drag, etc.) are transmitted in real-time between users who have joined the same session via a PIN. This ensures a synchronized drawing experience.

## Code Overview

### Main Components

- `Main`: The JavaFX entry point, handling the UI, drawing events, and session management.
- `WhiteboardClient`: Manages the client-side socket connection and communication with the server. Sends drawing data to the server and receives updates from other clients.
- `WhiteboardServer`: The server-side component that listens for client connections, validates the PIN, and broadcasts drawing data to all clients in the same session.

### Drawing Data Transmission

Each drawing event is sent as a string containing:

- Action (e.g., `pressed`, `dragged`)
- X, Y coordinates
- Color (RGB)
- Line width
- Drawing mode (`normal`, `highlight`, `erase`)

Example format:

    pressed,100,150,255,0,0,1.0,normal
    
### How PINs Work

- PIN-based sessions: Clients are grouped by a session PIN. Only clients with the same PIN can share a whiteboard. This is managed in the `WhiteboardServer` class using a `Map<String, Set<PrintWriter>>` where the key is the PIN, and the value is the set of clients in that session.

## License

This project is licensed under the MIT License.
