package org.example.project

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.project.dtos.CurrentUserOrganizationDto
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.web.servlet.AsyncHandlerInterceptor
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import org.springframework.web.servlet.support.RequestContextUtils
import java.util.Locale


@Configuration
@EnableWebMvc
class WebMvcConfig : WebMvcConfigurer {

    @Bean
    fun localeResolver() = SessionLocaleResolver().apply {
        setDefaultLocale(Locale("uz"))
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(object : AsyncHandlerInterceptor {
            override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

                request.getHeader("hl")?.let {
                    RequestContextUtils.getLocaleResolver(request)
                        ?.setLocale(request, response, Locale(it))
                }
                return true
            }
        })
    }
}


