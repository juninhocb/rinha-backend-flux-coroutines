package com.example.rinhaback2.person

import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.coRouter

@Component
class PersonRouter(private val personHandler: PersonHandler) {
    @Bean
    fun routes() = coRouter {
        GET("/pessoas/{id}", accept(APPLICATION_JSON), personHandler::findById)
        GET("/pessoas", accept(APPLICATION_JSON), personHandler::findByCriteria)
        GET("/contagem-pessoas", personHandler::count)
        POST("/pessoas", contentType(APPLICATION_JSON), personHandler::create)
    }

}