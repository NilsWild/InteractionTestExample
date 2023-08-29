package de.rwth.swc.banking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BankingComponent

fun main(args: Array<String>) {
    runApplication<BankingComponent>(*args)
}