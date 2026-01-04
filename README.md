# 🚁 SPIGA Simulation
> **S**ystème de **P**ilotage **I**ntelligent & **G**estion d'**A**ctifs

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-17-007396?style=for-the-badge&logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.6%2B-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

## 📋 Overview

**SPIGA Simulation** is a high-fidelity multi-agent simulation engine capable of orchestrating complex interactions between autonomous aerial drones and naval assets. Designed with a focus on real-time physics and swarm intelligence, SPIGA provides a robust environment for testing coordination algorithms, pathfinding strategies, and mission management protocols in dynamic conditions.

The system features a dual-interface architecture: a high-performance **CLI** for headless batch processing and a rich **JavaFX GUI** for real-time visualization and interactive control.

---

## ✨ Key Features

### 🧠 Advanced Swarm Intelligence
- **Collision Avoidance**: Real-time proximity detection and vector-based avoidance maneuvers.
- **Target Deconfliction**: Cooperative algorithm to prevent multiple agents from converging on the same exact coordinate.
- **Formation Logistics**: Efficient handling of swarm movements and spacing.

### 🌍 Dynamic Environment
- **3D Spatial Awareness**: Simulation handles 3D coordinates (X, Y, Altitude/Depth).
- **Obstacle Simulation**: Varied terrain including Surface Islands, Underwater Reefs, and Aerial Constraints.
- **Restricted Zones**: "No-Fly" and "No-Sail" zones with soft-boundary warnings and hard-boundary enforcement.
- **Weather System**: Variable wind speeds and conditions affecting asset performance.

### 🎮 Mission Management
- **Automated Dispatch**: Intelligent assignment of missions (Reconnaissance, Logistics, Surveillance) to available assets.
- **Asset Classes**:
  - **Drones**: `Reconnaissance` (Agile, Zone-Permissive), `Logistique` (Heavy, Zone-Restricted).
  - **Naval**: `Surface Vessel`, `Exploration Submarine`.

### 🖥️ Dual-Mode Interface
- **Graphical User Interface (GUI)**:
  - Real-time map rendering with zoom/pan.
  - Interactive mission creation and asset monitoring.
  - Live telemetry dashboard (Battery, Speed, GPS).
- **Command Line Interface (CLI)**:
  - Lightweight mode for automated testing and server environments.

---

## 🛠️ Architecture

The project follows a modular **MVC** (Model-View-Controller) architecture:

| Package | Description |
| :--- | :--- |
| `com.spiga.core` | Core simulation engine, physics calculations, and asset definitions. |
| `com.spiga.management` | High-level logic for swarm coordination and mission dispatching. |
| `com.spiga.environment` | World entities including Obstacles, Weather, and Restricted Zones. |
| `com.spiga.ui` | JavaFX controllers and FXML views for the graphical interface. |

---

## 🚀 Getting Started

### Prerequisites
- **Java Development Kit (JDK) 17** or higher.
- **Apache Maven 3.6** or higher.

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/MohammadSWAYDAN/spiga-simulation.git
   cd spiga-simulation
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

---

## 🏃 Usage

### Running the GUI (Default)
Launch the full interactive simulation with the dashboard:
```bash
mvn javafx:run
```

### Running the CLI (Headless)
Execute the simulation in console mode for logs and debugging:
```bash
mvn javafx:run -Djavafx.args="--cli"
```
*Alternatively, if running the built JAR:*
```bash
java -jar target/spiga-simulation-1.0-SNAPSHOT.jar --cli
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:
1. Fork the project.
2. Create your feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

---

<p align="center">
  <i>Developed by the SPIGA Team • 2025-2026</i>
</p>
