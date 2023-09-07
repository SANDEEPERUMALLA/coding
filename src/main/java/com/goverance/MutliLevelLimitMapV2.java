package com.goverance;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class MutliLevelLimitMapV2 {

    public static void main(String[] args) {
        List<String> clientIds = List.of("c1", "c2");
//        List<String> clientIds = List.of("c1");
        List<String> namespaces = List.of("ns1", "ns2", "ns3");
//        List<String> namespaces = List.of("ns1", "ns2", "ns3", "ns4", "ns5");
        List<String> subNamespaces = List.of("subns1", "subns2");
        List<String> tenants = List.of("ORG1", "ORG2", "ORG3");

        LimitData nodeLevelLimitData = new LimitData("Node", LimitLevel.NODE);

        System.out.println("List of tenants !!!");

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
                            String client = keyParts[0];
                            String ns = keyParts[1];
                            String subns = keyParts[2];
                            String tenantId = keyParts[3];
                            LimitData clientLevelLimitData = nodeLevelLimitData.getChildrenMap().computeIfAbsent(client, k -> new LimitData(k, LimitLevel.CLIENT));
                            clientLevelLimitData.getKeysForWhichLimitIsApplicable().add(ns);

                            LimitData namespaceLevelLimitData = clientLevelLimitData.getChildrenMap().computeIfAbsent(ns, k -> new LimitData(ns,  LimitLevel.NAMESPACE));
                            namespaceLevelLimitData.getKeysForWhichLimitIsApplicable().add(subns);

                            LimitData subNamespaceLevelLimitData = namespaceLevelLimitData.getChildrenMap().computeIfAbsent(subns, k -> new LimitData(subns,  LimitLevel.SUBNAMESPACE));
                            subNamespaceLevelLimitData.getKeysForWhichLimitIsApplicable().add(tenantId);

                            LimitData orgLevelLimitData = subNamespaceLevelLimitData.getChildrenMap().computeIfAbsent(tenantId, k -> new LimitData(tenantId,  LimitLevel.TENANT));

                        });
                    });
                });
            });
        });



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
            updateLimitsData(nodeLevelLimitData, limitParts, limitValue);
        });

        updateLimitsDataBasedOnChildLimits(nodeLevelLimitData);

        applyLimits(nodeLevelLimitData);

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


    private static void applyLimits(LimitData nodeLevelLimitData) {
        nodeLevelLimitData.getChildrenMap().forEach((k, v) -> applyClientLimits(v));
    }

    private static void applyClientLimits(LimitData clientLevelLimitData) {
        clientLevelLimitData.getChildrenMap().forEach((k, v) -> {
            applyNamespaceLimits(v, clientLevelLimitData.getResourceName());
        });
        long limit = clientLevelLimitData.getLimit();
        if (limit != 0) {
            List<String> tenants = new ArrayList<>();
            clientLevelLimitData.getChildrenMap().forEach((k, v) -> {
                if (!clientLevelLimitData.getKeysForWhichLimitIsApplicable().contains(k)) {
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
                        tenants.add(String.join(":", clientLevelLimitData.getResourceName(), k, k1, k2));
                    });
                });
            });
            System.out.println(String.format("Tenant list for eviction for namespace \"%s\": %s, limit: %d", clientLevelLimitData.getResourceName(),
                    tenants, limit));
        }

    }

    private static void applyNamespaceLimits(LimitData namespaceLevelLimitData, String clientName) {
        namespaceLevelLimitData.getChildrenMap().forEach((k, v) -> {
            applySubnamepaceLimits(v, clientName, namespaceLevelLimitData.getResourceName());
        });
        long limit = namespaceLevelLimitData.getLimit();
        List<String> tenants = new ArrayList<>();
        if (limit != 0) {
            namespaceLevelLimitData.getChildrenMap().forEach((k, v) -> {
                if (!namespaceLevelLimitData.getKeysForWhichLimitIsApplicable().contains(k)) {
                    return;
                }
                v.getChildrenMap().forEach((k1, v1) -> {
                    if (!v.getKeysForWhichLimitIsApplicable().contains(k1)) {
                        return;
                    }
                    tenants.add(String.join(":", clientName, namespaceLevelLimitData.getResourceName(), k, k1));
                });
            });
            System.out.println(String.format("Tenant list for eviction for subnamespace \"%s\": %s, limit: %d", String.join(":", clientName, namespaceLevelLimitData.getResourceName()),
                    tenants, limit));

        }
    }

    private static void applySubnamepaceLimits(LimitData subNamespaceLevelLimitData, String clientName, String namespace) {

        subNamespaceLevelLimitData.getChildrenMap().forEach((k, v) -> {
            applyOrgLimits(v, clientName, namespace, subNamespaceLevelLimitData.getResourceName());
        });

        List<String> tenants = new ArrayList<>();
        long limit = subNamespaceLevelLimitData.getLimit();
        if (limit != 0) {
            subNamespaceLevelLimitData.getChildrenMap().forEach((k, v) -> {
                if (!subNamespaceLevelLimitData.getKeysForWhichLimitIsApplicable().contains(k)) {
                    return;
                }
                tenants.add(String.join(":", clientName, namespace, subNamespaceLevelLimitData.getResourceName(), k));
            });
            System.out.println(String.format("Tenant list for eviction for subnamespace \"%s\": %s, limit : %d", String.join(":", clientName, namespace, subNamespaceLevelLimitData.getResourceName()),
                    tenants, limit));
        }

    }

    private static void applyOrgLimits(LimitData orgLevelLimitData, String clientName, String namespace, String subNamespace) {

        long limit = orgLevelLimitData.getLimit();
        if (limit != 0) {
            List<String> tenants = new ArrayList<>();
            tenants.add(String.join(":", clientName, namespace, subNamespace, orgLevelLimitData.getResourceName()));
            System.out.println(String.format("Tenant list for eviction for tenant \"%s\": %s, limit : %d", String.join(":", clientName, namespace, subNamespace, orgLevelLimitData.getResourceName()),
                    tenants, limit));
        }

    }

    public static void updateLimitsData(LimitData nodeLevelLimitData, String[] limitParts, Long limitValue) {

        if (limitParts.length == 1) {
            updateClientLevelData(nodeLevelLimitData, limitParts, limitValue);
        } else if (limitParts.length == 2) {
            updateNamespaceLevelData(nodeLevelLimitData, limitParts, limitValue);
        } else if (limitParts.length == 3) {
            updateSubNamespaceLevelData(nodeLevelLimitData, limitParts, limitValue);
        } else if (limitParts.length == 4) {
            updateTenantLevelData(nodeLevelLimitData, limitParts, limitValue);
        }
    }

    public static void updateClientLevelData(LimitData nodeLevelLimitData, String[] keyParts, long limit) {
        String client = keyParts[0];
        LimitData clientLevelLimitData = nodeLevelLimitData.getChildrenMap().get(client);
        clientLevelLimitData.setLimit(limit);
        nodeLevelLimitData.getKeysForWhichLimitIsApplicable().remove(client);
    }

    public static void updateNamespaceLevelData(LimitData nodeLevelLimitData, String[] keyParts, long limit) {
        String client = keyParts[0];
        String ns = keyParts[1];
        LimitData clientLevelLimitData = nodeLevelLimitData.getChildrenMap().get(client);
        clientLevelLimitData.getKeysForWhichLimitIsApplicable().remove(ns);
        LimitData namespaceLevelLimitData = clientLevelLimitData.getChildrenMap().get(ns);
        namespaceLevelLimitData.setLimit(limit);
    }

    public static void updateSubNamespaceLevelData(LimitData nodeLevelLimitData, String[] keyParts, long limit) {
        String client = keyParts[0];
        String ns = keyParts[1];
        String subNs = keyParts[2];
        LimitData clientLevelLimitData = nodeLevelLimitData.getChildrenMap().get(client);
        LimitData namespaceLevelLimitData = clientLevelLimitData.getChildrenMap().get(ns);
        namespaceLevelLimitData.getKeysForWhichLimitIsApplicable().remove(subNs);
        LimitData subNamespaceLevelLimitData = namespaceLevelLimitData.getChildrenMap().get(subNs);
        subNamespaceLevelLimitData.setLimit(limit);
    }

    public static void updateTenantLevelData(LimitData nodeLevelLimitData, String[] keyParts, long limit) {
        String client = keyParts[0];
        String ns = keyParts[1];
        String subNs = keyParts[2];
        String tenant = keyParts[3];
        LimitData clientLevelLimitData = nodeLevelLimitData.getChildrenMap().get(client);
        LimitData namespaceLevelLimitData = clientLevelLimitData.getChildrenMap().get(ns);
        LimitData subNamespaceLevelLimitData = namespaceLevelLimitData.getChildrenMap().get(subNs);
        subNamespaceLevelLimitData.getKeysForWhichLimitIsApplicable().remove(tenant);
        LimitData orgLevelLimitData = subNamespaceLevelLimitData.getChildrenMap().get(tenant);
        orgLevelLimitData.setLimit(limit);
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
        private Set<String> resourcesForWhichLimitIsApplicable = new HashSet<>();
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


    public class ClientLevelLimitData extends LimitData {

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


}
