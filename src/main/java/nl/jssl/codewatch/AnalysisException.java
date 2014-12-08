package nl.jssl.codewatch;

@SuppressWarnings("serial")
public class AnalysisException extends RuntimeException {

	public AnalysisException(Exception e) {
		super(e);
	}

}
