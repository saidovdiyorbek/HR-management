package uz.zero.notification


import com.fasterxml.jackson.databind.ObjectMapper
import feign.RequestInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.domain.AuditorAware
import org.springframework.http.HttpHeaders
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.web.SecurityFilterChain
import uz.zero.notification.dtos.UserInfoResponse
import java.util.*


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



@Configuration
@EnableMethodSecurity(prePostEnabled = true)
class ResourceServerConfig(
    private val objectMapper: ObjectMapper
) {

    @Bean
    fun resourceServerFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests {
                it.requestMatchers("/error").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { resourceServer ->
                resourceServer.jwt {
                    it.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }

        return http.build()
    }


    fun jwtAuthenticationConverter(): Converter<Jwt, JwtAuthenticationToken> {
        return Converter<Jwt, JwtAuthenticationToken> { source ->
            source
            val userDetailsJson = getHeader(USER_DETAILS_HEADER_KEY)?.decompress()
            val userDetails = userDetailsJson?.run { objectMapper.readValue(this, UserInfoResponse::class.java) }
            val username = userDetails?.username ?: username()
            val authorities = mutableListOf<SimpleGrantedAuthority>()
            if (userDetails != null) {
                authorities.add(SimpleGrantedAuthority("ROLE_${userDetails.role}"))
            }
            JwtAuthenticationToken(source, authorities, username).apply {
                details = userDetails
            }
        }
    }
}

@Configuration
class AuditConfig {

    @Bean
    fun auditorAware(): AuditorAware<String> {
        return AuditorAware {

            val authentication = SecurityContextHolder.getContext().authentication

            val currentAuditor = authentication?.name ?: "system"

            Optional.of(currentAuditor)
        }
    }
}
