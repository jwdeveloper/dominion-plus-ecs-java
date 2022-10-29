/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.engine.collections.ChunkedPool;
import dev.dominion.ecs.engine.collections.ChunkedPool.Item;
import dev.dominion.ecs.engine.system.UncheckedUpdater;

public final class IntEntity implements Entity, Item {
    private static final UncheckedUpdater.Int<IntEntity> idUpdater;

    static {
        UncheckedUpdater.Int<IntEntity> updater = null;
        try {
            updater = new UncheckedUpdater.Int<>(IntEntity.class, "id");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        idUpdater = updater;
    }

    ChunkedPool.LinkedChunk<IntEntity> chunk;
    ChunkedPool.LinkedChunk<IntEntity> state;

    @SuppressWarnings("FieldMayBeFinal")
    private volatile int id;

    private Object[] backup;

    public IntEntity(int id, String name) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int setId(int id) {
        int prev = this.id;
        return idUpdater.compareAndSet(this, prev, id) ? id : prev;
    }

    @Override
    public Item getPrev() {
        return null;
    }

    @Override
    public void setPrev(Item prev) {
    }

    @Override
    public Item getNext() {
        return null;
    }

    @Override
    public void setNext(Item next) {
    }

    public DataComposition getComposition() {
        return (DataComposition) chunk.getTenant().getOwner();
    }

    public ChunkedPool.LinkedChunk<IntEntity> getChunk() {
        return chunk;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setChunk(ChunkedPool.LinkedChunk<? extends Item> chunk) {
        this.chunk = (ChunkedPool.LinkedChunk<IntEntity>) chunk;
    }

    public Object[] getComponentArray() {
        if (chunk == null || getArrayLength() == 0) {
            return null;
        }
        return chunk.getData(id);
    }

    public int getArrayLength() {
        return chunk.getDataLength();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Entity add(Object component) {
        synchronized (this) {
            if (!isEnabled()) {
                return this;
            }
            return getComposition().getRepository().addComponent(this, component);
        }
    }

    @Override
    public boolean remove(Object component) {
        synchronized (this) {
            if (!isEnabled()) {
                return false;
            }
            return getComposition().getRepository().removeComponentType(this, component.getClass());
        }
    }

    public boolean modify(CompositionRepository compositions, PreparedComposition.TargetComposition targetComposition, Object[] addedComponents) {
        synchronized (this) {
            if (!isEnabled()) {
                return false;
            }
            compositions.modifyComponents(this, targetComposition, addedComponents);
            return true;
        }
    }

    @Override
    public boolean removeType(Class<?> componentType) {
        synchronized (this) {
            if (!isEnabled()) {
                return false;
            }
            return getComposition().getRepository().removeComponentType(this, componentType);
        }
    }

    @Override
    public boolean has(Class<?> componentType) {
        int dataLength;
        if (chunk == null || (dataLength = chunk.getDataLength()) == 0) return false;
        if (dataLength == 1) {
            return chunk.getFromDataArray(getId()).getClass().equals(componentType);
        }
        DataComposition composition = (DataComposition) chunk.getTenant().getOwner();
        return composition.fetchComponentIndex(componentType) > -1;
    }

    @Override
    public boolean contains(Object component) {
        int dataLength;
        if (chunk == null || (dataLength = chunk.getDataLength()) == 0) return false;
        if (dataLength == 1) {
            return chunk.getFromDataArray(getId()).equals(component);
        }
        DataComposition composition = (DataComposition) chunk.getTenant().getOwner();
        int idx;
        return (idx = composition.fetchComponentIndex(component.getClass())) > -1
                && chunk.getData(getId())[idx].equals(component);
    }

    @Override
    public <S extends Enum<S>> Entity setState(S state) {
        return this;
    }

    @Override
    public boolean isEnabled() {
        return !isDeleted() && backup == null;
    }

    @Override
    public Entity setEnabled(boolean enabled) {
        if (enabled && !isEnabled() && !isDeleted()) {
            chunk.renew(this, backup);
            backup = null;
        } else if (!enabled && isEnabled()) {
            backup = chunk.disable(this);
        }
        return this;
    }

    boolean delete() {
        synchronized (this) {
            if (!isEnabled()) {
                return false;
            }
            chunk.getTenant().freeId(id);
            flagDetachedId();
            chunk = null;
            return true;
        }
    }

    @Override
    public boolean isDeleted() {
        return (id & ChunkedPool.IdSchema.DETACHED_BIT) == ChunkedPool.IdSchema.DETACHED_BIT;
    }

    void flagDetachedId() {
        setId(id | ChunkedPool.IdSchema.DETACHED_BIT);
    }

    @Override
    public String toString() {
        ChunkedPool.IdSchema idSchema = getComposition().getIdSchema();
        return "Entity={id=" + idSchema.idToString(id) + '}';
    }
}
