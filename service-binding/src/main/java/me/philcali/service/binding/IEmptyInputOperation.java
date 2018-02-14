package me.philcali.service.binding;

public interface IEmptyInputOperation<O> extends IOperation<Void, O> {
    @Override
    default O apply(final Void input) {
        return get();
    }

    O get();
}
