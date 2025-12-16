package eu.amdex.research.dipgwithduties.pepsystem.beans;

import eu.amdex.research.dipgwithduties.pepsystem.scenarios.ExampleDutiesTemplate;
import eu.amdex.research.dipgwithduties.webinterface.WebBeanBase;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import org.amdex.dossiers.dataclasses.MemberDossier;
import org.amdex.dossiers.session.DefaultSession;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Named("contributeDataBean")
@SessionScoped
public class ContributeDataBean extends WebBeanBase implements Serializable {
    private String contributionData = "{\"data\" : \"data\"}";
    private String message;

    public String contributeData() {
        try {
            MemberDossier owner = getMember();

            Map<String, Object> json = new HashMap<>();
            json.put("data", contributionData);

            DefaultSession session = new DefaultSession(getMember());
            session.set("username", getUsername());

            String uuid = UUID.randomUUID().toString();
            ExampleDutiesTemplate exampleDutiesTemplate =
                    (ExampleDutiesTemplate) getTemplateLoader().createNewDataDossier(
                    owner,
                    ExampleDutiesTemplate.class.getName(),
                    "ContributeData",
                    json, session, uuid, uuid);

//            DataContributionLifeCycle dataContributionLifeCycle =
//                    (DataContributionLifeCycle) getTemplateLoader().createNewDataDossier(
//                            owner,
//                            DataContributionLifeCycle.class.getName(),
//                            "ContributeData",
//                            json, session);
//
//            dataContributionLifeCycle.process();

            message = "Data contribution SUCCESS";
            clearForm();
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            message = "Error contributing data: " + e.getMessage();
            return "error";
        }
    }

    private void clearForm() {
        this.contributionData = null;
    }

    // Getters and Setters
    public String getContributionData() {
        return contributionData;
    }

    public void setContributionData(String contributionData) {
        this.contributionData = contributionData;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}