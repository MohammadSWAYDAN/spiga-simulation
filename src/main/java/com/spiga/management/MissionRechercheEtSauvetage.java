package com.spiga.management;

/**
 * MissionRechercheEtSauvetage - Mission de recherche et sauvetage
 * Conforme à SPIGA-SPEC.txt section 3.2
 */
public class MissionRechercheEtSauvetage extends Mission {

    public MissionRechercheEtSauvetage(String titre) {
        super(titre, MissionType.SEARCH_AND_RESCUE);
        this.objectives = "Rechercher et secourir les personnes en détresse";
    }

    @Override
    public Mission copy() {
        MissionRechercheEtSauvetage m = new MissionRechercheEtSauvetage(this.titre);
        m.setTarget(this.targetX, this.targetY, this.targetZ);
        m.setObjectives(this.objectives);
        return m;
    }
}
