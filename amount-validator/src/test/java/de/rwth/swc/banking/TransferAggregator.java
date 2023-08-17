package de.rwth.swc.banking;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

public class TransferAggregator implements ArgumentsAggregator {

    public TransferAggregator() {
    }

    @Override
    public Object aggregateArguments(ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext) throws ArgumentsAggregationException {
        return new Transfer(argumentsAccessor.getInteger(0), argumentsAccessor.getString(1), argumentsAccessor.getString(2));
    }
}