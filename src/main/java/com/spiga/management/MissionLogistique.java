package com.spiga.management;

/**
 * Implémentation concrète d'une mission de logistique.
 * <p>
 * Spécialisée pour le transport et la livraison. Utilisée par les
 * {@link com.spiga.core.DroneLogistique}.
 * </p>
 */
public class MissionLogistique extends Mission {

    /**
     * Crée une mission de logistique.
     * 
     * @param titre Nom de la mission.
     */
    public MissionLogistique(String titre) {
        super(titre, MissionType.LOGISTICS);
        this.objectives = "Transport et livraison de matériel";
    }

    /**
     * Clone la mission.
     * Pattern Prototype pour dupliquer une mission existante.
     */
    @Override
    public Mission copy() {
        MissionLogistique m = new MissionLogistique(this.titre);
        m.setTarget(this.targetX, this.targetY, this.targetZ);
        m.setObjectives(this.objectives);
        return m;
    }
}
