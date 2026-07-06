package br.pucpr.authserver.integration.quotes

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject

@Component
class QuoteClient {
    fun randomQuote(): Quote? =
        try {
            val client = RestTemplate()
            client.getForObject<Array<Quote>>(
                "https://zenquotes.io/api/random"
            )?.firstOrNull()
        } catch (error: RestClientException) {
            log.error("Problems accessing the quotes", error)
            null
        }

    companion object {
        private val log = LoggerFactory.getLogger(QuoteClient::class.java)
    }
}