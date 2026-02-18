package uz.zero.notification

import feign.RequestInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

@Configuration
class FeignOAuth2TokenConfig {


    private val INTERNAL_API_KEY = "MY_SUPER_SECRET_KEY_123"

    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->


            val authentication = SecurityContextHolder.getContext().authentication


            if (authentication != null && authentication is JwtAuthenticationToken) {
                template.header(HttpHeaders.AUTHORIZATION, "Bearer ${authentication.token.tokenValue}")
            }

            else {

                template.header("X-INTERNAL-KEY", INTERNAL_API_KEY)


                println("🔐 Feign: Kafka jarayoni. Maxfiy kalit ishlatildi.")
            }
        }
    }
}