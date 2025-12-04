package com.spiga.management;

/**
 * MissionSurveillanceMaritime - Mission de surveillance maritime
 * Conforme à SPIGA-SPEC.txt section 3.2
 */
public class MissionSurveillanceMaritime extends Mission {

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
