package ca.phon.worker;

@FunctionalInterface
public interface PhonTaskErrorHandler {

	public void handleError(Throwable err);

}
