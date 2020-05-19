package com.boyarsky.dapos.core.service.validator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StakeholderPunishmentData {
    private long punishmentAmount;
    private int removed;
}
