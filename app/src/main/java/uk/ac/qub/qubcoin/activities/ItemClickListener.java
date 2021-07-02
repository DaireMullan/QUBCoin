package uk.ac.qub.qubcoin.activities;

import uk.ac.qub.qubcoin.models.Module;

public interface ItemClickListener {

    void startQrCodeActivity(Module module);

    void startCreateQrCodeActivity(String moduleId);

}
