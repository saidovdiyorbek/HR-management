package org.example.project

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

//package org.example.project
//
//import jakarta.servlet.http.HttpServletRequest
//import jakarta.servlet.http.HttpServletResponse
//import org.example.project.dtos.CurrentUserOrganizationDto
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
//import org.springframework.cloud.openfeign.EnableFeignClients
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.context.annotation.Primary
//import org.springframework.web.servlet.AsyncHandlerInterceptor
//import org.springframework.web.servlet.config.annotation.EnableWebMvc
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
//import org.springframework.web.servlet.i18n.SessionLocaleResolver
//import org.springframework.web.servlet.support.RequestContextUtils
//import java.util.Locale
//
//
//@Configuration
//@EnableWebMvc
//class WebMvcConfig : WebMvcConfigurer {
//
//    @Bean
//    fun localeResolver() = SessionLocaleResolver().apply {
//        setDefaultLocale(Locale("uz"))
//    }
//
//    override fun addInterceptors(registry: InterceptorRegistry) {
//        registry.addInterceptor(object : AsyncHandlerInterceptor {
//            override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
//
//                request.getHeader("hl")?.let {
//                    RequestContextUtils.getLocaleResolver(request)
//                        ?.setLocale(request, response, Locale(it))
//                }
//                return true
//            }
//        })
//    }
//}
//
//

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