package org.acme.orderpicking.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.AnchorShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableGraphType;

/**
 * Represents a "stop" in a Trolley's path where an order item is to be picked.
 *
 * The TrolleyStep is the only PlanningEntity entity that will be changed during the problem solving, and the
 * {@link TrolleyStep#previousElement} is the only PlanningVariable defined.
 *
 * By using the CHAINED graph modelling strategy, combined with shadow variables {@link TrolleyStep#trolley} and
 * {@link TrolleyOrTrolleyStep#nextElement}, the Solver will create a structure like the following:
 *
 * Trolley1 <-> TrolleyStepA <-> TrolleyStepB <-> TrolleyStepC -> null
 *
 * Trolley2 <-> TrolleyStepD <-> TrolleyStepE -> null
 *
 * Where the initial element of each the chain, the Trolley, is known as the "anchor" and will always have a reference
 * to the next element, a TrolleyStep. (a null value, represents that this Trolley was not used yet or was left free).
 *
 * The intermediary elements, the TrolleySteps, when assigned will always have a reference to "anchor", the previous
 * element and the next element. (a null value on the next element, indicates that current step is currently the last
 * element).
 *
 */
@PlanningEntity
public class TrolleyStep extends TrolleyOrTrolleyStep {

    private OrderItem orderItem;

    /**
     * Planning variable: changes during planning, between score calculations.
     *
     * The valueRangeProviderRefs configured below indicates that:
     *
     * 1) The "anchor" elements, the Trolleys, for building the chains must be taken from the value range provider
     * with name TROLLEY_RANGE.
     *
     * 2) The intermediary elements, the TrolleySteps, for building the chains must be taken from the value range
     * provider with name TROLLEY_STEP_RANGE.
     *
     * @see OrderPickingSolution#getTrolleyList()
     * @see OrderPickingSolution#getTrolleyStepList()
     */
    @JsonIgnore
    @PlanningVariable(valueRangeProviderRefs = { TROLLEY_RANGE, TROLLEY_STEP_RANGE },
            graphType = PlanningVariableGraphType.CHAINED)
    private TrolleyOrTrolleyStep previousElement;

    /**
     * Shadow variable: Is automatically set by the Solver and facilitates that all the trolley steps can have a
     * reference to the chain "anchor", the Trolley.
     */
    @JsonIgnore
    @AnchorShadowVariable(sourceVariableName = PREVIOUS_ELEMENT)
    private Trolley trolley;

    public TrolleyStep() {
        //marshaling constructor.
    }

    public TrolleyStep(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    @Override
    public WarehouseLocation getLocation() {
        return orderItem.getProduct().getLocation();
    }

    public TrolleyOrTrolleyStep getPreviousElement() {
        return previousElement;
    }

    public void setPreviousElement(TrolleyOrTrolleyStep previousElement) {
        this.previousElement = previousElement;
    }

    public Trolley getTrolley() {
        return trolley;
    }

    public void setTrolley(Trolley trolley) {
        this.trolley = trolley;
    }

    public boolean isLast() {
        return nextElement == null;
    }

    /**
     * Helper method, facilitates UI building.
     */
    public String getTrolleyId() {
        return trolley != null ? trolley.getId() : null;
    }
}
