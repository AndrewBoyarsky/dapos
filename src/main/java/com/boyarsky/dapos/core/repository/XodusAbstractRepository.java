package com.boyarsky.dapos.core.repository;

import com.boyarsky.dapos.core.model.BlockchainEntity;
import com.boyarsky.dapos.core.repository.aop.Transactional;
import com.boyarsky.dapos.utils.CollectionUtils;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.StoreTransaction;
import lombok.NonNull;

import java.util.List;

public abstract class XodusAbstractRepository<T extends BlockchainEntity> {
    private final String entityName;
    private final boolean supportUpdate;
    private final XodusRepoContext context;

    protected XodusAbstractRepository(@NonNull String entityName, boolean supportUpdate, @NonNull XodusRepoContext context) {
        this.entityName = entityName;
        this.supportUpdate = supportUpdate;
        this.context = context;
    }

    @Transactional(requiredExisting = true)
    public void save(@NonNull T t) {
        Entity toSave = null;

        if (t.getDbId() != null) {
            toSave = getTx().getEntity(t.getDbId());
        } else {
            DbParam id = idParam(t);
            if (id != null) {
                toSave = getByDbParam(id);
            }
        }

        if (!supportUpdate && toSave != null) {
            throw new IllegalArgumentException("Existing entity found: " + toSave + " , but store does not support update");
        }
        if (toSave == null) {
            toSave = getTx().newEntity(entityName);
        }
        toSave.setProperty("height", t.getHeight());
        storeToDbEntity(toSave, t);
    }

    protected abstract void storeToDbEntity(Entity e, T t);

    @Transactional(readonly = true)
    public <V extends DbParam> T get(@NonNull V id) {
        Entity e = getByDbParam(id);
        if (e == null) {
            return null;
        }
        return map(e);
    }

    @SafeVarargs
    @Transactional(readonly = true)
    public final <V extends DbParam> List<T> getAll(@NonNull V... params) {
        if (params != null && params.length != 0) {
            EntityIterable iterable = null;
            for (V param : params) {
                EntityIterable filtered = getTx().find(entityName, param.name(), param.value());
                if (iterable != null) {
                    iterable = iterable.intersect(filtered);
                } else {
                    iterable = filtered;
                }
            }
            return CollectionUtils.toList(iterable, this::map);
        } else {
            return CollectionUtils.toList(getTx().getAll(entityName), this::map);
        }
    }

    public <V extends DbParam> Entity getByDbParam(@NonNull V id) {
        return CollectionUtils.requireAtMostOne(context.getTx().find(entityName, id.name(), id.value()));
    }

    protected T map(Entity e) {
        if (e == null) {
            return null;
        }
        T t = doMap(e);
        t.setDbId(e.getId());
        t.setHeight((Long) e.getProperty("height"));
        return t;
    }

    protected abstract T doMap(Entity e);

    protected <V extends DbParam> V idParam(T value) {
        return null;
    }

    protected StoreTransaction getTx() {
        return context.getTx();
    }
}
