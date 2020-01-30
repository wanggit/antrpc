package io.github.wanggit.antrpc.commons.org.apache.commons.lang3.concurrent;

import org.apache.commons.lang3.concurrent.AbstractCircuitBreaker;
import org.apache.commons.lang3.concurrent.CircuitBreakingException;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple implementation of the <a
 * href="http://martinfowler.com/bliki/CircuitBreaker.html">Circuit Breaker</a> pattern that counts
 * specific events.
 *
 * <p>A <em>circuit breaker</em> can be used to protect an application against unreliable services
 * or unexpected load. A newly created {@code EventCountCircuitBreaker} object is initially in state
 * <em>closed</em> meaning that no problem has been detected. When the application encounters
 * specific events (like errors or service timeouts), it tells the circuit breaker to increment an
 * internal counter. If the number of events reported in a specific time interval exceeds a
 * configurable threshold, the circuit breaker changes into state <em>open</em>. This means that
 * there is a problem with the associated sub system; the application should no longer call it, but
 * give it some time to settle down. The circuit breaker can be configured to switch back to
 * <em>closed</em> state after a certain time frame if the number of events received goes below a
 * threshold.
 *
 * <p>When a {@code EventCountCircuitBreaker} object is constructed the following parameters can be
 * provided:
 *
 * <ul>
 *   <li>A threshold for the number of events that causes a state transition to <em>open</em> state.
 *       If more events are received in the configured check interval, the circuit breaker switches
 *       to <em>open</em> state.
 *   <li>The interval for checks whether the circuit breaker should open. So it is possible to
 *       specify something like "The circuit breaker should open if more than 10 errors are
 *       encountered in a minute."
 *   <li>The same parameters can be specified for automatically closing the circuit breaker again,
 *       as in "If the number of requests goes down to 100 per minute, the circuit breaker should
 *       close itself again". Depending on the use case, it may make sense to use a slightly lower
 *       threshold for closing the circuit breaker than for opening it to avoid continuously
 *       flipping when the number of events received is close to the threshold.
 * </ul>
 *
 * <p>This class supports the following typical use cases:
 *
 * <p><strong>Protecting against load peaks</strong>
 *
 * <p>Imagine you have a server which can handle a certain number of requests per minute. Suddenly,
 * the number of requests increases significantly - maybe because a connected partner system is
 * going mad or due to a denial of service attack. A {@code EventCountCircuitBreaker} can be
 * configured to stop the application from processing requests when a sudden peak load is detected
 * and to start request processing again when things calm down. The following code fragment shows a
 * typical example of such a scenario. Here the {@code EventCountCircuitBreaker} allows up to 1000
 * requests per minute before it interferes. When the load goes down again to 800 requests per
 * second it switches back to state <em>closed</em>:
 *
 * <pre>
 * EventCountCircuitBreaker breaker = new EventCountCircuitBreaker(1000, 1, TimeUnit.MINUTE, 800);
 * ...
 * public void handleRequest(Request request) {
 *     if (breaker.incrementAndCheckState()) {
 *         // actually handle this request
 *     } else {
 *         // do something else, e.g. send an error code
 *     }
 * }
 * </pre>
 *
 * <p><strong>Deal with an unreliable service</strong>
 *
 * <p>In this scenario, an application uses an external service which may fail from time to time. If
 * there are too many errors, the service is considered down and should not be called for a while.
 * This can be achieved using the following pattern - in this concrete example we accept up to 5
 * errors in 2 minutes; if this limit is reached, the service is given a rest time of 10 minutes:
 *
 * <pre>
 * EventCountCircuitBreaker breaker = new EventCountCircuitBreaker(5, 2, TimeUnit.MINUTE, 5, 10, TimeUnit.MINUTE);
 * ...
 * public void handleRequest(Request request) {
 *     if (breaker.checkState()) {
 *         try {
 *             service.doSomething();
 *         } catch (ServiceException ex) {
 *             breaker.incrementAndCheckState();
 *         }
 *     } else {
 *         // return an error code, use an alternative service, etc.
 *     }
 * }
 * </pre>
 *
 * <p>In addition to automatic state transitions, the state of a circuit breaker can be changed
 * manually using the methods {@link #open()} and {@link #close()}. It is also possible to register
 * {@code PropertyChangeListener} objects that get notified whenever a state transition occurs. This
 * is useful, for instance to directly react on a freshly detected error condition.
 *
 * <p><em>Implementation notes:</em>
 *
 * <ul>
 *   <li>This implementation uses non-blocking algorithms to update the internal counter and state.
 *       This should be pretty efficient if there is not too much contention.
 *   <li>This implementation is not intended to operate as a high-precision timer in very short
 *       check intervals. It is deliberately kept simple to avoid complex and time-consuming state
 *       checks. It should work well in time intervals from a few seconds up to minutes and longer.
 *       If the intervals become too short, there might be race conditions causing spurious state
 *       transitions.
 *   <li>The handling of check intervals is a bit simplistic. Therefore, there is no guarantee that
 *       the circuit breaker is triggered at a specific point in time; there may be some delay (less
 *       than a check interval).
 * </ul>
 *
 * @since 3.5
 */
public class EventCountCircuitBreaker extends AbstractCircuitBreaker<Integer> {

    /** A map for accessing the strategy objects for the different states. */
    private static final Map<State, StateStrategy> STRATEGY_MAP = createStrategyMap();

    /** Stores information about the current check interval. */
    private final AtomicReference<EventCountCircuitBreaker.CheckIntervalData> checkIntervalData;

    /** The threshold for opening the circuit breaker. */
    private final int openingThreshold;

    /** The time interval for opening the circuit breaker. */
    private final long openingInterval;

    /** The threshold for closing the circuit breaker. */
    private final int closingThreshold;

    /** The time interval for closing the circuit breaker. */
    private final long closingInterval;

    /** is near by threshlod 3s */
    private final long nearByTimeThreshlod = 3 * 1000 * 1000000L;

    /**
     * Creates a new instance of {@code EventCountCircuitBreaker} and initializes all properties for
     * opening and closing it based on threshold values for events occurring in specific intervals.
     *
     * @param openingThreshold the threshold for opening the circuit breaker; if this number of
     *     events is received in the time span determined by the opening interval, the circuit
     *     breaker is opened
     * @param openingInterval the interval for opening the circuit breaker
     * @param openingUnit the {@code TimeUnit} defining the opening interval
     * @param closingThreshold the threshold for closing the circuit breaker; if the number of
     *     events received in the time span determined by the closing interval goes below this
     *     threshold, the circuit breaker is closed again
     * @param closingInterval the interval for closing the circuit breaker
     * @param closingUnit the {@code TimeUnit} defining the closing interval
     */
    private EventCountCircuitBreaker(
            int openingThreshold,
            long openingInterval,
            TimeUnit openingUnit,
            int closingThreshold,
            long closingInterval,
            TimeUnit closingUnit) {
        super();
        checkIntervalData =
                new AtomicReference<>(new EventCountCircuitBreaker.CheckIntervalData(0, 0));
        this.openingThreshold = openingThreshold;
        this.openingInterval = openingUnit.toNanos(openingInterval);
        this.closingThreshold = closingThreshold;
        this.closingInterval = closingUnit.toNanos(closingInterval);
    }

    /**
     * Creates a new instance of {@code EventCountCircuitBreaker} with the same interval for opening
     * and closing checks.
     *
     * @param openingThreshold the threshold for opening the circuit breaker; if this number of
     *     events is received in the time span determined by the check interval, the circuit breaker
     *     is opened
     * @param checkInterval the check interval for opening or closing the circuit breaker
     * @param checkUnit the {@code TimeUnit} defining the check interval
     * @param closingThreshold the threshold for closing the circuit breaker; if the number of
     *     events received in the time span determined by the check interval goes below this
     *     threshold, the circuit breaker is closed again
     */
    private EventCountCircuitBreaker(
            int openingThreshold, long checkInterval, TimeUnit checkUnit, int closingThreshold) {
        this(
                openingThreshold,
                checkInterval,
                checkUnit,
                closingThreshold,
                checkInterval,
                checkUnit);
    }

    /**
     * Creates a new instance of {@code EventCountCircuitBreaker} which uses the same parameters for
     * opening and closing checks.
     *
     * @param threshold the threshold for changing the status of the circuit breaker; if the number
     *     of events received in a check interval is greater than this value, the circuit breaker is
     *     opened; if it is lower than this value, it is closed again
     * @param checkInterval the check interval for opening or closing the circuit breaker
     * @param checkUnit the {@code TimeUnit} defining the check interval
     */
    public EventCountCircuitBreaker(int threshold, long checkInterval, TimeUnit checkUnit) {
        this(threshold, checkInterval, checkUnit, threshold);
    }

    /**
     * Returns the threshold value for opening the circuit breaker. If this number of events is
     * received in the time span determined by the opening interval, the circuit breaker is opened.
     *
     * @return the opening threshold
     */
    int getOpeningThreshold() {
        return openingThreshold;
    }

    /**
     * Returns the interval (in nanoseconds) for checking for the opening threshold.
     *
     * @return the opening check interval
     */
    long getOpeningInterval() {
        return openingInterval;
    }

    /**
     * Returns the threshold value for closing the circuit breaker. If the number of events received
     * in the time span determined by the closing interval goes below this threshold, the circuit
     * breaker is closed again.
     *
     * @return the closing threshold
     */
    int getClosingThreshold() {
        return closingThreshold;
    }

    /**
     * Returns the interval (in nanoseconds) for checking for the closing threshold.
     *
     * @return the opening check interval
     */
    long getClosingInterval() {
        return closingInterval;
    }

    /**
     * {@inheritDoc} This implementation checks the internal event counter against the threshold
     * values and the check intervals. This may cause a state change of this circuit breaker.
     */
    @Override
    public boolean checkState() {
        return performStateCheck(0);
    }

    /** @return if System.nanoTime() - checkIntervalStart > 3s return true else false */
    public boolean checkNearBy() {
        CheckIntervalData currentCheckIntervalData = this.checkIntervalData.get();
        long checkIntervalStart = currentCheckIntervalData.getCheckIntervalStart();
        return (System.nanoTime() - checkIntervalStart) > nearByTimeThreshlod;
    }

    /**
     * get current check interval data
     *
     * @return
     */
    public AtomicReference<CheckIntervalData> getCheckIntervalData() {
        return this.checkIntervalData;
    }

    /** {@inheritDoc} */
    @Override
    public boolean incrementAndCheckState(Integer increment) {
        return performStateCheck(increment);
    }

    /**
     * Increments the monitored value by <strong>1</strong> and performs a check of the current
     * state of this circuit breaker. This method works like {@link #checkState()}, but the
     * monitored value is incremented before the state check is performed.
     *
     * @return <strong>true</strong> if the circuit breaker is now closed; <strong>false</strong>
     *     otherwise
     */
    public boolean incrementAndCheckState() {
        return incrementAndCheckState(1);
    }

    /**
     * {@inheritDoc} This circuit breaker may close itself again if the number of events received
     * during a check interval goes below the closing threshold. If this circuit breaker is already
     * open, this method has no effect, except that a new check interval is started.
     */
    @Override
    public void open() {
        super.open();
        checkIntervalData.set(new EventCountCircuitBreaker.CheckIntervalData(0, now()));
    }

    /**
     * {@inheritDoc} A new check interval is started. If too many events are received in this
     * interval, the circuit breaker changes again to state open. If this circuit breaker is already
     * closed, this method has no effect, except that a new check interval is started.
     */
    @Override
    public void close() {
        super.close();
        checkIntervalData.set(new EventCountCircuitBreaker.CheckIntervalData(0, now()));
    }

    /**
     * Actually checks the state of this circuit breaker and executes a state transition if
     * necessary.
     *
     * @param increment the increment for the internal counter
     * @return a flag whether the circuit breaker is now closed
     */
    private boolean performStateCheck(int increment) {
        EventCountCircuitBreaker.CheckIntervalData currentData;
        EventCountCircuitBreaker.CheckIntervalData nextData;
        State currentState;

        do {
            long time = now();
            currentState = state.get();
            currentData = checkIntervalData.get();
            nextData = nextCheckIntervalData(increment, currentData, currentState, time);
        } while (!updateCheckIntervalData(currentData, nextData));

        // This might cause a race condition if other changes happen in between!
        // Refer to the header comment!
        if (stateStrategy(currentState).isStateTransition(this, currentData, nextData)) {
            currentState = currentState.oppositeState();
            changeStateAndStartNewCheckInterval(currentState);
        }
        return !isOpen(currentState);
    }

    /**
     * Updates the {@code CheckIntervalData} object. The current data object is replaced by the one
     * modified by the last check. The return value indicates whether this was successful. If it is
     * <strong>false</strong>, another thread interfered, and the whole operation has to be redone.
     *
     * @param currentData the current check data object
     * @param nextData the replacing check data object
     * @return a flag whether the update was successful
     */
    private boolean updateCheckIntervalData(
            EventCountCircuitBreaker.CheckIntervalData currentData,
            EventCountCircuitBreaker.CheckIntervalData nextData) {
        return currentData == nextData || checkIntervalData.compareAndSet(currentData, nextData);
    }

    /**
     * Changes the state of this circuit breaker and also initializes a new {@code
     * CheckIntervalData} object.
     *
     * @param newState the new state to be set
     */
    private void changeStateAndStartNewCheckInterval(State newState) {
        changeState(newState);
        checkIntervalData.set(new EventCountCircuitBreaker.CheckIntervalData(0, now()));
    }

    /**
     * Calculates the next {@code CheckIntervalData} object based on the current data and the
     * current state. The next data object takes the counter increment and the current time into
     * account.
     *
     * @param increment the increment for the internal counter
     * @param currentData the current check data object
     * @param currentState the current state of the circuit breaker
     * @param time the current time
     * @return the updated {@code CheckIntervalData} object
     */
    private EventCountCircuitBreaker.CheckIntervalData nextCheckIntervalData(
            int increment,
            EventCountCircuitBreaker.CheckIntervalData currentData,
            State currentState,
            long time) {
        EventCountCircuitBreaker.CheckIntervalData nextData;
        if (stateStrategy(currentState).isCheckIntervalFinished(this, currentData, time)) {
            nextData = new EventCountCircuitBreaker.CheckIntervalData(increment, time);
        } else {
            nextData = currentData.increment(increment);
        }
        return nextData;
    }

    /**
     * Returns the current time in nanoseconds. This method is used to obtain the current time. This
     * is needed to calculate the check intervals correctly.
     *
     * @return the current time in nanoseconds
     */
    private long now() {
        return System.nanoTime();
    }

    /**
     * Returns the {@code StateStrategy} object responsible for the given state.
     *
     * @param state the state
     * @return the corresponding {@code StateStrategy}
     * @throws CircuitBreakingException if the strategy cannot be resolved
     */
    private static EventCountCircuitBreaker.StateStrategy stateStrategy(State state) {
        return STRATEGY_MAP.get(state);
    }

    /**
     * Creates the map with strategy objects. It allows access for a strategy for a given state.
     *
     * @return the strategy map
     */
    private static Map<State, StateStrategy> createStrategyMap() {
        Map<State, StateStrategy> map = new EnumMap<>(State.class);
        map.put(State.CLOSED, new EventCountCircuitBreaker.StateStrategyClosed());
        map.put(State.OPEN, new EventCountCircuitBreaker.StateStrategyOpen());
        return map;
    }

    /**
     * An internally used data class holding information about the checks performed by this class.
     * Basically, the number of received events and the start time of the current check interval are
     * stored.
     */
    public static class CheckIntervalData {
        /** The counter for events. */
        private final int eventCount;

        /** The start time of the current check interval. */
        private final long checkIntervalStart;

        /**
         * Creates a new instance of {@code CheckIntervalData}.
         *
         * @param count the current count value
         * @param intervalStart the start time of the check interval
         */
        CheckIntervalData(int count, long intervalStart) {
            eventCount = count;
            checkIntervalStart = intervalStart;
        }

        /**
         * Returns the event counter.
         *
         * @return the number of received events
         */
        public int getEventCount() {
            return eventCount;
        }

        /**
         * Returns the start time of the current check interval.
         *
         * @return the check interval start time
         */
        public long getCheckIntervalStart() {
            return checkIntervalStart;
        }

        /**
         * Returns a new instance of {@code CheckIntervalData} with the event counter incremented by
         * the given delta. If the delta is 0, this object is returned.
         *
         * @param delta the delta
         * @return the updated instance
         */
        EventCountCircuitBreaker.CheckIntervalData increment(int delta) {
            return (delta == 0)
                    ? this
                    : new EventCountCircuitBreaker.CheckIntervalData(
                            getEventCount() + delta, getCheckIntervalStart());
        }
    }

    /**
     * Internally used class for executing check logic based on the current state of the circuit
     * breaker. Having this logic extracted into special classes avoids complex if-then-else
     * cascades.
     */
    private abstract static class StateStrategy {
        /**
         * Returns a flag whether the end of the current check interval is reached.
         *
         * @param breaker the {@code CircuitBreaker}
         * @param currentData the current state object
         * @param now the current time
         * @return a flag whether the end of the current check interval is reached
         */
        boolean isCheckIntervalFinished(
                EventCountCircuitBreaker breaker,
                EventCountCircuitBreaker.CheckIntervalData currentData,
                long now) {
            return now - currentData.getCheckIntervalStart() > fetchCheckInterval(breaker);
        }

        /**
         * Checks whether the specified {@code CheckIntervalData} objects indicate that a state
         * transition should occur. Here the logic which checks for thresholds depending on the
         * current state is implemented.
         *
         * @param breaker the {@code CircuitBreaker}
         * @param currentData the current {@code CheckIntervalData} object
         * @param nextData the updated {@code CheckIntervalData} object
         * @return a flag whether a state transition should be performed
         */
        public abstract boolean isStateTransition(
                EventCountCircuitBreaker breaker,
                EventCountCircuitBreaker.CheckIntervalData currentData,
                EventCountCircuitBreaker.CheckIntervalData nextData);

        /**
         * Obtains the check interval to applied for the represented state from the given {@code
         * CircuitBreaker}.
         *
         * @param breaker the {@code CircuitBreaker}
         * @return the check interval to be applied
         */
        abstract long fetchCheckInterval(EventCountCircuitBreaker breaker);
    }

    /** A specialized {@code StateStrategy} implementation for the state closed. */
    private static class StateStrategyClosed extends StateStrategy {

        /** {@inheritDoc} */
        @Override
        public boolean isStateTransition(
                EventCountCircuitBreaker breaker,
                EventCountCircuitBreaker.CheckIntervalData currentData,
                EventCountCircuitBreaker.CheckIntervalData nextData) {
            return nextData.getEventCount() > breaker.getOpeningThreshold();
        }

        /** {@inheritDoc} */
        @Override
        protected long fetchCheckInterval(EventCountCircuitBreaker breaker) {
            return breaker.getOpeningInterval();
        }
    }

    /** A specialized {@code StateStrategy} implementation for the state open. */
    private static class StateStrategyOpen extends EventCountCircuitBreaker.StateStrategy {
        /** {@inheritDoc} */
        @Override
        public boolean isStateTransition(
                EventCountCircuitBreaker breaker,
                EventCountCircuitBreaker.CheckIntervalData currentData,
                EventCountCircuitBreaker.CheckIntervalData nextData) {
            return nextData.getCheckIntervalStart() != currentData.getCheckIntervalStart()
                    && currentData.getEventCount() < breaker.getClosingThreshold();
        }

        /** {@inheritDoc} */
        @Override
        protected long fetchCheckInterval(EventCountCircuitBreaker breaker) {
            return breaker.getClosingInterval();
        }
    }
}
