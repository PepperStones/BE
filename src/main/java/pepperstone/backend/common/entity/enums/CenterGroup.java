package pepperstone.backend.common.entity.enums;

public enum CenterGroup {
//    음성 1센터	음성 2센터	용인백암센터	남양주센터	파주센터	사업기획팀	그로스팀	CX팀
    ES1("음성 1센터"),
    ES2("음성 2센터"),
    YB("용인백암센터"),
    NYJ("남양주센터"),
    PJ("파주센터"),
    BUSSINESS_PLANNING("사업기획팀"),
    GROWTH("그로스팀"),
    CX("CX팀");

    private final String name;

    CenterGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
