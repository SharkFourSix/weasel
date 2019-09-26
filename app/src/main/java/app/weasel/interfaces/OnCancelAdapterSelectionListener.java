package app.weasel.interfaces;

public interface OnCancelAdapterSelectionListener {
    /**
     * <p>Cancel a list adapter selection</p>
     *
     * @return <p>true if the target fragment contained a list adapter
     * that has just had its selection mode cancelled.</p>
     * <p>Returning false implies the adapter was never in selection mode or the
     * fragment has no list adapter.</p>
     */
    boolean onCancelAdapterSelection();
}
