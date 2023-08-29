package de.rwth.swc.banking

import de.rwth.swc.interact.rest.RestMessage
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.aggregator.ArgumentsAccessor
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException
import org.junit.jupiter.params.aggregator.ArgumentsAggregator

class BlacklistCheckResponseAggregator : ArgumentsAggregator {

    @Throws(ArgumentsAggregationException::class)
    override fun aggregateArguments(
        argumentsAccessor: ArgumentsAccessor,
        parameterContext: ParameterContext
    ): RestMessage<BlacklistCheckResponse> {
        return when (argumentsAccessor.getBoolean(0)) {
            true -> RestMessage(
                mutableMapOf(), mutableSetOf(),
                BlacklistCheckResponse.NotBlacklisted
            )

            false -> RestMessage(
                mutableMapOf(), mutableSetOf(),
                BlacklistCheckResponse.Blacklisted("IBAN is on blacklist")
            )

            else -> throw IllegalArgumentException("Invalid argument")
        }
    }

}
