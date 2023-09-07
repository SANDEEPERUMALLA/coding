package com.goverance;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class MutliLevelLimitMapV3 {

    private static final NodeLevelLimitData nodeLevelLimitData = new NodeLevelLimitData("");

    public static void main(String[] args) {
        List<String> clientIds = List.of("c1", "c2");
//        List<String> clientIds = List.of("c1");
        List<String> namespaces = List.of("ns1", "ns2", "ns3");
//        List<String> namespaces = List.of("ns1", "ns2", "ns3", "ns4", "ns5");
        List<String> subNamespaces = List.of("subns1", "subns2");
        List<String> tenants = List.of("ORG1", "ORG2", "ORG3");



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

                            LimitData clientLevelLimitData = nodeLevelLimitData.getChildKeysMap().computeIfAbsent(clientName, k -> new ClientLevelLimitData(clientName));
                            clientLevelLimitData.getKeysForWhichLimitIsApplicable().add(namespaceName);

                            LimitData namespaceLevelLimitData = clientLevelLimitData.getChildKeysMap().computeIfAbsent(namespaceName, k -> new NamespaceLevelLimitData(namespaceName));
                            namespaceLevelLimitData.getKeysForWhichLimitIsApplicable().add(subnamespaceName);

                            LimitData subNamespaceLevelLimitData = namespaceLevelLimitData.getChildKeysMap().computeIfAbsent(subnamespaceName, k -> new SubNamespaceLevelLimitData(subnamespaceName));
                            subNamespaceLevelLimitData.getKeysForWhichLimitIsApplicable().add(tenantName);

                            subNamespaceLevelLimitData.getChildKeysMap().computeIfAbsent(tenantName, k -> new TenantLevelLimitData(tenantName));

                        });
                    });
                });
            });
        });

        System.out.println();
        System.out.println();
        System.out.println();

        List<Pair<String, Long>> limits = List.of(
                Pair.of("c1:.*:.*:.*", 200L),
                Pair.of("c1:ns1:.*:.*", 50L),
                Pair.of("c1:ns1:subns1:.*", 20L),
                Pair.of("c1:ns1:subns1:ORG1", 10L),
                Pair.of("c1:ns2:.*:.*", 60L),
                Pair.of("c1:ns2:subns2:.*", 40L),
                Pair.of("c1:ns2:subns2:ORG2", 10L),
                Pair.of("c1:ns2:subns2:ORG1", 10L)
//                Pair.of("c1", 100L),
//                Pair.of("c1:ns2", 20L),
//                Pair.of("c1:ns1", 20L)
        );

        limits.forEach(limit -> {
            Long limitValue = limit.getRight();
            String limitStr = limit.getLeft();
            String[] limitParts = limitStr.split(":", -1);
            String clientId = limitParts[0];
            LimitData clientLevelLimitData = nodeLevelLimitData.getChildKeysMap().get(clientId);
            updateLimitsData(null, clientLevelLimitData, limitParts, 0, limitValue);
        });

        updateLimitsDataBasedOnChildLimits(nodeLevelLimitData);

        applyLimits(nodeLevelLimitData);
        System.out.println();

    }

    private static void updateLimitsDataBasedOnChildLimits(LimitData limitData) {

        if (LimitLevel.TENANT == limitData.getLimitLevel()) {
            return;
        }

        AtomicLong limitToBeReduced = new AtomicLong(0);
        limitData.getChildKeysMap().forEach((k, v) -> {
            if (!limitData.getKeysForWhichLimitIsApplicable().contains(k)) {
                limitToBeReduced.addAndGet(limitData.getChildKeysMap().get(k).getLimit());
            }
        });
        limitData.setLimit(limitData.getLimit() - limitToBeReduced.get());
        limitData.getChildKeysMap().forEach((k, v) -> {
            updateLimitsDataBasedOnChildLimits(v);
        });
    }

    private static void applyLimits(NodeLevelLimitData nodeLevelLimitData) {
        nodeLevelLimitData.getChildKeysMap().forEach((k, v) -> {
            applyLimits((ClientLevelLimitData) v);
        });
    }

    private static void applyLimits(ClientLevelLimitData clientLevelLimitData) {
        clientLevelLimitData.getChildKeysMap().forEach((k, v) -> {
            applyLimits((NamespaceLevelLimitData) v, clientLevelLimitData.getClientName());
        });
        long limit = clientLevelLimitData.getLimit();
        if (limit != -1) {
            List<String> tenants = new ArrayList<>();
            clientLevelLimitData.getChildKeysMap().forEach((k, v) -> {
                if (!clientLevelLimitData.getNamespacesForWhichLimitIsApplicable().contains(k)) {
                    return;
                }
                v.getChildKeysMap().forEach((k1, v1) -> {
                    if (!v.getKeysForWhichLimitIsApplicable().contains(k1)) {
                        return;
                    }
                    v1.getChildKeysMap().forEach((k2, v2) -> {
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
        namespaceLevelLimitData.getChildKeysMap().forEach((k, v) -> {
            applyLimits((SubNamespaceLevelLimitData) v, clientName, namespaceLevelLimitData.getNamespace());
        });
        long limit = namespaceLevelLimitData.getLimit();
        List<String> tenants = new ArrayList<>();
        if (limit != -1) {
            namespaceLevelLimitData.getChildKeysMap().forEach((k, v) -> {
                if (!namespaceLevelLimitData.getSubNamespacesForWhichLimitIsApplicable().contains(k)) {
                    return;
                }
                v.getChildKeysMap().forEach((k1, v1) -> {
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

        subNamespaceLevelLimitData.getChildKeysMap().forEach((k, v) -> {
            applyLimits((TenantLevelLimitData) v, clientName, namespace, subNamespaceLevelLimitData.getSubNamespace());
        });

        List<String> tenants = new ArrayList<>();
        long limit = subNamespaceLevelLimitData.getLimit();
        if (limit != -1) {
            subNamespaceLevelLimitData.getChildKeysMap().forEach((k, v) -> {
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
        if (limit != -1) {
            List<String> tenants = new ArrayList<>();
            tenants.add(String.join(":", clientName, namespace, subNamespace, orgLevelLimitData.getOrgName()));
            System.out.println(String.format("Tenant list for eviction for tenant \"%s\": %s, limit : %d", String.join(":", clientName, namespace, subNamespace, orgLevelLimitData.getOrgName()),
                    tenants, limit));
        }

    }

    public static void updateLimitsData(LimitData parentLimitData, LimitData limitData, String[] keysTokensForLimit, int currentLevel, Long limitValue) {

        if (LimitLevel.TENANT == limitData.getLimitLevel()) {
            limitData.setLimit(limitValue);
            return;
        }

        String key = keysTokensForLimit[currentLevel + 1];
        if (key.equals(".*")) {
            limitData.setLimit(limitValue);
            if (parentLimitData != null) {
                parentLimitData.getKeysForWhichLimitIsApplicable().remove(limitData.getResourceName());
            }
        } else {
            LimitData ld = limitData.getChildKeysMap().get(key);
            if (ld == null) {
                return;
            }
            updateLimitsData(limitData, ld, keysTokensForLimit, currentLevel + 1, limitValue);
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
        private long limit = -1;
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

        public Map<String, LimitData> getChildKeysMap() {
            return childrenMap;
        }

        public LimitLevel getLimitLevel() {
            return limitLevel;
        }
    }

    public static class NodeLevelLimitData extends LimitData {

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

    private static class TenantLevelLimitData extends LimitData {


        public TenantLevelLimitData(String tenantName) {
            super(tenantName, LimitLevel.TENANT);
        }

        public String getOrgName() {
            return super.getResourceName();
        }
    }
}
