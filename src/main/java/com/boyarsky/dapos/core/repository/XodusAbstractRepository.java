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
    // TODO when update is enabled, one atomic Transaction required per block. To support 'non @Transactional' approach we need to implement prev copy storage
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
            List<DbParam> ids = idParams(t);
            if (ids != null && !ids.isEmpty()) {
                toSave = getByDbParams(ids);
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

    @Transactional(requiredExisting = true)
    public void remove(T t) {
        if (t.getDbId() != null) {
            getTx().getEntity(t.getDbId()).delete();
        }
        List<DbParam> dbParams = idParams(t);
        if (supportUpdate && dbParams != null) {
            getByDbParams(dbParams).delete();
        } else {
            throw new UnsupportedOperationException("Removal is not supported for " + entityName);
        }
    }

    protected abstract void storeToDbEntity(Entity e, T t);

    @Transactional(readonly = true)
    public <V extends DbParam> T get(@NonNull V id) {
        Entity e = getByDbParams(List.of(id));
        if (e == null) {
            return null;
        }
        return map(e);
    }

    protected EntityIterable sort(EntityIterable iterable, Sort sort) {
        if (sort == null) {
            sort = Sort.defaultSort();
        }
        for (Sort.SortColumn column : sort.columns) {
            iterable = getTx().sort(entityName, column.getColumn(), iterable, column.isAsc());
        }
        return iterable;
    }

    protected EntityIterable sort(EntityIterable iterable) {
        return sort(iterable, null);
    }

    @Transactional(readonly = true)
    public <V extends DbParam> List<T> getAll(@NonNull V... params) {
        EntityIterable allEntities = getAllEntities(params);
        return CollectionUtils.toList(allEntities, this::map);
    }

    public <V extends DbParam> List<T> getAll(Pagination pagination, @NonNull V... params) {
        EntityIterable allEntities = getAllEntities(pagination, params);
        return CollectionUtils.toList(allEntities, this::map);
    }

    public <V extends DbParam> List<T> getAll(Sort sort, Pagination pagination, @NonNull V... params) {
        EntityIterable allEntities = getAllEntities(sort, pagination, params);
        return CollectionUtils.toList(allEntities, this::map);
    }

    public <V extends DbParam> List<T> getAll(Sort sort, @NonNull V... params) {
        EntityIterable allEntities = getAllEntities(sort, params);
        return CollectionUtils.toList(allEntities, this::map);
    }

    protected <V extends DbParam> EntityIterable getAllEntities(Sort sort, Pagination pagination, @NonNull V... params) {
        EntityIterable iterable = null;
        if (params.length != 0) {
            for (V param : params) {
                EntityIterable filtered = getTx().find(entityName, param.name(), param.value());
                if (iterable != null) {
                    iterable = iterable.intersect(filtered);
                } else {
                    iterable = filtered;
                }
            }
        } else {
            iterable = getTx().getAll(entityName);
        }
        iterable = sort(iterable, sort);
        iterable = paginate(iterable, pagination);
        return iterable;
    }

    protected EntityIterable paginate(EntityIterable iterable, Pagination pagination) {
        if (pagination != null) {
            return iterable.skip(pagination.getPage() * pagination.getLimit()).take(pagination.getLimit());
        }
        return iterable;
    }

    protected <V extends DbParam> EntityIterable getAllEntities(@NonNull V... params) {
        return getAllEntities(null, null, params);
    }

    protected <V extends DbParam> EntityIterable getAllEntities(Sort sort, @NonNull V... params) {
        return getAllEntities(sort, null, params);
    }

    protected <V extends DbParam> EntityIterable getAllEntities(Pagination pagination, @NonNull V... params) {
        return getAllEntities(null, pagination, params);
    }

    public Entity getByDbParams(@NonNull List<DbParam> id) {
        return CollectionUtils.requireAtMostOne(getAllEntities(id.toArray(new DbParam[]{})));
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

    protected List<DbParam> idParams(T value) {
        return List.of();
    }

    protected StoreTransaction getTx() {
        return context.getTx();
    }
}
