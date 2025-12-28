package com.spiga.core;

import com.spiga.management.Mission;

/**
 * Classe abstraite ActifMobile - ENHANCED VERSION
 * Conforme à SPIGA-SPEC.txt section 1.1
 */
public abstract class ActifMobile implements Deplacable, Rechargeable, Communicable, Pilotable, Alertable {

    public enum EtatOperationnel {
        AU_SOL, EN_MISSION, EN_MAINTENANCE, EN_PANNE
    }

    public enum AssetState {
        IDLE, // Au repos
        MOVING_TO_TARGET, // En déplacement vers une cible
        EXECUTING_MISSION, // Exécution de mission
        LOW_BATTERY, // Batterie faible (warning only)
        RETURNING_TO_BASE, // Retour automatique à la base
        STOPPED, // Arrêté (batterie vide)
        RECHARGING // En recharge
    }

    protected String id;
    protected double x, y, z;
    protected double vitesseMax;
    protected double autonomieMax;
    protected double autonomieActuelle;
    protected EtatOperationnel etat;

    // NOUVEAUX ATTRIBUTS
    protected double velocityX, velocityY, velocityZ;
    protected double targetX, targetY, targetZ;
    protected AssetState state;
    protected Mission currentMission;
    protected boolean selected;
    protected double speedModifier = 1.0;
    protected String collisionWarning = null; // Alert message for UI

    public ActifMobile(String id, double x, double y, double z, double vitesseMax, double autonomieMax) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.vitesseMax = vitesseMax;
        this.autonomieMax = autonomieMax;
        this.autonomieActuelle = autonomieMax;
        this.etat = EtatOperationnel.AU_SOL;

