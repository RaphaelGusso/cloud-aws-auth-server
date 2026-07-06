package br.pucpr.authserver.users

import br.pucpr.authserver.exceptions.UnsupportedMediaTypeException
import br.pucpr.authserver.files.FileStorage
import br.pucpr.authserver.integration.avatar.AvatarResolverClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class AvatarService(
    @Qualifier("fileStorage") val storage: FileStorage,
    val resolverClient: AvatarResolverClient,
) {
    fun save(user: User, avatar: MultipartFile): String =
        try {
            val extension = when (avatar.contentType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                else -> throw UnsupportedMediaTypeException("jpeg", "png")
            }
            val path = "${user.id}/a_${user.id}.$extension"
            storage.save(user, "$ROOT/$path", avatar)
            "${user.id}/xl_a_${user.id}.png"
        } catch (exception: Error) {
            log.error("Unable to store avatar of user ${user.id}! Using default.", exception)
            DEFAULT_AVATAR
        }

    fun resolve(user: User): String {
        val image = resolverClient.resolve(user.email, user.name)
        if (image == null) {
            log.warn("Unable to resolve an avatar for user ${user.id}. Using default.")
            return DEFAULT_AVATAR
        }
        return try {
            val extension = extensionFor(image.contentType)
            val path = "${user.id}/a_${user.id}.$extension"
            storage.save(user, "$ROOT/$path", image.bytes, image.contentType)
            "${user.id}/xl_a_${user.id}.png"
        } catch (exception: Exception) {
            log.error("Unable to store resolved avatar of user ${user.id}! Using default.", exception)
            DEFAULT_AVATAR
        }
    }

    private fun extensionFor(contentType: String) = when (contentType) {
        "image/png" -> "png"
        "image/jpeg", "image/jpg" -> "jpg"
        else -> "png"
    }

    fun urlFor(path: String) = storage.urlFor("$ROOT/$path")

    companion object {
        const val ROOT = "avatars"
        const val DEFAULT_AVATAR = "default.jpg"
        private val log = LoggerFactory.getLogger(AvatarService::class.java)
    }
}