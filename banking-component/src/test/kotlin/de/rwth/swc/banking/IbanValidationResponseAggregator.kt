package de.rwth.swc.banking

import de.rwth.swc.interact.rest.RestMessage
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.aggregator.ArgumentsAccessor
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException
import org.junit.jupiter.params.aggregator.ArgumentsAggregator

class IbanValidationResponseAggregator : ArgumentsAggregator {
    @Throws(ArgumentsAggregationException::class)
    override fun aggregateArguments(
        argumentsAccessor: ArgumentsAccessor,
        parameterContext: ParameterContext
    ): RestMessage<IbanValidationResponse> {
        return when (argumentsAccessor.getBoolean(0)) {
            true -> RestMessage(
                mutableMapOf(), mutableSetOf(),
                IbanValidationResponse.Valid
            )

            false -> RestMessage(
                mutableMapOf(), mutableSetOf(),
                IbanValidationResponse.Invalid("Invalid IBAN")
            )

            else -> throw IllegalArgumentException("Invalid argument")

        }
    }
}
