package ca.phon.ui.toast;

/**
 * Collection of static methods used to create {@link Toast} objects.
 * @author ghedlund
 *
 */
public class ToastFactory {
	
	/**
	 * Make a new toast with default options and given message.
	 * 
	 * @param message
	 * @return the new toast
	 */
	public static Toast makeToast(String message) {
		final Toast retVal = new Toast();
		retVal.setMessage(message);
		return retVal;
	}

}
