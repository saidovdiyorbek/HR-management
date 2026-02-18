package org.example.organization

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class InternalApiFilter : OncePerRequestFilter() {


    private val INTERNAL_API_KEY = "MY_SUPER_SECRET_KEY_123"

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val headerKey = request.getHeader("X-INTERNAL-KEY")


        if (INTERNAL_API_KEY == headerKey) {


            val systemUser = UsernamePasswordAuthenticationToken(
                "SYSTEM_USER",
                null,
                listOf(SimpleGrantedAuthority("ROLE_INTERNAL_SYSTEM")) // Role beramiz
            )

            SecurityContextHolder.getContext().authentication = systemUser
            println("🔓 Internal API Key orqali kirish ruxsat etildi: ${request.requestURI}")
        }

        filterChain.doFilter(request, response)
    }
}