package org.gridmind.backend.inventory.api

import org.gridmind.backend.category.application.CategoryService
import org.gridmind.backend.category.domain.Category
import org.gridmind.backend.inventory.application.ImageStorageService
import org.gridmind.backend.inventory.application.InventoryService
import org.gridmind.backend.inventory.domain.Item
import org.gridmind.backend.inventory.domain.StoredImage
import org.gridmind.backend.shared.config.SecurityConfig
import org.gridmind.backend.shared.error.ItemNotFoundException
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

@WebMvcTest(ItemController::class)
@Import(SecurityConfig::class)
class ItemControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var inventoryService: InventoryService

    @MockitoBean
    private lateinit var categoryService: CategoryService

    @MockitoBean
    private lateinit var imageStorageService: ImageStorageService

    @Test
    fun `create returns 201 with the created item`() {
        `when`(categoryService.findAll()).thenReturn(emptyList())
        `when`(inventoryService.create(Item(name = "ESP32-S3", quantity = 4)))
            .thenReturn(Item(id = 1L, name = "ESP32-S3", quantity = 4))

        mockMvc.perform(
            post("/api/inventory/items")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mapOf("name" to "ESP32-S3", "quantity" to 4))),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("ESP32-S3"))
            .andExpect(jsonPath("$.imageUrl").doesNotExist())
        verifyNoInteractions(imageStorageService)
    }

    @Test
    fun `create resolves a source image before creating the item`() {
        `when`(categoryService.findAll()).thenReturn(emptyList())
        `when`(imageStorageService.downloadAndStore("https://example.com/a.png", "DigiKey"))
            .thenReturn(StoredImage(id = 5L, checksum = "abc", contentType = "image/png", filePath = "/data/media/abc.png"))
        `when`(inventoryService.create(Item(name = "ESP32-S3", quantity = 4, imageId = 5L)))
            .thenReturn(Item(id = 1L, name = "ESP32-S3", quantity = 4, imageId = 5L))

        mockMvc.perform(
            post("/api/inventory/items")
                .contentType("application/json")
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "name" to "ESP32-S3",
                            "quantity" to 4,
                            "sourceImageUrl" to "https://example.com/a.png",
                            "sourceImageProvider" to "DigiKey",
                        ),
                    ),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.imageUrl").value("/api/media/5"))
    }

    @Test
    fun `create still succeeds when the image download fails`() {
        `when`(categoryService.findAll()).thenReturn(emptyList())
        `when`(imageStorageService.downloadAndStore(anyString(), anyString())).thenReturn(null)
        `when`(inventoryService.create(Item(name = "ESP32-S3", quantity = 4)))
            .thenReturn(Item(id = 1L, name = "ESP32-S3", quantity = 4))

        mockMvc.perform(
            post("/api/inventory/items")
                .contentType("application/json")
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "name" to "ESP32-S3",
                            "quantity" to 4,
                            "sourceImageUrl" to "https://example.com/broken.png",
                            "sourceImageProvider" to "DigiKey",
                        ),
                    ),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.imageUrl").doesNotExist())
    }

    @Test
    fun `create returns 400 when name is blank`() {
        mockMvc.perform(
            post("/api/inventory/items")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mapOf("name" to "", "quantity" to 4))),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fieldErrors.name").exists())
    }

    @Test
    fun `createItemWithPhoto returns 201 and attaches the uploaded image`() {
        val itemPart = MockMultipartFile(
            "item",
            "",
            "application/json",
            objectMapper.writeValueAsString(mapOf("name" to "D1 Mini", "quantity" to 1)).toByteArray(),
        )
        val imagePart = MockMultipartFile("image", "photo.jpg", "image/jpeg", byteArrayOf(1, 2, 3))
        `when`(categoryService.findAll()).thenReturn(emptyList())
        `when`(imageStorageService.storeUploaded(byteArrayOf(1, 2, 3), "image/jpeg", "Analyse IA locale"))
            .thenReturn(StoredImage(id = 3L, checksum = "abc", contentType = "image/jpeg", filePath = "/data/media/abc.jpg"))
        `when`(inventoryService.create(Item(name = "D1 Mini", quantity = 1, imageId = 3L)))
            .thenReturn(Item(id = 1L, name = "D1 Mini", quantity = 1, imageId = 3L))

        mockMvc.perform(multipart("/api/inventory/items/with-photo").file(itemPart).file(imagePart))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.imageUrl").value("/api/media/3"))
    }

    @Test
    fun `createItemWithPhoto succeeds without an image part`() {
        val itemPart = MockMultipartFile(
            "item",
            "",
            "application/json",
            objectMapper.writeValueAsString(mapOf("name" to "D1 Mini", "quantity" to 1)).toByteArray(),
        )
        `when`(categoryService.findAll()).thenReturn(emptyList())
        `when`(inventoryService.create(Item(name = "D1 Mini", quantity = 1)))
            .thenReturn(Item(id = 1L, name = "D1 Mini", quantity = 1))

        mockMvc.perform(multipart("/api/inventory/items/with-photo").file(itemPart))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.imageUrl").doesNotExist())
        verifyNoInteractions(imageStorageService)
    }

    @Test
    fun `createItemWithPhoto still succeeds when storing the uploaded image fails`() {
        val itemPart = MockMultipartFile(
            "item",
            "",
            "application/json",
            objectMapper.writeValueAsString(mapOf("name" to "D1 Mini", "quantity" to 1)).toByteArray(),
        )
        val imagePart = MockMultipartFile("image", "photo.jpg", "image/jpeg", byteArrayOf(1, 2, 3))
        `when`(categoryService.findAll()).thenReturn(emptyList())
        `when`(imageStorageService.storeUploaded(byteArrayOf(1, 2, 3), "image/jpeg", "Analyse IA locale")).thenReturn(null)
        `when`(inventoryService.create(Item(name = "D1 Mini", quantity = 1)))
            .thenReturn(Item(id = 1L, name = "D1 Mini", quantity = 1))

        mockMvc.perform(multipart("/api/inventory/items/with-photo").file(itemPart).file(imagePart))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.imageUrl").doesNotExist())
    }

    @Test
    fun `createItemWithPhoto returns 400 when name is blank`() {
        val itemPart = MockMultipartFile(
            "item",
            "",
            "application/json",
            objectMapper.writeValueAsString(mapOf("name" to "", "quantity" to 1)).toByteArray(),
        )

        mockMvc.perform(multipart("/api/inventory/items/with-photo").file(itemPart))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `getItem returns 200 with the item`() {
        `when`(categoryService.findAll()).thenReturn(listOf(Category(id = 2L, name = "Modules")))
        `when`(inventoryService.findById(1L))
            .thenReturn(Item(id = 1L, name = "ESP32-S3", quantity = 4, categoryId = 2L))

        mockMvc.perform(get("/api/inventory/items/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("ESP32-S3"))
            .andExpect(jsonPath("$.categoryName").value("Modules"))
    }

    @Test
    fun `getItem returns 404 for an unknown id`() {
        `when`(inventoryService.findById(99L)).thenThrow(ItemNotFoundException(99L))

        mockMvc.perform(get("/api/inventory/items/99"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `listItems passes search params through`() {
        `when`(categoryService.findAll()).thenReturn(emptyList())
        `when`(inventoryService.search("esp", 2L, "Espressif")).thenReturn(emptyList())

        mockMvc.perform(get("/api/inventory/items?search=esp&categoryId=2&manufacturer=Espressif"))
            .andExpect(status().isOk)
    }

    @Test
    fun `updateItem returns 200 with the updated item`() {
        `when`(categoryService.findAll()).thenReturn(emptyList())
        `when`(inventoryService.update(1L, Item(name = "Updated", quantity = 9)))
            .thenReturn(Item(id = 1L, name = "Updated", quantity = 9))

        mockMvc.perform(
            patch("/api/inventory/items/1")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mapOf("name" to "Updated", "quantity" to 9))),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Updated"))
    }

    @Test
    fun `deleteItem returns 204`() {
        mockMvc.perform(delete("/api/inventory/items/1"))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `deleteItem returns 404 for an unknown id`() {
        doThrow(ItemNotFoundException(99L)).`when`(inventoryService).delete(99L)

        mockMvc.perform(delete("/api/inventory/items/99"))
            .andExpect(status().isNotFound)
    }
}
