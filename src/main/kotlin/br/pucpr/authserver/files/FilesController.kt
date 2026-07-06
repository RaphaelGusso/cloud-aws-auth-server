package br.pucpr.authserver.files

import br.pucpr.authserver.exception.NotFoundException
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/files")
class FilesController(@Qualifier("fileStorage") val storage: FileStorage) {
    @SecurityRequirement(name = "AuthServer")
    @GetMapping("/{filename}", produces = ["image/jpeg", "image/png"])
    suspend fun serve(@PathVariable filename: String): ResponseEntity<Resource> {
        val contentType = if (filename.endsWith("png"))
            MediaType.IMAGE_PNG else MediaType.IMAGE_JPEG
        return storage.load(filename)
            ?.let {
                ResponseEntity.ok()
                    .contentType(contentType)
                    .body(it)
            }
            ?: throw NotFoundException(filename)
    }
}