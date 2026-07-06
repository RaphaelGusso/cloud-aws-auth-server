package br.pucpr.authserver.files

import br.pucpr.authserver.users.User
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

interface FileStorage {
    fun save(user: User, path: String, file: MultipartFile)

    /** Saves raw bytes (e.g. downloaded from an external service) under the given path. */
    fun save(user: User, path: String, bytes: ByteArray, contentType: String)

    fun load(path: String): Resource?
    fun urlFor(name: String): String
}