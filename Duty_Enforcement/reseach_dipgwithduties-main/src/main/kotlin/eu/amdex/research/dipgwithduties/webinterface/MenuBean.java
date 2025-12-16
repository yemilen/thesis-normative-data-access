package eu.amdex.research.dipgwithduties.webinterface;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.inject.Named;

import java.io.Serializable;

@Named("menu")
@SessionScoped
public class MenuBean implements Serializable {
    private HtmlPanelGroup menuItems;
    
    public HtmlPanelGroup getMenuItems() {
        return menuItems;
    }
    
    public void setMenuItems(HtmlPanelGroup menuItems) {
        this.menuItems = menuItems;
    }

    // You can add methods here to dynamically populate menu items
    // based on user roles or other conditions
}