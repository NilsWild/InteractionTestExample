package de.rwth.swc.banking

import de.rwth.swc.interact.rest.RestMessage
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.aggregator.ArgumentsAccessor
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException
import org.junit.jupiter.params.aggregator.ArgumentsAggregator


class TransferAggregator : ArgumentsAggregator {
    @Throws(ArgumentsAggregationException::class)
    override fun aggregateArguments(
        argumentsAccessor: ArgumentsAccessor,
        parameterContext: ParameterContext
    ): RestMessage<Transfer> {
        return RestMessage(
            mutableMapOf(), mutableSetOf(),
            Transfer(
                argumentsAccessor.getString(0),
                argumentsAccessor.getString(1),
                argumentsAccessor.getInteger(2)
            )
        )
    }
}