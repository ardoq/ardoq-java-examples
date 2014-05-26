package com.ardoq;

import com.ardoq.model.*;
import com.ardoq.service.ComponentService;

import java.util.Arrays;
import java.util.List;

public class SimpleImport {

    private static final String token = System.getenv("ardoqToken");
    private static final String host = System.getenv("ardoqHost");

    public static void main(String[] args) {

        ArdoqClient client = new ArdoqClient(host, token);

        Model model = client.model().getModelByName("Application service");

        Workspace workspace = client.workspace().createWorkspace(new Workspace("demo-workspace", model.getId(), "Description"));

        ComponentService componentService = client.component();

        Component webshop = componentService.createComponent(new Component("Webshop", workspace.getId(), "Webshop description"));
        String serviceTypeId = model.getComponentTypeByName("Service");
        Component webShopCreateOrder = componentService.createComponent(new Component("createOrder", workspace.getId(), "Order from cart", serviceTypeId, webshop.getId()));

        Component erp = componentService.createComponent(new Component("ERP", workspace.getId(), ""));
        // With typeId = null the component service resolves the type
        Component erpCreateOrder = componentService.createComponent(new Component("createOrder", workspace.getId(), "", null, erp.getId()));
        //Create a Synchronous integration between the Webshop:createOrder and ERP:createOrder services
        Reference createOrderRef = new Reference(workspace.getId(), "Order from cart", webShopCreateOrder.getId(), erpCreateOrder.getId(), model.getReferenceTypeByName("Synchronous"));
        createOrderRef.setReturnValue("Created order");
        Reference reference = client.reference().createReference(createOrderRef);

        List<String> componentIds = Arrays.asList(webShopCreateOrder.getId(), erpCreateOrder.getId());
        List<String> referenceIds = Arrays.asList(reference.getId());
        client.tag().createTag(new Tag("Customer", workspace.getId(), "", componentIds, referenceIds));
    }
}
