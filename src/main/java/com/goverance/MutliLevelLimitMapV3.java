package com.goverance;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
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

        List<Triple<String, Long, String>> limits = List.of(
                Triple.of("c1:.*:.*:.*", 200L, "S"),
                Triple.of("c1:ns1:.*:.*", 50L, "S"),
                Triple.of("c1:ns1:subns1:.*", 20L, "S"),
                Triple.of("c1:ns1:subns1:ORG1|ORG2", 5L, "M"),
                Triple.of("c1:ns2:.*:.*", 60L, "S"),
                Triple.of("c1:ns2:subns2:.*", 40L, "S"),
                Triple.of("c1:ns2:subns2:ORG2", 10L, "S"),
                Triple.of("c1:ns2:subns2:ORG1", 10L, "S")
//                Pair.of("c1", 100L),
//                Pair.of("c1:ns2", 20L),
//                Pair.of("c1:ns1", 20L)
        );

        limits.forEach(limit -> {
            Long limitValue = limit.getMiddle();
            String limitStr = limit.getLeft();
            String limitType = limit.getRight();
            String[] limitParts = limitStr.split(":", -1);
            String clientId = limitParts[0];
            LimitData clientLevelLimitData = nodeLevelLimitData.getChildKeysMap().get(clientId);
            updateLimitsData(null, clientLevelLimitData, limitParts, 0, limitValue, limitType);
        });

        updateLimitsDataBasedOnChildLimits(nodeLevelLimitData);

        applyLimits(nodeLevelLimitData);
        System.out.println();

    }

    public static void updateLimitsData(LimitData parentLimitData, LimitData limitData, String[] keysTokensForLimit, int currentLevel, Long limitValue, String limitType) {

        if (LimitLevel.TENANT == limitData.getLimitLevel()) {
            if (parentLimitData != null) {
                parentLimitData.getKeysForWhichLimitIsApplicable().remove(limitData.getResourceName());
            }
            limitData.setLimit(limitValue);
            return;
        }

        String childLevelToken = keysTokensForLimit[currentLevel + 1];
        if (childLevelToken.equals(".*")) {
            limitData.setLimit(limitValue);
            if (parentLimitData != null) {
                parentLimitData.getKeysForWhichLimitIsApplicable().remove(limitData.getResourceName());
            }
        } else if (childLevelToken.contains("|") && isMultipleTypeLimit(limitType)) {
            String[] childLevelTokens = childLevelToken.split("\\|");
            Set<String> resourceListForTheMultiLimit = Arrays.stream(childLevelTokens).collect(Collectors.toSet());
            limitData.getMultiLimitMap().put(resourceListForTheMultiLimit, limitValue);
            limitData.getKeysForWhichLimitIsApplicable().removeAll(resourceListForTheMultiLimit);
            limitData.getChildResourcesForWhichMulLimitIsDefined().addAll(resourceListForTheMultiLimit);
        } else if (childLevelToken.contains("|")) {
            String[] childLevelTokens = childLevelToken.split("\\|");
            Arrays.stream(childLevelTokens).forEach(t -> {
                LimitData ld = limitData.getChildKeysMap().get(t);
                if (ld == null) {
                    return;
                }
                updateLimitsData(limitData, ld, keysTokensForLimit, currentLevel + 1, limitValue, limitType);
            });
        } else {
            LimitData ld = limitData.getChildKeysMap().get(childLevelToken);
            if (ld == null) {
                return;
            }
            updateLimitsData(limitData, ld, keysTokensForLimit, currentLevel + 1, limitValue, limitType);
        }


    }

    private static boolean isMultipleTypeLimit(String limitType) {
        return "M".equals(limitType);
    }

    private static void updateLimitsDataBasedOnChildLimits(LimitData limitData) {

        if (LimitLevel.TENANT == limitData.getLimitLevel()) {
            return;
        }

        AtomicLong limitToBeReduced = new AtomicLong(0);
        limitData.getChildKeysMap().forEach((k, v) -> {
            if (!(limitData.getKeysForWhichLimitIsApplicable().contains(k) || limitData.getChildResourcesForWhichMulLimitIsDefined().contains(k))) {
                limitToBeReduced.addAndGet(limitData.getChildKeysMap().get(k).getLimit());
            }
        });
        limitData.getMultiLimitMap().values().forEach(limitToBeReduced::addAndGet);

        limitData.setLimit(limitData.getLimit() - limitToBeReduced.get());
        limitData.getChildKeysMap().forEach((k, v) -> {
            updateLimitsDataBasedOnChildLimits(v);
        });
    }

    private static void applyLimits(NodeLevelLimitData nodeLevelLimitData) {
        nodeLevelLimitData.getChildKeysMap().forEach((k, v) -> applyLimits(v, ""));
    }

    private static void applyLimits(LimitData limitData, String qualifiedResourceName) {
        limitData.getChildKeysMap().forEach((k, v) -> {
            applyLimits(v, qualifiedResourceName + ":" + limitData.getResourceName());
        });

        limitData.getMultiLimitMap().forEach((rSet, limit) -> {
            List<String> tenants = new ArrayList<>();
            rSet.forEach(r -> {
                LimitData childLimitData = limitData.getChildKeysMap().get(r);
                if (childLimitData != null) {
                    buildTenantList(tenants, childLimitData, qualifiedResourceName + ":" + limitData.getResourceName());
                }
            });
            System.out.println(String.format("Tenant list for eviction for \"%s\": %s, limit: %d", qualifiedResourceName + ":" + limitData.getResourceName() + ":" + rSet,
                    tenants, limit));
        });
        long limit = limitData.getLimit();
        if (limit != -1) {
            List<String> tenants = new ArrayList<>();
            buildTenantList(tenants, limitData, qualifiedResourceName);
            System.out.println(String.format("Tenant list for eviction for \"%s\": %s, limit: %d", qualifiedResourceName + ":" + limitData.getResourceName(),
                    tenants, limit));
        }
    }


    private static void buildTenantList(List<String> tenants, LimitData limitData, String qualifiedResourceName) {

        if (LimitLevel.TENANT == limitData.getLimitLevel()) {
            tenants.add(qualifiedResourceName + ":" + limitData.getResourceName());
        }

        limitData.getChildKeysMap().forEach((k, v) -> {
            if (!limitData.getKeysForWhichLimitIsApplicable().contains(k)) {
                return;
            }

            buildTenantList(tenants, limitData.getChildKeysMap().get(k), qualifiedResourceName + ":" + limitData.getResourceName());
        });
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

        public Set<String> getChildResourcesForWhichMulLimitIsDefined() {
            return childResourcesForWhichMulLimitIsDefined;
        }

        private final Set<String> childResourcesForWhichMulLimitIsDefined = new HashSet<>();
        private final Map<Set<String>, Long> multiLimitMap = new ConcurrentHashMap<>();
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

        public Map<Set<String>, Long> getMultiLimitMap() {
            return multiLimitMap;
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
