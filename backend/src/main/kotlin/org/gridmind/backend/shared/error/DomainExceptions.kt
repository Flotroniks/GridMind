package org.gridmind.backend.shared.error

/** Base type for "the thing you asked for doesn't exist" — mapped to 404 by [GlobalExceptionHandler]. */
abstract class NotFoundException(message: String) : RuntimeException(message)

class ItemNotFoundException(id: Long) : NotFoundException("Item with id $id was not found.")

class StorageLocationNotFoundException(id: Long) :
    NotFoundException("Storage location with id $id was not found.")

class StoredImageNotFoundException(id: Long) : NotFoundException("Stored image with id $id was not found.")

/** Thrown when an allocation or move would push an item's stock past its available quantity. Mapped to 409. */
class InsufficientStockException(message: String) : RuntimeException(message)

class InvalidStockAllocationException(message: String) : RuntimeException(message)

/** Thrown when deleting a storage location that still has children or stock in it. Mapped to 409. */
class LocationInUseException(message: String) : RuntimeException(message)
