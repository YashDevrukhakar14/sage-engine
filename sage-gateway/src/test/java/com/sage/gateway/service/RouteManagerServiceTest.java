package com.sage.gateway.service;

import com.sage.gateway.routing.RouteDefinition;
import com.sage.gateway.routing.RouteNode;
import com.sage.gateway.routing.RouteRegistry;
import com.sage.gateway.routing.RouteResolver;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RouteManagerServiceTest {

    @Test
    void shouldBuildTreeAndTriggerHotSwap() {
        // Arrange: Mock the resolver so we don't test it twice
        RouteResolver mockResolver = mock(RouteResolver.class);
        RouteManagerService service = new RouteManagerService(mockResolver, new RouteRegistry(List.of()));

        RouteDefinition newRoute = new RouteDefinition("test-route-id","/fast-track/{id}", "https://httpbin.org", null);

        //  Act: Add the route
        service.addRoute(newRoute);

        // Assert: Capture the new Root Node right as it gets hot-swapped
        ArgumentCaptor<RouteNode> nodeCaptor = ArgumentCaptor.forClass(RouteNode.class);
        verify(mockResolver, times(1)).updateTree(nodeCaptor.capture());

        RouteNode newRoot = nodeCaptor.getValue();
        assertNotNull(newRoot, "The hot-swapped tree should not be null");

        // Verify the Radix Tree structure was built perfectly: root -> fast-track -> {id}
        assertTrue(newRoot.exactChildren.containsKey("fast-track"), "Should create static node");
        RouteNode fastTrackNode = newRoot.exactChildren.get("fast-track");

        assertNotNull(fastTrackNode.variableChild, "Should create variable child node");
        assertEquals("id", fastTrackNode.variableName, "Should extract variable name");
        assertEquals(newRoute, fastTrackNode.variableChild.route, "Leaf node should hold the route definition");
    }

    @Test
    void addingRouteKeepsBootstrappedRoutes() {
        RouteResolver mockResolver = mock(RouteResolver.class);
        RouteDefinition products = new RouteDefinition("shop-products-route", "/products", "http://upstream", Map.of());
        RouteManagerService service = new RouteManagerService(mockResolver, new RouteRegistry(List.of(products)));

        RouteDefinition added = new RouteDefinition("test-route-id", "/fast-track/{id}", "https://httpbin.org", null);
        service.addRoute(added);

        ArgumentCaptor<RouteNode> nodeCaptor = ArgumentCaptor.forClass(RouteNode.class);
        verify(mockResolver).updateTree(nodeCaptor.capture());
        RouteNode root = nodeCaptor.getValue();

        assertEquals(products, root.exactChildren.get("products").route, "Bootstrapped route must survive a hot-add");
        assertEquals(added, root.exactChildren.get("fast-track").variableChild.route, "New route must be added");
    }
}
