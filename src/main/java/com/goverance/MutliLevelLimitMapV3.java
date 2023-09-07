package com.goverance;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class MutliLevelLimitMapV3 {

    public static void main(String[] args) {
        List<String> clientIds = List.of("c1", "c2");
//        List<String> clientIds = List.of("c1");
        List<String> namespaces = List.of("ns1", "ns2", "ns3");
//        List<String> namespaces = List.of("ns1", "ns2", "ns3", "ns4", "ns5");
        List<String> subNamespaces = List.of("subns1", "subns2");
        List<String> tenants = List.of("ORG1", "ORG2", "ORG3");

        NodeLevelLimitData nodeLevelLimitData = new NodeLevelLimitData("");

        System.out.println("List of tenants !!!");

        Map<String, LimitData> subnamespaceMap = new ConcurrentHashMap<>();
        Map<String, LimitData> namespaceMap = new ConcurrentHashMap<>();

        clientIds.forEach(clientId -> {
            namespaces.forEach(namespace -> {
                subNamespaces.forEach(subNamespace -> {
                    tenants.forEach(tenant -> {
                        System.out.println(String.join(":", clientId, namespace, subNamespace, tenant));
                        int keyCount = 10;

                        IntStream.rangeClosed(1, keyCount).forEach(e -> {

                            String key = RandomStringUtils.randomAlphabetic(10);
                            String fullyQualifiedKey = String.join(":", clientId, namespace, subNamespace, tenant, key);
                            String[] keyParts = fullyQualifiedKey.split(":", -1);
                            String clientName = keyParts[0];
                            String namespaceName = keyParts[1];
                            String subnamespaceName = keyParts[2];
                            String tenantName = keyParts[3];

                            LimitData clientLevelLimitData = nodeLevelLimitData.getChildrenMap().computeIfAbsent(clientName, k -> new ClientLevelLimitData(clientName));
                            clientLevelLimitData.getKeysForWhichLimitIsApplicable().add(namespaceName);

                            LimitData namespaceLevelLimitData = clientLevelLimitData.getChildrenMap().computeIfAbsent(namespaceName, k -> new NamespaceLevelLimitData(namespaceName));
                            namespaceLevelLimitData.getKeysForWhichLimitIsApplicable().add(subnamespaceName);

                            LimitData subNamespaceLevelLimitData = namespaceLevelLimitData.getChildrenMap().computeIfAbsent(subnamespaceName, k -> new SubNamespaceLevelLimitData(subnamespaceName));
                            subNamespaceLevelLimitData.getKeysForWhichLimitIsApplicable().add(tenantName);

                            subNamespaceLevelLimitData.getChildrenMap().computeIfAbsent(tenantName, k -> new TenantLevelLimitData(tenantName));

                        });
                    });
                });
            });
        });

        System.out.println();
        System.out.println();
        System.out.println();

        List<Pair<String, Long>> limits = List.of(
//                Pair.of("c1", 200L),
//                Pair.of("c1:ns1", 50L),
//                Pair.of("c1:ns1:subns1", 20L),
//                Pair.of("c1:ns1:subns1:ORG1", 10L),
//                Pair.of("c1:ns2", 60L),
//                Pair.of("c1:ns2:subns2", 40L),
//                Pair.of("c1:ns2:subns2:ORG2", 10L),
//                Pair.of("c1:ns2:subns2:ORG1", 10L)
                Pair.of("c1", 100L),
                Pair.of("c1:ns2", 20L),
                Pair.of("c1:ns1", 20L)
        );

        limits.forEach(limit -> {
            Long limitValue = limit.getRight();
            String limitStr = limit.getLeft();
            String[] limitParts = limitStr.split(":", -1);
            updateLimitsData(null, nodeLevelLimitData, limitParts, -1, limitValue);
        });

        updateLimitsDataBasedOnChildLimits(nodeLevelLimitData);

    }

    private static void updateLimitsDataBasedOnChildLimits(LimitData limitData) {

        if(LimitLevel.TENANT == limitData.getLimitLevel()) {
            return;
        }

        AtomicLong limitToBeReduced = new AtomicLong(0);
        limitData.getChildrenMap().forEach((k, v) -> {
            if (!limitData.getKeysForWhichLimitIsApplicable().contains(k)) {
                limitToBeReduced.addAndGet(limitData.getChildrenMap().get(k).getLimit());
            }
        });
        limitData.setLimit(limitData.getLimit() - limitToBeReduced.get());
        limitData.getChildrenMap().forEach((k, v) -> {
            updateLimitsDataBasedOnChildLimits(v);
        });
    }

    private static void applyLimits(TopLevelLimitData topLevelLimitData) {
        topLevelLimitData.getClientMap().forEach((k, v) -> {

            applyLimits(v);
        });
    }

    private static void applyLimits(ClientLevelLimitData clientLevelLimitData) {
        clientLevelLimitData.getChildrenMap().forEach((k, v) -> {
            applyLimits((NamespaceLevelLimitData) v, clientLevelLimitData.getClientName());
        });
        long limit = clientLevelLimitData.getLimit();
        if (limit != 0) {
            List<String> tenants = new ArrayList<>();
            clientLevelLimitData.getChildrenMap().forEach((k, v) -> {
                if (!clientLevelLimitData.getNamespacesForWhichLimitIsApplicable().contains(k)) {
                    return;
                }
                v.getChildrenMap().forEach((k1, v1) -> {
                    if (!v.getKeysForWhichLimitIsApplicable().contains(k1)) {
                        return;
                    }
                    v1.getChildrenMap().forEach((k2, v2) -> {
                        if (!v1.getKeysForWhichLimitIsApplicable().contains(k2)) {
                            return;
                        }
                        tenants.add(String.join(":", clientLevelLimitData.getClientName(), k, k1, k2));
                    });
                });
            });
            System.out.println(String.format("Tenant list for eviction for namespace \"%s\": %s, limit: %d", clientLevelLimitData.getClientName(),
                    tenants, limit));
        }

    }

    private static void applyLimits(NamespaceLevelLimitData namespaceLevelLimitData, String clientName) {
        namespaceLevelLimitData.getChildrenMap().forEach((k, v) -> {
            applyLimits((SubNamespaceLevelLimitData) v, clientName, namespaceLevelLimitData.getNamespace());
        });
        long limit = namespaceLevelLimitData.getLimit();
        List<String> tenants = new ArrayList<>();
        if (limit != 0) {
            namespaceLevelLimitData.getChildrenMap().forEach((k, v) -> {
                if (!namespaceLevelLimitData.getSubNamespacesForWhichLimitIsApplicable().contains(k)) {
                    return;
                }
                v.getChildrenMap().forEach((k1, v1) -> {
                    if (!v.getKeysForWhichLimitIsApplicable().contains(k1)) {
                        return;
                    }
                    tenants.add(String.join(":", clientName, namespaceLevelLimitData.getNamespace(), k, k1));
                });
            });
            System.out.println(String.format("Tenant list for eviction for subnamespace \"%s\": %s, limit: %d", String.join(":", clientName, namespaceLevelLimitData.getNamespace()),
                    tenants, limit));

        }
    }

    private static void applyLimits(SubNamespaceLevelLimitData subNamespaceLevelLimitData, String clientName, String namespace) {

        subNamespaceLevelLimitData.getChildrenMap().forEach((k, v) -> {
            applyLimits((TenantLevelLimitData) v, clientName, namespace, subNamespaceLevelLimitData.getSubNamespace());
        });

        List<String> tenants = new ArrayList<>();
        long limit = subNamespaceLevelLimitData.getLimit();
        if (limit != 0) {
            subNamespaceLevelLimitData.getChildrenMap().forEach((k, v) -> {
                if (!subNamespaceLevelLimitData.getTenantsForWhichLimitIsApplicable().contains(k)) {
                    return;
                }
                tenants.add(String.join(":", clientName, namespace, subNamespaceLevelLimitData.getSubNamespace(), k));
            });
            System.out.println(String.format("Tenant list for eviction for subnamespace \"%s\": %s, limit : %d", String.join(":", clientName, namespace, subNamespaceLevelLimitData.getSubNamespace()),
                    tenants, limit));
        }

    }

    private static void applyLimits(TenantLevelLimitData orgLevelLimitData, String clientName, String namespace, String subNamespace) {

        long limit = orgLevelLimitData.getLimit();
        if (limit != 0) {
            List<String> tenants = new ArrayList<>();
            tenants.add(String.join(":", clientName, namespace, subNamespace, orgLevelLimitData.getOrgName()));
            System.out.println(String.format("Tenant list for eviction for tenant \"%s\": %s, limit : %d", String.join(":", clientName, namespace, subNamespace, orgLevelLimitData.getOrgName()),
                    tenants, limit));
        }

    }

//    public static void updateLimitsData(NodeLevelLimitData topLevelLimitData, String[] limitParts, Long limitValue) {
//
//        if (limitParts.length == 1) {
//            updateClientLevelData(topLevelLimitData, limitParts, limitValue);
//        } else if (limitParts.length == 2) {
//            updateNamespaceLevelData(topLevelLimitData, limitParts, limitValue);
//        } else if (limitParts.length == 3) {
//            updateSubNamespaceLevelData(topLevelLimitData, limitParts, limitValue);
//        } else if (limitParts.length == 4) {
//            updateTenantLevelData(topLevelLimitData, limitParts, limitValue);
//        }
//    }

    public static void updateLimitsData(LimitData parentLimitData, LimitData limitData, String[] keysTokensForLimit, int index, Long limitValue) {

        if (LimitLevel.TENANT == limitData.getLimitLevel()) {
            limitData.setLimit(limitValue);
            return;
        }

        String key = keysTokensForLimit[index + 1];
        if (key.equals("*")) {
            limitData.setLimit(limitValue);
            parentLimitData.getKeysForWhichLimitIsApplicable().remove(key);
        } else {
            LimitData ld = limitData.getChildrenMap().get(key);
            if (ld == null) {
                return;
            }
            updateLimitsData(limitData, ld, keysTokensForLimit, index, limitValue);
        }

    }

    public static void updateClientLevelData(NodeLevelLimitData nodeLevelLimitData, String[] keyParts, long limit) {
        String client = keyParts[0];
        ClientLevelLimitData clientLevelLimitData = (ClientLevelLimitData) nodeLevelLimitData.getChildrenMap().get(client);
        clientLevelLimitData.setLimit(limit);
        nodeLevelLimitData.getClientsForWhichLimitIsApplicable().remove(client);
    }

    public static void updateNamespaceLevelData(NodeLevelLimitData nodeLevelLimitData, String[] keyParts, long limit) {
        String client = keyParts[0];
        String ns = keyParts[1];
        ClientLevelLimitData clientLevelLimitData = (ClientLevelLimitData) nodeLevelLimitData.getChildrenMap().get(client);
        clientLevelLimitData.getNamespacesForWhichLimitIsApplicable().remove(ns);
        NamespaceLevelLimitData namespaceLevelLimitData = (NamespaceLevelLimitData) clientLevelLimitData.getChildrenMap().get(ns);
        namespaceLevelLimitData.setLimit(limit);
    }

    public static void updateSubNamespaceLevelData(LimitData topLevelLimitData, String[] keyParts, long limit) {
        String client = keyParts[0];
        String ns = keyParts[1];
        String subNs = keyParts[2];
        ClientLevelLimitData clientLevelLimitData = (ClientLevelLimitData)topLevelLimitData.getChildrenMap().get(client);
        NamespaceLevelLimitData namespaceLevelLimitData = (NamespaceLevelLimitData) clientLevelLimitData.getChildrenMap().get(ns);
        namespaceLevelLimitData.getSubNamespacesForWhichLimitIsApplicable().remove(subNs);
        SubNamespaceLevelLimitData subNamespaceLevelLimitData = (SubNamespaceLevelLimitData) namespaceLevelLimitData.getChildrenMap().get(subNs);
        subNamespaceLevelLimitData.setLimit(limit);
    }

    public static void updateTenantLevelData(TopLevelLimitData topLevelLimitData, String[] keyParts, long limit) {
        String client = keyParts[0];
        String ns = keyParts[1];
        String subNs = keyParts[2];
        String tenant = keyParts[3];
        ClientLevelLimitData clientLevelLimitData = topLevelLimitData.getClientMap().get(client);
        NamespaceLevelLimitData namespaceLevelLimitData = (NamespaceLevelLimitData) clientLevelLimitData.getChildrenMap().get(ns);
        SubNamespaceLevelLimitData subNamespaceLevelLimitData = (SubNamespaceLevelLimitData) namespaceLevelLimitData.getChildrenMap().get(subNs);
        subNamespaceLevelLimitData.getTenantsForWhichLimitIsApplicable().remove(tenant);
        TenantLevelLimitData orgLevelLimitData = (TenantLevelLimitData) subNamespaceLevelLimitData.getChildrenMap().get(tenant);
        orgLevelLimitData.setLimit(limit);
    }


    public static class TopLevelLimitData {
        private String nodeName = "";
        private long limit;
        private Set<String> clientListForWhichLimitIsApplicable = new HashSet<>();
        Map<String, ClientLevelLimitData> clientMap = new ConcurrentHashMap<>();

        public TopLevelLimitData(String nodeName, long limit) {
            this.nodeName = nodeName;
            this.limit = limit;
        }

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public long getLimit() {
            return limit;
        }

        public void setLimit(long limit) {
            this.limit = limit;
        }

        public Set<String> getClientListForWhichLimitIsApplicable() {
            return clientListForWhichLimitIsApplicable;
        }

        public void setClientListForWhichLimitIsApplicable(Set<String> clientListForWhichLimitIsApplicable) {
            this.clientListForWhichLimitIsApplicable = clientListForWhichLimitIsApplicable;
        }

        public Map<String, ClientLevelLimitData> getClientMap() {
            return clientMap;
        }

        public void setClientMap(Map<String, ClientLevelLimitData> clientMap) {
            this.clientMap = clientMap;
        }
    }


    public enum LimitLevel {
        NODE,
        CLIENT,
        NAMESPACE,
        SUBNAMESPACE,
        TENANT
    }

    public static class LimitData {
        private final String resourceName;
        private final LimitLevel limitLevel;
        private long limit;
        private final Set<String> resourcesForWhichLimitIsApplicable = new HashSet<>();
        Map<String, LimitData> childrenMap = new ConcurrentHashMap<>();

        public LimitData(String resourceName, LimitLevel limitLevel) {
            this.resourceName = resourceName;
            this.limitLevel = limitLevel;
        }

        public String getResourceName() {
            return resourceName;
        }

        public long getLimit() {
            return limit;
        }

        public void setLimit(long limit) {
            this.limit = limit;
        }

        public Set<String> getKeysForWhichLimitIsApplicable() {
            return resourcesForWhichLimitIsApplicable;
        }

        public Map<String, LimitData> getChildrenMap() {
            return childrenMap;
        }

        public LimitLevel getLimitLevel() {
            return limitLevel;
        }
    }

    public static class NodeLevelLimitData extends LimitData{

        public NodeLevelLimitData(String nodeName) {
            super(nodeName, LimitLevel.NODE);
        }

        public String getNodeName() {
            return this.getResourceName();
        }

        public Set<String> getClientsForWhichLimitIsApplicable() {
            return super.getKeysForWhichLimitIsApplicable();
        }

    }

    public static class ClientLevelLimitData extends LimitData {

        public ClientLevelLimitData(String clientName) {
            super(clientName, LimitLevel.CLIENT);
        }

        public Set<String> getNamespacesForWhichLimitIsApplicable() {
            return super.getKeysForWhichLimitIsApplicable();
        }

        public String getClientName() {
            return super.getResourceName();
        }

    }

    private static class NamespaceLevelLimitData extends LimitData {

        public NamespaceLevelLimitData(String namespace) {
            super(namespace, LimitLevel.NAMESPACE);
        }

        public Set<String> getSubNamespacesForWhichLimitIsApplicable() {
            return super.getKeysForWhichLimitIsApplicable();
        }

        public String getNamespace() {
            return super.getResourceName();
        }
    }

    private static class SubNamespaceLevelLimitData extends LimitData {

        public SubNamespaceLevelLimitData(String namespace) {
            super(namespace, LimitLevel.SUBNAMESPACE);
        }

        public Set<String> getTenantsForWhichLimitIsApplicable() {
            return super.getKeysForWhichLimitIsApplicable();
        }

        public String getSubNamespace() {
            return super.getResourceName();
        }
    }

    private static class TenantLevelLimitData extends LimitData{


        public TenantLevelLimitData(String tenantName) {
            super(tenantName, LimitLevel.TENANT);
        }

        public String getOrgName() {
            return super.getResourceName();
        }
    }

}
