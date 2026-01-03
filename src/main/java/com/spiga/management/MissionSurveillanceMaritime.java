package com.spiga.management;

/**
 * Implémentation concrète d'une mission de surveillance maritime.
 * <p>
 * Spécialisée pour l'observation de zones. Utilisée par les
 * {@link com.spiga.core.DroneReconnaissance}
 * ou les {@link com.spiga.core.VehiculeSurface}.
 * </p>
 */
public class MissionSurveillanceMaritime extends Mission {

    /**
     * Crée une mission de surveillance.
     * 
     * @param titre Titre.
     */
    public MissionSurveillanceMaritime(String titre) {
        super(titre, MissionType.SURVEILLANCE);
        this.objectives = "Surveiller la zone maritime désignée";
    }

    @Override
    public Mission copy() {
        MissionSurveillanceMaritime m = new MissionSurveillanceMaritime(this.titre);
        m.setTarget(this.targetX, this.targetY, this.targetZ);
        m.setObjectives(this.objectives);
        return m;
    }
}
