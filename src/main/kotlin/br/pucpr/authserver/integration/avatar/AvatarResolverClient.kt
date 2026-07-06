package br.pucpr.authserver.integration.avatar

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

data class AvatarImage(val bytes: ByteArray, val contentType: String)

@Component
class AvatarResolverClient {
    private val rest = RestTemplate()

    fun resolve(email: String, name: String): AvatarImage? =
        fetchGravatar(email) ?: fetchUiAvatars(name)

    private fun fetchGravatar(email: String): AvatarImage? = try {
        val hash = sha256(email.trim().lowercase())

        val url = "https://gravatar.com/avatar/$hash?d=404&s=$IMAGE_SIZE"
        val response = rest.getForEntity(url, ByteArray::class.java)
        val body = response.body
        if (body == null || body.isEmpty()) null
        else AvatarImage(body, response.headers.contentType?.toString() ?: "image/jpeg")
    } catch (notFound: HttpClientErrorException.NotFound) {
        log.info("User $email has no Gravatar. Falling back to ui-avatars.")
        null
    } catch (error: RestClientException) {
        log.error("Error while accessing Gravatar for $email", error)
        null
    }

    private fun fetchUiAvatars(name: String): AvatarImage? = try {
        val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8)
        val url = "https://ui-avatars.com/api/?name=$encodedName&size=$IMAGE_SIZE&format=png"
        val response = rest.getForEntity(url, ByteArray::class.java)
        response.body?.let { AvatarImage(it, "image/png") }
    } catch (error: RestClientException) {
        log.error("Error while accessing ui-avatars for $name", error)
        null
    }

    private fun sha256(text: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(text.toByteArray(StandardCharsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val IMAGE_SIZE = 512
        private val log = LoggerFactory.getLogger(AvatarResolverClient::class.java)
    }
}
