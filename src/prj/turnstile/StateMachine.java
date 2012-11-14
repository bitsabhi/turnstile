package prj.turnstile;

import java.util.*;

public class StateMachine<STATE, EVENT>
{
    private STATE _current_state;
    private Map<STATE, Map<EVENT, STATE>> _stateTransitions;
    private STATE _default_error_state;
    private Set<StateChangeListener<STATE, EVENT>> _listeners;

    public StateMachine(STATE defaultErrorState)
    {
        _current_state = null;
        _default_error_state = defaultErrorState;
        _stateTransitions = new HashMap<STATE, Map<EVENT, STATE>>();
        _listeners = new HashSet<StateChangeListener<STATE, EVENT>>();
    }

    public void addSimilarTransitions(STATE state, STATE newState, List<EVENT> events)
    {
        if (state.equals(newState))
        {
            throw new IllegalArgumentException("start state is equal to end state");
        }
        for (EVENT e : events)
        {
            doAddTransition(state, e, newState);
        }
    }

    public void addSimilarTransitions(STATE state, List<EVENT> events)
    {
        for (EVENT e : events)
        {
            doAddTransition(state, e, state);
        }
    }

    public void addTransition(STATE state, EVENT event, STATE newState)
    {
        if (state.equals(newState))
        {
            throw new IllegalArgumentException("start state is equal to end state");
        }
        doAddTransition(state, event, newState);
    }

    private void doAddTransition(STATE state, EVENT event, STATE newState)
    {
        Map<EVENT, STATE> s = _stateTransitions.get(state);
        if (s == null)
        {
            HashMap<EVENT, STATE> m = new HashMap<EVENT, STATE>();
            m.put(event, newState);
            _stateTransitions.put(state, m);
        }
        else
        {
            STATE existingNewState = s.get(event);
            if (existingNewState == null)
            {
                s.put(event, newState);
            }
            else
            {
                throw new IllegalArgumentException("Another transition for this state and event already exists");
            }
        }
    }

    public void addTransition(STATE state, EVENT event)
    {
        doAddTransition(state, event, state);
    }

    public void start(STATE state)
    {
        _current_state = state;
    }

    public void process(EVENT event) throws IllegalStateException, InitializationException
    {
        STATE prev_state = _current_state;
        if (_current_state == null)
        {
            throw new InitializationException("Current state not setup via start()");
        }

        if (_stateTransitions == null || _stateTransitions.size() == 0)
        {
            throw new InitializationException("No states added via addTransition()");
        }

        Map<EVENT, STATE> allowedTransition = _stateTransitions.get(_current_state);
        if (isEventAllowedForTransition(event, allowedTransition))
        {
            _current_state = allowedTransition.get(event);
            fireStateChangedListeners(prev_state, event, _current_state);
        }
        else
        {
            _current_state = _default_error_state;
            throw new IllegalStateException("Event: " + event + " cannot be applied to state: " + prev_state);
        }
    }

    public boolean isAllowed(STATE forState, EVENT event)
    {
        Map<EVENT, STATE> m = _stateTransitions.get(forState);
        return isEventAllowedForTransition(event, m);
    }

    private boolean isEventAllowedForTransition(EVENT event, Map<EVENT, STATE> m)
    {
        if (m == null)
        {
            return false;
        }
        else
        {
            STATE newState = m.get(event);
            return newState != null;
        }
    }

    public STATE getCurrentState()
    {
        return _current_state;
    }

    public void addStateChangeListener(StateChangeListener<STATE, EVENT> listener)
    {
        _listeners.add(listener);
    }

    public void removeStateChangeListener(StateChangeListener<STATE, EVENT> listener)
    {
        _listeners.remove(listener);
    }

    public void fireStateChangedListeners(STATE oldState, EVENT cause, STATE newState)
    {
        for (StateChangeListener<STATE, EVENT> l : _listeners)
        {
            l.onChange(oldState, cause, newState);
        }
    }
}
