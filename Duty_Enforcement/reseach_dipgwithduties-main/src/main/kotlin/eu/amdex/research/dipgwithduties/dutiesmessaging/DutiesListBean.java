package eu.amdex.research.dipgwithduties.dutiesmessaging;

import eu.amdex.research.dipgwithduties.webinterface.WebBeanBase;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("dutiesListBean")
@ViewScoped
public class DutiesListBean extends WebBeanBase implements Serializable {

    private static final long serialVersionUID = 1L;

    private DutiesMessageServer dutiesMessageServer = DutiesMessageServer.Companion.getInstance();

    private String viewType = "all"; // all, to, from
    private List<eu.amdex.research.dipgwithduties.dutiesmessaging.DutiesMessage> messages;
    private String statusMessage;

    public void loadMessages() {
        try {
            String memberId = sessionBean.getMemberId();

            messages = switch (viewType) {
                case "to" -> dutiesMessageServer.listDutyMessagesTo(memberId);
                case "from" -> dutiesMessageServer.listDutyMessagesFrom(memberId);
                default -> dutiesMessageServer.listDutyMessages();
            };
        } catch (Exception e) {
            statusMessage = "Error loading messages: " + e.getMessage();
            messages = new ArrayList<>();
        }
    }

    // Getters and Setters
    public String getViewType() {
        return viewType;
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
    }

    public List<DutiesMessage> getMessages() {
        return messages;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getTitle() {
        return switch (viewType) {
            case "to" -> "Duties Assigned To Me";
            case "from" -> "Duties Assigned By Me";
            default -> "All Duties";
        };
    }
}