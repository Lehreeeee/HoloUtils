package me.lehreeeee.HoloUtils.reroll;

import java.util.HashMap;
import java.util.Map;

public class RerollRequirement {
    private final RerollRequirementType requirementType;
    private final Map<String, String> parameters;
    private final String requirementLore;
    private boolean isValid = false;

    public RerollRequirement(String req) {
        if (req == null || !req.contains("{") || !req.contains("}")) {
            throw new IllegalArgumentException("Invalid requirement format: " + req);
        }

        String[] parts = req.split("\\{", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid requirement format: " + req);
        }

        this.requirementType = RerollRequirementType.fromString(parts[0].trim());
        this.parameters = parseParameters(parts[1].replace("}", "").trim());
        this.requirementLore = RerollRequirementValidator.getRequirementLore(this);
    }

    public RerollRequirementType getRequirementType() {
        return requirementType;
    }

    public Map<String, String> getParameters(){
        return parameters;
    }

    public String getRequirementLore(){
        return requirementLore;
    }

    public boolean isValid(){
        return isValid;
    }

    public void setValid(boolean valid){
        this.isValid = valid;
    }

    private Map<String, String> parseParameters(String rawParams) {
        Map<String, String> paramMap = new HashMap<>();
        String[] pairs = rawParams.split(";");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                paramMap.put(keyValue[0], keyValue[1]);
            }
        }
        return paramMap;
    }

    @Override
    public String toString() {
        return "RequirementType: " + requirementType + ", Params: " + this.parameters;
    }
}
