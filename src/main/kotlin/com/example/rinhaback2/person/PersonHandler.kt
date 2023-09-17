package com.example.rinhaback2.person

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.util.UriComponentsBuilder

@Component
class PersonHandler(private val repository: PersonRepository,
                    private val redisTemplate: RedisTemplate<String, Person>) {

    suspend fun create(request: ServerRequest): ServerResponse {
        val person = request.awaitBody<Person>()

        if (person.nome.length > 100
            || person.apelido.length > 32
            || !person.nascimento.matches("\\d{4}-\\d{2}-\\d{2}".toRegex())
        ) {
            return unprocessableEntity().buildAndAwait()
        }

        val addedNickname = redisTemplate.opsForValue().get(person.apelido)

        if (addedNickname != null) {
            return unprocessableEntity().buildAndAwait()
        }

        val id = repository.save(person)

        val p = Person(
            id = id,
            nome = person.nome,
            apelido = person.apelido,
            nascimento = person.nascimento,
            stack = person.stack
        )

        redisTemplate.opsForValue().set(id, p)
        redisTemplate.opsForValue().set(p.apelido, p)

        val uri = UriComponentsBuilder.fromUriString("http://localhost:9999/pessoas/$id").build().toUri()
        return created(uri).buildAndAwait()
    }

    suspend fun findById(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id")

        val cachedPerson = redisTemplate.opsForValue().get(id)

        return if (cachedPerson != null) {
            ok().bodyValueAndAwait(cachedPerson)
        } else {
            badRequest().buildAndAwait()
        }
    }

    suspend fun findByCriteria(request: ServerRequest): ServerResponse {

        val criteria = request.queryParam("t").orElse("")

        if (criteria.isBlank()) {
            return badRequest().buildAndAwait()
        }
        return ok().bodyValueAndAwait(repository.findByCriteria(criteria))
    }

    suspend fun count(request: ServerRequest): ServerResponse {
        return ok().bodyValueAndAwait(repository.count())
    }

}