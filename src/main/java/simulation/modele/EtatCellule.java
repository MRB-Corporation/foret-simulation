package simulation.modele;

public enum EtatCellule {

    INTACT,       // Forêt normale, pas encore touchée
    HUMIDE,       // Végétation humide, très résistante au feu
    EN_FEU,       // Cellule en train de brûler
    BRULE,        // Complètement consumée, ne peut plus brûler
    COUPE_FEU,    // Sans végétation (rocher, terre nue) — stoppe la propagation
    EAU,          // Rivière, lac — stoppe la propagation
    ZONE_URBANISEE; // Village, bâtiments — à protéger, ne brûle pas

    public boolean peutSEnflammer() {
        return this == INTACT || this == HUMIDE;
    }

    public boolean stoppeLesFeu() {
        return this == EAU || this == COUPE_FEU || this == ZONE_URBANISEE;
    }

    public boolean estEnCombustion() {
        return this == EN_FEU;
    }
}
// TODO
