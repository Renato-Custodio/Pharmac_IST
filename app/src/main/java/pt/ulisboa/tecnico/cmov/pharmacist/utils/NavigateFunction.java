package pt.ulisboa.tecnico.cmov.pharmacist.utils;

@FunctionalInterface
public interface NavigateFunction<A> {
    void apply(A data, String origin);
}
