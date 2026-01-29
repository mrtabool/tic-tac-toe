# Tic-Tac-Toe Distributed Application

This is a distributed Tic Tac Toe application where microservices play against each other automatically.

## Components

1.  **Game Engine Service (Port 8081)**: Manages core game logic, board state, and move validation.
2.  **Game Session Service (Port 8082)**: Manages game sessions and automates moves for both players.
3.  **UI Service (Port 8080)**: Provides a web interface to start and visualize the game simulation.

## Prerequisites

- Java 21
- Maven

## How to Run

1.  **Build the project**:
    From the root directory, run:
    ```bash
    mvn clean install
    ```

2.  **Run the services**:
    You need to start all three microservices. Open three separate terminal windows and run each command:

    *   **Game Engine Service**:
        ```bash
        cd game-engine
        mvn spring-boot:run
        ```
    *   **Game Session Service**:
        ```bash
        cd game-session
        mvn spring-boot:run
        ```
    *   **UI Service**:
        ```bash
        cd ui-service
        mvn spring-boot:run
        ```

3.  **Access the UI**:
    Open your browser and go to [http://localhost:8080/index.html](http://localhost:8080/index.html).

## Running Tests

To run tests for all modules, use the following command from the root directory:
```bash
mvn test
```

Or you can run tests for a specific module:
```bash
mvn test -pl game-engine
```

## Game Simulation

- Click **"Start Simulation"** to create a new session and begin the automated game.
- The simulation continues until there is a winner or a draw.

## Technical Details

- **In-Memory Storage**: Uses H2 in-memory database for both Game Engine and Game Session services.
- **REST Communication**: Services communicate via REST APIs using `RestTemplate`.
- **Lombok**: Used for reducing boilerplate code.
- **DTOs**: Shared objects for state and requests are located in the `common-dto` module.
