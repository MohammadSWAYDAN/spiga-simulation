
## For Team Members

### Prerequisites

Before cloning the project, ensure you have:

1. **Java JDK 17 or higher**
   - Download: https://www.oracle.com/java/technologies/downloads/#java17
   - Verify installation: `java -version`

2. **Git**
   - Download: https://git-scm.com/downloads
   - Verify installation: `git --version`

3. **A GitHub Account**
   - Sign up at https://github.com if you don't have one

### Setup Instructions

#### 1. Accept the Invitation
- Check your email for the GitHub invitation
- Click **"Accept invitation"**

#### 2. Clone the Repository

Open PowerShell (or Terminal) and run:

```powershell
# Clone the repository (replace with actual URL)
git clone https://github.com/YOUR_USERNAME/spiga-simulation.git

# Navigate into the project
cd spiga-simulation
```

#### 3. Build the Project

```powershell
# Clean and compile
.\mvnw.cmd clean compile
```

**Expected output**: `BUILD SUCCESS`

#### 4. Run the Simulation

```powershell
.\mvnw.cmd javafx:run
```

**Expected result**: The SPIGA simulation window should open!

### Testing the Features

Once the application is running, try these:

1. **Add Assets**:
   - Click "Add Drone" → Choose "Cursor" → Click on the map
   - Add a few drones, boats, or submarines

2. **Multi-Selection**:
   - Hold `Ctrl` and click multiple drones
   - OR click the **"Select All"** button

3. **Swarm Movement**:
   - Select multiple drones
   - Click anywhere on the map
   - Watch them move in formation! 🐝

4. **Create a Mission**:
   - Click "Create Mission" (right panel)
   - Select a mission type
   - Click "Set Target" and click on the map
   - Select assets and click "Start Mission"

### Troubleshooting

**Problem**: `java: command not found`
- **Solution**: Install Java JDK 17 (see Prerequisites)

**Problem**: Build fails with "JAVA_HOME not set"
- **Solution**: Set JAVA_HOME environment variable to your JDK installation path

**Problem**: JavaFX errors
- **Solution**: The Maven wrapper handles JavaFX automatically. Just ensure you're using Java 17+

### Making Changes

When you make changes to the code:

```powershell
# Check what changed
git status

# Add your changes
git add .

# Commit with a message
git commit -m "Description of your changes"

# Push to GitHub
git push
```

### Getting Updates

If someone else makes changes:

```powershell
# Pull the latest changes
git pull
```

---

## Quick Reference

| Command | Purpose |
|---------|---------|
| `.\mvnw.cmd clean compile` | Build the project |
| `.\mvnw.cmd javafx:run` | Run the simulation |
| `git pull` | Get latest changes |
| `git add .` | Stage all changes |
| `git commit -m "message"` | Commit changes |
| `git push` | Upload to GitHub |

---

## Need Help?

- Check the main [README.md](README.md) for feature documentation
- Review the [SPIGA-SPEC.txt](SPIGA-SPEC.txt) for technical specifications
- Contact the project owner if you encounter issues

**Happy Coding! 🚁🚢🌊**
