package org.gridmind.backend.inventory.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface ItemRepository : JpaRepository<ItemEntity, Long>, JpaSpecificationExecutor<ItemEntity>