        this.velocityX = 0;
        this.velocityY = 0;
        this.velocityZ = 0;
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        this.state = AssetState.IDLE;
        this.currentMission = null;
        this.selected = false;
    }

    // Updated update signature to include Weather
    public void update(double dt, com.spiga.environment.Weather weather) {
        speedModifier = 1.0; // Reset every frame, SimulationService will override if collision detected
        if (state == AssetState.MOVING_TO_TARGET || state == AssetState.EXECUTING_MISSION
                || state == AssetState.RETURNING_TO_BASE) {
            moveTowards(targetX, targetY, targetZ, dt, weather); // Pass weather for drag
            updateBattery(dt, weather);
        }
        clampPosition(); // Force constraints every frame
        checkBatteryState();
    }

    /**
     * Enforces strict physical domain constraints.
     * e.g. Surface vessels must have Z=0.
     */
    protected abstract void clampPosition();

    public void moveTowards(double targetX, double targetY, double targetZ, double dt,
            com.spiga.environment.Weather weather) {
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance < 1.0) {
            x = targetX;
            y = targetY;
            z = targetZ;
            velocityX = 0;
            velocityY = 0;
            velocityZ = 0; // Fix missing reset

            if (state == AssetState.RETURNING_TO_BASE) {
                state = AssetState.RECHARGING;
                recharger();
            } else if (state == AssetState.EXECUTING_MISSION && currentMission != null) {
                currentMission.complete();
                state = AssetState.IDLE;
            } else {
                state = AssetState.IDLE;
            }
        } else {
            double dirX = dx / distance;
            double dirY = dy / distance;
            double dirZ = dz / distance;

            // Apply Weather Drag
            double effectiveSpeed = vitesseMax;
            if (weather != null) {
                // Simple drag model: 1% speed loss per 10 km/h of wind
                double dragFactor = 1.0 - (weather.getWindSpeed() / 1000.0);
                if (weather.getSeaWaveHeight() > 0 && this instanceof ActifMarin) {
                    dragFactor -= weather.getSeaWaveHeight() * 0.05; // 5% per meter
                }
                if (dragFactor < 0.1)
                    dragFactor = 0.1; // Min speed
                effectiveSpeed *= dragFactor;
            }

            effectiveSpeed *= speedModifier; // Apply Speed Modifier

            velocityX = dirX * effectiveSpeed;
            velocityY = dirY * effectiveSpeed;
            velocityZ = dirZ * effectiveSpeed; // Corrected assignment

            x += velocityX * dt;
            y += velocityY * dt;
            z += velocityZ * dt;
        }
    }

    public void updateBattery(double dt, com.spiga.environment.Weather weather) {
        if (state == AssetState.EXECUTING_MISSION || state == AssetState.MOVING_TO_TARGET
                || state == AssetState.RETURNING_TO_BASE) {
            // Base Consumption
            double consumption = (getConsommation() / 3600.0) * dt;

            // Speed Factor
            double speed = Math.sqrt(velocityX * velocityX + velocityY * velocityY + velocityZ * velocityZ);
            double speedFactor = 1.0 + (speed / vitesseMax);

            // Weather Factor Integration
            // Weather Factor Integration (Delegated to subclasses)
            double weatherFactor = 1.0;
            if (weather != null) {
                weatherFactor = getWeatherImpact(weather);
            }

            consumption *= speedFactor * weatherFactor;

            autonomieActuelle -= consumption;
            if (autonomieActuelle < 0)
                autonomieActuelle = 0;
        }
    }

    private void checkBatteryState() {
        if (autonomieActuelle <= 0) {
            state = AssetState.STOPPED;
            etat = EtatOperationnel.EN_PANNE;
            velocityX = 0;
            velocityY = 0;
            velocityZ = 0;
            notifierEtatCritique("Battery depleted!");
        } else {
            // Smart Return Logic
            double distToBase = Math.sqrt(x * x + y * y + z * z); // Base at 0,0,0
            double timeToReturn = distToBase / vitesseMax; // Seconds
            double energyNeeded = (timeToReturn / 3600.0) * getConsommation(); // Hours
            double safetyMargin = autonomieMax * 0.10; // 10% buffer

            if (autonomieActuelle < (energyNeeded + safetyMargin)) {
                if (state != AssetState.RETURNING_TO_BASE && state != AssetState.STOPPED
                        && state != AssetState.RECHARGING) {
                    returnToBase();
                    notifierEtatCritique("Low battery (Smart Return) - Returning to base");
                }
            }
        }
    }

    public void returnToBase() {
        setTarget(0, 0, 0); // Base at origin
        this.state = AssetState.RETURNING_TO_BASE;
    }

    public boolean hasReachedTarget() {
        double dx = targetX - x;
        double dy = targetY - y;
        return Math.sqrt(dx * dx + dy * dy) < 1.0;
    }

    public void assignMission(Mission mission) {
        this.currentMission = mission;
        this.targetX = mission.getTargetX();
        this.targetY = mission.getTargetY();
        this.targetZ = mission.getTargetZ();
        this.state = AssetState.EXECUTING_MISSION;
        this.etat = EtatOperationnel.EN_MISSION;
    }

    public void setTarget(double x, double y, double z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        this.state = AssetState.MOVING_TO_TARGET;
    }

    public double getBatteryPercent() {
        return autonomieActuelle / autonomieMax;
    }

    public abstract double getConsommation();

    /**
     * Calculates impact of weather on consumption.
     * 1.0 = No impact. >1.0 = Increased consumption.
     */
    protected double getWeatherImpact(com.spiga.environment.Weather w) {
        return 1.0; // Default implementation
    }

    @Override
    public void deplacer(double targetX, double targetY, double targetZ) {
        setTarget(targetX, targetY, targetZ);
    }

    @Override
    public void calculerTrajet(double targetX, double targetY, double targetZ) {
        double distance = Math.sqrt(Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2) + Math.pow(targetZ - z, 2));
        System.out.println(id + " - Distance vers cible: " + (int) distance + "m");
    }

    @Override
    public void recharger() {
        autonomieActuelle = autonomieMax;
        if (state == AssetState.STOPPED || state == AssetState.RECHARGING) {
            state = AssetState.IDLE;
        }
        if (etat == EtatOperationnel.EN_PANNE) {
            etat = EtatOperationnel.AU_SOL;
        }
        System.out.println(id + " rechargé à 100%");
    }

    @Override
    public void ravitailler() {
        recharger();
    }

    @Override
    public void transmettreAlerte(String message, ActifMobile actifCible) {
        System.out.println(id + " → " + actifCible.getId() + ": " + message);
    }

    @Override
    public void demarrer() {
        if (etat == EtatOperationnel.AU_SOL && autonomieActuelle > 0) {
            etat = EtatOperationnel.EN_MISSION;
            state = AssetState.IDLE;
            System.out.println(id + " démarré");
        }
    }

    @Override
    public void eteindre() {
        if (etat == EtatOperationnel.EN_MISSION) {
            etat = EtatOperationnel.AU_SOL;
            state = AssetState.IDLE;
            velocityX = 0;
            velocityY = 0;
            System.out.println(id + " éteint");
        }
    }

    @Override
    public void notifierEtatCritique(String typeAlerte) {
        System.out.println("⚠️ ALERTE " + id + ": " + typeAlerte);
    }

    // Getters
    public double getCurrentSpeed() {
        return Math.sqrt(velocityX * velocityX + velocityY * velocityY + velocityZ * velocityZ);
    }

    public String getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getVitesseMax() {
        return vitesseMax;
    }

    public double getAutonomieMax() {
        return autonomieMax;
    }

    public double getAutonomieActuelle() {
        return autonomieActuelle;
    }

    public EtatOperationnel getEtat() {
        return etat;
    }

    public AssetState getState() {
        return state;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public double getVelocityZ() {
        return velocityZ;
    }

    public double getTargetX() {
        return targetX;
    }

    public double getTargetY() {
        return targetY;
    }

    public double getTargetZ() {
        return targetZ;
    }

    public Mission getCurrentMission() {
        return currentMission;
    }

    public boolean isSelected() {
        return selected;
    }

    // Setters
    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setEtat(EtatOperationnel etat) {
        this.etat = etat;
    }

    public void setState(AssetState state) {
        this.state = state;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setSpeedModifier(double modifier) {
        this.speedModifier = modifier;
    }

    public void setCollisionWarning(String warning) {
        this.collisionWarning = warning;
    }

    public String getCollisionWarning() {
        return collisionWarning;
    }
}
