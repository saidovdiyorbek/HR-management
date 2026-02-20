package org.example.project

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.project.dtos.UserInfoResponse
import org.example.project.username
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
class ResourceServerConfig(
    private val objectMapper: ObjectMapper,
    private val internalApiFilter: InternalApiFilter
) {

    @Bean
    fun resourceServerFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterBefore(internalApiFilter, BearerTokenAuthenticationFilter::class.java)
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