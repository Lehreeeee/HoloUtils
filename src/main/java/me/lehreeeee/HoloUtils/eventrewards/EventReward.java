package me.lehreeeee.HoloUtils.eventrewards;

import java.util.List;

public record EventReward(String rewardId, String displayName, String skullTexture, List<String> commands) {}

