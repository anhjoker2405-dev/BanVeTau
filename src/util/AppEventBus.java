package util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class AppEventBus {
    public static final String EVENT_BOOKING_CONFIRMED = "BOOKING_CONFIRMED";

    private static final AppEventBus INSTANCE = new AppEventBus();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private AppEventBus() {}

    public static AppEventBus getInstance() { return INSTANCE; }

    public void addListener(PropertyChangeListener l) { pcs.addPropertyChangeListener(l); }
    public void removeListener(PropertyChangeListener l) { pcs.removePropertyChangeListener(l); }

    // payload: maChuyenTau (String)
    public void fireBookingConfirmed(String maChuyenTau) {
        pcs.firePropertyChange(EVENT_BOOKING_CONFIRMED, null, maChuyenTau);
    }
}
