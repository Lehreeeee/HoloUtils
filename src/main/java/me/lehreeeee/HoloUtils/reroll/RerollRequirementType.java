package me.lehreeeee.HoloUtils.reroll;

public enum RerollRequirementType {
    MMOITEMS,
    MONEY,
    RECURRENCY;


    public static RerollRequirementType fromString(String type) {
        for (RerollRequirementType requirementType : values()) {
            if (requirementType.name().equalsIgnoreCase(type)) {
                return requirementType;
            }
        }
        return null;
    }
}
