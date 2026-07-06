package br.pucpr.authserver.integration.sms

import br.pucpr.authserver.users.User
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.sns.AmazonSNSAsync
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.model.SetSMSAttributesRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SMSClient {
    private val sns: AmazonSNSAsync =
        AmazonSNSAsyncClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .withCredentials(EnvironmentVariableCredentialsProvider())
            .build()

    fun send(user: User, text: String, important: Boolean = false) {
        if (user.phone.isBlank()) return

        try {
            val type = if (important) "Transactional" else "Promotional"
            val attributes = SetSMSAttributesRequest().apply {
                attributes = mapOf("DefaultSMSType" to type)
            }
            sns.setSMSAttributes(attributes)

            sns.publishAsync(
                PublishRequest().apply {
                    phoneNumber = user.phone
                    message = text
                }
            )
            log.info("SMS sent to ${user.name}: $text")
        } catch (e: Exception) {
            log.error("Could not send to ${user.name}: $text")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SMSClient::class.java)
    }
}
