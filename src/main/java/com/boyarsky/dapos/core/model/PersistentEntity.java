package com.boyarsky.dapos.core.model;

import jetbrains.exodus.entitystore.EntityId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersistentEntity {
    private EntityId id;
}
