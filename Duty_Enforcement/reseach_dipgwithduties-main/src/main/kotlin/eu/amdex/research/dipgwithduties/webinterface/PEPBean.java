//package eu.amdex.research.dipgwithduties.webinterface;
//
//import eu.amdex.research.dipgwithduties.pepsystem.pep.PEPWebServer;
//import jakarta.enterprise.context.SessionScoped;
//import jakarta.faces.context.FacesContext;
//import jakarta.inject.Inject;
//import jakarta.inject.Named;
//import jakarta.servlet.http.HttpServletRequest;
//
//import java.io.Serializable;
//import java.util.HashMap;
//import java.util.Map;
//
//@Named("pepBean")
//@SessionScoped
//public class PEPBean implements Serializable {
//
//    private static final long serialVersionUID = 1L;
//
//    @Inject
//    private PEPWebServer pepWebServer;
//
//    private String member;
//    private String researcher;
//    private String contributionData;
//    private String requestData;
//    private String selectionData;
//    private String message;
//
//    private HttpServletRequest getRequest() {
//        return (HttpServletRequest) FacesContext.getCurrentInstance()
//                .getExternalContext().getRequest();
//    }
//
//    public String contributeData() {
//        try {
//            Map<String, Object> json = new HashMap<>();
//            json.put("data", contributionData);
//             result = pepWebServer.contributeData(getRequest(), member, json);
//            message = "Data contribution " + result;
//            return "success";
//        } catch (Exception e) {
//            message = "Error contributing data: " + e.getMessage();
//            return "error";
//        }
//    }
//
//    public String requestData() {
//        try {
//            Map<String, Object> json = new HashMap<>();
//            json.put("request", requestData);
//            String result = pepWebServer.requestData(getRequest(), researcher, json);
//            message = "Data request " + result;
//            return "success";
//        } catch (Exception e) {
//            message = "Error requesting data: " + e.getMessage();
//            return "error";
//        }
//    }
//
//    public String selectData() {
//        try {
//            Map<String, Object> json = new HashMap<>();
//            json.put("selection", selectionData);
//            String result = pepWebServer.selectData(getRequest(), json);
//            message = "Data selection " + result;
//            return "success";
//        } catch (Exception e) {
//            message = "Error selecting data: " + e.getMessage();
//            return "error";
//        }
//    }
//
//
//    // Getters and setters
//    public String getMember() {
//        return member;
//    }
//
//    public void setMember(String member) {
//        this.member = member;
//    }
//
//    public String getResearcher() {
//        return researcher;
//    }
//
//    public void setResearcher(String researcher) {
//        this.researcher = researcher;
//    }
//
//    public String getContributionData() {
//        return contributionData;
//    }
//
//    public void setContributionData(String contributionData) {
//        this.contributionData = contributionData;
//    }
//
//    public String getRequestData() {
//        return requestData;
//    }
//
//    public void setRequestData(String requestData) {
//        this.requestData = requestData;
//    }
//
//    public String getSelectionData() {
//        return selectionData;
//    }
//
//    public void setSelectionData(String selectionData) {
//        this.selectionData = selectionData;
//    }
//
//    public String getMessage() {
//        return message;
//    }
//
//    public void setMessage(String message) {
//        this.message = message;
//    }
//}