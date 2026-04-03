package me.predefine.mixer.impl;

import me.predefine.mixer.api.IngredientContext;

public class IngredientContextImpl implements IngredientContext {
    private Object returnValue = null;
    private boolean canceled = false;

    @SuppressWarnings("unused")
    public boolean isCanceled()
    {
        return canceled;
    }

    public Object getReturn()
    {
        return returnValue;
    }

    @Override
    public void setReturn(Object value) {
        this.returnValue = value;
    }

    @Override
    public void cancel() {
        this.canceled = true;
    }
}
