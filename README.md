# SPIGA Simulation System 🚁🚢🌊

**SPIGA** (Système de Pilotage Intelligent et Gestion d'Actifs) is a robust JavaFX-based simulation platform for managing autonomous fleets of drones, boats, and submarines.

This project demonstrates advanced object-oriented programming concepts, real-time physics simulation, and agentic behavior logic.

## 🌟 Key Features

### 🧠 Intelligent Behavior
- **Smart Return-to-Base**: Assets automatically calculate energy requirements to return home and abort missions if battery is critical (with safety buffers).
- **Swarm Intelligence**: Select multiple drones to move them in formation (Circle Formation) without stacking or collisions.
- **3D Obstacle Avoidance**:
    - **Drones**: Fly *over* mountains.
    - **Submarines**: Dive *under* reefs.
    - **Boats**: Steer *around* islands.

### 🎮 Interactive Control
- **Multi-Selection**: Use `Ctrl+Click` or the **"Select All"** button to command entire fleets.
- **Click-to-Move**: Instantly deploy assets to any map location (supports 3D altitude/depth input).
- **Mission Management**: Plan, assign, and monitor missions (Surveillance, Logistics, Rescue).
- **Real-time Feedback**: Live battery monitoring, mission status, and weather effects.

### 🌍 Dynamic Environment
- **3D Physics**: Full X, Y, Z movement logic.
- **Weather System**: Random wind and rain cycles affecting asset performance.
- **Day/Night Cycle**: Visual feedback (simulated).

## 🛠️ Requirements

- **Java JDK 17** or higher
- **Maven** (Wrapper included)

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/spiga-simulation.git
cd spiga-simulation
```

### 2. Build the Project
```bash
./mvnw clean compile
```

### 3. Run the Simulation
```bash
./mvnw javafx:run
```

## 📖 User Guide

### Asset Creation
- Click **"Add Drone"**, **"Add Boat"**, or **"Add Sub"**.
- Choose **Cursor** (click on map) or **Manual** (enter coordinates).
- For 3D assets, you will be prompted for Altitude (Z > 0) or Depth (Z < 0).

### Controlling Assets
- **Select**: Click an asset icon. `Ctrl+Click` for multiple.
- **Move**: With assets selected, click anywhere on the map.
- **Missions**:
    1. Select a mission type from the right panel.
    2. Click **"Create Mission"**.
    3. Select the mission in the list AND select assets on the map.
    4. Click **"Start Mission"**.

### Camera Views
- **Top View**: Main tactical map (X/Y).
- **Profile View**: Bottom panel showing altitude/depth (X/Z).

## 📂 Project Structure

- `com.spiga.core`: Core asset classes (`ActifMobile`, `Drone`, `SousMarin`, etc.).
- `com.spiga.management`: Mission and Fleet management logic.
- `com.spiga.environment`: Weather, Obstacles, and Zone logic.
- `com.spiga.ui`: JavaFX Controllers and Canvas drawing logic.

---
*Project created for POO Java Course 2025-2026.*
