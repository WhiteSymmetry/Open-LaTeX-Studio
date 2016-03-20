/* 
 * Copyright (c) 2016 Sebastian Brudzinski
 * 
 * See the file LICENSE for copying permission.
 */
package latexstudio.editor.remote;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import latexstudio.editor.ApplicationLogger;
import latexstudio.editor.DropboxRevisionsTopComponent;
import latexstudio.editor.TopComponentFactory;
import latexstudio.editor.settings.ApplicationSettings;
import latexstudio.editor.settings.SettingsService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Remote",
        id = "latexstudio.editor.remote.DisconnectDropbox"
)
@ActionRegistration(
        displayName = "#CTL_DisconnectDropbox"
)
@ActionReference(path = "Menu/Remote", position = 3508)
@Messages("CTL_DisconnectDropbox=Disconnect from Dropbox")
public final class DisconnectDropbox implements ActionListener {

    private final DropboxRevisionsTopComponent drtc = new TopComponentFactory<DropboxRevisionsTopComponent>()
            .getTopComponent(DropboxRevisionsTopComponent.class.getSimpleName());

    private static final ApplicationLogger LOGGER = new ApplicationLogger("Cloud Support");

    @Override
    public void actionPerformed(ActionEvent e) {
        int currentCloudStatus = CloudStatus.getInstance().getStatus();
        CloudStatus.getInstance().setStatus(CloudStatus.STATUS_CONNECTING);
        
        DbxClient client = DbxUtil.getDbxClient();

        if (client == null) {
            LOGGER.log("Dropbox account already disconnected.");
            CloudStatus.getInstance().setStatus(CloudStatus.STATUS_DISCONNECTED);
            return;
        }

        String userToken = client.getAccessToken();

        if (userToken != null && !userToken.isEmpty()) {
            try {

                client.disableAccessToken();

                drtc.updateRevisionsList(null);
                drtc.close();

                ApplicationSettings appSettings = SettingsService.loadApplicationSettings();
                appSettings.setDropboxToken("");
                SettingsService.saveApplicationSettings(appSettings);
                LOGGER.log("Successfully disconnected from Dropbox account.");
            CloudStatus.getInstance().setStatus(CloudStatus.STATUS_DISCONNECTED);

            } catch (DbxException ex) {
                DbxUtil.showDbxAccessDeniedPrompt();
                CloudStatus.getInstance().setStatus(currentCloudStatus);
            }
        } else {
            LOGGER.log("Dropbox account already disconnected.");
            CloudStatus.getInstance().setStatus(CloudStatus.STATUS_DISCONNECTED);
        }
    }
}
