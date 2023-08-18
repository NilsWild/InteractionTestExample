package de.rwth.swc.banking;

import de.rwth.swc.interact.rest.RestMessage;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

import java.util.Map;
import java.util.Set;

public class TransferAggregator implements ArgumentsAggregator {

    public TransferAggregator() {
    }

    @Override
    public Object aggregateArguments(ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext) throws ArgumentsAggregationException {
        return new RestMessage<>(
                Map.of(),
                Set.of(),
                new Transfer(
                        argumentsAccessor.getInteger(0),
                        argumentsAccessor.getString(1),
                        argumentsAccessor.getString(2)
                )
        );
    }
}