package pepperstone.backend.common.entity.enums;

public enum Level {
    // F 그룹
    F1_I("F1-I", 0),
    F1_II("F1-II", 13500),
    F2_I("F2-I", 27000),
    F2_II("F2-II", 39000),
    F2_III("F2-III", 51000),
    F3_I("F3-I", 63000),
    F3_II("F3-II", 78000),
    F3_III("F3-III", 93000),
    F4_I("F4-I", 108000),
    F4_II("F4-II", 126000),
    F4_III("F4-III", 144000),
    F5("F5", 162000),

    // B 그룹
    B1("B1", 0),
    B2("B2", 24000),
    B3("B3", 52000),
    B4("B4", 78000),
    B5("B5", 117000),
    B6("B6", 169000),

    // G 그룹
    G1("G1", 0),
    G2("G2", 24000),
    G3("G3", 52000),
    G4("G4", 78000),
    G5("G5", 117000),
    G6("G6", 169000),

    // T 그룹
    T1("T1", 0),
    T2("T2", 0), // T 그룹의 경험치 값을 설정해야 함
    T3("T3", 0),
    T4("T4", 0),
    T5("T5", 0),
    T6("T6", 0);

    private final String name;
    private final int requiredExperience;

    Level(String name, int requiredExperience) {
        this.name = name;
        this.requiredExperience = requiredExperience;
    }

    public String getName() {
        return name;
    }

    public int getRequiredExperience() {
        return requiredExperience;
    }

    public static Level fromName(String name) {
        for (Level level : Level.values()) {
            if (level.name.equals(name)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown level: " + name);
    }
}
