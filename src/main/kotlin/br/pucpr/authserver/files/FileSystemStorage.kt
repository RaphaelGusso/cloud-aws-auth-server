package br.pucpr.authserver.files

import br.pucpr.authserver.users.User
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.web.multipart.MultipartFile
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.isRegularFile

class FileSystemStorage : FileStorage {
    override fun save(user: User, path: String, file: MultipartFile) {
        val root = Paths.get(ROOT)
        val destinationFile = root.resolve(path)
            .normalize()
            .toAbsolutePath()
        Files.createDirectories(destinationFile.parent)
        file.inputStream.use {
            Files.copy(
                it, destinationFile,
                StandardCopyOption.REPLACE_EXISTING
            )
        }
    }

    override fun save(user: User, path: String, bytes: ByteArray, contentType: String) {
        val root = Paths.get(ROOT)
        val destinationFile = root.resolve(path)
            .normalize()
            .toAbsolutePath()
        Files.createDirectories(destinationFile.parent)
        Files.write(destinationFile, bytes)
    }

    override fun urlFor(name: String): String =
        "http://localhost:8080/api/files/" +
                URLEncoder.encode(
                    name.replace("/", "--"),
                    StandardCharsets.UTF_8
                )

    override fun load(path: String): Resource? =
        Paths.get(ROOT, path.replace("--", "/"))
            .takeIf { it.isRegularFile() }
            ?.let { UrlResource(it.toUri()) }

    companion object {
        const val ROOT = "./fs"
    }
}