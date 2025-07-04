package ca.phon.app.session;

import ca.phon.session.Tier;

import java.awt.datatransfer.*;
import java.io.IOException;

/**
 * AWT transferrable for tier data
 */
public class TierTransferrable implements Transferable {

	public static DataFlavor FLAVOR = new DataFlavor(Tier.class, "TierTransferrable");

	private Tier<?> tier;

	public TierTransferrable(Tier<?> tier) {
		super();
		this.tier = tier;
	}

	public Tier<?> getTier() {
		return this.tier;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { FLAVOR, DataFlavor.stringFlavor };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (flavor == FLAVOR || flavor == DataFlavor.stringFlavor);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if(flavor == FLAVOR) {
			return this;
		} else if (flavor == DataFlavor.stringFlavor) {
			return tier.toString();
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

}
