package eu.amdex.research.dipgwithduties.dutiesmessaging;

import eu.amdex.research.dipgwithduties.pepsystem.scenarios.ADutiesDataDossierTemplate;
import eu.amdex.research.dipgwithduties.webinterface.WebBeanBase;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.amdex.dossiers.dataclasses.DataDossier;
import org.amdex.dossiers.session.DefaultSession;

import java.io.Serializable;

@Named("dutiesMessageBean")
@ViewScoped
public class DutiesMessageBean extends WebBeanBase implements Serializable {

    private static final long serialVersionUID = 1L;

    private String uuid;
    private DutiesMessage message;
    private String statusMessage;

    private static DutiesMessageServer dutiesMessageServer = DutiesMessageServer.Companion.getInstance();

    public void loadMessage() {
        try {
            message = dutiesMessageServer.getDutyMessage(uuid);
            if (message == null) {
                statusMessage = "Message not found for UUID: " + uuid;
            }
        } catch (Exception e) {
            statusMessage = "Error loading message: " + e.getMessage();
        }
    }

    public String handleCompleted() {
        try {
            message = dutiesMessageServer.getDutyMessage(uuid);
            DataDossier dataDossier = getDossierManager().getDossierByUUID(message.getDutiesDossierUuid());

            DefaultSession session = new DefaultSession(getMember());
            session.set("username", getUsername());

            ADutiesDataDossierTemplate template = (ADutiesDataDossierTemplate) getTemplateLoader().loadTemplateWithDossier(dataDossier, session);
            template.dutyCompleted(message.getDutyUuid());

            // Sample implementation
            statusMessage = "Duty marked as completed for UUID: " + uuid;
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            statusMessage = "Error marking duty as completed: " + e.getMessage();
            return "error";
        }
    }

    public String handleViolated() {
        try {
            message = dutiesMessageServer.getDutyMessage(uuid);
            DataDossier dataDossier = getDossierManager().getDossierByUUID(message.getDutiesDossierUuid());

            DefaultSession session = new DefaultSession(getMember());
            session.set("username", getUsername());

            ADutiesDataDossierTemplate template = (ADutiesDataDossierTemplate) getTemplateLoader().loadTemplateWithDossier(dataDossier, session);
            template.dutyViolated(message.getDutyUuid());

            // Sample implementation
            statusMessage = "Duty marked as violated for UUID: " + uuid;
            return "success";
        } catch (Exception e) {
            statusMessage = "Error marking duty as violated: " + e.getMessage();
            return "error";
        }
    }

    // Getters and Setters
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public DutiesMessage getMessage() {
        return message;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
}