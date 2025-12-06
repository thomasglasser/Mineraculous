package dev.thomasglasser.mineraculous.api.world.entity.ai.memory;

/// The state of an entity during the replication process.
public enum ReplicationState {
    /// Looking for somewhere to replicate
    LOOKING_FOR_RESTING_LOCATION,
    /// Actively replicating
    REPLICATING
}
