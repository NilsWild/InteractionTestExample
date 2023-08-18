package de.rwth.swc.banking;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

public class StringAggregator implements ArgumentsAggregator {

    public StringAggregator() {
    }

    @Override
    public Object aggregateArguments(ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext) throws ArgumentsAggregationException {
        return new de.rwth.swc.interact.rest.RestMessage<>(
                java.util.Map.of(),
                java.util.Set.of(),
                argumentsAccessor.getString(0)
        );
    }
}
