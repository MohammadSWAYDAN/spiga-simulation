package com.spiga.management;

/**
 * MissionLogistique - Mission logistique
 * Conforme a SPIGA-SPEC.txt section 3.2
 * 
 * Concept : Polymorphisme (Missions)
 * Cette classe est une forme specifique de "Mission".
 */
public class MissionLogistique extends Mission {

    public MissionLogistique(String titre) {
        super(titre, MissionType.LOGISTICS);
        this.objectives = "Transport et livraison de matériel";
    }

    @Override
    public Mission copy() {
        MissionLogistique m = new MissionLogistique(this.titre);
        m.setTarget(this.targetX, this.targetY, this.targetZ);
        m.setObjectives(this.objectives);
        return m;
    }
}
