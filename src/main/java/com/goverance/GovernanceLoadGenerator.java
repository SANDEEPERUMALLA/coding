package com.goverance;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;
import java.util.stream.IntStream;

public class GovernanceLoadGenerator {

    public static void main(String[] args) {


        List<String> clientIds = List.of("c1", "c2");
        List<String> namespaces = List.of("ns1", "ns2", "ns3");
        List<String> subNamespaces = List.of("subn1", "subn2");
        List<String> tenants = List.of("ORG1", "ORG2", "OGG3");


        clientIds.forEach(clientId -> {
            namespaces.forEach(namespace -> {
                subNamespaces.forEach(subNamespace -> {
                    tenants.forEach(tenant -> {
                        int keyCount = 10;
                        IntStream.rangeClosed(1, keyCount).forEach(e -> {
                            String key = RandomStringUtils.randomAlphabetic(10);
                            String fullyQualifiedKey = String.join(":", clientId, namespace, subNamespace,
                                    tenant, key);

                        });
                    });
                });
            });
        });
    }


}