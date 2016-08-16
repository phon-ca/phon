package ca.phon.app.log;

/**
 * Interface for exporting data from a buffer to another
 * format.
 *
 */
public interface BufferExporter<T> {

	/**
	 * Export buffer to annotation type.
	 * 
	 * @param buffer
	 * @return
	 * @throws BufferExportException
	 */
	public T exportBuffer(LogBuffer buffer)
		throws BufferExportException;
	
}
