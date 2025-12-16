package eu.amdex.research.dipgwithduties.webinterface;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Named;
import org.amdex.dossiers.dataclasses.MemberDossier;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

@Named("sessionBean")
@SessionScoped
public class SessionBean extends SessionBeanBase implements Serializable {

    private String memberId = "";
    private String message = "";
    private MemberDossier currentMember;
    private String username;

    public void updateMember(AjaxBehaviorEvent event) {
        try {
            if (memberId != null && !memberId.trim().isEmpty()) {
                currentMember = getMember(); // Store the member after validation
                message = "Member ID set to: " + memberId;
            } else {
                currentMember = null;
                message = "Please enter a valid Member ID";
            }
        } catch (Exception e) {
            currentMember = null;
            message = "Error setting member: " + e.getMessage();
        }
    }

    @NotNull
    public MemberDossier getMember() {
        if (currentMember != null) {
            return currentMember;
        }

        try {
            if (memberId == null || memberId.trim().isEmpty()) {
                throw new RuntimeException("Member ID is required");
            }

            username = memberId;

            // Extract organization identifier from email
            String email = memberId.toLowerCase();
            String organizationId;

            if (email.endsWith("@hospital1")) {
                organizationId = "Hospital 1";
            } else if (email.endsWith("@hospital2")) {
                organizationId = "Hospital 2";
            } else if (email.endsWith("@research")) {
                organizationId = "Research Institute";
            } else if (email.endsWith("@dipgboard")) {
                organizationId = "DIPG Board";
            } else {
                throw new RuntimeException("Invalid email domain. Must be from a recognized organization.");
            }

            // Get the organization's member dossier
            List<MemberDossier> memberDossiers = getDossierManager().getMembersByOrganisationId(organizationId);
            if (memberDossiers.isEmpty()) {
                throw new RuntimeException("Organization not found: " + organizationId);
            }

            MemberDossier orgDossier = memberDossiers.getFirst();

            currentMember = orgDossier;
            return currentMember;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error getting member: " + e.getMessage(), e);
        }
    }

    // Add getter for currentMember
    public MemberDossier getCurrentMember() {
        return currentMember;
    }

    // Getters and Setters
    public String getMemberId() {
        return memberId;
    }

    public String getUsername() {
        return username;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}