package org.github.seonwkim.springpekko

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringPekkoApplication

fun main(args: Array<String>) {
    runApplication<SpringPekkoApplication>(*args)
}
