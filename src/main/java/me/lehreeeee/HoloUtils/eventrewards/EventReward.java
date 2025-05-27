package me.lehreeeee.HoloUtils.eventrewards;

import java.util.List;

public record EventReward(String rewardId, String displayName, List<String> loreLines, String skullTexture, List<String> commands) {}

